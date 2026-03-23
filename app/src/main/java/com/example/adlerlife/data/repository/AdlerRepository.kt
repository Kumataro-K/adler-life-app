package com.forestmood.app.data.repository

import com.forestmood.app.data.local.AdlerDao
import com.forestmood.app.data.model.ChatMessage
import com.forestmood.app.data.model.TraceDaySummary
import com.forestmood.app.data.model.TraceInput
import com.forestmood.app.data.model.TraceLog
import com.forestmood.app.data.model.toLocalDate
import com.forestmood.app.domain.AiCoach
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
                mood = input.mood,
                energy = input.energy,
                tags = input.tags.joinToString(","),
                feeling = input.feeling.trim()
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
                        averageMood = dailyLogs.map { it.mood }.average().toInt(),
                        logs = dailyLogs.sortedByDescending { it.timestamp }
                    )
                }
        }
    }

    fun observeLogsForMonth(month: YearMonth): Flow<List<TraceLog>> {
        val start = month.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return dao.observeTraceLogsBetween(start, end)
    }

    fun getLogsAfter(after: Long): Flow<List<TraceLog>> = dao.getLogsAfter(after)

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
