package com.rtiqa.feature.admin.school

import com.rtiqa.core.domain.model.Assessment
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.School
import com.rtiqa.core.domain.model.Section
import com.rtiqa.core.domain.model.Subject
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState

data class SchoolUiState(
    val schools: List<School> = emptyList(),
    val activeSchool: School? = null,
    val activeSchoolId: String = "school_001",
    val students: List<EnterpriseMember> = emptyList(),
    val teachers: List<EnterpriseMember> = emptyList(),
    val sections: List<Section> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val courses: List<Course> = emptyList(),
    val assessments: List<Assessment> = emptyList(),
    val isLoading: Boolean = false,
    val editingSchool: School? = null,
    val isFormDialogOpen: Boolean = false,
    val selectedTab: Int = 0, // 0: قائمة المدارس, 1: بيانات المدرسة النشطة
    val activeCategoryTab: Int = 0, // 0: الطلاب, 1: المعلمون, 2: الصفوف, 3: المواد, 4: الدورات, 5: الاختبارات
    val errorMessage: String? = null
) : ViewUiState

sealed interface SchoolUiAction : ViewUiAction {
    data class SelectActiveSchool(val schoolId: String) : SchoolUiAction
    data class OpenFormDialog(val school: School? = null) : SchoolUiAction
    object CloseFormDialog : SchoolUiAction
    data class SaveSchool(
        val id: String?,
        val name: String,
        val code: String,
        val address: String,
        val phone: String,
        val studentsCount: Int,
        val teachersCount: Int
    ) : SchoolUiAction
    data class DeleteSchool(val schoolId: String) : SchoolUiAction
    data class SelectTab(val index: Int) : SchoolUiAction
    data class SelectCategoryTab(val index: Int) : SchoolUiAction
    data class AddStudentToActiveSchool(val name: String, val email: String, val department: String) : SchoolUiAction
    data class AddTeacherToActiveSchool(val name: String, val email: String, val department: String) : SchoolUiAction
    data class AddCourseToActiveSchool(val title: String, val category: String) : SchoolUiAction
    data class AddAssessmentToActiveSchool(val title: String, val passingScore: Int) : SchoolUiAction
}

sealed interface SchoolUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : SchoolUiEvent
}
