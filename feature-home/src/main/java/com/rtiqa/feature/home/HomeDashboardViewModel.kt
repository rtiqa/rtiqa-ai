package com.rtiqa.feature.home

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.usecase.GetCoursesUseCase
import com.rtiqa.core.domain.usecase.GetLessonsForCourseUseCase
import com.rtiqa.core.domain.usecase.GetUserProfileUseCase
import com.rtiqa.core.domain.usecase.ObserveSyncStatusUseCase
import com.rtiqa.core.domain.usecase.UpdateUserStreakUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

data class HomeDashboardUiState(
    val userProfile: UserProfile? = null,
    val featuredCourses: List<Course> = emptyList(),
    val currentLesson: Lesson? = null,
    val completedLessonsCount: Int = 0,
    val passedQuizzesCount: Int = 0,
    val pendingSyncCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOfflineMode: Boolean = false
) : ViewUiState

sealed interface HomeDashboardUiAction : ViewUiAction {
    object RefreshDashboard : HomeDashboardUiAction
    object ClaimDailyStreak : HomeDashboardUiAction
    data class CourseSelected(val courseId: String) : HomeDashboardUiAction
}

sealed interface HomeDashboardUiEvent : ViewUiEvent {
    data class NavigateToCourseDetail(val courseId: String) : HomeDashboardUiEvent
    data class ShowNotification(val message: String) : HomeDashboardUiEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeDashboardViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getCoursesUseCase: GetCoursesUseCase,
    private val getLessonsForCourseUseCase: GetLessonsForCourseUseCase,
    private val observeSyncStatusUseCase: ObserveSyncStatusUseCase,
    private val updateUserStreakUseCase: UpdateUserStreakUseCase
) : BaseViewModel<HomeDashboardUiState, HomeDashboardUiAction, HomeDashboardUiEvent>(
    HomeDashboardUiState()
) {

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val coursesWithLessons = getCoursesUseCase().flatMapLatest { courses ->
            val featuredCourses = courses.take(5)
            val enrolledCourses = featuredCourses.filter { it.isEnrolled }
            if (enrolledCourses.isEmpty()) {
                flowOf(HomeLearningData(featuredCourses, emptyList()))
            } else {
                combine(enrolledCourses.map { getLessonsForCourseUseCase(it.id) }) { lessonsByCourse ->
                    HomeLearningData(featuredCourses, lessonsByCourse.flatMap { it })
                }
            }
        }
        combine(
            getUserProfileUseCase(),
            coursesWithLessons,
            observeSyncStatusUseCase()
        ) { profile, learningData, syncCount ->
            val continueCourse = learningData.courses
                .filter { it.isEnrolled }
                .firstOrNull { it.progressPercent < 1f }
            val currentLesson = continueCourse?.let { course ->
                selectFirstIncompleteLesson(course.id, learningData.lessons)
            }
            setState {
                copy(
                    userProfile = profile,
                    featuredCourses = learningData.courses,
                    currentLesson = currentLesson,
                    completedLessonsCount = countCompletedLessons(learningData.lessons),
                    passedQuizzesCount = 0,
                    pendingSyncCount = syncCount,
                    isLoading = false,
                    isOfflineMode = profile?.isOfflineModeEnabled ?: false
                )
            }
        }.launchIn(viewModelScope)
    }

    private data class HomeLearningData(
        val courses: List<Course>,
        val lessons: List<Lesson>
    )

    override fun onAction(action: HomeDashboardUiAction) {
        when (action) {
            is HomeDashboardUiAction.RefreshDashboard -> loadDashboardData()
            is HomeDashboardUiAction.ClaimDailyStreak -> claimStreak()
            is HomeDashboardUiAction.CourseSelected -> {
                sendEvent(HomeDashboardUiEvent.NavigateToCourseDetail(action.courseId))
            }
        }
    }

    private fun claimStreak() {
        viewModelScope.launch {
            val result = updateUserStreakUseCase()
            if (result is com.rtiqa.core.domain.result.RtiqaResult.Success) {
                sendEvent(HomeDashboardUiEvent.ShowNotification("تم تحديث التتابع اليومي! واصل التعلم! 🔥"))
            }
        }
    }
}

fun selectFirstIncompleteLesson(courseId: String, lessons: List<Lesson>): Lesson? =
    lessons.asSequence()
        .filter { it.courseId == courseId && !it.isCompleted }
        .minByOrNull { it.order }

fun countCompletedLessons(lessons: List<Lesson>): Int = lessons.count { it.isCompleted }
