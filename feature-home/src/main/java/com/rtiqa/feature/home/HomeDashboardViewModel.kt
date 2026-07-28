package com.rtiqa.feature.home

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.usecase.GetCoursesUseCase
import com.rtiqa.core.domain.usecase.GetUserProfileUseCase
import com.rtiqa.core.domain.usecase.ObserveSyncStatusUseCase
import com.rtiqa.core.domain.usecase.UpdateUserStreakUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

data class HomeDashboardUiState(
    val userProfile: UserProfile? = null,
    val featuredCourses: List<Course> = emptyList(),
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

class HomeDashboardViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getCoursesUseCase: GetCoursesUseCase,
    private val observeSyncStatusUseCase: ObserveSyncStatusUseCase,
    private val updateUserStreakUseCase: UpdateUserStreakUseCase
) : BaseViewModel<HomeDashboardUiState, HomeDashboardUiAction, HomeDashboardUiEvent>(
    HomeDashboardUiState()
) {

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        combine(
            getUserProfileUseCase(),
            getCoursesUseCase(),
            observeSyncStatusUseCase()
        ) { profile, courses, syncCount ->
            setState {
                copy(
                    userProfile = profile,
                    featuredCourses = courses.take(5),
                    pendingSyncCount = syncCount,
                    isLoading = false,
                    isOfflineMode = profile?.isOfflineModeEnabled ?: false
                )
            }
        }.launchIn(viewModelScope)
    }

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
                sendEvent(HomeDashboardUiEvent.ShowNotification("Streak updated! Keep learning! 🔥"))
            }
        }
    }
}
