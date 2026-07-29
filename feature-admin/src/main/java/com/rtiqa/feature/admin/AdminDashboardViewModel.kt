package com.rtiqa.feature.admin

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.usecase.GetCoursesUseCase
import com.rtiqa.core.domain.usecase.GetUserProfileUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

data class AdminDashboardUiState(
    val currentUser: UserProfile? = null,
    val isUserAdmin: Boolean = false,
    val courses: List<Course> = emptyList(),
    val totalCoursesCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) : ViewUiState

sealed interface AdminDashboardUiAction : ViewUiAction {
    object RefreshMetrics : AdminDashboardUiAction
    data class DeleteCourseRequested(val courseId: String) : AdminDashboardUiAction
}

sealed interface AdminDashboardUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : AdminDashboardUiEvent
}

class AdminDashboardViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getCoursesUseCase: GetCoursesUseCase
) : BaseViewModel<AdminDashboardUiState, AdminDashboardUiAction, AdminDashboardUiEvent>(AdminDashboardUiState()) {

    init {
        observeAdminMetrics()
    }

    private fun observeAdminMetrics() {
        combine(
            getUserProfileUseCase(),
            getCoursesUseCase()
        ) { user, courses ->
            setState {
                copy(
                    currentUser = user,
                    isUserAdmin = user?.isAdmin ?: false,
                    courses = courses,
                    totalCoursesCount = courses.size,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: AdminDashboardUiAction) {
        when (action) {
            is AdminDashboardUiAction.RefreshMetrics -> observeAdminMetrics()
            is AdminDashboardUiAction.DeleteCourseRequested -> {
                sendEvent(AdminDashboardUiEvent.ShowToast("تم طلب إجراء الإدارة للدورة ${action.courseId}"))
            }
        }
    }
}
