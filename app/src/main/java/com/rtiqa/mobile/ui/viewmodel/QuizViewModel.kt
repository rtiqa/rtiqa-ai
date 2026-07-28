package com.rtiqa.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.rtiqa.mobile.data.repository.QuizRepository
import com.rtiqa.mobile.domain.model.Quiz
import com.rtiqa.mobile.domain.model.QuizQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class QuizUiState(
    val quiz: Quiz,
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isSubmitted: Boolean = false,
    val isCorrect: Boolean = false,
    val showHint: Boolean = false,
    val totalScore: Int = 0,
    val isCompleted: Boolean = false,
    val xpEarned: Int = 0,
    val coinsEarned: Int = 0
)

class QuizViewModel(
    private val quizRepository: QuizRepository = QuizRepository()
) : ViewModel() {

    private val sampleQuiz = quizRepository.getSampleQuiz("c_ai_101")

    private val _uiState = MutableStateFlow(QuizUiState(quiz = sampleQuiz))
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    val currentQuestion: QuizQuestion
        get() = _uiState.value.quiz.questions[_uiState.value.currentQuestionIndex]

    fun selectOption(index: Int) {
        if (_uiState.value.isSubmitted) return
        _uiState.value = _uiState.value.copy(selectedOptionIndex = index)
    }

    fun toggleHint() {
        _uiState.value = _uiState.value.copy(showHint = !_uiState.value.showHint)
    }

    fun submitAnswer() {
        val state = _uiState.value
        val selected = state.selectedOptionIndex ?: return
        val correct = currentQuestion.correctAnswerIndex == selected
        val addedScore = if (correct) currentQuestion.xpReward else 0

        _uiState.value = state.copy(
            isSubmitted = true,
            isCorrect = correct,
            totalScore = state.totalScore + addedScore,
            xpEarned = state.xpEarned + addedScore,
            coinsEarned = state.coinsEarned + (if (correct) 15 else 0)
        )
    }

    fun nextQuestion() {
        val state = _uiState.value
        val nextIdx = state.currentQuestionIndex + 1
        if (nextIdx < state.quiz.questions.size) {
            _uiState.value = state.copy(
                currentQuestionIndex = nextIdx,
                selectedOptionIndex = null,
                isSubmitted = false,
                isCorrect = false,
                showHint = false
            )
        } else {
            _uiState.value = state.copy(isCompleted = true)
        }
    }

    fun restartQuiz() {
        _uiState.value = QuizUiState(quiz = sampleQuiz)
    }
}
