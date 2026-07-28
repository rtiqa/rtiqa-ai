package com.rtiqa.feature.lessons

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.CompleteLessonUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.launch

data class LessonViewerUiState(
    val lessonId: String = "",
    val courseId: String = "",
    val lesson: Lesson? = null,
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : ViewUiState

sealed interface LessonViewerUiAction : ViewUiAction {
    data class InitializeLesson(val lessonId: String, val courseId: String, val title: String, val content: String) : LessonViewerUiAction
    object MarkLessonCompleteClicked : LessonViewerUiAction
}

sealed interface LessonViewerUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : LessonViewerUiEvent
    object NavigateBack : LessonViewerUiEvent
}

class LessonViewerViewModel(
    private val completeLessonUseCase: CompleteLessonUseCase
) : BaseViewModel<LessonViewerUiState, LessonViewerUiAction, LessonViewerUiEvent>(LessonViewerUiState()) {

    override fun onAction(action: LessonViewerUiAction) {
        when (action) {
            is LessonViewerUiAction.InitializeLesson -> {
                val mockLesson = Lesson(
                    id = action.lessonId,
                    courseId = action.courseId,
                    title = action.title,
                    content = action.content,
                    order = 1,
                    isCompleted = false
                )
                setState {
                    copy(
                        lessonId = action.lessonId,
                        courseId = action.courseId,
                        lesson = mockLesson,
                        isLoading = false
                    )
                }
            }
            is LessonViewerUiAction.MarkLessonCompleteClicked -> markComplete()
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
                    setState { copy(isCompleted = true, isLoading = false) }
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
}
