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
        override fun getCoursesForSchool(schoolId: String) = flowOf(list.filter { it.schoolId == schoolId })
        override suspend fun getAllCoursesList() = list.toList()
        override fun getCourseById(id: String) = flowOf(list.find { it.id == id })
        override suspend fun insertCourse(course: CourseEntity) {
            list.add(course)
        }
        override suspend fun insertCourses(courses: List<CourseEntity>) {
            list.addAll(courses)
        }
        override suspend fun deleteCourseById(id: String) {
            list.removeAll { it.id == id }
        }
        override suspend fun updateEnrollmentStatus(id: String, isEnrolled: Boolean) {
            val idx = list.indexOfFirst { it.id == id }
            if (idx != -1) list[idx] = list[idx].copy(isEnrolled = isEnrolled)
        }
        override suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean) {
            val idx = list.indexOfFirst { it.id == id }
            if (idx != -1) list[idx] = list[idx].copy(isBookmarked = isBookmarked)
        }
        override suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean) {
            val idx = list.indexOfFirst { it.id == id }
            if (idx != -1) list[idx] = list[idx].copy(isDownloaded = isDownloaded)
        }
        override suspend fun updateCourseProgress(id: String, progressPercent: Float) {
            val idx = list.indexOfFirst { it.id == id }
            if (idx != -1) list[idx] = list[idx].copy(progressPercent = progressPercent)
        }
    }

    private class FakeLessonDao : LessonDao {
        val lessonList = mutableListOf(
            com.rtiqa.core.database.entity.LessonEntity("l1", "c1", "Lesson 1", "Content 1", 1, false, null),
            com.rtiqa.core.database.entity.LessonEntity("l2", "c1", "Lesson 2", "Content 2", 2, false, null)
        )
        override fun getLessonsForCourse(courseId: String) = flowOf(lessonList.filter { it.courseId == courseId })
        override suspend fun getLessonsForCourseList(courseId: String) = lessonList.filter { it.courseId == courseId }
        override suspend fun getLessonById(id: String) = lessonList.find { it.id == id }
        override fun observeLessonById(id: String) = flowOf(lessonList.find { it.id == id })
        override fun getNextLessonEntity(courseId: String, currentLessonId: String): kotlinx.coroutines.flow.Flow<com.rtiqa.core.database.entity.LessonEntity?> {
            val curr = lessonList.find { it.id == currentLessonId } ?: return flowOf(null)
            val next = lessonList.filter { it.courseId == courseId && it.order > curr.order }.minByOrNull { it.order }
            return flowOf(next)
        }
        override suspend fun insertLesson(lesson: com.rtiqa.core.database.entity.LessonEntity) {
            val idx = lessonList.indexOfFirst { it.id == lesson.id }
            if (idx != -1) lessonList[idx] = lesson else lessonList.add(lesson)
        }
        override suspend fun insertLessons(lessons: List<com.rtiqa.core.database.entity.LessonEntity>) {
            lessonList.addAll(lessons)
        }
        override suspend fun deleteLessonsForCourse(courseId: String) {
            lessonList.removeAll { it.courseId == courseId }
        }
        override suspend fun updateLessonCompletion(id: String, isCompleted: Boolean) {
            val idx = lessonList.indexOfFirst { it.id == id }
            if (idx != -1) lessonList[idx] = lessonList[idx].copy(isCompleted = isCompleted)
        }
        override suspend fun getTotalLessonsCount(courseId: String): Int = lessonList.count { it.courseId == courseId }
        override suspend fun getCompletedLessonsCount(courseId: String): Int = lessonList.count { it.courseId == courseId && it.isCompleted }
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

    @Test
    fun enrollInCourse_updatesEnrollmentStatus() = runTest {
        val fakeDao = FakeCourseDao()
        val repository = CourseRepositoryImpl(fakeDao, FakeLessonDao())
        
        val result = repository.enrollInCourse("1")

        assertEquals(com.rtiqa.core.domain.result.RtiqaResult.Success(Unit), result)
        val course = repository.getCourseById("1").first()
        assertNotNull(course)
        assertEquals(true, course?.isEnrolled)
    }

    @Test
    fun toggleBookmark_updatesBookmarkStatus() = runTest {
        val fakeDao = FakeCourseDao()
        val repository = CourseRepositoryImpl(fakeDao, FakeLessonDao())
        
        repository.toggleBookmark("1", true)

        val course = repository.getCourseById("1").first()
        assertEquals(true, course?.isBookmarked)
    }

    @Test
    fun getLessonById_returnsCorrespondingLesson() = runTest {
        val repository = CourseRepositoryImpl(FakeCourseDao(), FakeLessonDao())
        val lesson = repository.getLessonById("l1").first()
        assertNotNull(lesson)
        assertEquals("Lesson 1", lesson?.title)
    }

    @Test
    fun getNextLesson_returnsNextLessonInOrder() = runTest {
        val repository = CourseRepositoryImpl(FakeCourseDao(), FakeLessonDao())
        val nextLesson = repository.getNextLesson("c1", "l1").first()
        assertNotNull(nextLesson)
        assertEquals("l2", nextLesson?.id)
        assertEquals("Lesson 2", nextLesson?.title)
    }

    @Test
    fun updateLessonProgress_returnsSuccess() = runTest {
        val repository = CourseRepositoryImpl(FakeCourseDao(), FakeLessonDao())
        val result = repository.updateLessonProgress("l1", "c1", 0.8f)
        assertEquals(com.rtiqa.core.domain.result.RtiqaResult.Success(Unit), result)
    }
}
