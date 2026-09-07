package com.rtiqa.feature.admin.academic

import com.rtiqa.core.domain.model.AcademicYear
import com.rtiqa.core.domain.model.Department
import com.rtiqa.core.domain.model.GradeLevel
import com.rtiqa.core.domain.model.Major
import com.rtiqa.core.domain.model.Section
import com.rtiqa.core.domain.model.Semester
import com.rtiqa.core.domain.model.StudyPlan
import com.rtiqa.core.domain.model.Subject

enum class AcademicTab(val titleAr: String) {
    YEARS("الأعوام الدراسية"),
    TERMS("الفصول الدراسية"),
    GRADE_LEVELS("المراحل الدراسية"),
    DEPARTMENTS("الأقسام"),
    MAJORS("التخصصات"),
    SUBJECTS("المواد الدراسية"),
    SECTIONS("الشعب"),
    STUDY_PLANS("الخطط الدراسية")
}

data class AcademicStructureUiState(
    val currentTab: AcademicTab = AcademicTab.YEARS,
    val isLoading: Boolean = false,
    val error: String? = null,
    
    val currentOrgId: String = "org_001",
    val currentSchoolId: String = "school_001",
    
    // Parent selections for drill-down
    val selectedYearId: String? = null,
    val selectedDepartmentId: String? = null,
    val selectedMajorId: String? = null,

    // Lists
    val academicYears: List<AcademicYear> = emptyList(),
    val semesters: List<Semester> = emptyList(),
    val gradeLevels: List<GradeLevel> = emptyList(),
    val departments: List<Department> = emptyList(),
    val majors: List<Major> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val sections: List<Section> = emptyList(),
    val studyPlans: List<StudyPlan> = emptyList()
)

sealed class AcademicStructureAction {
    data class SelectTab(val tab: AcademicTab) : AcademicStructureAction()
    
    data class SelectAcademicYear(val id: String?) : AcademicStructureAction()
    data class SelectDepartment(val id: String?) : AcademicStructureAction()
    data class SelectMajor(val id: String?) : AcademicStructureAction()

    data class SaveAcademicYear(val item: AcademicYear) : AcademicStructureAction()
    data class DeleteAcademicYear(val id: String) : AcademicStructureAction()

    data class SaveSemester(val item: Semester) : AcademicStructureAction()
    data class DeleteSemester(val id: String) : AcademicStructureAction()

    data class SaveGradeLevel(val item: GradeLevel) : AcademicStructureAction()
    data class DeleteGradeLevel(val id: String) : AcademicStructureAction()

    data class SaveDepartment(val item: Department) : AcademicStructureAction()
    data class DeleteDepartment(val id: String) : AcademicStructureAction()

    data class SaveMajor(val item: Major) : AcademicStructureAction()
    data class DeleteMajor(val id: String) : AcademicStructureAction()

    data class SaveSubject(val item: Subject) : AcademicStructureAction()
    data class DeleteSubject(val id: String) : AcademicStructureAction()

    data class SaveSection(val item: Section) : AcademicStructureAction()
    data class DeleteSection(val id: String) : AcademicStructureAction()

    data class SaveStudyPlan(val item: StudyPlan) : AcademicStructureAction()
    data class DeleteStudyPlan(val id: String) : AcademicStructureAction()
    
    object ClearError : AcademicStructureAction()
}
