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
import kotlinx.coroutines.Job
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
    data class InitializeLesson(val lessonId: String, val courseId: String) : LessonViewerUiAction
    data class SaveProgress(val progressPercent: Float) : LessonViewerUiAction
    object MarkLessonCompleteClicked : LessonViewerUiAction
    object NextLessonClicked : LessonViewerUiAction
    object PrevLessonClicked : LessonViewerUiAction
}

sealed interface LessonViewerUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : LessonViewerUiEvent
    data class NavigateToNextLesson(val courseId: String, val lessonId: String) : LessonViewerUiEvent
    data class NavigateToPrevLesson(val courseId: String, val lessonId: String) : LessonViewerUiEvent
    object NavigateBack : LessonViewerUiEvent
}

class LessonViewerViewModel(
    private val completeLessonUseCase: CompleteLessonUseCase,
    private val getLessonDetailUseCase: GetLessonDetailUseCase,
    private val getNextLessonUseCase: GetNextLessonUseCase,
    private val saveLessonProgressUseCase: SaveLessonProgressUseCase,
    private val getLessonsForCourseUseCase: GetLessonsForCourseUseCase
) : BaseViewModel<LessonViewerUiState, LessonViewerUiAction, LessonViewerUiEvent>(LessonViewerUiState()) {

    private var lessonJob: Job? = null
    private var nextLessonJob: Job? = null
    private var courseLessonsJob: Job? = null

    override fun onAction(action: LessonViewerUiAction) {
        when (action) {
            is LessonViewerUiAction.InitializeLesson -> loadLesson(action.lessonId, action.courseId)
            is LessonViewerUiAction.SaveProgress -> updateProgress(action.progressPercent)
            is LessonViewerUiAction.MarkLessonCompleteClicked -> markComplete()
            is LessonViewerUiAction.NextLessonClicked -> handleNextLesson()
            is LessonViewerUiAction.PrevLessonClicked -> handlePrevLesson()
        }
    }

    private fun loadLesson(lessonId: String, courseId: String) {
        lessonJob?.cancel()
        nextLessonJob?.cancel()
        courseLessonsJob?.cancel()
        if (lessonId.isBlank() || courseId.isBlank()) {
            setState { copy(lesson = null, isLoading = false, errorMessage = "Lesson unavailable.") }
            return
        }
        setState {
            copy(
                lessonId = lessonId,
                courseId = courseId,
                lesson = null,
                nextLesson = null,
                prevLesson = null,
                isLoading = true,
                errorMessage = null
            )
        }
        lessonJob = viewModelScope.launch {
            getLessonDetailUseCase(lessonId).collectLatest { fetchedLesson ->
                if (fetchedLesson == null || fetchedLesson.courseId != courseId) {
                    setState { copy(lesson = null, isLoading = false, errorMessage = "Lesson unavailable.") }
                } else {
                    setState {
                        copy(
                            lesson = fetchedLesson,
                            isCompleted = fetchedLesson.isCompleted,
                            progressPercent = if (fetchedLesson.isCompleted) 1f else 0f,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            }
        }
        observeLessons(courseId, lessonId)
    }

    private fun observeLessons(courseId: String, currentLessonId: String) {
        nextLessonJob = viewModelScope.launch {
            getNextLessonUseCase(courseId, currentLessonId).collectLatest { next ->
                setState { copy(nextLesson = next) }
            }
        }
        courseLessonsJob = viewModelScope.launch {
            getLessonsForCourseUseCase(courseId).collectLatest { courseLessons ->
                val sorted = courseLessons.sortedBy { it.order }
                val currentIndex = sorted.indexOfFirst { it.id == currentLessonId }
                setState {
                    copy(
                        prevLesson = if (currentIndex > 0) sorted[currentIndex - 1] else null,
                        nextLesson = if (currentIndex >= 0 && currentIndex + 1 < sorted.size) {
                            sorted[currentIndex + 1]
                        } else currentState.nextLesson
                    )
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

        val clamped = percent.coerceIn(0f, 1f)
        viewModelScope.launch {
            when (val result = saveLessonProgressUseCase(lId, cId, clamped)) {
                is RtiqaResult.Success -> setState { copy(progressPercent = clamped, errorMessage = null) }
                is RtiqaResult.Error -> {
                    setState { copy(errorMessage = result.error.message) }
                    sendEvent(LessonViewerUiEvent.ShowToast(result.error.message))
                }
                is RtiqaResult.Loading -> Unit
            }
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
            when (val result = completeLessonUseCase(lId, cId)) {
                is RtiqaResult.Success -> {
                    setState { copy(isCompleted = true, progressPercent = 1.0f, isLoading = false) }
                    sendEvent(LessonViewerUiEvent.ShowToast("تم إكمال الدرس بنجاح"))
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
            sendEvent(LessonViewerUiEvent.NavigateToNextLesson(currentState.courseId, nextId))
        } else {
            sendEvent(LessonViewerUiEvent.ShowToast("وصلت لأخر درس في هذا المقرر 👍"))
        }
    }

    private fun handlePrevLesson() {
        val prevId = currentState.prevLesson?.id
        if (prevId != null) {
            sendEvent(LessonViewerUiEvent.NavigateToPrevLesson(currentState.courseId, prevId))
        }
    }
}


class LessonViewerViewModelFactory(
    private val completeLessonUseCase: CompleteLessonUseCase,
    private val getLessonDetailUseCase: GetLessonDetailUseCase,
    private val getNextLessonUseCase: GetNextLessonUseCase,
    private val saveLessonProgressUseCase: SaveLessonProgressUseCase,
    private val getLessonsForCourseUseCase: GetLessonsForCourseUseCase
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
