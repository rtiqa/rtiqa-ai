package com.rtiqa.feature.admin.school

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.domain.usecase.DeleteAcademicYearUseCase
import com.rtiqa.core.domain.usecase.DeleteClassUseCase
import com.rtiqa.core.domain.usecase.DeleteGradeLevelUseCase
import com.rtiqa.core.domain.usecase.DeleteSchoolUseCase
import com.rtiqa.core.domain.usecase.DeleteSectionUseCase
import com.rtiqa.core.domain.usecase.DeleteSubjectUseCase
import com.rtiqa.core.domain.usecase.GetAcademicYearsUseCase
import com.rtiqa.core.domain.usecase.GetAssessmentsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetClassesForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetCoursesForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetGradeLevelsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetSchoolsUseCase
import com.rtiqa.core.domain.usecase.GetSectionsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetStudentsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetSubjectsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetTeachersForSchoolUseCase
import com.rtiqa.core.domain.usecase.SaveAcademicYearUseCase
import com.rtiqa.core.domain.usecase.SaveAssessmentUseCase
import com.rtiqa.core.domain.usecase.SaveClassUseCase
import com.rtiqa.core.domain.usecase.SaveCourseUseCase
import com.rtiqa.core.domain.usecase.SaveEnterpriseMemberUseCase
import com.rtiqa.core.domain.usecase.SaveGradeLevelUseCase
import com.rtiqa.core.domain.usecase.SaveSchoolUseCase
import com.rtiqa.core.domain.usecase.SaveSectionUseCase
import com.rtiqa.core.domain.usecase.SaveSubjectUseCase

class SchoolViewModelFactory(
    private val getSchoolsUseCase: GetSchoolsUseCase,
    private val saveSchoolUseCase: SaveSchoolUseCase,
    private val deleteSchoolUseCase: DeleteSchoolUseCase,
    private val getStudentsForSchoolUseCase: GetStudentsForSchoolUseCase,
    private val getTeachersForSchoolUseCase: GetTeachersForSchoolUseCase,
    private val getSectionsForSchoolUseCase: GetSectionsForSchoolUseCase,
    private val getSubjectsForSchoolUseCase: GetSubjectsForSchoolUseCase,
    private val getCoursesForSchoolUseCase: GetCoursesForSchoolUseCase,
    private val getAssessmentsForSchoolUseCase: GetAssessmentsForSchoolUseCase?,
    private val saveEnterpriseMemberUseCase: SaveEnterpriseMemberUseCase?,
    private val saveCourseUseCase: SaveCourseUseCase?,
    private val saveAssessmentUseCase: SaveAssessmentUseCase?,
    private val getGradeLevelsForSchoolUseCase: GetGradeLevelsForSchoolUseCase? = null,
    private val saveGradeLevelUseCase: SaveGradeLevelUseCase? = null,
    private val deleteGradeLevelUseCase: DeleteGradeLevelUseCase? = null,
    private val getClassesForSchoolUseCase: GetClassesForSchoolUseCase? = null,
    private val saveClassUseCase: SaveClassUseCase? = null,
    private val deleteClassUseCase: DeleteClassUseCase? = null,
    private val getAcademicYearsUseCase: GetAcademicYearsUseCase? = null,
    private val saveAcademicYearUseCase: SaveAcademicYearUseCase? = null,
    private val deleteAcademicYearUseCase: DeleteAcademicYearUseCase? = null,
    private val saveSectionUseCase: SaveSectionUseCase? = null,
    private val deleteSectionUseCase: DeleteSectionUseCase? = null,
    private val saveSubjectUseCase: SaveSubjectUseCase? = null,
    private val deleteSubjectUseCase: DeleteSubjectUseCase? = null,
    private val preferencesDataStore: RtiqaPreferencesDataStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SchoolViewModel(
            getSchoolsUseCase = getSchoolsUseCase,
            saveSchoolUseCase = saveSchoolUseCase,
            deleteSchoolUseCase = deleteSchoolUseCase,
            getStudentsForSchoolUseCase = getStudentsForSchoolUseCase,
            getTeachersForSchoolUseCase = getTeachersForSchoolUseCase,
            getSectionsForSchoolUseCase = getSectionsForSchoolUseCase,
            getSubjectsForSchoolUseCase = getSubjectsForSchoolUseCase,
            getCoursesForSchoolUseCase = getCoursesForSchoolUseCase,
            getAssessmentsForSchoolUseCase = getAssessmentsForSchoolUseCase,
            saveEnterpriseMemberUseCase = saveEnterpriseMemberUseCase,
            saveCourseUseCase = saveCourseUseCase,
            saveAssessmentUseCase = saveAssessmentUseCase,
            getGradeLevelsForSchoolUseCase = getGradeLevelsForSchoolUseCase,
            saveGradeLevelUseCase = saveGradeLevelUseCase,
            deleteGradeLevelUseCase = deleteGradeLevelUseCase,
            getClassesForSchoolUseCase = getClassesForSchoolUseCase,
            saveClassUseCase = saveClassUseCase,
            deleteClassUseCase = deleteClassUseCase,
            getAcademicYearsUseCase = getAcademicYearsUseCase,
            saveAcademicYearUseCase = saveAcademicYearUseCase,
            deleteAcademicYearUseCase = deleteAcademicYearUseCase,
            saveSectionUseCase = saveSectionUseCase,
            deleteSectionUseCase = deleteSectionUseCase,
            saveSubjectUseCase = saveSubjectUseCase,
            deleteSubjectUseCase = deleteSubjectUseCase,
            preferencesDataStore = preferencesDataStore
        ) as T
    }
}
