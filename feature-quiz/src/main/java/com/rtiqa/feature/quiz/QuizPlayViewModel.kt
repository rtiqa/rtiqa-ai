package com.rtiqa.feature.quiz

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.Quiz
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.GetQuizForCourseUseCase
import com.rtiqa.core.domain.usecase.SubmitQuizResultUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class QuizPlayUiState(
    val courseId: String = "",
    val quiz: Quiz? = null,
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(),
    val isSubmitted: Boolean = false,
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) : ViewUiState

sealed interface QuizPlayUiAction : ViewUiAction {
    data class LoadQuizForCourse(val courseId: String) : QuizPlayUiAction
    data class AnswerSelected(val questionIndex: Int, val optionIndex: Int) : QuizPlayUiAction
    object NextQuestionClicked : QuizPlayUiAction
    object SubmitQuizClicked : QuizPlayUiAction
}

sealed interface QuizPlayUiEvent : ViewUiEvent {
    data class QuizSubmitted(val score: Int, val total: Int, val xpEarned: Int) : QuizPlayUiEvent
    data class ShowToast(val message: String) : QuizPlayUiEvent
}

class QuizPlayViewModel(
    private val getQuizForCourseUseCase: GetQuizForCourseUseCase,
    private val submitQuizResultUseCase: SubmitQuizResultUseCase
) : BaseViewModel<QuizPlayUiState, QuizPlayUiAction, QuizPlayUiEvent>(QuizPlayUiState()) {

    override fun onAction(action: QuizPlayUiAction) {
        when (action) {
            is QuizPlayUiAction.LoadQuizForCourse -> loadQuiz(action.courseId)
            is QuizPlayUiAction.AnswerSelected -> {
                val updated = currentState.selectedAnswers.toMutableMap()
                updated[action.questionIndex] = action.optionIndex
                setState { copy(selectedAnswers = updated) }
            }
            is QuizPlayUiAction.NextQuestionClicked -> {
                val current = currentState.currentQuestionIndex
                val total = currentState.quiz?.questions?.size ?: 0
                if (current + 1 < total) {
                    setState { copy(currentQuestionIndex = current + 1) }
                }
            }
            is QuizPlayUiAction.SubmitQuizClicked -> submitQuiz()
        }
    }

    private fun loadQuiz(courseId: String) {
        setState { copy(courseId = courseId, isLoading = true) }
        getQuizForCourseUseCase(courseId)
            .onEach { quiz ->
                setState {
                    copy(
                        quiz = quiz,
                        totalQuestions = quiz?.questions?.size ?: 0,
                        isLoading = false,
                        errorMessage = if (quiz == null) "Quiz not found for this course" else null
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun submitQuiz() {
        val q = currentState.quiz ?: return
        var correctCount = 0
        q.questions.forEachIndexed { index, question ->
            val selectedOption = currentState.selectedAnswers[index]
            if (selectedOption == question.correctAnswerIndex) {
                correctCount++
            }
        }

        setState { copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = submitQuizResultUseCase(q.id, correctCount, q.questions.size)) {
                is RtiqaResult.Success -> {
                    val passPercent = ((correctCount.toFloat() / q.questions.size) * 100).toInt()
                    val xp = if (passPercent >= 70) 50 else 0
                    setState {
                        copy(
                            isSubmitted = true,
                            score = correctCount,
                            isLoading = false
                        )
                    }
                    sendEvent(QuizPlayUiEvent.QuizSubmitted(correctCount, q.questions.size, xp))
                }
                is RtiqaResult.Error -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                    sendEvent(QuizPlayUiEvent.ShowToast(result.error.message))
                }
                is RtiqaResult.Loading -> {
                    setState { copy(isLoading = true) }
                }
            }
        }
    }
}
