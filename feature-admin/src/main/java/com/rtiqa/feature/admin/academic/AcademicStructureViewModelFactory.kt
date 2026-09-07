package com.rtiqa.feature.admin.academic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rtiqa.core.domain.usecase.*

class AcademicStructureViewModelFactory(
    private val getAcademicYearsUseCase: GetAcademicYearsUseCase,
    private val saveAcademicYearUseCase: SaveAcademicYearUseCase,
    private val deleteAcademicYearUseCase: DeleteAcademicYearUseCase,
    private val getSemestersUseCase: GetSemestersUseCase,
    private val saveSemesterUseCase: SaveSemesterUseCase,
    private val deleteSemesterUseCase: DeleteSemesterUseCase,
    private val getGradeLevelsForSchoolUseCase: GetGradeLevelsForSchoolUseCase,
    private val saveGradeLevelUseCase: SaveGradeLevelUseCase,
    private val deleteGradeLevelUseCase: DeleteGradeLevelUseCase,
    private val getDepartmentsUseCase: GetDepartmentsUseCase,
    private val saveDepartmentUseCase: SaveDepartmentUseCase,
    private val deleteDepartmentUseCase: DeleteDepartmentUseCase,
    private val getMajorsUseCase: GetMajorsUseCase,
    private val saveMajorUseCase: SaveMajorUseCase,
    private val deleteMajorUseCase: DeleteMajorUseCase,
    private val getSubjectsForSchoolUseCase: GetSubjectsForSchoolUseCase,
    private val saveSubjectUseCase: SaveSubjectUseCase,
    private val deleteSubjectUseCase: DeleteSubjectUseCase,
    private val getSectionsForSchoolUseCase: GetSectionsForSchoolUseCase,
    private val saveSectionUseCase: SaveSectionUseCase,
    private val deleteSectionUseCase: DeleteSectionUseCase,
    private val getStudyPlansUseCase: GetStudyPlansUseCase,
    private val saveStudyPlanUseCase: SaveStudyPlanUseCase,
    private val deleteStudyPlanUseCase: DeleteStudyPlanUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AcademicStructureViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AcademicStructureViewModel(
                getAcademicYearsUseCase, saveAcademicYearUseCase, deleteAcademicYearUseCase,
                getSemestersUseCase, saveSemesterUseCase, deleteSemesterUseCase,
                getGradeLevelsForSchoolUseCase, saveGradeLevelUseCase, deleteGradeLevelUseCase,
                getDepartmentsUseCase, saveDepartmentUseCase, deleteDepartmentUseCase,
                getMajorsUseCase, saveMajorUseCase, deleteMajorUseCase,
                getSubjectsForSchoolUseCase, saveSubjectUseCase, deleteSubjectUseCase,
                getSectionsForSchoolUseCase, saveSectionUseCase, deleteSectionUseCase,
                getStudyPlansUseCase, saveStudyPlanUseCase, deleteStudyPlanUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
