package com.rtiqa.feature.admin.academic

import com.rtiqa.core.domain.model.*
import com.rtiqa.core.domain.repository.EnterpriseRepository
import com.rtiqa.core.domain.repository.SchoolManagementCoreRepository
import com.rtiqa.core.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AcademicStructureViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeEnterpriseRepo = object : EnterpriseRepository {
        override fun getSchools(): Flow<List<School>> = flowOf(emptyList())
        override fun getSchoolById(id: String): Flow<School?> = flowOf(null)
        override suspend fun saveSchool(school: School) {}
        override suspend fun deleteSchool(id: String) {}
        override fun getStudentsForSchool(schoolId: String): Flow<List<EnterpriseMember>> = flowOf(emptyList())
        override fun getTeachersForSchool(schoolId: String): Flow<List<EnterpriseMember>> = flowOf(emptyList())
        override fun getUsersForSchool(schoolId: String): Flow<List<EnterpriseMember>> = flowOf(emptyList())
        override fun getSectionsForSchool(schoolId: String): Flow<List<Section>> = flowOf(emptyList())
        override fun getSubjectsForSchool(schoolId: String): Flow<List<Subject>> = flowOf(emptyList())
        override fun getOrganizations(): Flow<List<Organization>> = flowOf(emptyList())
        override fun getOrganizationById(id: String): Flow<Organization?> = flowOf(null)
        override suspend fun saveOrganization(org: Organization) {}
        override suspend fun deleteOrganization(id: String) {}
        override fun getBranches(orgId: String): Flow<List<Branch>> = flowOf(emptyList())
        override suspend fun saveBranch(branch: Branch) {}
        override suspend fun deleteBranch(id: String) {}
        override fun getAcademicYears(orgId: String): Flow<List<AcademicYear>> = flowOf(emptyList())
        override suspend fun saveAcademicYear(academicYear: AcademicYear) {}
        override suspend fun deleteAcademicYear(id: String) {}
        override fun getSemesters(academicYearId: String): Flow<List<Semester>> = flowOf(emptyList())
        override suspend fun saveSemester(semester: Semester) {}
        override suspend fun deleteSemester(id: String) {}
        override fun getDepartments(orgId: String): Flow<List<Department>> = flowOf(emptyList())
        override suspend fun saveDepartment(department: Department) {}
        override suspend fun deleteDepartment(id: String) {}
        override fun getMajors(departmentId: String): Flow<List<Major>> = flowOf(emptyList())
        override suspend fun saveMajor(major: Major) {}
        override suspend fun deleteMajor(id: String) {}
        override fun getStudyPlans(majorId: String): Flow<List<StudyPlan>> = flowOf(emptyList())
        override suspend fun saveStudyPlan(studyPlan: StudyPlan) {}
        override suspend fun deleteStudyPlan(id: String) {}
        override fun getSections(majorId: String): Flow<List<Section>> = flowOf(emptyList())
        override fun getSubjects(majorId: String): Flow<List<Subject>> = flowOf(emptyList())
        override fun getMembers(orgId: String): Flow<List<EnterpriseMember>> = flowOf(emptyList())
        override suspend fun saveMember(member: EnterpriseMember) {}
        override suspend fun deleteMember(id: String) {}
        override suspend fun saveSection(section: Section) {}
        override suspend fun deleteSection(id: String) {}
        override suspend fun saveSubject(subject: Subject) {}
        override suspend fun deleteSubject(id: String) {}
    }

    private val fakeSchoolManagementRepo = object : SchoolManagementCoreRepository {
        override fun getGradeLevelsForSchool(schoolId: String): Flow<List<GradeLevel>> = flowOf(emptyList())
        override fun getGradeLevelById(id: String): Flow<GradeLevel?> = flowOf(null)
        override suspend fun saveGradeLevel(gradeLevel: GradeLevel) {}
        override suspend fun deleteGradeLevel(id: String) {}
        override fun getTeacherAssignmentsForSchool(schoolId: String): Flow<List<TeacherAssignment>> = flowOf(emptyList())
        override fun getAssignmentsForTeacher(teacherId: String): Flow<List<TeacherAssignment>> = flowOf(emptyList())
        override fun getAssignmentsForSection(sectionId: String): Flow<List<TeacherAssignment>> = flowOf(emptyList())
        override suspend fun saveTeacherAssignment(assignment: TeacherAssignment) {}
        override suspend fun deleteTeacherAssignment(id: String) {}
        override fun getEnrollmentsForSchool(schoolId: String): Flow<List<StudentEnrollment>> = flowOf(emptyList())
        override fun getEnrollmentsForClass(classId: String): Flow<List<StudentEnrollment>> = flowOf(emptyList())
        override fun getEnrollmentsForSection(sectionId: String): Flow<List<StudentEnrollment>> = flowOf(emptyList())
        override fun getEnrollmentsForStudent(studentId: String): Flow<List<StudentEnrollment>> = flowOf(emptyList())
        override suspend fun saveStudentEnrollment(enrollment: StudentEnrollment) {}
        override suspend fun deleteStudentEnrollment(id: String) {}
    }

    private fun createViewModel() = AcademicStructureViewModel(
        getAcademicYearsUseCase = GetAcademicYearsUseCase(fakeEnterpriseRepo),
        saveAcademicYearUseCase = SaveAcademicYearUseCase(fakeEnterpriseRepo),
        deleteAcademicYearUseCase = DeleteAcademicYearUseCase(fakeEnterpriseRepo),
        getSemestersUseCase = GetSemestersUseCase(fakeEnterpriseRepo),
        saveSemesterUseCase = SaveSemesterUseCase(fakeEnterpriseRepo),
        deleteSemesterUseCase = DeleteSemesterUseCase(fakeEnterpriseRepo),
        getGradeLevelsForSchoolUseCase = GetGradeLevelsForSchoolUseCase(fakeSchoolManagementRepo),
        saveGradeLevelUseCase = SaveGradeLevelUseCase(fakeSchoolManagementRepo),
        deleteGradeLevelUseCase = DeleteGradeLevelUseCase(fakeSchoolManagementRepo),
        getDepartmentsUseCase = GetDepartmentsUseCase(fakeEnterpriseRepo),
        saveDepartmentUseCase = SaveDepartmentUseCase(fakeEnterpriseRepo),
        deleteDepartmentUseCase = DeleteDepartmentUseCase(fakeEnterpriseRepo),
        getMajorsUseCase = GetMajorsUseCase(fakeEnterpriseRepo),
        saveMajorUseCase = SaveMajorUseCase(fakeEnterpriseRepo),
        deleteMajorUseCase = DeleteMajorUseCase(fakeEnterpriseRepo),
        getSubjectsForSchoolUseCase = GetSubjectsForSchoolUseCase(fakeEnterpriseRepo),
        saveSubjectUseCase = SaveSubjectUseCase(fakeEnterpriseRepo),
        deleteSubjectUseCase = DeleteSubjectUseCase(fakeEnterpriseRepo),
        getSectionsForSchoolUseCase = GetSectionsForSchoolUseCase(fakeEnterpriseRepo),
        saveSectionUseCase = SaveSectionUseCase(fakeEnterpriseRepo),
        deleteSectionUseCase = DeleteSectionUseCase(fakeEnterpriseRepo),
        getStudyPlansUseCase = GetStudyPlansUseCase(fakeEnterpriseRepo),
        saveStudyPlanUseCase = SaveStudyPlanUseCase(fakeEnterpriseRepo),
        deleteStudyPlanUseCase = DeleteStudyPlanUseCase(fakeEnterpriseRepo)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun defaultState_hasNoFabricatedOrganizationId() {
        val state = AcademicStructureUiState()
        assertNull(state.currentOrgId)
        assertNotEquals("org_001", state.currentOrgId)
    }

    @Test
    fun defaultState_hasNoFabricatedSchoolId() {
        val state = AcademicStructureUiState()
        assertNull(state.currentSchoolId)
        assertNotEquals("school_001", state.currentSchoolId)
    }

    @Test
    fun explicitRealOrganizationId_isPreserved() {
        val state = AcademicStructureUiState(currentOrgId = "org_real_555")
        assertEquals("org_real_555", state.currentOrgId)
    }

    @Test
    fun explicitRealSchoolId_isPreserved() {
        val state = AcademicStructureUiState(currentSchoolId = "sch_real_666")
        assertEquals("sch_real_666", state.currentSchoolId)
    }

    @Test
    fun viewModelInit_startsWithoutFabricatedIdentity() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.currentOrgId)
        assertNull(state.currentSchoolId)
        assertNotEquals("org_001", state.currentOrgId)
        assertNotEquals("school_001", state.currentSchoolId)
    }

    @Test
    fun setOrganization_and_setSchool_updatesWithRealIds() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onAction(AcademicStructureAction.SetOrganization("org_ministry_101"))
        vm.onAction(AcademicStructureAction.SetSchool("sch_riyadh_202"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("org_ministry_101", state.currentOrgId)
        assertEquals("sch_riyadh_202", state.currentSchoolId)
    }
}
