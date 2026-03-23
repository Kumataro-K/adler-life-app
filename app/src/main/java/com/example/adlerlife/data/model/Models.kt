package com.forestmood.app.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Entity(tableName = "trace_logs")
data class TraceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mood: Int,
    val energy: Int,
    val tags: String,
    val feeling: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TraceInput(
    val mood: Int = 50,
    val energy: Int = 50,
    val tags: Set<String> = emptySet(),
    val feeling: String = ""
)

data class TraceDaySummary(
    val date: LocalDate,
    val averageMood: Int,
    val logs: List<TraceLog>
) {
    val color: Color
        get() = when {
            logs.isEmpty() -> Color(0xFFE0E0E0)
            averageMood <= 33 -> Color(0xFFEF9A9A)
            averageMood <= 66 -> Color(0xFFFFE082)
            else -> Color(0xFF81C784)
        }
}

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ChatRole {
    USER,
    AI
}

fun TraceLog.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
