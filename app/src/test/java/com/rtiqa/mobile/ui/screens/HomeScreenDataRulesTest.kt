package com.rtiqa.mobile.ui.screens

import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.feature.home.countCompletedLessons
import com.rtiqa.feature.home.selectFirstIncompleteLesson
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

    @Test
    fun continueLearning_usesActualFirstIncompleteLesson() {
        val lessons = listOf(
            lesson("later", order = 3),
            lesson("completed", order = 1, isCompleted = true),
            lesson("next", order = 2)
        )

        assertEquals("next", selectFirstIncompleteLesson("course", lessons)?.id)
    }

    @Test
    fun continueLearning_doesNotNavigateToHardcodedLessonId() {
        val selected = selectFirstIncompleteLesson("course", listOf(lesson("server-lesson-42", order = 1)))

        assertEquals("server-lesson-42", selected?.id)
    }

    @Test
    fun noIncompleteLesson_doesNotUseFakeLesson() {
        assertNull(
            selectFirstIncompleteLesson(
                "course",
                listOf(lesson("completed", order = 1, isCompleted = true))
            )
        )
    }

    @Test
    fun completedLessonCount_usesRealCoreLessons() {
        val lessons = listOf(
            lesson("one", order = 1, isCompleted = true),
            lesson("two", order = 2),
            lesson("three", order = 3, isCompleted = true)
        )

        assertEquals(2, countCompletedLessons(lessons))
    }

    private fun lesson(id: String, order: Int, isCompleted: Boolean = false) = Lesson(
        id = id,
        courseId = "course",
        title = id,
        content = "",
        order = order,
        isCompleted = isCompleted
    )
}
