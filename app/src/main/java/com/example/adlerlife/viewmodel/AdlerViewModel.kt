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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

    val recordLogs: StateFlow<List<TraceLog>> = selectedRecordPeriod
        .flatMapLatest { period ->
            val since = if (period == 0) {
                System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            } else {
                System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            }
            repository.getLogsAfter(since)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val chartData: StateFlow<List<Pair<String, Int>>> = combine(selectedRecordPeriod, recordLogs) { period, logs ->
        val pattern = if (period == 0) "E" else "d日"
        logs
            .groupBy { SimpleDateFormat(pattern, Locale.JAPAN).format(Date(it.timestamp)) }
            .map { (date, entries) -> date to entries.map { it.mood }.average().toInt() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
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

    fun countRecordedDays(logs: List<TraceLog>): Int = logs.map { it.toLocalDate() }.distinct().size

    fun averageMood(logs: List<TraceLog>): Int? = logs.takeIf { it.isNotEmpty() }?.map { it.mood }?.average()?.toInt()

    fun averageEnergy(logs: List<TraceLog>): Int? = logs.takeIf { it.isNotEmpty() }?.map { it.energy }?.average()?.toInt()

    fun logsForDate(date: LocalDate): List<TraceLog> = traceLogs.value.filter { it.toLocalDate() == date }

    class Factory(
        private val repository: AdlerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AdlerViewModel(repository) as T
    }
}
