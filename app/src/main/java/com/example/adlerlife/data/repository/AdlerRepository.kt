package com.example.adlerlife.data.repository

import com.example.adlerlife.data.local.AdlerDao
import com.example.adlerlife.data.model.ActionCategory
import com.example.adlerlife.data.model.ActionLogEntity
import com.example.adlerlife.data.model.CoachingInsight
import com.example.adlerlife.data.model.DailyTrajectory
import com.example.adlerlife.data.model.ImpulseInput
import com.example.adlerlife.data.model.ImpulseLogEntity
import com.example.adlerlife.data.model.ReflectionInput
import com.example.adlerlife.data.model.ReflectionLogEntity
import com.example.adlerlife.domain.AiCoach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class AdlerRepository(
    private val dao: AdlerDao,
    private val aiCoach: AiCoach
) {
    val latestImpulse = dao.observeLatestImpulseLog()
    val actionLogs = dao.observeActionLogs()
    val latestReflection = dao.observeLatestReflection()

    val calendarTrajectory: Flow<List<DailyTrajectory>> = dao.observeActionLogs().map { logs ->
        logs.groupBy { LocalDate.parse(it.createdAt.take(10)) }
            .entries
            .sortedByDescending { it.key }
            .map { (date, dailyLogs) ->
                DailyTrajectory(
                    date = date.toString(),
                    categories = dailyLogs.map { it.category }.distinct()
                )
            }
    }

    val coachingInsight: Flow<CoachingInsight> = combine(actionLogs, latestReflection) { actions, reflection ->
        if (reflection != null) {
            CoachingInsight(reflection.aiFeedback, "その感覚を明日にも置いておくなら、何を少しだけ残したいですか？")
        } else {
            aiCoach.reflectOnValues(ReflectionInput(), actions.take(5))
        }
    }

    suspend fun createImpulseSuggestion(input: ImpulseInput): String {
        val suggestion = aiCoach.suggestMicroAction(input.desire, input.mood, input.energyLevel)
        dao.insertImpulseLog(
            ImpulseLogEntity(
                desire = input.desire,
                mood = input.mood,
                energyLevel = input.energyLevel,
                suggestion = suggestion
            )
        )
        return suggestion
    }

    suspend fun addActionLog(action: String, feeling: String, category: ActionCategory) {
        dao.insertActionLog(ActionLogEntity(action = action, feeling = feeling, category = category))
    }

    suspend fun reflect(input: ReflectionInput): CoachingInsight {
        val recentActions = dao.observeActionLogs().first().take(7)
        val insight = aiCoach.reflectOnValues(input, recentActions)
        dao.insertReflectionLog(
            ReflectionLogEntity(
                actionsSummary = input.actionsSummary,
                memorableMoment = input.memorableMoment,
                smallJoy = input.smallJoy,
                aiFeedback = insight.summary
            )
        )
        return insight
    }
}
