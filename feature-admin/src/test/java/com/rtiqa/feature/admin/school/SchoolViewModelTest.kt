package com.rtiqa.feature.admin.school

import android.content.Context
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.data.datastore.UserPreferences
import com.rtiqa.core.domain.model.AcademicYear
import com.rtiqa.core.domain.model.Branch
import com.rtiqa.core.domain.model.Department
import com.rtiqa.core.domain.model.EducationStage
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.GradeLevel
import com.rtiqa.core.domain.model.Major
import com.rtiqa.core.domain.model.Organization
import com.rtiqa.core.domain.model.School
import com.rtiqa.core.domain.model.SchoolClass
import com.rtiqa.core.domain.model.Section
import com.rtiqa.core.domain.model.Semester
import com.rtiqa.core.domain.model.StudentEnrollment
import com.rtiqa.core.domain.model.StudyPlan
import com.rtiqa.core.domain.model.Subject
import com.rtiqa.core.domain.model.TeacherAssignment
import com.rtiqa.core.domain.repository.ClassRepository
import com.rtiqa.core.domain.repository.EnterpriseRepository
import com.rtiqa.core.domain.repository.SchoolManagementCoreRepository
import com.rtiqa.core.domain.usecase.DeleteAcademicYearUseCase
import com.rtiqa.core.domain.usecase.DeleteClassUseCase
import com.rtiqa.core.domain.usecase.DeleteGradeLevelUseCase
import com.rtiqa.core.domain.usecase.DeleteSchoolUseCase
import com.rtiqa.core.domain.usecase.DeleteSectionUseCase
import com.rtiqa.core.domain.usecase.DeleteSubjectUseCase
import com.rtiqa.core.domain.usecase.GetAcademicYearsUseCase
import com.rtiqa.core.domain.usecase.GetClassesForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetCoursesForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetGradeLevelsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetSchoolsUseCase
import com.rtiqa.core.domain.usecase.GetSectionsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetStudentsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetSubjectsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetTeachersForSchoolUseCase
import com.rtiqa.core.domain.usecase.SaveAcademicYearUseCase
import com.rtiqa.core.domain.usecase.SaveClassUseCase
import com.rtiqa.core.domain.usecase.SaveEnterpriseMemberUseCase
import com.rtiqa.core.domain.usecase.SaveGradeLevelUseCase
import com.rtiqa.core.domain.usecase.SaveSchoolUseCase
import com.rtiqa.core.domain.usecase.SaveSectionUseCase
import com.rtiqa.core.domain.usecase.SaveSubjectUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SchoolViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeSchools = mutableListOf(
        School(id = "school_001", name = "المدرسة النموذجية الأولية", code = "SCH-001", address = "الرياض - حي الملز", phone = "0112345678", studentsCount = 450, teachersCount = 32),
        School(id = "school_002", name = "مدرسة التميز الثانوية", code = "SCH-002", address = "جدة - حي الشاطئ", phone = "0126543210", studentsCount = 380, teachersCount = 28)
    )

    private val fakeAcademicYears = mutableListOf(
        AcademicYear("ay_1", "org_1", "2024-2025", "2024-09-01", "2025-06-30", true)
    )

    private val fakeGradeLevels = mutableListOf(
        GradeLevel("gl_1", "school_001", "الصف الأول الابتدائي", "PRI-1", 1, EducationStage.PRIMARY)
    )

    private val fakeClasses = mutableListOf(
        SchoolClass("cls_1", "school_001", "101 ثانٍ", "الصف الأول الابتدائي", "A-1", 30, 1)
    )

    private val fakeSections = mutableListOf(
        Section("sec_1", "school_001", "sem_1", "branch_1", "شعبة أ", 25, 20, "school_001")
    )

    private val fakeSubjects = mutableListOf(
        Subject(id = "sub_1", majorId = "school_001", code = "MATH-101", name = "الرياضيات", creditHours = 3, schoolId = "school_001")
    )

    private val activeSchoolIdFlow = MutableStateFlow("school_001")

    private val fakeSchoolManagementRepo = object : SchoolManagementCoreRepository {
        override fun getGradeLevelsForSchool(schoolId: String): Flow<List<GradeLevel>> = flowOf(fakeGradeLevels.filter { it.schoolId == schoolId })
        override fun getGradeLevelById(id: String): Flow<GradeLevel?> = flowOf(fakeGradeLevels.find { it.id == id })
        override suspend fun saveGradeLevel(gradeLevel: GradeLevel) {
            fakeGradeLevels.removeAll { it.id == gradeLevel.id }
            fakeGradeLevels.add(gradeLevel)
        }
        override suspend fun deleteGradeLevel(id: String) {
            fakeGradeLevels.removeAll { it.id == id }
        }
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

    private val fakeClassRepo = object : ClassRepository {
        override fun getClassesForSchool(schoolId: String): Flow<List<SchoolClass>> = flowOf(fakeClasses.filter { it.schoolId == schoolId })
        override suspend fun getClassById(id: String): SchoolClass? = fakeClasses.find { it.id == id }
        override suspend fun isClassNameUniqueInSchool(schoolId: String, name: String, excludeId: String): Boolean = true
        override suspend fun saveClass(schoolClass: SchoolClass) {
            fakeClasses.removeAll { it.id == schoolClass.id }
            fakeClasses.add(schoolClass)
        }
        override suspend fun deleteClass(id: String) {
            fakeClasses.removeAll { it.id == id }
        }
        override suspend fun updateClassOrder(classes: List<SchoolClass>) {}
    }

    private val fakeEnterpriseRepo = object : EnterpriseRepository {
        override fun getSchools(): Flow<List<School>> = flowOf(fakeSchools)
        override fun getSchoolById(id: String): Flow<School?> = flowOf(fakeSchools.find { it.id == id })
        override suspend fun saveSchool(school: School) {
            fakeSchools.removeAll { it.id == school.id }
            fakeSchools.add(school)
        }
        override suspend fun deleteSchool(id: String) {
            fakeSchools.removeAll { it.id == id }
        }
        override fun getStudentsForSchool(schoolId: String): Flow<List<EnterpriseMember>> = flowOf(emptyList())
        override fun getTeachersForSchool(schoolId: String): Flow<List<EnterpriseMember>> = flowOf(emptyList())
        override fun getUsersForSchool(schoolId: String): Flow<List<EnterpriseMember>> = flowOf(emptyList())
        override fun getSectionsForSchool(schoolId: String): Flow<List<Section>> = flowOf(fakeSections.filter { it.schoolId == schoolId })
        override fun getSubjectsForSchool(schoolId: String): Flow<List<Subject>> = flowOf(fakeSubjects.filter { it.schoolId == schoolId })
        override fun getOrganizations(): Flow<List<Organization>> = flowOf(emptyList())
        override fun getOrganizationById(id: String): Flow<Organization?> = flowOf(null)
        override suspend fun saveOrganization(org: Organization) {}
        override suspend fun deleteOrganization(id: String) {}
        override fun getBranches(orgId: String): Flow<List<Branch>> = flowOf(emptyList())
        override suspend fun saveBranch(branch: Branch) {}
        override suspend fun deleteBranch(id: String) {}
        override fun getAcademicYears(orgId: String): Flow<List<AcademicYear>> = flowOf(fakeAcademicYears)
        override suspend fun saveAcademicYear(academicYear: AcademicYear) {
            fakeAcademicYears.removeAll { it.id == academicYear.id }
            fakeAcademicYears.add(academicYear)
        }
        override suspend fun deleteAcademicYear(id: String) {
            fakeAcademicYears.removeAll { it.id == id }
        }
        override fun getSemesters(academicYearId: String): Flow<List<Semester>> = flowOf(emptyList())
        override suspend fun saveSemester(semester: Semester) {}
        override fun getDepartments(orgId: String): Flow<List<Department>> = flowOf(emptyList())
        override suspend fun saveDepartment(department: Department) {}
        override fun getMajors(departmentId: String): Flow<List<Major>> = flowOf(emptyList())
        override suspend fun saveMajor(major: Major) {}
        override fun getSections(majorId: String): Flow<List<Section>> = flowOf(fakeSections)
        override suspend fun saveSection(section: Section) {
            fakeSections.removeAll { it.id == section.id }
            fakeSections.add(section)
        }
        override suspend fun deleteSection(id: String) {
            fakeSections.removeAll { it.id == id }
        }
        override fun getSubjects(majorId: String): Flow<List<Subject>> = flowOf(fakeSubjects)
        override suspend fun saveSubject(subject: Subject) {
            fakeSubjects.removeAll { it.id == subject.id }
            fakeSubjects.add(subject)
        }
        override suspend fun deleteSubject(id: String) {
            fakeSubjects.removeAll { it.id == id }
        }
        override fun getStudyPlans(majorId: String): Flow<List<StudyPlan>> = flowOf(emptyList())
        override suspend fun saveStudyPlan(studyPlan: StudyPlan) {}
        override fun getMembers(orgId: String): Flow<List<EnterpriseMember>> = flowOf(emptyList())
        override suspend fun saveMember(member: EnterpriseMember) {}
        override suspend fun deleteMember(id: String) {}
    }

    private lateinit var preferencesDataStore: RtiqaPreferencesDataStore
    private lateinit var viewModel: SchoolViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context: Context = RuntimeEnvironment.getApplication()
        preferencesDataStore = object : RtiqaPreferencesDataStore(context) {
            override val userPreferencesFlow: Flow<UserPreferences> = flowOf(
                UserPreferences(
                    isDarkTheme = false,
                    isOfflineModeEnabled = false,
                    activeUserId = null,
                    lastSyncTimestamp = 0L,
                    activeSchoolId = "school_001"
                )
            )
            override suspend fun setActiveSchoolId(schoolId: String) {}
        }

        viewModel = SchoolViewModel(
            getSchoolsUseCase = GetSchoolsUseCase(fakeEnterpriseRepo),
            saveSchoolUseCase = SaveSchoolUseCase(fakeEnterpriseRepo),
            deleteSchoolUseCase = DeleteSchoolUseCase(fakeEnterpriseRepo),
            getStudentsForSchoolUseCase = GetStudentsForSchoolUseCase(fakeEnterpriseRepo),
            getTeachersForSchoolUseCase = GetTeachersForSchoolUseCase(fakeEnterpriseRepo),
            getSectionsForSchoolUseCase = GetSectionsForSchoolUseCase(fakeEnterpriseRepo),
            getSubjectsForSchoolUseCase = GetSubjectsForSchoolUseCase(fakeEnterpriseRepo),
            getCoursesForSchoolUseCase = GetCoursesForSchoolUseCase(object : com.rtiqa.core.domain.repository.CourseRepositoryContract {
                override fun getCourses(): Flow<List<com.rtiqa.core.domain.model.Course>> = flowOf(emptyList())
                override fun getCoursesForSchool(schoolId: String): Flow<List<com.rtiqa.core.domain.model.Course>> = flowOf(emptyList())
                override fun getCourseById(courseId: String): Flow<com.rtiqa.core.domain.model.Course?> = flowOf(null)
                override fun getLessonsForCourse(courseId: String): Flow<List<com.rtiqa.core.domain.model.Lesson>> = flowOf(emptyList())
                override fun getLessonById(lessonId: String): Flow<com.rtiqa.core.domain.model.Lesson?> = flowOf(null)
                override fun getNextLesson(courseId: String, currentLessonId: String): Flow<com.rtiqa.core.domain.model.Lesson?> = flowOf(null)
                override fun getPagedCourses(request: com.rtiqa.core.domain.model.PageRequest): Flow<com.rtiqa.core.domain.model.PagedData<com.rtiqa.core.domain.model.Course>> = flowOf(com.rtiqa.core.domain.model.PagedData(emptyList(), 0, 1, 1, false))
                override suspend fun searchCourses(query: String): List<com.rtiqa.core.domain.model.Course> = emptyList()
                override suspend fun markLessonCompleted(lessonId: String, courseId: String): com.rtiqa.core.domain.result.RtiqaResult<Unit> = com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
                override suspend fun updateLessonProgress(lessonId: String, courseId: String, progressPercent: Float): com.rtiqa.core.domain.result.RtiqaResult<Unit> = com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
                override suspend fun saveCourse(course: com.rtiqa.core.domain.model.Course): com.rtiqa.core.domain.result.RtiqaResult<Unit> = com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
                override suspend fun deleteCourse(courseId: String): com.rtiqa.core.domain.result.RtiqaResult<Unit> = com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
                override suspend fun saveLesson(lesson: com.rtiqa.core.domain.model.Lesson): com.rtiqa.core.domain.result.RtiqaResult<Unit> = com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
                override suspend fun enrollInCourse(courseId: String): com.rtiqa.core.domain.result.RtiqaResult<Unit> = com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
                override suspend fun toggleBookmark(courseId: String, isBookmarked: Boolean): com.rtiqa.core.domain.result.RtiqaResult<Unit> = com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
                override suspend fun toggleCourseDownload(courseId: String, isDownloaded: Boolean): com.rtiqa.core.domain.result.RtiqaResult<Unit> = com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
                override suspend fun syncCourses(): com.rtiqa.core.domain.result.RtiqaResult<Unit> = com.rtiqa.core.domain.result.RtiqaResult.Success(Unit)
            }),
            getAssessmentsForSchoolUseCase = null,
            saveEnterpriseMemberUseCase = SaveEnterpriseMemberUseCase(fakeEnterpriseRepo),
            saveCourseUseCase = null,
            saveAssessmentUseCase = null,
            getGradeLevelsForSchoolUseCase = GetGradeLevelsForSchoolUseCase(fakeSchoolManagementRepo),
            saveGradeLevelUseCase = SaveGradeLevelUseCase(fakeSchoolManagementRepo),
            deleteGradeLevelUseCase = DeleteGradeLevelUseCase(fakeSchoolManagementRepo),
            getClassesForSchoolUseCase = GetClassesForSchoolUseCase(fakeClassRepo),
            saveClassUseCase = SaveClassUseCase(fakeClassRepo),
            deleteClassUseCase = DeleteClassUseCase(fakeClassRepo),
            getAcademicYearsUseCase = GetAcademicYearsUseCase(fakeEnterpriseRepo),
            saveAcademicYearUseCase = SaveAcademicYearUseCase(fakeEnterpriseRepo),
            deleteAcademicYearUseCase = DeleteAcademicYearUseCase(fakeEnterpriseRepo),
            saveSectionUseCase = SaveSectionUseCase(fakeEnterpriseRepo),
            deleteSectionUseCase = DeleteSectionUseCase(fakeEnterpriseRepo),
            saveSubjectUseCase = SaveSubjectUseCase(fakeEnterpriseRepo),
            deleteSubjectUseCase = DeleteSubjectUseCase(fakeEnterpriseRepo),
            preferencesDataStore = preferencesDataStore
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun searchQueryFiltersSchoolsCorrectly() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(SchoolUiAction.UpdateSearchQuery("التميز"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("التميز", state.searchQuery)
        assertEquals(1, state.filteredSchools.size)
        assertEquals("school_002", state.filteredSchools.first().id)
    }

    @Test
    fun switchingTabUpdatesState() = runTest {
        viewModel.onAction(SchoolUiAction.SelectTab(2))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.selectedTab)
    }

    @Test
    fun savingNewAcademicYearWorks() = runTest {
        viewModel.onAction(
            SchoolUiAction.SaveAcademicYear(
                id = null,
                name = "2025-2026",
                startDate = "2025-09-01",
                endDate = "2026-06-30",
                isCurrent = false
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(fakeAcademicYears.find { it.name == "2025-2026" })
    }

    @Test
    fun savingNewSubjectWorks() = runTest {
        viewModel.onAction(
            SchoolUiAction.SaveSubject(
                id = null,
                name = "الفيزياء",
                code = "PHYS-101",
                creditHours = 4
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(fakeSubjects.find { it.name == "الفيزياء" })
    }
}
