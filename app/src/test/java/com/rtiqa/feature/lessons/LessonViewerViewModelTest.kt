package com.rtiqa.feature.lessons

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.*
import com.rtiqa.core.domain.repository.CourseRepositoryContract
import com.rtiqa.core.domain.repository.UserRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonViewerViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeCourseRepository()
    private val users = FakeUserRepository()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = LessonViewerViewModel(
        CompleteLessonUseCase(repository, users), GetLessonDetailUseCase(repository),
        GetNextLessonUseCase(repository), SaveLessonProgressUseCase(repository),
        GetLessonsForCourseUseCase(repository)
    )

    @Test fun missingLesson_doesNotCreateFallbackLesson_andSurfacesHonestError() = runTest {
        val vm = viewModel(); vm.onAction(LessonViewerUiAction.InitializeLesson("missing", "c1")); advanceUntilIdle()
        assertNull(vm.currentState.lesson)
        assertEquals("Lesson unavailable.", vm.currentState.errorMessage)
    }

    @Test fun saveProgress_clampsToZeroAndOne() = runTest {
        repository.lessons.value = listOf(lesson("l1", 1))
        val vm = viewModel(); vm.onAction(LessonViewerUiAction.InitializeLesson("l1", "c1")); advanceUntilIdle()
        vm.onAction(LessonViewerUiAction.SaveProgress(4f)); advanceUntilIdle(); assertEquals(1f, repository.savedProgress)
        vm.onAction(LessonViewerUiAction.SaveProgress(-2f)); advanceUntilIdle(); assertEquals(0f, repository.savedProgress)
    }

    @Test fun saveProgressFailure_surfacesError() = runTest {
        repository.lessons.value = listOf(lesson("l1", 1)); repository.progressResult = RtiqaResult.Error(RtiqaError.DatabaseError("save failed"))
        val vm = viewModel(); vm.onAction(LessonViewerUiAction.InitializeLesson("l1", "c1")); advanceUntilIdle()
        vm.onAction(LessonViewerUiAction.SaveProgress(.5f)); advanceUntilIdle()
        assertEquals("save failed", vm.currentState.errorMessage)
    }

    @Test fun completionFailure_doesNotMarkUiCompleted() = runTest {
        repository.lessons.value = listOf(lesson("l1", 1)); repository.completeResult = RtiqaResult.Error(RtiqaError.NetworkError("complete failed"))
        val vm = viewModel(); vm.onAction(LessonViewerUiAction.InitializeLesson("l1", "c1")); advanceUntilIdle()
        vm.onAction(LessonViewerUiAction.MarkLessonCompleteClicked); advanceUntilIdle()
        assertFalse(vm.currentState.isCompleted); assertEquals("complete failed", vm.currentState.errorMessage)
    }

    @Test fun nextAndPrevious_emitActualCourseAndLessonIds() = runTest {
        repository.lessons.value = listOf(lesson("l1", 1), lesson("l2", 2), lesson("l3", 3))
        val vm = viewModel(); val events = mutableListOf<LessonViewerUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiEvent.collect { events += it } }
        vm.onAction(LessonViewerUiAction.InitializeLesson("l2", "c1")); advanceUntilIdle()
        vm.onAction(LessonViewerUiAction.NextLessonClicked); vm.onAction(LessonViewerUiAction.PrevLessonClicked); advanceUntilIdle()
        assertTrue(events.contains(LessonViewerUiEvent.NavigateToNextLesson("c1", "l3")))
        assertTrue(events.contains(LessonViewerUiEvent.NavigateToPrevLesson("c1", "l1")))
    }

    private fun lesson(id: String, order: Int) = Lesson(id, "c1", id, "real content", order)

    private class FakeCourseRepository : CourseRepositoryContract {
        val lessons = MutableStateFlow<List<Lesson>>(emptyList())
        var savedProgress = -1f
        var progressResult: RtiqaResult<Unit> = RtiqaResult.Success(Unit)
        var completeResult: RtiqaResult<Unit> = RtiqaResult.Success(Unit)
        override fun getCourses() = flowOf(emptyList<Course>())
        override fun getCoursesForSchool(schoolId: String) = flowOf(emptyList<Course>())
        override fun getCourseById(courseId: String) = flowOf<Course?>(null)
        override fun getLessonsForCourse(courseId: String) = lessons.map { list -> list.filter { it.courseId == courseId } }
        override fun getLessonById(lessonId: String) = lessons.map { list -> list.find { it.id == lessonId } }
        override fun getNextLesson(courseId: String, currentLessonId: String) = lessons.map { list ->
            val current = list.find { it.id == currentLessonId }
            list.filter { it.courseId == courseId && current != null && it.order > current.order }.minByOrNull { it.order }
        }
        override fun getPagedCourses(request: PageRequest) = flowOf(PagedData<Course>(emptyList(), 1, 1, 0, false))
        override suspend fun searchCourses(query: String) = emptyList<Course>()
        override suspend fun markLessonCompleted(lessonId: String, courseId: String): RtiqaResult<Unit> = completeResult
        override suspend fun updateLessonProgress(lessonId: String, courseId: String, progressPercent: Float): RtiqaResult<Unit> { savedProgress = progressPercent; return progressResult }
        override suspend fun saveCourse(course: Course) = RtiqaResult.Success(Unit)
        override suspend fun deleteCourse(courseId: String) = RtiqaResult.Success(Unit)
        override suspend fun saveLesson(lesson: Lesson) = RtiqaResult.Success(Unit)
        override suspend fun enrollInCourse(courseId: String) = RtiqaResult.Success(Unit)
        override suspend fun toggleBookmark(courseId: String, isBookmarked: Boolean) = RtiqaResult.Success(Unit)
        override suspend fun toggleCourseDownload(courseId: String, isDownloaded: Boolean) = RtiqaResult.Success(Unit)
        override suspend fun syncCourses() = RtiqaResult.Success(Unit)
    }

    private class FakeUserRepository : UserRepositoryContract {
        override fun getUserProfile() = flowOf<UserProfile?>(UserProfile("u", "User", "u@example.com"))
        override suspend fun updateUserProfile(profile: UserProfile) = RtiqaResult.Success(Unit)
        override suspend fun addXp(amount: Int) = RtiqaResult.Success(Unit)
        override suspend fun incrementStreak() = RtiqaResult.Success(Unit)
    }
}
