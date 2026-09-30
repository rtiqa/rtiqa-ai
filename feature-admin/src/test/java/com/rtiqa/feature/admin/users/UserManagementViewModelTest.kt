package com.rtiqa.feature.admin.users

import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.data.datastore.UserPreferences
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.EnterpriseRole
import com.rtiqa.core.domain.model.MemberStatus
import com.rtiqa.core.domain.model.School
import com.rtiqa.core.domain.repository.EnterpriseRepository
import com.rtiqa.core.domain.usecase.DeleteEnterpriseMemberUseCase
import com.rtiqa.core.domain.usecase.GetSchoolsUseCase
import com.rtiqa.core.domain.usecase.GetUsersForSchoolUseCase
import com.rtiqa.core.domain.usecase.SaveEnterpriseMemberUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class UserManagementViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeSchools = listOf(
        School(id = "sch_1", orgId = "org_1", name = "المدرسة الأولى", code = "SCH-01"),
        School(id = "sch_2", orgId = "org_1", name = "المدرسة الثانية", code = "SCH-02")
    )

    private val savedMembers = mutableListOf<EnterpriseMember>()

    private val fakeEnterpriseRepo = object : EnterpriseRepository {
        override fun getSchools(): Flow<List<School>> = flowOf(fakeSchools)
        override fun getSchoolById(id: String): Flow<School?> = flowOf(fakeSchools.find { it.id == id })
        override suspend fun saveSchool(school: School) {}
        override suspend fun deleteSchool(id: String) {}
        override fun getStudentsForSchool(schoolId: String): Flow<List<EnterpriseMember>> = flowOf(emptyList())
        override fun getTeachersForSchool(schoolId: String): Flow<List<EnterpriseMember>> = flowOf(emptyList())
        override fun getUsersForSchool(schoolId: String): Flow<List<EnterpriseMember>> = flowOf(
            savedMembers.filter { it.schoolId == schoolId }
        )
        override fun getSectionsForSchool(schoolId: String) = flowOf(emptyList<com.rtiqa.core.domain.model.Section>())
        override fun getSubjectsForSchool(schoolId: String) = flowOf(emptyList<com.rtiqa.core.domain.model.Subject>())
        override fun getOrganizations() = flowOf(emptyList<com.rtiqa.core.domain.model.Organization>())
        override fun getOrganizationById(id: String) = flowOf<com.rtiqa.core.domain.model.Organization?>(null)
        override suspend fun saveOrganization(org: com.rtiqa.core.domain.model.Organization) {}
        override suspend fun deleteOrganization(id: String) {}
        override fun getBranches(orgId: String) = flowOf(emptyList<com.rtiqa.core.domain.model.Branch>())
        override suspend fun saveBranch(branch: com.rtiqa.core.domain.model.Branch) {}
        override suspend fun deleteBranch(id: String) {}
        override fun getAcademicYears(orgId: String) = flowOf(emptyList<com.rtiqa.core.domain.model.AcademicYear>())
        override suspend fun saveAcademicYear(academicYear: com.rtiqa.core.domain.model.AcademicYear) {}
        override suspend fun deleteAcademicYear(id: String) {}
        override fun getSemesters(academicYearId: String) = flowOf(emptyList<com.rtiqa.core.domain.model.Semester>())
        override suspend fun saveSemester(semester: com.rtiqa.core.domain.model.Semester) {}
        override fun getDepartments(orgId: String) = flowOf(emptyList<com.rtiqa.core.domain.model.Department>())
        override suspend fun saveDepartment(department: com.rtiqa.core.domain.model.Department) {}
        override fun getMajors(departmentId: String) = flowOf(emptyList<com.rtiqa.core.domain.model.Major>())
        override suspend fun saveMajor(major: com.rtiqa.core.domain.model.Major) {}
        override fun getSections(majorId: String) = flowOf(emptyList<com.rtiqa.core.domain.model.Section>())
        override suspend fun saveSection(section: com.rtiqa.core.domain.model.Section) {}
        override suspend fun deleteSection(id: String) {}
        override fun getSubjects(majorId: String) = flowOf(emptyList<com.rtiqa.core.domain.model.Subject>())
        override suspend fun saveSubject(subject: com.rtiqa.core.domain.model.Subject) {}
        override suspend fun deleteSubject(id: String) {}
        override fun getStudyPlans(majorId: String) = flowOf(emptyList<com.rtiqa.core.domain.model.StudyPlan>())
        override suspend fun saveStudyPlan(studyPlan: com.rtiqa.core.domain.model.StudyPlan) {}
        override fun getMembers(orgId: String): Flow<List<EnterpriseMember>> = flowOf(savedMembers)
        override suspend fun saveMember(member: EnterpriseMember) {
            savedMembers.removeAll { it.id == member.id }
            savedMembers.add(member)
        }
        override suspend fun deleteMember(id: String) {
            savedMembers.removeAll { it.id == id }
        }
        override suspend fun deleteSemester(id: String) {}
        override suspend fun deleteDepartment(id: String) {}
        override suspend fun deleteMajor(id: String) {}
        override suspend fun deleteStudyPlan(id: String) {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        savedMembers.clear()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun emptySchoolList_doesNotCreateFabricatedSchoolId_andRemainsNull() = runTest {
        val emptyEnterpriseRepo = object : EnterpriseRepository by fakeEnterpriseRepo {
            override fun getSchools(): Flow<List<School>> = flowOf(emptyList())
        }
        val emptyPrefs = object : RtiqaPreferencesDataStore(RuntimeEnvironment.getApplication()) {
            override val userPreferencesFlow: Flow<UserPreferences> = flowOf(
                UserPreferences(
                    isDarkTheme = false,
                    isOfflineModeEnabled = false,
                    activeUserId = null,
                    lastSyncTimestamp = 0L,
                    activeSchoolId = null
                )
            )
            override suspend fun setActiveSchoolId(schoolId: String?) {}
        }

        val vm = UserManagementViewModel(
            getUsersForSchoolUseCase = GetUsersForSchoolUseCase(emptyEnterpriseRepo),
            saveEnterpriseMemberUseCase = SaveEnterpriseMemberUseCase(emptyEnterpriseRepo),
            deleteEnterpriseMemberUseCase = DeleteEnterpriseMemberUseCase(emptyEnterpriseRepo),
            getSchoolsUseCase = GetSchoolsUseCase(emptyEnterpriseRepo),
            preferencesDataStore = emptyPrefs
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.activeSchoolId)
        assertNull(state.activeSchool)
        assertNotEquals("school_001", state.activeSchoolId)
    }

    @Test
    fun savedValidSchool_isPreserved_whenInLoadedSchools() = runTest {
        val prefs = object : RtiqaPreferencesDataStore(RuntimeEnvironment.getApplication()) {
            override val userPreferencesFlow: Flow<UserPreferences> = flowOf(
                UserPreferences(
                    isDarkTheme = false,
                    isOfflineModeEnabled = false,
                    activeUserId = null,
                    lastSyncTimestamp = 0L,
                    activeSchoolId = "sch_2"
                )
            )
            override suspend fun setActiveSchoolId(schoolId: String?) {}
        }

        val vm = UserManagementViewModel(
            getUsersForSchoolUseCase = GetUsersForSchoolUseCase(fakeEnterpriseRepo),
            saveEnterpriseMemberUseCase = SaveEnterpriseMemberUseCase(fakeEnterpriseRepo),
            deleteEnterpriseMemberUseCase = DeleteEnterpriseMemberUseCase(fakeEnterpriseRepo),
            getSchoolsUseCase = GetSchoolsUseCase(fakeEnterpriseRepo),
            preferencesDataStore = prefs
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("sch_2", state.activeSchoolId)
        assertEquals("المدرسة الثانية", state.activeSchool?.name)
    }

    @Test
    fun noSavedSchool_usesFirstRealSchool_withoutFabricatingId() = runTest {
        val prefs = object : RtiqaPreferencesDataStore(RuntimeEnvironment.getApplication()) {
            override val userPreferencesFlow: Flow<UserPreferences> = flowOf(
                UserPreferences(
                    isDarkTheme = false,
                    isOfflineModeEnabled = false,
                    activeUserId = null,
                    lastSyncTimestamp = 0L,
                    activeSchoolId = null
                )
            )
            override suspend fun setActiveSchoolId(schoolId: String?) {}
        }

        val vm = UserManagementViewModel(
            getUsersForSchoolUseCase = GetUsersForSchoolUseCase(fakeEnterpriseRepo),
            saveEnterpriseMemberUseCase = SaveEnterpriseMemberUseCase(fakeEnterpriseRepo),
            deleteEnterpriseMemberUseCase = DeleteEnterpriseMemberUseCase(fakeEnterpriseRepo),
            getSchoolsUseCase = GetSchoolsUseCase(fakeEnterpriseRepo),
            preferencesDataStore = prefs
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("sch_1", state.activeSchoolId)
        assertEquals("المدرسة الأولى", state.activeSchool?.name)
    }

    @Test
    fun operationsRequiringSchool_doNotExecuteUsingFakeId_whenNoSchoolExists() = runTest {
        val emptyEnterpriseRepo = object : EnterpriseRepository by fakeEnterpriseRepo {
            override fun getSchools(): Flow<List<School>> = flowOf(emptyList())
        }
        val emptyPrefs = object : RtiqaPreferencesDataStore(RuntimeEnvironment.getApplication()) {
            override val userPreferencesFlow: Flow<UserPreferences> = flowOf(
                UserPreferences(
                    isDarkTheme = false,
                    isOfflineModeEnabled = false,
                    activeUserId = null,
                    lastSyncTimestamp = 0L,
                    activeSchoolId = null
                )
            )
            override suspend fun setActiveSchoolId(schoolId: String?) {}
        }

        val vm = UserManagementViewModel(
            getUsersForSchoolUseCase = GetUsersForSchoolUseCase(emptyEnterpriseRepo),
            saveEnterpriseMemberUseCase = SaveEnterpriseMemberUseCase(emptyEnterpriseRepo),
            deleteEnterpriseMemberUseCase = DeleteEnterpriseMemberUseCase(emptyEnterpriseRepo),
            getSchoolsUseCase = GetSchoolsUseCase(emptyEnterpriseRepo),
            preferencesDataStore = emptyPrefs
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val countBefore = savedMembers.size

        // Attempt to save user when no school is active
        vm.onAction(
            UsersUiAction.SaveUser(
                id = null,
                name = "أحمد خالد",
                email = "ahmed@example.com",
                role = EnterpriseRole.TEACHER,
                department = "الرياضيات",
                phone = "0551234567",
                status = MemberStatus.ACTIVE
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify member was not saved under fake ID or added to repository
        assertEquals(countBefore, savedMembers.size)
        assertTrue(savedMembers.none { it.name == "أحمد خالد" })
    }

    @Test
    fun selectActiveSchool_persistsToPreferences() = runTest {
        var savedSchoolId: String? = null
        val recordingPrefs = object : RtiqaPreferencesDataStore(RuntimeEnvironment.getApplication()) {
            override val userPreferencesFlow: Flow<UserPreferences> = flowOf(
                UserPreferences(
                    isDarkTheme = false,
                    isOfflineModeEnabled = false,
                    activeUserId = null,
                    lastSyncTimestamp = 0L,
                    activeSchoolId = null
                )
            )
            override suspend fun setActiveSchoolId(schoolId: String?) {
                savedSchoolId = schoolId
            }
        }

        val vm = UserManagementViewModel(
            getUsersForSchoolUseCase = GetUsersForSchoolUseCase(fakeEnterpriseRepo),
            saveEnterpriseMemberUseCase = SaveEnterpriseMemberUseCase(fakeEnterpriseRepo),
            deleteEnterpriseMemberUseCase = DeleteEnterpriseMemberUseCase(fakeEnterpriseRepo),
            getSchoolsUseCase = GetSchoolsUseCase(fakeEnterpriseRepo),
            preferencesDataStore = recordingPrefs
        )

        testDispatcher.scheduler.advanceUntilIdle()

        vm.onAction(UsersUiAction.SelectActiveSchool("sch_target_99"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("sch_target_99", savedSchoolId)
    }

    @Test
    fun emptyRealUserSource_producesEmptyRuntimeList_andNoDemoUsersInjected() = runTest {
        val prefs = object : RtiqaPreferencesDataStore(RuntimeEnvironment.getApplication()) {
            override val userPreferencesFlow: Flow<UserPreferences> = flowOf(
                UserPreferences(
                    isDarkTheme = false,
                    isOfflineModeEnabled = false,
                    activeUserId = null,
                    lastSyncTimestamp = 0L,
                    activeSchoolId = "sch_1"
                )
            )
            override suspend fun setActiveSchoolId(schoolId: String?) {}
        }

        savedMembers.clear()

        val vm = UserManagementViewModel(
            getUsersForSchoolUseCase = GetUsersForSchoolUseCase(fakeEnterpriseRepo),
            saveEnterpriseMemberUseCase = SaveEnterpriseMemberUseCase(fakeEnterpriseRepo),
            deleteEnterpriseMemberUseCase = DeleteEnterpriseMemberUseCase(fakeEnterpriseRepo),
            getSchoolsUseCase = GetSchoolsUseCase(fakeEnterpriseRepo),
            preferencesDataStore = prefs
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("sch_1", state.activeSchoolId)
        assertTrue("When no real users exist, rawUsers must be empty", state.rawUsers.isEmpty())
        assertTrue("When no real users exist, filteredUsers must be empty", state.filteredUsers.isEmpty())
        assertTrue("No demo users should have been seeded into repository", savedMembers.isEmpty())
        assertFalse(state.rawUsers.any { it.id == "usr_pr1" || it.email == "ahmed.parent@school1.edu" })
    }

    @Test
    fun realUsersFromDataSource_arePreservedAndVisible() = runTest {
        val realMember = EnterpriseMember(
            id = "real_user_01",
            orgId = "org_1",
            name = "سارة العلي",
            email = "sara@real.edu",
            role = EnterpriseRole.TEACHER,
            department = "قسم العلوم",
            status = MemberStatus.ACTIVE,
            phone = "+966501234567",
            schoolId = "sch_1"
        )
        savedMembers.clear()
        savedMembers.add(realMember)

        val prefs = object : RtiqaPreferencesDataStore(RuntimeEnvironment.getApplication()) {
            override val userPreferencesFlow: Flow<UserPreferences> = flowOf(
                UserPreferences(
                    isDarkTheme = false,
                    isOfflineModeEnabled = false,
                    activeUserId = null,
                    lastSyncTimestamp = 0L,
                    activeSchoolId = "sch_1"
                )
            )
            override suspend fun setActiveSchoolId(schoolId: String?) {}
        }

        val vm = UserManagementViewModel(
            getUsersForSchoolUseCase = GetUsersForSchoolUseCase(fakeEnterpriseRepo),
            saveEnterpriseMemberUseCase = SaveEnterpriseMemberUseCase(fakeEnterpriseRepo),
            deleteEnterpriseMemberUseCase = DeleteEnterpriseMemberUseCase(fakeEnterpriseRepo),
            getSchoolsUseCase = GetSchoolsUseCase(fakeEnterpriseRepo),
            preferencesDataStore = prefs
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(1, state.rawUsers.size)
        assertEquals("real_user_01", state.rawUsers.first().id)
        assertEquals("سارة العلي", state.rawUsers.first().name)
        assertEquals(1, state.filteredUsers.size)
    }
}
