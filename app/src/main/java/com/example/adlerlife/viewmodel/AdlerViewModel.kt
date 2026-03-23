package com.forestmood.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.forestmood.app.data.model.ChatMessage
import com.forestmood.app.data.model.ChatRole
import com.forestmood.app.data.model.TraceDaySummary
import com.forestmood.app.data.model.TraceInput
import com.forestmood.app.data.model.TraceLog
import com.forestmood.app.data.model.toLocalDate
import com.forestmood.app.data.repository.AdlerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar
import java.util.UUID

data class AdlerUiState(
    val traceInput: TraceInput = TraceInput(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val conversationDate: LocalDate = LocalDate.now(),
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatRole.AI,
            text = "今日の記録を振り返ると、どんな場面がいちばん心に残っていますか？"
        )
    ),
    val chatDraft: String = "",
    val aiReply: String = "必要なときだけ、振り返りをサポートします。",
    val isSavingTrace: Boolean = false,
    val isLoadingConversation: Boolean = false
)

class AdlerViewModel(
    private val repository: AdlerRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdlerUiState())
    val uiState = _uiState.asStateFlow()

    private val _selectedRecordPeriod = MutableStateFlow(0)
    val selectedRecordPeriod: StateFlow<Int> = _selectedRecordPeriod.asStateFlow()

    val traceLogs: StateFlow<List<TraceLog>> = repository.traceLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val monthSummaries: StateFlow<List<TraceDaySummary>> = uiState
        .flatMapLatest { repository.observeMonthSummaries(it.selectedMonth) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val thisMonthLogs: StateFlow<List<TraceLog>> = repository.observeLogsForMonth(YearMonth.now())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val recordLogs: StateFlow<List<TraceLog>> = combine(traceLogs, selectedRecordPeriod) { logs, period ->
        if (period == 0) filterThisWeekLogs(logs) else filterThisMonthLogs(logs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val chartData: StateFlow<List<Triple<String, Int?, Int?>>> = combine(selectedRecordPeriod, recordLogs) { period, logs ->
        if (period == 0) getThisWeekData(logs) else getThisMonthData(logs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = getThisWeekData(emptyList())
    )

    fun updateMood(value: Int) = _uiState.update {
        it.copy(traceInput = it.traceInput.copy(mood = value))
    }

    fun updateEnergy(value: Int) = _uiState.update {
        it.copy(traceInput = it.traceInput.copy(energy = value))
    }

    fun updateTags(value: Set<String>) = _uiState.update {
        it.copy(traceInput = it.traceInput.copy(tags = value))
    }

    fun updateFeeling(value: String) = _uiState.update {
        it.copy(traceInput = it.traceInput.copy(feeling = value))
    }

    fun saveTrace(onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingTrace = true) }
            repository.saveTrace(uiState.value.traceInput)
            _uiState.update {
                it.copy(
                    traceInput = TraceInput(mood = it.traceInput.mood, energy = it.traceInput.energy),
                    isSavingTrace = false
                )
            }
            onSaved()
        }
    }

    fun previousMonth() = _uiState.update { it.copy(selectedMonth = it.selectedMonth.minusMonths(1)) }
    fun nextMonth() = _uiState.update { it.copy(selectedMonth = it.selectedMonth.plusMonths(1)) }
    fun selectRecordPeriod(index: Int) {
        _selectedRecordPeriod.value = index
    }

    fun updateChatDraft(value: String) = _uiState.update { it.copy(chatDraft = value) }

    fun reflectToday() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val todayLogs = repository.getTodayLogs(today)
            _uiState.update { state ->
                val baseMessages = if (state.conversationDate == today) {
                    state.chatMessages
                } else {
                    listOf(
                        ChatMessage(
                            id = UUID.randomUUID().toString(),
                            role = ChatRole.AI,
                            text = "今日の記録を振り返ると、どんな場面がいちばん心に残っていますか？"
                        )
                    )
                }
                state.copy(
                    conversationDate = today,
                    chatMessages = baseMessages,
                    isLoadingConversation = true
                )
            }
            val response = repository.reflectToday(todayLogs)
            _uiState.update {
                val aiMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = ChatRole.AI,
                    text = response
                )
                it.copy(
                    aiReply = response,
                    isLoadingConversation = false,
                    chatMessages = it.chatMessages + aiMessage
                )
            }
        }
    }

    fun sendChatMessage() {
        val userText = uiState.value.chatDraft.trim()
        if (userText.isBlank()) return
        viewModelScope.launch {
            val today = LocalDate.now()
            val todayLogs = repository.getTodayLogs(today)
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = ChatRole.USER,
                text = userText
            )
            _uiState.update { state ->
                val baseHistory = if (state.conversationDate == today) {
                    state.chatMessages
                } else {
                    emptyList()
                }
                state.copy(
                    conversationDate = today,
                    chatDraft = "",
                    isLoadingConversation = true,
                    chatMessages = baseHistory + userMessage
                )
            }
            val history = uiState.value.chatMessages
            val reply = repository.continueConversation(todayLogs, history, userText)
            _uiState.update {
                val aiMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = ChatRole.AI,
                    text = reply
                )
                it.copy(
                    aiReply = reply,
                    isLoadingConversation = false,
                    chatMessages = it.chatMessages + aiMessage
                )
            }
        }
    }

    fun countRecordedDays(logs: List<TraceLog>): Int = logs.map { it.toLocalDate(zoneId) }.distinct().size

    fun logsForDate(date: LocalDate): List<TraceLog> = traceLogs.value.filter { it.toLocalDate(zoneId) == date }

    fun getThisWeekData(logs: List<TraceLog>): List<Triple<String, Int?, Int?>> {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = (dayOfWeek + 5) % 7
        calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        val dayLabels = listOf("月", "火", "水", "木", "金", "土", "日")
        return dayLabels.map { label ->
            val dayStart = calendar.clone() as Calendar
            dayStart.set(Calendar.HOUR_OF_DAY, 0)
            dayStart.set(Calendar.MINUTE, 0)
            dayStart.set(Calendar.SECOND, 0)
            dayStart.set(Calendar.MILLISECOND, 0)

            val dayEnd = calendar.clone() as Calendar
            dayEnd.set(Calendar.HOUR_OF_DAY, 23)
            dayEnd.set(Calendar.MINUTE, 59)
            dayEnd.set(Calendar.SECOND, 59)

            val dayLogs = logs.filter {
                it.timestamp >= dayStart.timeInMillis &&
                    it.timestamp <= dayEnd.timeInMillis
            }
            val avgMood = if (dayLogs.isEmpty()) null else dayLogs.map { it.mood }.average().toInt()
            val avgEnergy = if (dayLogs.isEmpty()) null else dayLogs.map { it.energy }.average().toInt()
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            Triple(label, avgMood, avgEnergy)
        }
    }

    fun getThisMonthData(logs: List<TraceLog>): List<Triple<String, Int?, Int?>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        return (1..maxDay).map { day ->
            val dayStart = calendar.clone() as Calendar
            val dayEnd = calendar.clone() as Calendar
            dayEnd.set(Calendar.HOUR_OF_DAY, 23)
            dayEnd.set(Calendar.MINUTE, 59)
            dayEnd.set(Calendar.SECOND, 59)

            val dayLogs = logs.filter {
                it.timestamp >= dayStart.timeInMillis &&
                    it.timestamp <= dayEnd.timeInMillis
            }
            val avgMood = if (dayLogs.isEmpty()) null else dayLogs.map { it.mood }.average().toInt()
            val avgEnergy = if (dayLogs.isEmpty()) null else dayLogs.map { it.energy }.average().toInt()

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val label = if (day == 1 || day % 5 == 0 || day == maxDay) day.toString() else ""
            Triple(label, avgMood, avgEnergy)
        }
    }

    private fun filterThisWeekLogs(logs: List<TraceLog>): List<TraceLog> {
        val calendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val weekStart = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 7)
        val weekEndExclusive = calendar.timeInMillis
        return logs.filter { it.timestamp in weekStart until weekEndExclusive }
    }

    private fun filterThisMonthLogs(logs: List<TraceLog>): List<TraceLog> {
        val currentMonth = YearMonth.now(zoneId)
        return logs.filter { YearMonth.from(it.toLocalDate(zoneId)) == currentMonth }
    }

    class Factory(
        private val repository: AdlerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AdlerViewModel(repository) as T
    }
}
