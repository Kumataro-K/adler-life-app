package com.example.adlerlife.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Entity(tableName = "trace_logs")
data class TraceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mood: Float,
    val energy: Float,
    val whatHappened: String,
    val feeling: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TraceInput(
    val mood: Float = 0.5f,
    val energy: Float = 0.5f,
    val whatHappened: String = "",
    val feeling: String = ""
)

data class TraceDaySummary(
    val date: LocalDate,
    val averageMood: Float,
    val logs: List<TraceLog>
) {
    val color: Color
        get() = when {
            logs.isEmpty() -> Color(0xFFF5F5F5)
            averageMood <= 0.33f -> Color(0xFFFFCDD2)
            averageMood <= 0.66f -> Color(0xFFFFF9C4)
            else -> Color(0xFFC8E6C9)
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
