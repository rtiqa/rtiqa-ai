package com.rtiqa.feature.admin.teacher

import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState

data class TeacherClassItem(
    val id: String,
    val name: String,
    val subject: String,
    val gradeLevel: String,
    val section: String,
    val studentCount: Int,
    val scheduleTime: String,
    val room: String,
    val attendanceRate: Float = 0.95f
)

data class PendingSubmissionItem(
    val id: String,
    val assignmentTitle: String,
    val studentName: String,
    val className: String,
    val submittedTimeAgo: String,
    val type: String, // "واجب" or "مختبر" or "مشروع"
    val maxScore: Int = 100
)

data class UpcomingScheduleItem(
    val id: String,
    val title: String,
    val className: String,
    val time: String,
    val location: String,
    val isLiveNow: Boolean = false
)

data class TeacherMetric(
    val title: String,
    val value: String,
    val subtext: String,
    val iconType: String // "classes", "students", "pending", "attendance"
)

data class TeacherDashboardUiState(
    val teacherName: String = "أ. عبد الله الشهري",
    val schoolName: String = "المدرسة النموذجية الأولية",
    val department: String = "قسم الحاسب والذكاء الاصطناعي",
    val selectedTab: Int = 0, // 0: Overview, 1: Classes & Schedule, 2: Grading Queue, 3: Analytics
    val metrics: List<TeacherMetric> = emptyList(),
    val classes: List<TeacherClassItem> = emptyList(),
    val pendingSubmissions: List<PendingSubmissionItem> = emptyList(),
    val upcomingSchedule: List<UpcomingScheduleItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showQuickNoticeDialog: Boolean = false,
    val selectedClassForNotice: TeacherClassItem? = null
) : ViewUiState

sealed interface TeacherDashboardUiAction : ViewUiAction {
    data class SelectTab(val tabIndex: Int) : TeacherDashboardUiAction
    data class SearchQueryChanged(val query: String) : TeacherDashboardUiAction
    object Refresh : TeacherDashboardUiAction
    data class ClassClicked(val classId: String) : TeacherDashboardUiAction
    data class GradeSubmissionClicked(val submissionId: String) : TeacherDashboardUiAction
    data class OpenQuickNoticeDialog(val classItem: TeacherClassItem?) : TeacherDashboardUiAction
    object CloseQuickNoticeDialog : TeacherDashboardUiAction
    data class SendQuickNotice(val target: String, val message: String) : TeacherDashboardUiAction
}

sealed interface TeacherDashboardUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : TeacherDashboardUiEvent
    data class NavigateToClassDetail(val classId: String) : TeacherDashboardUiEvent
    data class NavigateToGrading(val submissionId: String) : TeacherDashboardUiEvent
}
