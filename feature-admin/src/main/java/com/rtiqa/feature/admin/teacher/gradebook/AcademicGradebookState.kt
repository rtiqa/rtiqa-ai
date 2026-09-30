package com.rtiqa.feature.admin.teacher.gradebook

import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState

/**
 * Grade evaluation scale for student status.
 */
enum class GradeStatus(val labelAr: String) {
    EXCELLENT("ممتاز (A)"),
    VERY_GOOD("جيد جداً (B)"),
    GOOD("جيد (C)"),
    NEEDS_SUPPORT("يحتاج دعم (D)"),
    CRITICAL("خطر الرسوب (F)")
}

/**
 * Single assessment column (e.g. Midterm exam, Homework 1, Lab Project, Final Exam).
 */
data class AssessmentColumn(
    val id: String,
    val title: String,
    val maxScore: Double,
    val weightPercentage: Int, // e.g. 20%
    val category: String // "واجبات", "اختبارات قصيرة", "مشروع عملي", "اختبار نهائي"
)

/**
 * Student's score on a particular assessment column.
 */
data class StudentAssessmentScore(
    val columnId: String,
    val score: Double?, // null if not graded or absent
    val isExcused: Boolean = false,
    val note: String? = null
)

/**
 * Full student grade row in the gradebook matrix.
 */
data class StudentGradeRow(
    val studentId: String,
    val studentName: String,
    val studentNumber: String,
    val avatarInitial: String = studentName.take(1),
    val scores: Map<String, StudentAssessmentScore>, // columnId -> score
    val totalScore: Double,
    val maxPossibleScore: Double = 100.0,
    val percentage: Double = if (maxPossibleScore > 0) (totalScore / maxPossibleScore) * 100.0 else 0.0,
    val attendanceRate: Float = 0.95f,
    val status: GradeStatus = when {
        percentage >= 90 -> GradeStatus.EXCELLENT
        percentage >= 80 -> GradeStatus.VERY_GOOD
        percentage >= 70 -> GradeStatus.GOOD
        percentage >= 60 -> GradeStatus.NEEDS_SUPPORT
        else -> GradeStatus.CRITICAL
    }
)

/**
 * Class selector option in Gradebook.
 */
data class GradebookClassOption(
    val classId: String,
    val className: String,
    val subject: String,
    val academicTerm: String
)

/**
 * Academic Gradebook UI State for Jetpack Compose.
 */
data class AcademicGradebookUiState(
    val selectedClassId: String = "",
    val availableClasses: List<GradebookClassOption> = emptyList(),
    val selectedTerm: String = "الفصل الدراسي الثاني 1447هـ",
    val columns: List<AssessmentColumn> = emptyList(),
    val students: List<StudentGradeRow> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: GradeStatus? = null,
    val selectedViewMode: Int = 0, // 0: Matrix (Table/Cards), 1: Statistics/Distribution, 2: At-Risk Students
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val classAverage: Double = 0.0,
    val highestScore: Double = 0.0,
    val lowestScore: Double = 0.0,
    val passRatePercentage: Int = 0,
    val editingStudentScore: Pair<StudentGradeRow, AssessmentColumn>? = null,
    val currentEditScoreValue: String = "",
    val showAddAssessmentDialog: Boolean = false,
    val showExportDialog: Boolean = false
) : ViewUiState

/**
 * User interactions & actions on Academic Gradebook.
 */
sealed interface AcademicGradebookUiAction : ViewUiAction {
    data class SelectClass(val classId: String) : AcademicGradebookUiAction
    data class SelectTerm(val term: String) : AcademicGradebookUiAction
    data class SearchQueryChanged(val query: String) : AcademicGradebookUiAction
    data class FilterStatus(val status: GradeStatus?) : AcademicGradebookUiAction
    data class SelectViewMode(val modeIndex: Int) : AcademicGradebookUiAction
    object Refresh : AcademicGradebookUiAction
    data class OpenScoreEditor(val student: StudentGradeRow, val column: AssessmentColumn) : AcademicGradebookUiAction
    data class UpdateScoreValue(val value: String) : AcademicGradebookUiAction
    object SaveScoreEditor : AcademicGradebookUiAction
    object DismissScoreEditor : AcademicGradebookUiAction
    object OpenAddAssessmentDialog : AcademicGradebookUiAction
    object DismissAddAssessmentDialog : AcademicGradebookUiAction
    data class SaveNewAssessment(val title: String, val maxScore: Double, val weight: Int, val category: String) : AcademicGradebookUiAction
    object OpenExportDialog : AcademicGradebookUiAction
    object DismissExportDialog : AcademicGradebookUiAction
    data class ExportGradebook(val format: String) : AcademicGradebookUiAction // "PDF", "EXCEL", "CSV"
}

/**
 * Single-shot UI events (toasts, alerts, navigation).
 */
sealed interface AcademicGradebookUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : AcademicGradebookUiEvent
    data class NavigateToStudentProfile(val studentId: String) : AcademicGradebookUiEvent
}
