package com.rtiqa.feature.admin.teacher.gradebook

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.ClassGradebook
import com.rtiqa.core.domain.model.GradebookAssessment
import com.rtiqa.core.domain.model.GradebookScore
import com.rtiqa.core.domain.model.GradebookStudent
import com.rtiqa.core.domain.repository.GradebookRepository
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.GetClassGradebookUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AcademicGradebookViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeGradebookRepository : GradebookRepository {
        var lastQueriedClassId: String? = null
        var resultToReturn: RtiqaResult<ClassGradebook> = RtiqaResult.Success(
            ClassGradebook(
                classId = "cls_default",
                students = emptyList(),
                assessments = emptyList(),
                scores = emptyList()
            )
        )

        override suspend fun getClassGradebook(classId: String): RtiqaResult<ClassGradebook> {
            lastQueriedClassId = classId
            return resultToReturn
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun realClassId_isPassedToUseCase_viaInitialClassId() = runTest {
        val fakeRepo = FakeGradebookRepository()
        val useCase = GetClassGradebookUseCase(fakeRepo)
        val viewModel = AcademicGradebookViewModel(
            getClassGradebookUseCase = useCase,
            initialClassId = "cls_science_301"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("cls_science_301", fakeRepo.lastQueriedClassId)
        assertEquals("cls_science_301", viewModel.uiState.value.selectedClassId)
    }

    @Test
    fun realClassId_isPassedToUseCase_viaSelectClassAction() = runTest {
        val fakeRepo = FakeGradebookRepository()
        val useCase = GetClassGradebookUseCase(fakeRepo)
        val viewModel = AcademicGradebookViewModel(
            getClassGradebookUseCase = useCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(AcademicGradebookUiAction.SelectClass("cls_math_202"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("cls_math_202", fakeRepo.lastQueriedClassId)
        assertEquals("cls_math_202", viewModel.uiState.value.selectedClassId)
    }

    @Test
    fun realDomainStudentsAssessmentsScores_mapCorrectlyToUiState() = runTest {
        val student1 = GradebookStudent("std_01", "أحمد علي", "441001")
        val student2 = GradebookStudent("std_02", "سعد خالد", "441002")
        val assessment1 = GradebookAssessment("asm_01", "الواجب الأول", 10.0, 0.1)
        val assessment2 = GradebookAssessment("asm_02", "الاختبار النصفي", 20.0, 0.2)
        val scores = listOf(
            GradebookScore("std_01", "asm_01", 9.0),
            GradebookScore("std_01", "asm_02", 18.0),
            GradebookScore("std_02", "asm_01", 8.0),
            GradebookScore("std_02", "asm_02", null) // missing score
        )
        val realGradebook = ClassGradebook("cls_physics_101", listOf(student1, student2), listOf(assessment1, assessment2), scores)

        val fakeRepo = FakeGradebookRepository().apply {
            resultToReturn = RtiqaResult.Success(realGradebook)
        }
        val useCase = GetClassGradebookUseCase(fakeRepo)
        val viewModel = AcademicGradebookViewModel(
            getClassGradebookUseCase = useCase,
            initialClassId = "cls_physics_101"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.students.size)
        assertEquals(2, state.columns.size)

        // Verify column mapping
        assertEquals("asm_01", state.columns[0].id)
        assertEquals("الواجب الأول", state.columns[0].title)
        assertEquals(10.0, state.columns[0].maxScore, 0.001)
        assertEquals(10, state.columns[0].weightPercentage)

        // Verify student 1 mapping
        val s1 = state.students[0]
        assertEquals("std_01", s1.studentId)
        assertEquals("أحمد علي", s1.studentName)
        assertEquals("441001", s1.studentNumber)
        assertEquals(9.0, s1.scores["asm_01"]?.score!!, 0.001)
        assertEquals(18.0, s1.scores["asm_02"]?.score!!, 0.001)
        assertEquals(27.0, s1.totalScore, 0.001)

        // Verify student 2 mapping with null/missing score
        val s2 = state.students[1]
        assertEquals("std_02", s2.studentId)
        assertEquals("سعد خالد", s2.studentName)
        assertEquals("441002", s2.studentNumber)
        assertEquals(8.0, s2.scores["asm_01"]?.score!!, 0.001)
        assertNull(s2.scores["asm_02"]?.score)
        assertEquals(8.0, s2.totalScore, 0.001)
    }

    @Test
    fun emptyGradebook_staysEmpty() = runTest {
        val emptyGradebook = ClassGradebook("cls_empty", emptyList(), emptyList(), emptyList())
        val fakeRepo = FakeGradebookRepository().apply {
            resultToReturn = RtiqaResult.Success(emptyGradebook)
        }
        val useCase = GetClassGradebookUseCase(fakeRepo)
        val viewModel = AcademicGradebookViewModel(
            getClassGradebookUseCase = useCase,
            initialClassId = "cls_empty"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.students.isEmpty())
        assertTrue(state.columns.isEmpty())
        assertEquals(0.0, state.classAverage, 0.001)
        assertEquals(0, state.passRatePercentage)
    }

    @Test
    fun noClassId_doesNotCallUseCase_andExposesEmptyState() = runTest {
        val fakeRepo = FakeGradebookRepository()
        val useCase = GetClassGradebookUseCase(fakeRepo)
        val viewModel = AcademicGradebookViewModel(
            getClassGradebookUseCase = useCase,
            initialClassId = null
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(fakeRepo.lastQueriedClassId)
        assertEquals("", viewModel.uiState.value.selectedClassId)
        assertTrue(viewModel.uiState.value.students.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0.0, viewModel.uiState.value.classAverage, 0.001)
        assertEquals(0.0, viewModel.uiState.value.highestScore, 0.001)
        assertEquals(0.0, viewModel.uiState.value.lowestScore, 0.001)
        assertEquals(0, viewModel.uiState.value.passRatePercentage)
    }

    @Test
    fun academicGradebookUiState_defaultValuesAreSafeAndZero() {
        val defaultState = AcademicGradebookUiState()
        assertEquals("", defaultState.selectedClassId)
        assertEquals(0.0, defaultState.classAverage, 0.001)
        assertEquals(0.0, defaultState.highestScore, 0.001)
        assertEquals(0.0, defaultState.lowestScore, 0.001)
        assertEquals(0, defaultState.passRatePercentage)
        assertTrue(defaultState.students.isEmpty())
        assertTrue(defaultState.columns.isEmpty())
    }

    @Test
    fun errorResult_exposesErrorState_withoutFakeData() = runTest {
        val fakeRepo = FakeGradebookRepository().apply {
            resultToReturn = RtiqaResult.Error(RtiqaError.DatabaseError("فشل تحميل الدرجات من قاعدة البيانات"))
        }
        val useCase = GetClassGradebookUseCase(fakeRepo)
        val viewModel = AcademicGradebookViewModel(
            getClassGradebookUseCase = useCase,
            initialClassId = "cls_error_trigger"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("فشل تحميل الدرجات من قاعدة البيانات", state.errorMessage)
        assertTrue(state.students.isEmpty())
        assertTrue(state.columns.isEmpty())
        assertEquals(0.0, state.classAverage, 0.001)
        assertFalse("Must not contain fabricated student IDs", state.students.any { it.studentId.startsWith("std-10") })
    }

    @Test
    fun statistics_areBasedOnlyOnRealScores() = runTest {
        val students = listOf(
            GradebookStudent("s1", "طالب 1"),
            GradebookStudent("s2", "طالب 2")
        )
        val assessments = listOf(GradebookAssessment("a1", "اختبار", 100.0))
        val scores = listOf(
            GradebookScore("s1", "a1", 80.0),
            GradebookScore("s2", "a1", 40.0)
        )
        val gradebook = ClassGradebook("cls_stats", students, assessments, scores)

        val fakeRepo = FakeGradebookRepository().apply {
            resultToReturn = RtiqaResult.Success(gradebook)
        }
        val useCase = GetClassGradebookUseCase(fakeRepo)
        val viewModel = AcademicGradebookViewModel(
            getClassGradebookUseCase = useCase,
            initialClassId = "cls_stats"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(60.0, state.classAverage, 0.001)
        assertEquals(80.0, state.highestScore, 0.001)
        assertEquals(40.0, state.lowestScore, 0.001)
        // 1 out of 2 passed (80% >= 60%, 40% < 60%) => 50%
        assertEquals(50, state.passRatePercentage)
    }
}
