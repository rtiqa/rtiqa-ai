package com.rtiqa.mobile.ui.screens

import com.rtiqa.core.domain.model.Course
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeScreenDataRulesTest {
    private fun course(
        id: String,
        isEnrolled: Boolean = false,
        progress: Float = 0f
    ) = Course(
        id = id,
        title = id,
        description = "",
        category = "",
        totalLessons = 1,
        durationMinutes = 1,
        isEnrolled = isEnrolled,
        progressPercent = progress
    )

    @Test
    fun emptyCoreCatalog_exposesNoCourses() {
        assertEquals(emptyList<Course>(), homeEnrolledCourses(emptyList()))
        assertNull(homeContinueLearningCourse(homeEnrolledCourses(emptyList())))
    }

    @Test
    fun noEnrollment_doesNotUseFirstCatalogCourseAsContinueLearning() {
        val catalog = listOf(course("first"), course("second"))

        assertNull(homeContinueLearningCourse(homeEnrolledCourses(catalog)))
    }

    @Test
    fun continueLearning_usesOnlyAnEnrolledInProgressCourse() {
        val catalog = listOf(
            course("not-enrolled", progress = 0.5f),
            course("completed", isEnrolled = true, progress = 1f),
            course("in-progress", isEnrolled = true, progress = 0.5f)
        )

        assertEquals(
            "in-progress",
            homeContinueLearningCourse(homeEnrolledCourses(catalog))?.id
        )
    }

    @Test
    fun completedLessonsCount_canDisplayZero() {
        assertEquals(0, homeDisplayCount(0))
    }

    @Test
    fun passedQuizzesCount_canDisplayZero() {
        assertEquals(0, homeDisplayCount(0))
    }
}
