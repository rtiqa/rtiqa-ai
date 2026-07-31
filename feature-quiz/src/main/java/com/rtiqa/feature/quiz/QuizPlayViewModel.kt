package com.rtiqa.feature.quiz

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.Quiz
import com.rtiqa.core.domain.model.QuizResult
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.EvaluateQuizAnswersUseCase
import com.rtiqa.core.domain.usecase.GetQuizForCourseUseCase
import com.rtiqa.core.domain.usecase.SubmitQuizResultUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val scorePercent: Int = 0,
    val isPassed: Boolean = false,
    val xpEarned: Int = 0,
    val timeLeftSeconds: Int = 300,
    val isTimerActive: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) : ViewUiState

sealed interface QuizPlayUiAction : ViewUiAction {
    data class LoadQuizForCourse(val courseId: String) : QuizPlayUiAction
    data class AnswerSelected(val questionIndex: Int, val optionIndex: Int) : QuizPlayUiAction
    object NextQuestionClicked : QuizPlayUiAction
    object PreviousQuestionClicked : QuizPlayUiAction
    object SubmitQuizClicked : QuizPlayUiAction
    object TimerTick : QuizPlayUiAction
}

sealed interface QuizPlayUiEvent : ViewUiEvent {
    data class QuizSubmitted(val result: QuizResult, val xpEarned: Int) : QuizPlayUiEvent
    data class ShowToast(val message: String) : QuizPlayUiEvent
}

class QuizPlayViewModel(
    private val getQuizForCourseUseCase: GetQuizForCourseUseCase,
    private val submitQuizResultUseCase: SubmitQuizResultUseCase,
    private val evaluateQuizAnswersUseCase: EvaluateQuizAnswersUseCase = EvaluateQuizAnswersUseCase()
) : BaseViewModel<QuizPlayUiState, QuizPlayUiAction, QuizPlayUiEvent>(QuizPlayUiState()) {

    private var timerJob: Job? = null

    override fun onAction(action: QuizPlayUiAction) {
        when (action) {
            is QuizPlayUiAction.LoadQuizForCourse -> loadQuiz(action.courseId)
            is QuizPlayUiAction.AnswerSelected -> {
                if (currentState.isSubmitted) return
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
            is QuizPlayUiAction.PreviousQuestionClicked -> {
                val current = currentState.currentQuestionIndex
                if (current > 0) {
                    setState { copy(currentQuestionIndex = current - 1) }
                }
            }
            is QuizPlayUiAction.SubmitQuizClicked -> submitQuiz()
            is QuizPlayUiAction.TimerTick -> onTimerTick()
        }
    }

    private fun loadQuiz(courseId: String) {
        timerJob?.cancel()
        setState { copy(courseId = courseId, isLoading = true) }
        getQuizForCourseUseCase(courseId)
            .onEach { quiz ->
                val timeLimit = quiz?.timeLimitSeconds ?: 300
                setState {
                    copy(
                        quiz = quiz,
                        totalQuestions = quiz?.questions?.size ?: 0,
                        timeLeftSeconds = timeLimit,
                        isTimerActive = quiz != null,
                        isLoading = false,
                        errorMessage = if (quiz == null) "لم يتم العثور على تقييم لهذه الدورة" else null
                    )
                }
                if (quiz != null) {
                    startTimer()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (currentState.isTimerActive && currentState.timeLeftSeconds > 0 && !currentState.isSubmitted) {
                delay(1000)
                onAction(QuizPlayUiAction.TimerTick)
            }
        }
    }

    private fun onTimerTick() {
        val nextTime = currentState.timeLeftSeconds - 1
        if (nextTime <= 0) {
            setState { copy(timeLeftSeconds = 0, isTimerActive = false) }
            submitQuiz()
        } else {
            setState { copy(timeLeftSeconds = nextTime) }
        }
    }

    private fun submitQuiz() {
        if (currentState.isSubmitted) return
        timerJob?.cancel()
        val q = currentState.quiz ?: return

        val userAnswersMap = currentState.selectedAnswers.mapKeys { (idx, _) ->
            q.questions.getOrNull(idx)?.id ?: ""
        }

        val evaluation = evaluateQuizAnswersUseCase(q, userAnswersMap)

        setState { copy(isLoading = true, isTimerActive = false) }
        viewModelScope.launch {
            when (val result = submitQuizResultUseCase(q.id, evaluation.score, evaluation.totalQuestions)) {
                is RtiqaResult.Success -> {
                    setState {
                        copy(
                            isSubmitted = true,
                            score = evaluation.score,
                            scorePercent = evaluation.scorePercent,
                            isPassed = evaluation.isPassed,
                            xpEarned = evaluation.xpEarned,
                            isLoading = false
                        )
                    }
                    sendEvent(QuizPlayUiEvent.QuizSubmitted(result.data, evaluation.xpEarned))
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

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
