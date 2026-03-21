package com.example.adlerlife.domain

import com.example.adlerlife.BuildConfig
import com.example.adlerlife.data.model.ChatMessage
import com.example.adlerlife.data.model.TraceLog
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
        if (logs.isEmpty()) return fallback
        return requestOpenAi(
            systemPrompt = SYSTEM_PROMPT,
            userPrompt = buildString {
                appendLine("今日の記録です：")
                logs.forEach { appendLine(it.toPromptLine()) }
                appendLine("この内容をもとに、私自身への問いかけをしてください。")
            }
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
                appendLine("今日の記録です：")
                if (logs.isEmpty()) {
                    appendLine("- 今日はまだ記録がありません。")
                } else {
                    logs.forEach { appendLine(it.toPromptLine()) }
                }
                appendLine("これまでの会話：")
                history.takeLast(6).forEach { message ->
                    appendLine("- ${message.role.name}: ${message.text}")
                }
                appendLine("ユーザーの追加入力: $userMessage")
                appendLine("この流れを受けて、問いかけ形式で返してください。")
            }
        ) ?: fallback
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
        return "- ${time} / 何をしたか: $what / どう感じたか: $howFelt / 気分: $mood / エネルギー: $energy"
    }

    private fun localReflection(logs: List<TraceLog>): String {
        if (logs.isEmpty()) return "今日はまだ記録がないようですが、いま言葉にしてみたい感覚はありますか？"
        val words = logs.flatMap { listOf(it.what, it.howFelt) }
            .flatMap { it.split(" ", "、", "。", "\n") }
            .map { it.trim() }
            .filter { it.length >= 2 }
        val echoed = words.lastOrNull() ?: logs.first().what
        return "「$echoed」という言葉に今日の手ざわりがあるとしたら、どんな場面がいちばん残っていますか？"
    }

    private fun localFollowUp(logs: List<TraceLog>, userMessage: String): String {
        val base = userMessage.takeIf { it.isNotBlank() } ?: logs.firstOrNull()?.howFelt ?: "今日の流れ"
        return "「$base」をもう少しほどいてみると、その奥にはどんな気持ちや願いがありそうですか？"
    }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
        private const val SYSTEM_PROMPT = """
あなたは評価をしないコーチです。
以下のルールを必ず守ってください：

「良い」「悪い」「すべき」「できた」「できなかった」は使わない
必ず問いかけ形式（疑問文）で終わる
3文以内で返す
ユーザーの言葉をそのまま使う
アドラー心理学に限定せず、人間の感情や動機に寄り添う
"""
    }
}
