package com.rtiqa.core.data.repository

import com.rtiqa.core.database.dao.CourseDao
import com.rtiqa.core.database.dao.LessonDao
import com.rtiqa.core.database.entity.CourseEntity
import com.rtiqa.core.domain.model.PageRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CourseRepositoryImplTest {

    private class FakeCourseDao : CourseDao {
        val list = mutableListOf(
            CourseEntity("1", "Kotlin Basics", "Learn Kotlin", "Mobile", 5, 120, null, false, 0f),
            CourseEntity("2", "Compose Masterclass", "Learn Compose", "Mobile", 8, 200, null, false, 0.5f)
        )

        override fun getAllCourses() = flowOf(list.toList())
        override suspend fun getAllCoursesList() = list.toList()
        override fun getCourseById(id: String) = flowOf(list.find { it.id == id })
        override suspend fun insertCourses(courses: List<CourseEntity>) {
            list.addAll(courses)
        }
    }

    private class FakeLessonDao : LessonDao {
        override fun getLessonsForCourse(courseId: String) = flowOf(emptyList<com.rtiqa.core.database.entity.LessonEntity>())
        override suspend fun getLessonById(id: String) = null
        override suspend fun insertLesson(lesson: com.rtiqa.core.database.entity.LessonEntity) {}
        override suspend fun insertLessons(lessons: List<com.rtiqa.core.database.entity.LessonEntity>) {}
    }

    @Test
    fun getCourses_returnsMappedDomainCourses() = runTest {
        val repository = CourseRepositoryImpl(FakeCourseDao(), FakeLessonDao())
        val courses = repository.getCourses().first()

        assertEquals(2, courses.size)
        assertEquals("Kotlin Basics", courses[0].title)
    }

    @Test
    fun getPagedCourses_filtersAndPagesCorrectly() = runTest {
        val repository = CourseRepositoryImpl(FakeCourseDao(), FakeLessonDao())
        val pagedData = repository.getPagedCourses(PageRequest(page = 1, pageSize = 1, searchQuery = "Kotlin")).first()

        assertEquals(1, pagedData.items.size)
        assertEquals("Kotlin Basics", pagedData.items[0].title)
    }

    @Test
    fun searchCourses_matchesQuery() = runTest {
        val repository = CourseRepositoryImpl(FakeCourseDao(), FakeLessonDao())
        val searchResult = repository.searchCourses("Compose")

        assertEquals(1, searchResult.size)
        assertEquals("Compose Masterclass", searchResult[0].title)
    }
}
