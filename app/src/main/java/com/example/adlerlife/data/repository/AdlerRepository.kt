package com.example.adlerlife.data.repository

import com.example.adlerlife.data.local.AdlerDao
import com.example.adlerlife.data.model.ChatMessage
import com.example.adlerlife.data.model.TraceDaySummary
import com.example.adlerlife.data.model.TraceInput
import com.example.adlerlife.data.model.TraceLog
import com.example.adlerlife.data.model.toLocalDate
import com.example.adlerlife.domain.AiCoach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class AdlerRepository(
    private val dao: AdlerDao,
    private val aiCoach: AiCoach,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    val traceLogs: Flow<List<TraceLog>> = dao.observeTraceLogs()

    suspend fun saveTrace(input: TraceInput) {
        dao.insertTraceLog(
            TraceLog(
                what = input.what.trim(),
                howFelt = input.howFelt.trim(),
                mood = input.mood,
                energy = input.energy
            )
        )
    }

    fun observeMonthSummaries(month: YearMonth): Flow<List<TraceDaySummary>> {
        val start = month.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return dao.observeTraceLogsBetween(start, end).map { logs ->
            logs.groupBy { it.toLocalDate(zoneId) }
                .map { (date, dailyLogs) ->
                    TraceDaySummary(
                        date = date,
                        averageMood = dailyLogs.map { it.mood }.average().toFloat(),
                        logs = dailyLogs.sortedByDescending { it.timestamp }
                    )
                }
        }
    }

    fun observeLogsForDate(date: LocalDate): Flow<List<TraceLog>> {
        val start = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return dao.observeTraceLogsBetween(start, end)
    }

    suspend fun getTodayLogs(today: LocalDate = LocalDate.now(zoneId)): List<TraceLog> {
        val start = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return dao.getTraceLogsBetween(start, end)
    }

    suspend fun reflectToday(logs: List<TraceLog>): String = aiCoach.reflectToday(logs)

    suspend fun continueConversation(
        logs: List<TraceLog>,
        history: List<ChatMessage>,
        userMessage: String
    ): String = aiCoach.continueConversation(logs, history, userMessage)
}
