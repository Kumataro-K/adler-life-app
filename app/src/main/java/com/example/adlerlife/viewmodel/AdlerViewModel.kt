package com.example.adlerlife.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.adlerlife.data.model.ChatMessage
import com.example.adlerlife.data.model.ChatRole
import com.example.adlerlife.data.model.TraceDaySummary
import com.example.adlerlife.data.model.TraceInput
import com.example.adlerlife.data.model.TraceLog
import com.example.adlerlife.data.model.toLocalDate
import com.example.adlerlife.data.repository.AdlerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Date
import java.util.Locale
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
    private val repository: AdlerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdlerUiState())
    val uiState = _uiState.asStateFlow()

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

    fun updateMood(value: Float) = _uiState.update {
        it.copy(traceInput = it.traceInput.copy(mood = value))
    }

    fun updateEnergy(value: Float) = _uiState.update {
        it.copy(traceInput = it.traceInput.copy(energy = value))
    }

    fun updateWhatHappened(value: String) = _uiState.update {
        it.copy(traceInput = it.traceInput.copy(whatHappened = value))
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

    fun getThisWeekLogs(): Flow<List<TraceLog>> {
        val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        return repository.getLogsAfter(weekAgo)
    }

    fun getThisMonthLogs(): Flow<List<TraceLog>> {
        val monthAgo = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
        return repository.getLogsAfter(monthAgo)
    }

    fun calcAvgMood(logs: List<TraceLog>): Float =
        if (logs.isEmpty()) 0f else logs.map { it.mood }.average().toFloat()

    fun calcAvgEnergy(logs: List<TraceLog>): Float =
        if (logs.isEmpty()) 0f else logs.map { it.energy }.average().toFloat()

    fun getChartData(logs: List<TraceLog>): List<Pair<String, Float>> {
        return logs
            .groupBy { SimpleDateFormat("MM/dd", Locale.JAPAN).format(Date(it.timestamp)) }
            .map { (date, entries) -> date to entries.map { it.mood }.average().toFloat() }
            .sortedBy { it.first }
    }

    fun countRecordedDays(logs: List<TraceLog>): Int = logs.map { it.toLocalDate() }.distinct().size

    fun logsForDate(date: LocalDate): List<TraceLog> = traceLogs.value.filter { it.toLocalDate() == date }

    class Factory(
        private val repository: AdlerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AdlerViewModel(repository) as T
    }
}
