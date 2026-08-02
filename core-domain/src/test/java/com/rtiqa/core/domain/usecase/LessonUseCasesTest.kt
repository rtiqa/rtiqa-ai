package com.rtiqa.core.domain.usecase

import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.domain.model.PageRequest
import com.rtiqa.core.domain.model.PagedData
import com.rtiqa.core.domain.repository.CourseRepositoryContract
import com.rtiqa.core.domain.repository.UserRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeCourseRepository : CourseRepositoryContract {
    val lessonsMap = mutableMapOf<String, Lesson>()
    var savedProgress: Float = 0f
    var markedCompletedId: String? = null

    override fun getCourses(): Flow<List<Course>> = flowOf(emptyList())
    override fun getCoursesForSchool(schoolId: String): Flow<List<Course>> = flowOf(emptyList())
    override fun getCourseById(courseId: String): Flow<Course?> = flowOf(null)
    override fun getLessonsForCourse(courseId: String): Flow<List<Lesson>> = flowOf(lessonsMap.values.filter { it.courseId == courseId })
    
    override fun getLessonById(lessonId: String): Flow<Lesson?> = flowOf(lessonsMap[lessonId])
    
    override fun getNextLesson(courseId: String, currentLessonId: String): Flow<Lesson?> {
        val current = lessonsMap[currentLessonId] ?: return flowOf(null)
        val next = lessonsMap.values
            .filter { it.courseId == courseId && it.order > current.order }
            .minByOrNull { it.order }
        return flowOf(next)
    }

    override fun getPagedCourses(request: PageRequest): Flow<PagedData<Course>> = flowOf(PagedData(emptyList(), 1, 0, 0, false))
    override suspend fun searchCourses(query: String): List<Course> = emptyList()

    override suspend fun markLessonCompleted(lessonId: String, courseId: String): RtiqaResult<Unit> {
        markedCompletedId = lessonId
        val existing = lessonsMap[lessonId]
        if (existing != null) {
            lessonsMap[lessonId] = existing.copy(isCompleted = true)
        }
        return RtiqaResult.Success(Unit)
    }

    override suspend fun updateLessonProgress(lessonId: String, courseId: String, progressPercent: Float): RtiqaResult<Unit> {
        savedProgress = progressPercent
        return RtiqaResult.Success(Unit)
    }

    override suspend fun saveCourse(course: Course): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
    override suspend fun deleteCourse(courseId: String): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
    override suspend fun saveLesson(lesson: Lesson): RtiqaResult<Unit> {
        lessonsMap[lesson.id] = lesson
        return RtiqaResult.Success(Unit)
    }
    override suspend fun enrollInCourse(courseId: String): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
    override suspend fun toggleBookmark(courseId: String, isBookmarked: Boolean): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
    override suspend fun toggleCourseDownload(courseId: String, isDownloaded: Boolean): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
    override suspend fun syncCourses(): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
}

class FakeUserRepository : UserRepositoryContract {
    var xpAdded: Int = 0
    override fun getUserProfile(): Flow<com.rtiqa.core.domain.model.UserProfile?> = flowOf(null)
    override suspend fun updateUserProfile(profile: com.rtiqa.core.domain.model.UserProfile): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
    override suspend fun addXp(amount: Int): RtiqaResult<Unit> {
        xpAdded += amount
        return RtiqaResult.Success(Unit)
    }
    override suspend fun incrementStreak(): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
}

class LessonUseCasesTest {

    private lateinit var courseRepo: FakeCourseRepository
    private lateinit var userRepo: FakeUserRepository
    private lateinit var getLessonDetailUseCase: GetLessonDetailUseCase
    private lateinit var getNextLessonUseCase: GetNextLessonUseCase
    private lateinit var saveLessonProgressUseCase: SaveLessonProgressUseCase
    private lateinit var completeLessonUseCase: CompleteLessonUseCase

    @Before
    fun setUp() {
        courseRepo = FakeCourseRepository()
        userRepo = FakeUserRepository()
        getLessonDetailUseCase = GetLessonDetailUseCase(courseRepo)
        getNextLessonUseCase = GetNextLessonUseCase(courseRepo)
        saveLessonProgressUseCase = SaveLessonProgressUseCase(courseRepo)
        completeLessonUseCase = CompleteLessonUseCase(courseRepo, userRepo)

        courseRepo.lessonsMap["l1"] = Lesson("l1", "c1", "Lesson 1", "Content 1", 1, false)
        courseRepo.lessonsMap["l2"] = Lesson("l2", "c1", "Lesson 2", "Content 2", 2, false)
    }

    @Test
    fun getLessonDetail_returnsCorrectLesson() = runBlocking {
        val result = getLessonDetailUseCase("l1").first()
        assertNotNull(result)
        assertEquals("Lesson 1", result?.title)
    }

    @Test
    fun getNextLesson_returnsSequentialLesson() = runBlocking {
        val next = getNextLessonUseCase("c1", "l1").first()
        assertNotNull(next)
        assertEquals("l2", next?.id)
        assertEquals("Lesson 2", next?.title)
    }

    @Test
    fun saveLessonProgress_savesClampedValue() = runBlocking {
        val result = saveLessonProgressUseCase("l1", "c1", 0.75f)
        assertTrue(result is RtiqaResult.Success)
        assertEquals(0.75f, courseRepo.savedProgress, 0.001f)
    }

    @Test
    fun completeLesson_marksCompletedAndRewardsXp() = runBlocking {
        val result = completeLessonUseCase("l1", "c1")
        assertTrue(result is RtiqaResult.Success)
        assertEquals("l1", courseRepo.markedCompletedId)
        assertEquals(25, userRepo.xpAdded)
    }
}
