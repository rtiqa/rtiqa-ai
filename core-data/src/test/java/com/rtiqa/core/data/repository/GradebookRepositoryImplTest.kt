package com.rtiqa.core.data.repository

import com.rtiqa.core.data.datasource.GradebookDataSource
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.ClassGradebook
import com.rtiqa.core.domain.model.GradebookAssessment
import com.rtiqa.core.domain.model.GradebookScore
import com.rtiqa.core.domain.model.GradebookStudent
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GradebookRepositoryImplTest {

    @Test
    fun realData_isReturnedUnchanged() = runTest {
        val realGradebook = ClassGradebook(
            classId = "cls_real_777",
            students = listOf(
                GradebookStudent(
                    studentId = "std_real_01",
                    displayName = "فهد العتيبي",
                    studentNumber = "442001"
                )
            ),
            assessments = listOf(
                GradebookAssessment(
                    assessmentId = "asm_quiz_1",
                    title = "اختبار قصير 1",
                    maxScore = 15.0,
                    weight = 0.15
                )
            ),
            scores = listOf(
                GradebookScore(
                    studentId = "std_real_01",
                    assessmentId = "asm_quiz_1",
                    score = 14.0
                )
            )
        )

        val fakeDataSource = object : GradebookDataSource {
            override suspend fun getClassGradebook(classId: String): ClassGradebook? {
                return if (classId == "cls_real_777") realGradebook else null
            }
        }

        val repository = GradebookRepositoryImpl(fakeDataSource)
        val result = repository.getClassGradebook("cls_real_777")

        assertTrue("Expected RtiqaResult.Success", result is RtiqaResult.Success)
        val data = (result as RtiqaResult.Success).data
        assertEquals("cls_real_777", data.classId)
        assertEquals(1, data.students.size)
        assertEquals("std_real_01", data.students.first().studentId)
        assertEquals("فهد العتيبي", data.students.first().displayName)
        assertEquals("442001", data.students.first().studentNumber)
        assertEquals(1, data.assessments.size)
        assertEquals("asm_quiz_1", data.assessments.first().assessmentId)
        assertEquals(1, data.scores.size)
        assertEquals(14.0, data.scores.first().score!!, 0.001)
    }

    @Test
    fun realClassId_isPassedToDataSource() = runTest {
        var queriedClassId: String? = null
        val fakeDataSource = object : GradebookDataSource {
            override suspend fun getClassGradebook(classId: String): ClassGradebook? {
                queriedClassId = classId
                return null
            }
        }

        val repository = GradebookRepositoryImpl(fakeDataSource)
        repository.getClassGradebook("cls_specific_999")

        assertEquals("cls_specific_999", queriedClassId)
    }

    @Test
    fun unavailableData_returnsEmptyGradebookForRealClassId_andNeverCreatesMockData() = runTest {
        val fakeDataSource = object : GradebookDataSource {
            override suspend fun getClassGradebook(classId: String): ClassGradebook? = null
        }

        val repository = GradebookRepositoryImpl(fakeDataSource)
        val result = repository.getClassGradebook("cls_unseeded_42")

        assertTrue("Expected RtiqaResult.Success", result is RtiqaResult.Success)
        val data = (result as RtiqaResult.Success).data
        assertEquals("cls_unseeded_42", data.classId)
        assertTrue("Students list must be empty when data unavailable", data.students.isEmpty())
        assertTrue("Assessments list must be empty when data unavailable", data.assessments.isEmpty())
        assertTrue("Scores list must be empty when data unavailable", data.scores.isEmpty())
    }

    @Test
    fun noDataSourceProvided_defaultsSafelyToEmptyGradebookForRealClassId() = runTest {
        val repository = GradebookRepositoryImpl(gradebookDataSource = null)
        val result = repository.getClassGradebook("cls_offline_standalone")

        assertTrue("Expected RtiqaResult.Success", result is RtiqaResult.Success)
        val data = (result as RtiqaResult.Success).data
        assertEquals("cls_offline_standalone", data.classId)
        assertTrue(data.students.isEmpty())
        assertTrue(data.assessments.isEmpty())
        assertTrue(data.scores.isEmpty())
    }

    @Test
    fun dataSourceException_mapsToRtiqaResultError() = runTest {
        val fakeDataSource = object : GradebookDataSource {
            override suspend fun getClassGradebook(classId: String): ClassGradebook? {
                throw IllegalStateException("Database I/O error")
            }
        }

        val repository = GradebookRepositoryImpl(fakeDataSource)
        val result = repository.getClassGradebook("cls_error_trigger")

        assertTrue("Expected RtiqaResult.Error", result is RtiqaResult.Error)
        val error = (result as RtiqaResult.Error).error
        assertTrue("Error should be DatabaseError", error is RtiqaError.DatabaseError)
        assertTrue("Error message should preserve failure description", error.message.contains("Database I/O error"))
    }

    @Test
    fun noFabricatedIdentityIsIntroduced() = runTest {
        val repository = GradebookRepositoryImpl(gradebookDataSource = null)
        val result = repository.getClassGradebook("cls_arbitrary_123")

        assertTrue(result is RtiqaResult.Success)
        val data = (result as RtiqaResult.Success).data
        assertFalse(data.classId == "cls-101")
        assertFalse(data.classId == "school_001")
        assertFalse(data.classId == "org_001")
        assertEquals("cls_arbitrary_123", data.classId)
    }
}
