package com.example.adlerlife.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.adlerlife.data.model.ActionCategory
import com.example.adlerlife.data.model.ActionLogEntity
import com.example.adlerlife.data.model.CoachingInsight
import com.example.adlerlife.data.model.DailyTrajectory
import com.example.adlerlife.data.model.ImpulseInput
import com.example.adlerlife.data.model.ReflectionInput
import com.example.adlerlife.data.repository.AdlerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdlerUiState(
    val impulseInput: ImpulseInput = ImpulseInput(),
    val currentSuggestion: String = "いまの気分に耳を澄ませると、小さな行動が見つかるかもしれません。",
    val reflectionInput: ReflectionInput = ReflectionInput(),
    val reflectionInsight: CoachingInsight = CoachingInsight(
        summary = "今日の軌跡は、まだ静かに余白を残しています。",
        prompt = "今日の中で、少しでも呼吸が楽だった場面はありましたか？"
    ),
    val actionDraft: String = "",
    val feelingDraft: String = "",
    val selectedCategory: ActionCategory = ActionCategory.REST,
    val isLoadingSuggestion: Boolean = false,
    val isLoadingReflection: Boolean = false
)

class AdlerViewModel(
    private val repository: AdlerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdlerUiState())
    val uiState = _uiState.asStateFlow()

    val actionLogs: StateFlow<List<ActionLogEntity>> = repository.actionLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val trajectories: StateFlow<List<DailyTrajectory>> = repository.calendarTrajectory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val coaching: StateFlow<CoachingInsight> = repository.coachingInsight.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AdlerUiState().reflectionInsight
    )

    fun updateDesire(value: String) = _uiState.update {
        it.copy(impulseInput = it.impulseInput.copy(desire = value))
    }

    fun updateMood(value: Float) = _uiState.update {
        it.copy(impulseInput = it.impulseInput.copy(mood = value.toInt()))
    }

    fun updateEnergy(value: Float) = _uiState.update {
        it.copy(impulseInput = it.impulseInput.copy(energyLevel = value.toInt()))
    }

    fun requestSuggestion() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSuggestion = true) }
            val suggestion = repository.createImpulseSuggestion(uiState.value.impulseInput)
            _uiState.update { it.copy(currentSuggestion = suggestion, isLoadingSuggestion = false) }
        }
    }

    fun updateActionDraft(value: String) = _uiState.update { it.copy(actionDraft = value) }
    fun updateFeelingDraft(value: String) = _uiState.update { it.copy(feelingDraft = value) }
    fun updateCategory(category: ActionCategory) = _uiState.update { it.copy(selectedCategory = category) }

    fun saveActionLog() {
        val snapshot = uiState.value
        if (snapshot.actionDraft.isBlank() || snapshot.feelingDraft.isBlank()) return
        viewModelScope.launch {
            repository.addActionLog(snapshot.actionDraft, snapshot.feelingDraft, snapshot.selectedCategory)
            _uiState.update {
                it.copy(
                    actionDraft = "",
                    feelingDraft = "",
                    selectedCategory = ActionCategory.REST
                )
            }
        }
    }

    fun updateReflectionActions(value: String) = _uiState.update {
        it.copy(reflectionInput = it.reflectionInput.copy(actionsSummary = value))
    }

    fun updateReflectionMoment(value: String) = _uiState.update {
        it.copy(reflectionInput = it.reflectionInput.copy(memorableMoment = value))
    }

    fun updateReflectionJoy(value: String) = _uiState.update {
        it.copy(reflectionInput = it.reflectionInput.copy(smallJoy = value))
    }

    fun submitReflection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReflection = true) }
            val insight = repository.reflect(uiState.value.reflectionInput)
            _uiState.update {
                it.copy(
                    reflectionInsight = insight,
                    isLoadingReflection = false,
                    reflectionInput = ReflectionInput()
                )
            }
        }
    }

    class Factory(
        private val repository: AdlerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AdlerViewModel(repository) as T
    }
}
