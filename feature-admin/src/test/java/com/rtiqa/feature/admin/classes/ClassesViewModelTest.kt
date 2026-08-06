package com.rtiqa.feature.admin.classes

import android.content.Context
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.data.datastore.UserPreferences
import com.rtiqa.core.domain.model.School
import com.rtiqa.core.domain.model.SchoolClass
import com.rtiqa.core.domain.repository.ClassRepository
import com.rtiqa.core.domain.repository.EnterpriseRepository
import com.rtiqa.core.domain.usecase.DeleteClassUseCase
import com.rtiqa.core.domain.usecase.GetClassesForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetSchoolsUseCase
import com.rtiqa.core.domain.usecase.ReorderClassesUseCase
import com.rtiqa.core.domain.usecase.SaveClassUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ClassesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeSchools = listOf(
        School(id = "school_001", name = "المدرسة النموذجية", code = "SCH-001", address = "الرياض", phone = "0112345678", studentsCount = 450, teachersCount = 32)
    )

    private val fakeClasses = mutableListOf(
        SchoolClass(id = "cls_1", schoolId = "school_001", name = "الصف الأول الابتدائي - أ", gradeLevel = "الابتدائي", sectionName = "أ", capacity = 30, studentsCount = 25, roomNumber = "101", displayOrder = 1),
        SchoolClass(id = "cls_2", schoolId = "school_001", name = "الصف الثاني الابتدائي - ب", gradeLevel = "الابتدائي", sectionName = "ب", capacity = 28, studentsCount = 22, roomNumber = "102", displayOrder = 2)
    )

    private val fakeClassRepo = object : ClassRepository {
        override fun getClassesForSchool(schoolId: String): Flow<List<SchoolClass>> = flowOf(fakeClasses)
        override suspend fun getClassById(id: String): SchoolClass? = fakeClasses.find { it.id == id }
        override suspend fun isClassNameUniqueInSchool(schoolId: String, name: String, excludeId: String): Boolean = true
        override suspend fun saveClass(schoolClass: SchoolClass) {
            fakeClasses.removeAll { it.id == schoolClass.id }
            fakeClasses.add(schoolClass)
        }
        override suspend fun deleteClass(id: String) {
            fakeClasses.removeAll { it.id == id }
        }
        override suspend fun updateClassOrder(classes: List<SchoolClass>) {
            fakeClasses.clear()
            fakeClasses.addAll(classes)
        }
    }

    private val fakeEnterpriseRepo = object : EnterpriseRepository {
        override fun getSchools(): Flow<List<School>> = flowOf(fakeSchools)
        override fun getSchoolById(id: String): Flow<School?> = flowOf(fakeSchools.find { it.id == id })
        override suspend fun saveSchool(school: School) {}
        override suspend fun deleteSchool(id: String) {}
        override fun getStudentsForSchool(schoolId: String): Flow<List<com.rtiqa.core.domain.model.EnterpriseMember>> = flowOf(emptyList())
        override fun getTeachersForSchool(schoolId: String): Flow<List<com.rtiqa.core.domain.model.EnterpriseMember>> = flowOf(emptyList())
        override fun getUsersForSchool(schoolId: String): Flow<List<com.rtiqa.core.domain.model.EnterpriseMember>> = flowOf(emptyList())
        override fun getSectionsForSchool(schoolId: String): Flow<List<com.rtiqa.core.domain.model.Section>> = flowOf(emptyList())
        override fun getSubjectsForSchool(schoolId: String): Flow<List<com.rtiqa.core.domain.model.Subject>> = flowOf(emptyList())
        override fun getOrganizations(): Flow<List<com.rtiqa.core.domain.model.Organization>> = flowOf(emptyList())
        override fun getOrganizationById(id: String): Flow<com.rtiqa.core.domain.model.Organization?> = flowOf(null)
        override suspend fun saveOrganization(org: com.rtiqa.core.domain.model.Organization) {}
        override suspend fun deleteOrganization(id: String) {}
        override fun getBranches(orgId: String): Flow<List<com.rtiqa.core.domain.model.Branch>> = flowOf(emptyList())
        override suspend fun saveBranch(branch: com.rtiqa.core.domain.model.Branch) {}
        override suspend fun deleteBranch(id: String) {}
        override fun getAcademicYears(orgId: String): Flow<List<com.rtiqa.core.domain.model.AcademicYear>> = flowOf(emptyList())
        override suspend fun saveAcademicYear(academicYear: com.rtiqa.core.domain.model.AcademicYear) {}
        override suspend fun deleteAcademicYear(id: String) {}
        override fun getSemesters(academicYearId: String): Flow<List<com.rtiqa.core.domain.model.Semester>> = flowOf(emptyList())
        override suspend fun saveSemester(semester: com.rtiqa.core.domain.model.Semester) {}
        override fun getDepartments(orgId: String): Flow<List<com.rtiqa.core.domain.model.Department>> = flowOf(emptyList())
        override suspend fun saveDepartment(department: com.rtiqa.core.domain.model.Department) {}
        override fun getMajors(departmentId: String): Flow<List<com.rtiqa.core.domain.model.Major>> = flowOf(emptyList())
        override suspend fun saveMajor(major: com.rtiqa.core.domain.model.Major) {}
        override fun getSections(majorId: String): Flow<List<com.rtiqa.core.domain.model.Section>> = flowOf(emptyList())
        override suspend fun saveSection(section: com.rtiqa.core.domain.model.Section) {}
        override suspend fun deleteSection(id: String) {}
        override fun getSubjects(majorId: String): Flow<List<com.rtiqa.core.domain.model.Subject>> = flowOf(emptyList())
        override suspend fun saveSubject(subject: com.rtiqa.core.domain.model.Subject) {}
        override suspend fun deleteSubject(id: String) {}
        override fun getStudyPlans(majorId: String): Flow<List<com.rtiqa.core.domain.model.StudyPlan>> = flowOf(emptyList())
        override suspend fun saveStudyPlan(studyPlan: com.rtiqa.core.domain.model.StudyPlan) {}
        override fun getMembers(orgId: String): Flow<List<com.rtiqa.core.domain.model.EnterpriseMember>> = flowOf(emptyList())
        override suspend fun saveMember(member: com.rtiqa.core.domain.model.EnterpriseMember) {}
        override suspend fun deleteMember(id: String) {}
    }

    private lateinit var viewModel: ClassesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context: Context = RuntimeEnvironment.getApplication()
        val preferencesDataStore = object : RtiqaPreferencesDataStore(context) {
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

        viewModel = ClassesViewModel(
            getClassesForSchoolUseCase = GetClassesForSchoolUseCase(fakeClassRepo),
            saveClassUseCase = SaveClassUseCase(fakeClassRepo),
            deleteClassUseCase = DeleteClassUseCase(fakeClassRepo),
            reorderClassesUseCase = ReorderClassesUseCase(fakeClassRepo),
            getSchoolsUseCase = GetSchoolsUseCase(fakeEnterpriseRepo),
            preferencesDataStore = preferencesDataStore
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun searchClassesFiltersCorrectly() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ClassesUiAction.SearchClasses("الأول"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("الأول", state.searchQuery)
        assertEquals(1, state.filteredClasses.size)
        assertEquals("cls_1", state.filteredClasses.first().id)
    }

    @Test
    fun saveClassAddsNewClass() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(
            ClassesUiAction.SaveClass(
                id = null,
                name = "الصف الثالث الابتدائي - ج",
                gradeLevel = "الابتدائي",
                sectionName = "ج",
                capacity = 30,
                roomNumber = "103"
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(fakeClasses.find { it.name == "الصف الثالث الابتدائي - ج" })
    }

    @Test
    fun deleteClassRemovesClass() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ClassesUiAction.DeleteClass("cls_1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(null, fakeClasses.find { it.id == "cls_1" })
    }
}
