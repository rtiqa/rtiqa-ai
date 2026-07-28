package com.rtiqa.feature.ai

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.AiInsight
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.AskAiTutorUseCase
import com.rtiqa.core.domain.usecase.GenerateAiSummaryUseCase
import com.rtiqa.core.domain.usecase.GetAiHistoryUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class AiTutorUiState(
    val queryInput: String = "",
    val courseContext: String? = null,
    val history: List<AiInsight> = emptyList(),
    val isGenerating: Boolean = false,
    val latestSummary: String? = null,
    val errorMessage: String? = null
) : ViewUiState

sealed interface AiTutorUiAction : ViewUiAction {
    data class QueryInputChanged(val query: String) : AiTutorUiAction
    data class SetCourseContext(val context: String?) : AiTutorUiAction
    object SubmitQuery : AiTutorUiAction
    data class GenerateSummaryRequested(val prompt: String) : AiTutorUiAction
    object ClearError : AiTutorUiAction
}

sealed interface AiTutorUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : AiTutorUiEvent
}

class AiTutorViewModel(
    private val askAiTutorUseCase: AskAiTutorUseCase,
    private val generateAiSummaryUseCase: GenerateAiSummaryUseCase,
    private val getAiHistoryUseCase: GetAiHistoryUseCase
) : BaseViewModel<AiTutorUiState, AiTutorUiAction, AiTutorUiEvent>(AiTutorUiState()) {

    init {
        observeHistory()
    }

    private fun observeHistory() {
        getAiHistoryUseCase()
            .onEach { insights ->
                setState { copy(history = insights) }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: AiTutorUiAction) {
        when (action) {
            is AiTutorUiAction.QueryInputChanged -> setState { copy(queryInput = action.query, errorMessage = null) }
            is AiTutorUiAction.SetCourseContext -> setState { copy(courseContext = action.context) }
            is AiTutorUiAction.SubmitQuery -> submitQuery()
            is AiTutorUiAction.GenerateSummaryRequested -> generateSummary(action.prompt)
            is AiTutorUiAction.ClearError -> setState { copy(errorMessage = null) }
        }
    }

    private fun submitQuery() {
        val q = currentState.queryInput.trim()
        if (q.isBlank() || currentState.isGenerating) return

        setState { copy(isGenerating = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = askAiTutorUseCase(q, currentState.courseContext)) {
                is RtiqaResult.Success -> {
                    setState { copy(isGenerating = false, queryInput = "") }
                    sendEvent(AiTutorUiEvent.ShowToast("AI Tutor answered!"))
                }
                is RtiqaResult.Error -> {
                    setState { copy(isGenerating = false, errorMessage = result.error.message) }
                    sendEvent(AiTutorUiEvent.ShowToast(result.error.message))
                }
                is RtiqaResult.Loading -> {
                    setState { copy(isGenerating = true) }
                }
            }
        }
    }

    private fun generateSummary(prompt: String) {
        if (prompt.isBlank() || currentState.isGenerating) return

        setState { copy(isGenerating = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = generateAiSummaryUseCase(prompt)) {
                is RtiqaResult.Success -> {
                    setState { copy(isGenerating = false, latestSummary = result.data) }
                }
                is RtiqaResult.Error -> {
                    setState { copy(isGenerating = false, errorMessage = result.error.message) }
                    sendEvent(AiTutorUiEvent.ShowToast(result.error.message))
                }
                is RtiqaResult.Loading -> {
                    setState { copy(isGenerating = true) }
                }
            }
        }
    }
}
