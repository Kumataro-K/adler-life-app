package com.example.adlerlife.domain

import com.example.adlerlife.BuildConfig
import com.example.adlerlife.data.model.ActionCategory
import com.example.adlerlife.data.model.ActionLogEntity
import com.example.adlerlife.data.model.CoachingInsight
import com.example.adlerlife.data.model.ReflectionInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

interface AiCoach {
    suspend fun suggestMicroAction(desire: String, mood: Int, energyLevel: Int): String
    suspend fun reflectOnValues(input: ReflectionInput, actions: List<ActionLogEntity>): CoachingInsight
}

class HybridAiCoach(
    private val client: OkHttpClient = OkHttpClient()
) : AiCoach {
    override suspend fun suggestMicroAction(desire: String, mood: Int, energyLevel: Int): String {
        val fallback = localSuggestion(desire, mood, energyLevel)
        return requestOpenAi(
            systemPrompt = """
                You are a gentle Adlerian coach. Avoid commands and evaluation.
                Suggest one tiny action for the present moment in Japanese.
                Keep it under 40 Japanese characters, soften with possibility, and never mention productivity.
            """.trimIndent(),
            userPrompt = "今やりたいこと: $desire / 気分: $mood / エネルギー: $energyLevel / fallback: $fallback"
        ) ?: fallback
    }

    override suspend fun reflectOnValues(
        input: ReflectionInput,
        actions: List<ActionLogEntity>
    ): CoachingInsight {
        val fallback = localInsight(input, actions)
        val response = requestOpenAi(
            systemPrompt = """
                You are a reflective Adlerian coach. In Japanese, infer values from daily notes without judging.
                Return exactly two lines:
                summary: <one sentence>
                prompt: <one open question>
            """.trimIndent(),
            userPrompt = buildString {
                appendLine("今日やったこと: ${input.actionsSummary}")
                appendLine("印象に残った瞬間: ${input.memorableMoment}")
                appendLine("小さな喜び: ${input.smallJoy}")
                appendLine("最近の行動カテゴリ: ${actions.joinToString { it.category.label }}")
            }
        ) ?: return fallback

        val lines = response.lines()
        val summary = lines.firstOrNull { it.startsWith("summary:") }
            ?.removePrefix("summary:")
            ?.trim()
            .orEmpty()
        val prompt = lines.firstOrNull { it.startsWith("prompt:") }
            ?.removePrefix("prompt:")
            ?.trim()
            .orEmpty()
        return if (summary.isNotBlank() && prompt.isNotBlank()) {
            CoachingInsight(summary = summary, prompt = prompt)
        } else {
            fallback
        }
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
            }.getOrNull()
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

    private fun localSuggestion(desire: String, mood: Int, energyLevel: Int): String {
        val desireText = desire.lowercase()
        return when {
            energyLevel < 30 -> "深呼吸して、窓辺で1分ぼんやりしてみる"
            "話" in desireText || "会" in desireText -> "気になる人にひとこと送ってみる"
            mood < 40 -> "好きな音を1曲だけ流してみる"
            energyLevel > 70 -> "5分だけ外の空気を吸いに行ってみる"
            else -> "気になる本やメモを5分だけひらいてみる"
        }
    }

    private fun localInsight(input: ReflectionInput, actions: List<ActionLogEntity>): CoachingInsight {
        val topCategory = actions.groupingBy { it.category }.eachCount().maxByOrNull { it.value }?.key
        val summary = when (topCategory) {
            ActionCategory.CONNECTION -> "今日は人とのつながりの中で、自分らしい温度を大切にしていたようです。"
            ActionCategory.BODY -> "今日は身体の感覚を手がかりに、無理のない流れを選んでいたようです。"
            ActionCategory.CREATIVE -> "今日は表現や遊び心が、あなたの充実につながっていたようです。"
            ActionCategory.DISCOVERY -> "今日は新しい発見に心がほどける瞬間を大切にしていたようです。"
            else -> "今日は静かな余白や小さなよろこびを、自分なりに守っていたようです。"
        }
        val prompt = when {
            input.memorableMoment.isNotBlank() -> "その瞬間に、どんな感覚がいちばん自然でしたか？"
            input.smallJoy.isNotBlank() -> "その小さな喜びは、これからの日々にどう置いておきたいですか？"
            else -> "最近の過ごし方の中で、いちばん肩の力が抜けるのはどんな場面ですか？"
        }
        return CoachingInsight(summary = summary, prompt = prompt)
    }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
