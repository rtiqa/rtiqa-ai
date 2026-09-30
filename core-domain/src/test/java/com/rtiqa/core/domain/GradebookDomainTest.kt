package com.rtiqa.core.domain

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.ClassGradebook
import com.rtiqa.core.domain.model.GradebookAssessment
import com.rtiqa.core.domain.model.GradebookScore
import com.rtiqa.core.domain.model.GradebookStudent
import com.rtiqa.core.domain.repository.GradebookRepository
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.GetClassGradebookUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GradebookDomainTest {

    @Test
    fun domainModel_preservesRealIdsAndData() {
        val student = GradebookStudent(
            studentId = "std_real_101",
            displayName = "عبدالله محمد",
            studentNumber = "441001"
        )
        val assessment = GradebookAssessment(
            assessmentId = "asm_midterm_1",
            title = "الاختبار النصفي",
            maxScore = 20.0,
            weight = 0.25
        )
        val score = GradebookScore(
            studentId = "std_real_101",
            assessmentId = "asm_midterm_1",
            score = 18.5
        )
        val gradebook = ClassGradebook(
            classId = "cls_real_301",
            students = listOf(student),
            assessments = listOf(assessment),
            scores = listOf(score)
        )

        assertEquals("cls_real_301", gradebook.classId)
        assertEquals(1, gradebook.students.size)
        assertEquals("std_real_101", gradebook.students.first().studentId)
        assertEquals("عبدالله محمد", gradebook.students.first().displayName)
        assertEquals("441001", gradebook.students.first().studentNumber)

        assertEquals(1, gradebook.assessments.size)
        assertEquals("asm_midterm_1", gradebook.assessments.first().assessmentId)
        assertEquals("الاختبار النصفي", gradebook.assessments.first().title)
        assertEquals(20.0, gradebook.assessments.first().maxScore, 0.001)
        assertEquals(0.25, gradebook.assessments.first().weight!!, 0.001)

        assertEquals(1, gradebook.scores.size)
        assertEquals("std_real_101", gradebook.scores.first().studentId)
        assertEquals("asm_midterm_1", gradebook.scores.first().assessmentId)
        assertEquals(18.5, gradebook.scores.first().score!!, 0.001)
    }

    @Test
    fun domainModel_emptyStudentsAssessmentsScores_areValid() {
        val gradebook = ClassGradebook(
            classId = "cls_empty_99",
            students = emptyList(),
            assessments = emptyList(),
            scores = emptyList()
        )

        assertEquals("cls_empty_99", gradebook.classId)
        assertTrue(gradebook.students.isEmpty())
        assertTrue(gradebook.assessments.isEmpty())
        assertTrue(gradebook.scores.isEmpty())
    }

    @Test
    fun domainModel_optionalFields_canBeNull() {
        val student = GradebookStudent(studentId = "std_no_num", displayName = "خالد")
        val assessment = GradebookAssessment(assessmentId = "asm_no_weight", title = "واجب 1", maxScore = 10.0)
        val score = GradebookScore(studentId = "std_no_num", assessmentId = "asm_no_weight", score = null)

        assertNull(student.studentNumber)
        assertNull(assessment.weight)
        assertNull(score.score)
    }

    @Test
    fun useCase_delegatesUsingRealClassId() = runTest {
        var requestedClassId: String? = null
        val expectedGradebook = ClassGradebook(
            classId = "cls_chemistry_101",
            students = emptyList(),
            assessments = emptyList(),
            scores = emptyList()
        )

        val fakeRepo = object : GradebookRepository {
            override suspend fun getClassGradebook(classId: String): RtiqaResult<ClassGradebook> {
                requestedClassId = classId
                return RtiqaResult.Success(expectedGradebook)
            }
        }

        val useCase = GetClassGradebookUseCase(fakeRepo)
        val result = useCase("cls_chemistry_101")

        assertEquals("cls_chemistry_101", requestedClassId)
        assertTrue(result is RtiqaResult.Success)
        assertEquals(expectedGradebook, (result as RtiqaResult.Success).data)
    }

    @Test
    fun useCase_rejectsBlankClassId_usingValidationError() = runTest {
        var repoInvoked = false
        val fakeRepo = object : GradebookRepository {
            override suspend fun getClassGradebook(classId: String): RtiqaResult<ClassGradebook> {
                repoInvoked = true
                return RtiqaResult.Error(RtiqaError.UnknownError("Should not be called"))
            }
        }

        val useCase = GetClassGradebookUseCase(fakeRepo)

        // Test empty string
        val emptyResult = useCase("")
        assertTrue("Empty classId must return RtiqaResult.Error", emptyResult is RtiqaResult.Error)
        val emptyError = (emptyResult as RtiqaResult.Error).error
        assertTrue("Error must be ValidationError", emptyError is RtiqaError.ValidationError)

        // Test whitespace string
        val whitespaceResult = useCase("   ")
        assertTrue("Whitespace classId must return RtiqaResult.Error", whitespaceResult is RtiqaResult.Error)
        val whitespaceError = (whitespaceResult as RtiqaResult.Error).error
        assertTrue("Error must be ValidationError", whitespaceError is RtiqaError.ValidationError)

        // Verify repository was not called
        assertTrue("Repository must not be invoked for blank classId", !repoInvoked)
    }
}
