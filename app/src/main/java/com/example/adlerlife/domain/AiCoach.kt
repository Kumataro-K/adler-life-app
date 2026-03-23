package com.forestmood.app.domain

import com.forestmood.app.BuildConfig
import com.forestmood.app.data.model.ChatMessage
import com.forestmood.app.data.model.TraceLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface AiCoach {
    suspend fun reflectToday(logs: List<TraceLog>): String
    suspend fun continueConversation(logs: List<TraceLog>, history: List<ChatMessage>, userMessage: String): String
}

class HybridAiCoach(
    private val client: OkHttpClient = OkHttpClient()
) : AiCoach {
    override suspend fun reflectToday(logs: List<TraceLog>): String {
        val fallback = localReflection(logs)
        return requestOpenAi(
            systemPrompt = SYSTEM_PROMPT,
            userPrompt = buildReflectionPrompt(logs)
        ) ?: fallback
    }

    override suspend fun continueConversation(
        logs: List<TraceLog>,
        history: List<ChatMessage>,
        userMessage: String
    ): String {
        val fallback = localFollowUp(logs, userMessage)
        return requestOpenAi(
            systemPrompt = SYSTEM_PROMPT,
            userPrompt = buildString {
                append(buildReflectionPrompt(logs))
                appendLine()
                appendLine("これまでの会話：")
                history.takeLast(6).forEach { message ->
                    appendLine("- ${message.role.name}: ${message.text}")
                }
                appendLine("ユーザーの追加入力: $userMessage")
                appendLine("この流れをもとに、気持ちに寄り添う言葉と問いかけをひとつ返してください。")
            }
        ) ?: fallback
    }

    private fun buildReflectionPrompt(logs: List<TraceLog>): String = buildString {
        appendLine("今日の記録です：")
        if (logs.isEmpty()) {
            appendLine("- 今日はまだ記録がありません。")
        } else {
            logs.forEach { appendLine(it.toPromptLine()) }
        }
        appendLine("この内容をもとに、私の気持ちに寄り添う言葉と、")
        appendLine("自分自身への問いかけをひとつしてください。")
    }

    private suspend fun requestOpenAi(systemPrompt: String, userPrompt: String): String? {
        if (BuildConfig.OPENAI_API_KEY.isBlank()) return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val payload = JSONObject()
                    .put("model", BuildConfig.OPENAI_MODEL)
                    .put(
                        "input",
                        JSONArray()
                            .put(messageJson("system", systemPrompt))
                            .put(messageJson("user", userPrompt))
                    )
                val request = Request.Builder()
                    .url(BuildConfig.OPENAI_BASE_URL + "responses")
                    .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .post(payload.toString().toRequestBody(JSON))
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use null
                    val responseJson = JSONObject(response.body?.string().orEmpty())
                    val output = responseJson.optJSONArray("output") ?: return@use null
                    for (i in 0 until output.length()) {
                        val item = output.optJSONObject(i) ?: continue
                        val content = item.optJSONArray("content") ?: continue
                        for (j in 0 until content.length()) {
                            val part = content.optJSONObject(j) ?: continue
                            if (part.optString("type") == "output_text") {
                                return@use part.optString("text")
                            }
                        }
                    }
                    null
                }
            }.getOrNull()?.trim()?.takeIf { it.isNotBlank() }
        }
    }

    private fun messageJson(role: String, text: String): JSONObject {
        return JSONObject()
            .put("role", role)
            .put(
                "content",
                JSONArray().put(
                    JSONObject()
                        .put("type", "input_text")
                        .put("text", text)
                )
            )
    }

    private fun TraceLog.toPromptLine(): String {
        val time = SimpleDateFormat("HH:mm", Locale.JAPAN).format(Date(timestamp))
        return buildString {
            append("- ")
            append(time)
            append(" / 気分: ")
            append(mood)
            append(" / エネルギー: ")
            append(energy)
            append(" / 今日あったこと: ")
            append(tags.ifBlank { "（未選択）" })
            append(" / 気持ち: ")
            append(feeling.ifBlank { "（未入力）" })
        }
    }

    private fun localReflection(logs: List<TraceLog>): String {
        if (logs.isEmpty()) return "今日はまだ言葉が少ないようですが、森の入り口で立ち止まるように、今いちばん近い気分はどんなものですか？"
        val echoed = logs.firstNotNullOfOrNull { it.feeling.ifBlank { it.tags }.takeIf(String::isNotBlank) } ?: "今日の気分"
        val severe = logs.any { textLooksSerious(it.feeling) || textLooksSerious(it.tags) }
        return if (severe) {
            "「$echoed」が続いている中で、今ひとりで抱え込まずにいられる相手や専門家は思い浮かびますか？ 必要なら、その一歩をやさしく考えてみませんか？"
        } else {
            "「$echoed」を木陰でそっと眺めるように見つめると、今日はどんな瞬間に気持ちが少し動いていましたか？"
        }
    }

    private fun localFollowUp(logs: List<TraceLog>, userMessage: String): String {
        val base = userMessage.takeIf { it.isNotBlank() }
            ?: logs.firstNotNullOfOrNull { it.feeling.ifBlank { it.tags }.takeIf(String::isNotBlank) }
            ?: "今の気持ち"
        val severe = textLooksSerious(base)
        return if (severe) {
            "「$base」をここまで言葉にしてくれたことに重さがあるように感じます。 木が嵐の中でも根を張るように、今つながれそうな人や専門家はいますか？"
        } else {
            "「$base」にそっと耳を澄ますと、森の中で風向きが変わるみたいに、どんなきっかけで少し楽になれそうですか？"
        }
    }

    private fun textLooksSerious(text: String): Boolean {
        val lowered = text.lowercase(Locale.JAPAN)
        return listOf("消えたい", "死にたい", "つらすぎる", "限界", "希死", "自傷").any { it in lowered }
    }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
        private const val SYSTEM_PROMPT = """
あなたは穏やかで共感的なメンタルヘルスのサポーターです。
以下のルールを必ず守ってください：

評価・採点・アドバイスはしない
「〜すべき」「〜しなければ」は使わない
必ず共感と問いかけで返す
3文以内で返す
ユーザーの言葉をそのまま大切に扱う
森・自然のメタファーを時々使う（例：「木が嵐の中でも根を張るように」）
深刻な内容には「専門家への相談」を優しく促す
"""
    }
}
