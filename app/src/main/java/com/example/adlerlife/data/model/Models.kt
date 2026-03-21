package com.example.adlerlife.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "impulse_logs")
data class ImpulseLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val desire: String,
    val mood: Int,
    val energyLevel: Int,
    val suggestion: String,
    val createdAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "action_logs")
data class ActionLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val action: String,
    val feeling: String,
    val category: ActionCategory,
    val createdAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "reflection_logs")
data class ReflectionLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val actionsSummary: String,
    val memorableMoment: String,
    val smallJoy: String,
    val aiFeedback: String,
    val createdAt: String = LocalDateTime.now().toString()
)

enum class ActionCategory(val label: String, val color: Color) {
    REST("余白", Color(0xFF8FB3A5)),
    CREATIVE("表現", Color(0xFFD6A77A)),
    CONNECTION("対話", Color(0xFFB38CB4)),
    BODY("身体", Color(0xFF7FA7D8)),
    DISCOVERY("発見", Color(0xFFB8C47A))
}

data class ImpulseInput(
    val desire: String = "",
    val mood: Int = 50,
    val energyLevel: Int = 50
)

data class ReflectionInput(
    val actionsSummary: String = "",
    val memorableMoment: String = "",
    val smallJoy: String = ""
)

data class DailyTrajectory(
    val date: String,
    val categories: List<ActionCategory>
)

data class CoachingInsight(
    val summary: String,
    val prompt: String
)
