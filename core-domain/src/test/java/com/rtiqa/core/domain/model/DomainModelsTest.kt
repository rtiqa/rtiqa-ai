package com.rtiqa.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelsTest {

    @Test
    fun userProfile_calculateLevelAndXpProgress() {
        val profile = UserProfile(id = "1", name = "Jane", email = "jane@rtiqa.com", levelXp = 250)
        assertEquals(3, profile.calculateLevel())
        assertEquals(0.5f, profile.calculateLevelProgressPercent(), 0.01f)

        val updated = profile.addXp(50)
        assertEquals(300, updated.levelXp)
        assertEquals(4, updated.calculateLevel())
    }

    @Test
    fun quiz_calculateScoreAndPassingGrade() {
        val quiz = Quiz(
            id = "q1",
            courseId = "c1",
            title = "Test Quiz",
            questions = listOf(
                Question("1", "Q1?", listOf("A", "B"), 0),
                Question("2", "Q2?", listOf("A", "B"), 1)
            ),
            passingScorePercent = 70
        )

        val answersMap = mapOf("1" to 0, "2" to 1)
        val scorePercent = quiz.calculateScorePercent(answersMap)
        assertEquals(100, scorePercent)
        assertTrue(quiz.isPassed(scorePercent))

        val wrongAnswersMap = mapOf("1" to 0, "2" to 0)
        val lowScorePercent = quiz.calculateScorePercent(wrongAnswersMap)
        assertEquals(50, lowScorePercent)
        assertFalse(quiz.isPassed(lowScorePercent))
    }
}
