package com.rtiqa.feature.admin.school

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.domain.usecase.DeleteSchoolUseCase
import com.rtiqa.core.domain.usecase.GetAssessmentsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetCoursesForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetSchoolsUseCase
import com.rtiqa.core.domain.usecase.GetSectionsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetStudentsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetSubjectsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetTeachersForSchoolUseCase
import com.rtiqa.core.domain.usecase.SaveAssessmentUseCase
import com.rtiqa.core.domain.usecase.SaveCourseUseCase
import com.rtiqa.core.domain.usecase.SaveEnterpriseMemberUseCase
import com.rtiqa.core.domain.usecase.SaveSchoolUseCase

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
            preferencesDataStore = preferencesDataStore
        ) as T
    }
}
