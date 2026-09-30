package com.rtiqa.feature.admin.teacher.gradebook

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AcademicGradebookViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadGradebookData_whenNoRealDataAvailable_studentsListRemainsEmpty() = runTest {
        val viewModel = AcademicGradebookViewModel()
        testDispatcher.scheduler.advanceTimeBy(500)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue("Students list must be empty when no real gradebook data exists", state.students.isEmpty())
        assertEquals(0, state.students.size)
    }

    @Test
    fun loadGradebookData_doesNotGenerateMockStudents() = runTest {
        val viewModel = AcademicGradebookViewModel()
        testDispatcher.scheduler.advanceTimeBy(500)

        val state = viewModel.uiState.value
        assertFalse("Must not contain fabricated student IDs", state.students.any { it.studentId.startsWith("std-10") })
        assertFalse("Must not contain fabricated student names", state.students.any { it.studentName.contains("الزهراني") })
    }

    @Test
    fun selectClass_doesNotManufactureStudents() = runTest {
        val viewModel = AcademicGradebookViewModel()
        testDispatcher.scheduler.advanceTimeBy(500)

        viewModel.onAction(AcademicGradebookUiAction.SelectClass("cls-102"))
        testDispatcher.scheduler.advanceTimeBy(500)

        val state = viewModel.uiState.value
        assertEquals("cls-102", state.selectedClassId)
        assertTrue("Students list must remain empty after selecting class without real data", state.students.isEmpty())
    }

    @Test
    fun stats_withEmptyStudents_areZero() = runTest {
        val viewModel = AcademicGradebookViewModel()
        testDispatcher.scheduler.advanceTimeBy(500)

        val state = viewModel.uiState.value
        assertEquals(0.0, state.classAverage, 0.001)
        assertEquals(0.0, state.highestScore, 0.001)
        assertEquals(0.0, state.lowestScore, 0.001)
        assertEquals(0, state.passRatePercentage)
    }
}
