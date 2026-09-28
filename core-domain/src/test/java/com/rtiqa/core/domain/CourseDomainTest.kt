package com.rtiqa.core.domain

import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.Lesson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
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

    @Test
    fun courseAndLesson_defaultSchoolId_isNullAndNotHardcoded() {
        val course = Course(
            id = "c-default",
            title = "Title",
            description = "Desc",
            category = "Cat",
            totalLessons = 1,
            durationMinutes = 10
        )
        assertNull(course.schoolId)
        assertNotEquals("school_001", course.schoolId)

        val lesson = Lesson(
            id = "l-default",
            courseId = "c-default",
            title = "Lesson Title",
            content = "Content",
            order = 1
        )
        assertNull(lesson.schoolId)
        assertNotEquals("school_001", lesson.schoolId)
    }

    @Test
    fun courseAndLesson_explicitSchoolId_isPreserved() {
        val course = Course(
            id = "c-school",
            title = "Title",
            description = "Desc",
            category = "Cat",
            totalLessons = 1,
            durationMinutes = 10,
            schoolId = "school_international_42"
        )
        assertEquals("school_international_42", course.schoolId)

        val lesson = Lesson(
            id = "l-school",
            courseId = "c-school",
            title = "Lesson Title",
            content = "Content",
            order = 1,
            schoolId = "school_international_42"
        )
        assertEquals("school_international_42", lesson.schoolId)
    }
}
