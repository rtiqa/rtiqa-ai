package com.rtiqa.core.domain

import com.rtiqa.core.domain.model.Course
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test foundation verifying course domain models and computation logic.
 */
class CourseDomainTest {

    @Test
    fun courseProgressCalculation_isCorrect() {
        val course = Course(
            id = "test-1",
            title = "Kotlin Fundamentals",
            description = "Learn Kotlin for Android",
            category = "Mobile",
            totalLessons = 10,
            durationMinutes = 300,
            progressPercent = 0.5f,
            isDownloaded = true,
            isEnrolled = true,
            isBookmarked = true
        )

        assertEquals("Kotlin Fundamentals", course.title)
        assertEquals(10, course.totalLessons)
        assertEquals(300, course.durationMinutes)
        assertEquals(0.5f, course.progressPercent, 0.001f)
        assertTrue(course.isDownloaded)
        assertTrue(course.isEnrolled)
        assertTrue(course.isBookmarked)
    }

    @Test
    fun courseIsCompleted_returnsTrueWhenProgressIsOne() {
        val course = Course(
            id = "test-2",
            title = "Android Clean Architecture",
            description = "Mastering Clean Architecture",
            category = "Mobile",
            totalLessons = 5,
            durationMinutes = 150,
            progressPercent = 1.0f
        )

        assertTrue(course.isFullyCompleted())
    }
}
