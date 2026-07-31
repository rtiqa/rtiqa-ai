package com.rtiqa.feature.lessons

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.CompleteLessonUseCase
import com.rtiqa.core.domain.usecase.GetLessonDetailUseCase
import com.rtiqa.core.domain.usecase.GetNextLessonUseCase
import com.rtiqa.core.domain.usecase.SaveLessonProgressUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class LessonViewerUiState(
    val lessonId: String = "",
    val courseId: String = "",
    val lesson: Lesson? = null,
    val nextLesson: Lesson? = null,
    val progressPercent: Float = 0f,
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : ViewUiState

sealed interface LessonViewerUiAction : ViewUiAction {
    data class InitializeLesson(val lessonId: String, val courseId: String, val title: String = "", val content: String = "") : LessonViewerUiAction
    data class SaveProgress(val progressPercent: Float) : LessonViewerUiAction
    object MarkLessonCompleteClicked : LessonViewerUiAction
    object NextLessonClicked : LessonViewerUiAction
}

sealed interface LessonViewerUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : LessonViewerUiEvent
    data class NavigateToNextLesson(val lessonId: String) : LessonViewerUiEvent
    object NavigateBack : LessonViewerUiEvent
}

class LessonViewerViewModel(
    private val completeLessonUseCase: CompleteLessonUseCase,
    private val getLessonDetailUseCase: GetLessonDetailUseCase? = null,
    private val getNextLessonUseCase: GetNextLessonUseCase? = null,
    private val saveLessonProgressUseCase: SaveLessonProgressUseCase? = null
) : BaseViewModel<LessonViewerUiState, LessonViewerUiAction, LessonViewerUiEvent>(LessonViewerUiState()) {

    override fun onAction(action: LessonViewerUiAction) {
        when (action) {
            is LessonViewerUiAction.InitializeLesson -> loadLesson(action.lessonId, action.courseId, action.title, action.content)
            is LessonViewerUiAction.SaveProgress -> updateProgress(action.progressPercent)
            is LessonViewerUiAction.MarkLessonCompleteClicked -> markComplete()
            is LessonViewerUiAction.NextLessonClicked -> handleNextLesson()
        }
    }

    private fun loadLesson(lessonId: String, courseId: String, fallbackTitle: String, fallbackContent: String) {
        setState { copy(lessonId = lessonId, courseId = courseId, isLoading = true) }
        
        if (getLessonDetailUseCase != null) {
            viewModelScope.launch {
                getLessonDetailUseCase.invoke(lessonId).collectLatest { fetchedLesson ->
                    val current = fetchedLesson ?: Lesson(
                        id = lessonId,
                        courseId = courseId,
                        title = fallbackTitle.ifBlank { "الدرس $lessonId" },
                        content = fallbackContent.ifBlank { "محتوى الدرس المفصل" },
                        order = 1,
                        isCompleted = false
                    )
                    setState {
                        copy(
                            lesson = current,
                            isCompleted = current.isCompleted,
                            isLoading = false
                        )
                    }
                    observeNextLesson(courseId, lessonId)
                }
            }
        } else {
            val fallbackLesson = Lesson(
                id = lessonId,
                courseId = courseId,
                title = fallbackTitle.ifBlank { "الدرس $lessonId" },
                content = fallbackContent.ifBlank { "محتوى الدرس المفصل" },
                order = 1,
                isCompleted = false
            )
            setState {
                copy(
                    lesson = fallbackLesson,
                    isCompleted = fallbackLesson.isCompleted,
                    isLoading = false
                )
            }
        }
    }

    private fun observeNextLesson(courseId: String, currentLessonId: String) {
        if (getNextLessonUseCase == null || courseId.isBlank() || currentLessonId.isBlank()) return
        viewModelScope.launch {
            getNextLessonUseCase.invoke(courseId, currentLessonId).collectLatest { next ->
                setState { copy(nextLesson = next) }
            }
        }
    }

    private fun updateProgress(percent: Float) {
        val lId = currentState.lessonId
        val cId = currentState.courseId
        if (lId.isBlank() || cId.isBlank()) return

        setState { copy(progressPercent = percent) }
        viewModelScope.launch {
            saveLessonProgressUseCase?.invoke(lId, cId, percent)
        }
    }

    private fun markComplete() {
        val lId = currentState.lessonId
        val cId = currentState.courseId
        if (lId.isBlank() || cId.isBlank()) return

        setState { copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = completeLessonUseCase(lId, cId)) {
                is RtiqaResult.Success -> {
                    setState { copy(isCompleted = true, progressPercent = 1.0f, isLoading = false) }
                    sendEvent(LessonViewerUiEvent.ShowToast("تم إكمال الدرس! كسبت +25 XP 🎉"))
                }
                is RtiqaResult.Error -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                    sendEvent(LessonViewerUiEvent.ShowToast(result.error.message))
                }
                is RtiqaResult.Loading -> {
                    setState { copy(isLoading = true) }
                }
            }
        }
    }

    private fun handleNextLesson() {
        val nextId = currentState.nextLesson?.id
        if (nextId != null) {
            sendEvent(LessonViewerUiEvent.NavigateToNextLesson(nextId))
        } else {
            sendEvent(LessonViewerUiEvent.ShowToast("وصلت لأخر درس في هذا المقرر 👍"))
        }
    }
}
