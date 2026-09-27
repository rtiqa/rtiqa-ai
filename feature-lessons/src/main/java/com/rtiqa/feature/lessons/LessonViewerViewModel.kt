package com.rtiqa.feature.lessons

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.CompleteLessonUseCase
import com.rtiqa.core.domain.usecase.GetLessonDetailUseCase
import com.rtiqa.core.domain.usecase.GetLessonsForCourseUseCase
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
    val prevLesson: Lesson? = null,
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
    object PrevLessonClicked : LessonViewerUiAction
}

sealed interface LessonViewerUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : LessonViewerUiEvent
    data class NavigateToNextLesson(val lessonId: String) : LessonViewerUiEvent
    data class NavigateToPrevLesson(val lessonId: String) : LessonViewerUiEvent
    object NavigateBack : LessonViewerUiEvent
}

class LessonViewerViewModel(
    private val completeLessonUseCase: CompleteLessonUseCase,
    private val getLessonDetailUseCase: GetLessonDetailUseCase? = null,
    private val getNextLessonUseCase: GetNextLessonUseCase? = null,
    private val saveLessonProgressUseCase: SaveLessonProgressUseCase? = null,
    private val getLessonsForCourseUseCase: GetLessonsForCourseUseCase? = null
) : BaseViewModel<LessonViewerUiState, LessonViewerUiAction, LessonViewerUiEvent>(LessonViewerUiState()) {

    override fun onAction(action: LessonViewerUiAction) {
        when (action) {
            is LessonViewerUiAction.InitializeLesson -> loadLesson(action.lessonId, action.courseId, action.title, action.content)
            is LessonViewerUiAction.SaveProgress -> updateProgress(action.progressPercent)
            is LessonViewerUiAction.MarkLessonCompleteClicked -> markComplete()
            is LessonViewerUiAction.NextLessonClicked -> handleNextLesson()
            is LessonViewerUiAction.PrevLessonClicked -> handlePrevLesson()
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
                            progressPercent = if (current.isCompleted) 1.0f else currentState.progressPercent,
                            isLoading = false
                        )
                    }
                    observeLessons(courseId, lessonId)
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
                    progressPercent = if (fallbackLesson.isCompleted) 1.0f else currentState.progressPercent,
                    isLoading = false
                )
            }
        }
    }

    private fun observeLessons(courseId: String, currentLessonId: String) {
        if (courseId.isBlank() || currentLessonId.isBlank()) return

        // 1. Observe next lesson via GetNextLessonUseCase (primary requirement)
        if (getNextLessonUseCase != null) {
            viewModelScope.launch {
                getNextLessonUseCase.invoke(courseId, currentLessonId).collectLatest { next ->
                    setState { copy(nextLesson = next) }
                }
            }
        }

        // 2. Observe course lessons to reliably resolve previous and next lessons from real course data
        if (getLessonsForCourseUseCase != null) {
            viewModelScope.launch {
                getLessonsForCourseUseCase.invoke(courseId).collectLatest { courseLessons ->
                    if (courseLessons.isNotEmpty()) {
                        val sorted = courseLessons.sortedBy { it.order }
                        val currentIndex = sorted.indexOfFirst { it.id == currentLessonId }
                        val prev = if (currentIndex > 0) sorted[currentIndex - 1] else null
                        val nextFromList = if (currentIndex != -1 && currentIndex + 1 < sorted.size) sorted[currentIndex + 1] else null
                        setState {
                            copy(
                                prevLesson = prev,
                                nextLesson = currentState.nextLesson ?: nextFromList
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateProgress(percent: Float) {
        val lId = currentState.lessonId
        val cId = currentState.courseId
        if (lId.isBlank() || cId.isBlank()) return
        // Never downgrade progress if lesson was already completed
        if (currentState.isCompleted || currentState.lesson?.isCompleted == true) return

        setState { copy(progressPercent = percent) }
        viewModelScope.launch {
            saveLessonProgressUseCase?.invoke(lId, cId, percent)
        }
    }

    private fun markComplete() {
        val lId = currentState.lessonId
        val cId = currentState.courseId
        if (lId.isBlank() || cId.isBlank()) return
        // Prevent awarding XP more than once if already completed
        if (currentState.isCompleted || currentState.lesson?.isCompleted == true) return

        setState { copy(isLoading = true) }
        viewModelScope.launch {
            saveLessonProgressUseCase?.invoke(lId, cId, 1.0f)
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

    private fun handlePrevLesson() {
        val prevId = currentState.prevLesson?.id
        if (prevId != null) {
            sendEvent(LessonViewerUiEvent.NavigateToPrevLesson(prevId))
        }
    }
}


class LessonViewerViewModelFactory(
    private val completeLessonUseCase: CompleteLessonUseCase,
    private val getLessonDetailUseCase: GetLessonDetailUseCase? = null,
    private val getNextLessonUseCase: GetNextLessonUseCase? = null,
    private val saveLessonProgressUseCase: SaveLessonProgressUseCase? = null,
    private val getLessonsForCourseUseCase: GetLessonsForCourseUseCase? = null
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LessonViewerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LessonViewerViewModel(
                completeLessonUseCase,
                getLessonDetailUseCase,
                getNextLessonUseCase,
                saveLessonProgressUseCase,
                getLessonsForCourseUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
