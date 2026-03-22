package com.example.adlerlife.util

import android.content.Context
import android.content.Intent
import com.example.adlerlife.data.model.TraceLog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun exportLogs(logs: List<TraceLog>): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.JAPAN)
    val sb = StringBuilder()
    sb.appendLine("🌿 気分記録エクスポート")
    sb.appendLine("出力日：${LocalDate.now()}")
    sb.appendLine()
    logs.forEach { log ->
        sb.appendLine("---")
        sb.appendLine("📅 ${log.timestamp.toDateTimeString(formatter)}")
        sb.appendLine("気分：${log.mood.toMoodEmoji()} ${log.mood}/100")
        sb.appendLine("エネルギー：${log.energy.toEnergyEmoji()} ${log.energy}/100")
        if (log.tags.isNotBlank()) {
            sb.appendLine("今日あったこと：${log.tags}")
        }
        if (log.feeling.isNotBlank()) {
            sb.appendLine("気持ち：${log.feeling}")
        }
        sb.appendLine()
    }
    return sb.toString()
}

fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "記録を共有"))
}

fun Long.toDateTimeString(formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.JAPAN)): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(formatter)

fun Int.toMoodEmoji(): String = when {
    this >= 67 -> "😊"
    this >= 34 -> "😌"
    else -> "😔"
}

fun Int.toEnergyEmoji(): String = when {
    this >= 67 -> "⚡"
    this >= 34 -> "🌿"
    else -> "🪫"
}
