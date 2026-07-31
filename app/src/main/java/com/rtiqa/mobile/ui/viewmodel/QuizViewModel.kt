package com.rtiqa.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rtiqa.mobile.data.repository.QuizRepository
import com.rtiqa.mobile.domain.model.Quiz
import com.rtiqa.mobile.domain.model.QuizQuestion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuizUiState(
    val quiz: Quiz,
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isSubmitted: Boolean = false,
    val isCorrect: Boolean = false,
    val showHint: Boolean = false,
    val totalScore: Int = 0,
    val correctAnswersCount: Int = 0,
    val scorePercent: Int = 0,
    val isPassed: Boolean = false,
    val isCompleted: Boolean = false,
    val xpEarned: Int = 0,
    val coinsEarned: Int = 0,
    val timeLeftSeconds: Int = 300,
    val isTimerRunning: Boolean = true
)

class QuizViewModel(
    private val quizRepository: QuizRepository = QuizRepository()
) : ViewModel() {

    private val sampleQuiz = quizRepository.getSampleQuiz("c_ai_101")
    private var timerJob: Job? = null

    private val _uiState = MutableStateFlow(
        QuizUiState(
            quiz = sampleQuiz,
            timeLeftSeconds = sampleQuiz.timeLimitSeconds
        )
    )
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        startTimer()
    }

    val currentQuestion: QuizQuestion
        get() = _uiState.value.quiz.questions[_uiState.value.currentQuestionIndex]

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isTimerRunning && _uiState.value.timeLeftSeconds > 0 && !_uiState.value.isCompleted) {
                delay(1000)
                val remaining = _uiState.value.timeLeftSeconds - 1
                if (remaining <= 0) {
                    _uiState.value = _uiState.value.copy(
                        timeLeftSeconds = 0,
                        isTimerRunning = false,
                        isCompleted = true
                    )
                    calculateFinalResults()
                    break
                } else {
                    _uiState.value = _uiState.value.copy(timeLeftSeconds = remaining)
                }
            }
        }
    }

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
        val newCorrectCount = if (correct) state.correctAnswersCount + 1 else state.correctAnswersCount

        _uiState.value = state.copy(
            isSubmitted = true,
            isCorrect = correct,
            correctAnswersCount = newCorrectCount,
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
            calculateFinalResults()
            _uiState.value = _uiState.value.copy(isCompleted = true, isTimerRunning = false)
            timerJob?.cancel()
        }
    }

    private fun calculateFinalResults() {
        val state = _uiState.value
        val total = state.quiz.questions.size
        val percent = if (total > 0) ((state.correctAnswersCount.toFloat() / total) * 100).toInt() else 0
        val isPassed = percent >= state.quiz.passingScorePercent

        _uiState.value = state.copy(
            scorePercent = percent,
            isPassed = isPassed
        )
    }

    fun restartQuiz() {
        timerJob?.cancel()
        _uiState.value = QuizUiState(
            quiz = sampleQuiz,
            timeLeftSeconds = sampleQuiz.timeLimitSeconds,
            isTimerRunning = true
        )
        startTimer()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
