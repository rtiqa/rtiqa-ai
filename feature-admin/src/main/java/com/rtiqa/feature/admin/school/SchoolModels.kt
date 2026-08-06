package com.rtiqa.feature.admin.school

import com.rtiqa.core.domain.model.AcademicYear
import com.rtiqa.core.domain.model.Assessment
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.EducationStage
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.GradeLevel
import com.rtiqa.core.domain.model.School
import com.rtiqa.core.domain.model.SchoolClass
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
    val gradeLevels: List<GradeLevel> = emptyList(),
    val schoolClasses: List<SchoolClass> = emptyList(),
    val sections: List<Section> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val academicYears: List<AcademicYear> = emptyList(),
    val courses: List<Course> = emptyList(),
    val assessments: List<Assessment> = emptyList(),
    val searchQuery: String = "",
    val stageFilter: EducationStage? = null,
    val isLoading: Boolean = false,
    val editingSchool: School? = null,
    val isFormDialogOpen: Boolean = false,
    val selectedTab: Int = 0, // 0: قائمة المدارس, 1: تفاصيل المدرسة, 2: الأعوام الدراسية, 3: المراحل الدراسية, 4: الصفوف, 5: الشعب, 6: المواد
    val activeCategoryTab: Int = 0, // 0: الطلاب, 1: المعلمون, 2: المراحل, 3: الصفوف, 4: الشعب, 5: المواد, 6: الدورات, 7: الاختبارات
    val errorMessage: String? = null
) : ViewUiState {
    val filteredSchools: List<School>
        get() = if (searchQuery.isBlank()) schools else schools.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.code.contains(searchQuery, ignoreCase = true) ||
            it.address.contains(searchQuery, ignoreCase = true)
        }

    val filteredAcademicYears: List<AcademicYear>
        get() = if (searchQuery.isBlank()) academicYears else academicYears.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }

    val filteredGradeLevels: List<GradeLevel>
        get() = gradeLevels.filter { gl ->
            val matchesSearch = searchQuery.isBlank() || gl.name.contains(searchQuery, ignoreCase = true) || gl.code.contains(searchQuery, ignoreCase = true)
            val matchesStage = stageFilter == null || gl.stage == stageFilter
            matchesSearch && matchesStage
        }

    val filteredSchoolClasses: List<SchoolClass>
        get() = if (searchQuery.isBlank()) schoolClasses else schoolClasses.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.gradeLevel.contains(searchQuery, ignoreCase = true) ||
            it.roomNumber.contains(searchQuery, ignoreCase = true)
        }

    val filteredSections: List<Section>
        get() = if (searchQuery.isBlank()) sections else sections.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }

    val filteredSubjects: List<Subject>
        get() = if (searchQuery.isBlank()) subjects else subjects.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.code.contains(searchQuery, ignoreCase = true)
        }
}

sealed interface SchoolUiAction : ViewUiAction {
    data class UpdateSearchQuery(val query: String) : SchoolUiAction
    data class UpdateStageFilter(val stage: EducationStage?) : SchoolUiAction
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

    // Academic Year Management
    data class SaveAcademicYear(
        val id: String?,
        val name: String,
        val startDate: String,
        val endDate: String,
        val isCurrent: Boolean
    ) : SchoolUiAction
    data class DeleteAcademicYear(val id: String) : SchoolUiAction

    // Grade Level Management
    data class SaveGradeLevel(
        val id: String?,
        val name: String,
        val code: String,
        val sequence: Int,
        val stage: EducationStage
    ) : SchoolUiAction
    data class DeleteGradeLevel(val id: String) : SchoolUiAction

    // Class Management
    data class SaveSchoolClass(
        val id: String?,
        val name: String,
        val gradeLevel: String,
        val roomNumber: String,
        val capacity: Int
    ) : SchoolUiAction
    data class DeleteSchoolClass(val id: String) : SchoolUiAction

    // Section Management
    data class SaveSection(
        val id: String?,
        val name: String,
        val capacity: Int
    ) : SchoolUiAction
    data class DeleteSection(val id: String) : SchoolUiAction

    // Subject Management
    data class SaveSubject(
        val id: String?,
        val name: String,
        val code: String,
        val creditHours: Int
    ) : SchoolUiAction
    data class DeleteSubject(val id: String) : SchoolUiAction

    data class AddStudentToActiveSchool(val name: String, val email: String, val department: String) : SchoolUiAction
    data class AddTeacherToActiveSchool(val name: String, val email: String, val department: String) : SchoolUiAction
    data class AddCourseToActiveSchool(val title: String, val category: String) : SchoolUiAction
    data class AddAssessmentToActiveSchool(val title: String, val passingScore: Int) : SchoolUiAction
}

sealed interface SchoolUiEvent : ViewUiEvent {
    data class ShowToast(val message: String) : SchoolUiEvent
}
