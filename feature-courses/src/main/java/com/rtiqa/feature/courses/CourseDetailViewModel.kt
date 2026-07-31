package com.rtiqa.feature.courses

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.domain.usecase.DownloadCourseUseCase
import com.rtiqa.core.domain.usecase.GetCourseDetailUseCase
import com.rtiqa.core.domain.usecase.GetLessonsForCourseUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

data class CourseDetailUiState(
    val courseId: String = "",
    val course: Course? = null,
    val lessons: List<Lesson> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) : ViewUiState

sealed interface CourseDetailUiAction : ViewUiAction {
    data class LoadCourseDetail(val id: String) : CourseDetailUiAction
    data class LessonClicked(val lessonId: String) : CourseDetailUiAction
    data class MarkLessonCompleted(val lessonId: String) : CourseDetailUiAction
    object EnrollClicked : CourseDetailUiAction
    object BookmarkToggled : CourseDetailUiAction
    object StartQuizClicked : CourseDetailUiAction
    object DownloadCourseClicked : CourseDetailUiAction
}

sealed interface CourseDetailUiEvent : ViewUiEvent {
    data class NavigateToLessonViewer(val lessonId: String) : CourseDetailUiEvent
    data class NavigateToQuiz(val courseId: String) : CourseDetailUiEvent
    data class ShowToast(val message: String) : CourseDetailUiEvent
}

class CourseDetailViewModel(
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val getLessonsForCourseUseCase: GetLessonsForCourseUseCase,
    private val downloadCourseUseCase: DownloadCourseUseCase,
    private val enrollCourseUseCase: com.rtiqa.core.domain.usecase.EnrollCourseUseCase? = null,
    private val toggleBookmarkUseCase: com.rtiqa.core.domain.usecase.ToggleBookmarkUseCase? = null,
    private val completeLessonUseCase: com.rtiqa.core.domain.usecase.CompleteLessonUseCase? = null
) : BaseViewModel<CourseDetailUiState, CourseDetailUiAction, CourseDetailUiEvent>(CourseDetailUiState()) {

    override fun onAction(action: CourseDetailUiAction) {
        when (action) {
            is CourseDetailUiAction.LoadCourseDetail -> observeCourse(action.id)
            is CourseDetailUiAction.LessonClicked -> sendEvent(CourseDetailUiEvent.NavigateToLessonViewer(action.lessonId))
            is CourseDetailUiAction.MarkLessonCompleted -> markLessonCompleted(action.lessonId)
            is CourseDetailUiAction.EnrollClicked -> enrollCourse()
            is CourseDetailUiAction.BookmarkToggled -> toggleBookmark()
            is CourseDetailUiAction.StartQuizClicked -> {
                if (currentState.courseId.isNotBlank()) {
                    sendEvent(CourseDetailUiEvent.NavigateToQuiz(currentState.courseId))
                }
            }
            is CourseDetailUiAction.DownloadCourseClicked -> downloadCourse()
        }
    }

    private fun observeCourse(id: String) {
        setState { copy(courseId = id, isLoading = true) }
        combine(
            getCourseDetailUseCase(id),
            getLessonsForCourseUseCase(id)
        ) { course, lessons ->
            setState {
                copy(
                    course = course,
                    lessons = lessons,
                    isLoading = false,
                    errorMessage = if (course == null) "لم يتم العثور على الدورة" else null
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun enrollCourse() {
        val id = currentState.courseId
        if (id.isBlank()) return
        viewModelScope.launch {
            when (val result = enrollCourseUseCase?.invoke(id)) {
                is com.rtiqa.core.domain.result.RtiqaResult.Success -> {
                    sendEvent(CourseDetailUiEvent.ShowToast("تم التسجيل في المقرر بنجاح!"))
                }
                is com.rtiqa.core.domain.result.RtiqaResult.Error -> {
                    sendEvent(CourseDetailUiEvent.ShowToast(result.error.message))
                }
                else -> {}
            }
        }
    }

    private fun toggleBookmark() {
        val course = currentState.course ?: return
        viewModelScope.launch {
            toggleBookmarkUseCase?.invoke(course.id, !course.isBookmarked)
        }
    }

    private fun markLessonCompleted(lessonId: String) {
        val courseId = currentState.courseId
        if (courseId.isBlank() || lessonId.isBlank()) return
        viewModelScope.launch {
            when (val result = completeLessonUseCase?.invoke(lessonId, courseId)) {
                is com.rtiqa.core.domain.result.RtiqaResult.Success -> {
                    sendEvent(CourseDetailUiEvent.ShowToast("تم إكمال الدرس! +25 XP 🎉"))
                }
                is com.rtiqa.core.domain.result.RtiqaResult.Error -> {
                    sendEvent(CourseDetailUiEvent.ShowToast(result.error.message))
                }
                else -> {}
            }
        }
    }

    private fun downloadCourse() {
        val id = currentState.courseId
        if (id.isBlank()) return
        viewModelScope.launch {
            when (val result = downloadCourseUseCase(id)) {
                is com.rtiqa.core.domain.result.RtiqaResult.Success -> {
                    sendEvent(CourseDetailUiEvent.ShowToast("بدأ التحميل للعمل بدون إنترنت بنجاح!"))
                }
                is com.rtiqa.core.domain.result.RtiqaResult.Error -> {
                    sendEvent(CourseDetailUiEvent.ShowToast(result.error.message))
                }
                is com.rtiqa.core.domain.result.RtiqaResult.Loading -> {
                    sendEvent(CourseDetailUiEvent.ShowToast("جاري تهيئة التحميل..."))
                }
            }
        }
    }
}
