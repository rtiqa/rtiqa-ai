package com.rtiqa.feature.courses

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.domain.model.PageRequest
import com.rtiqa.core.domain.model.PagedData
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.repository.CourseRepositoryContract
import com.rtiqa.core.domain.repository.DownloadManagerContract
import com.rtiqa.core.domain.repository.UserRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoursesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private class FakeCourseRepository : CourseRepositoryContract {
        val requests = mutableListOf<PageRequest>()
        var pages: (PageRequest) -> PagedData<Course> = { PagedData(emptyList(), it.page, 1, 0, false) }
        var syncResult: RtiqaResult<Unit> = RtiqaResult.Success(Unit)
        var bookmarkResult: RtiqaResult<Unit> = RtiqaResult.Success(Unit)
        var enrollResult: RtiqaResult<Unit> = RtiqaResult.Success(Unit)
        var completeResult: RtiqaResult<Unit> = RtiqaResult.Success(Unit)
        var completionCalls = 0
        val course = MutableStateFlow<Course?>(null)
        val lessons = MutableStateFlow<List<Lesson>>(emptyList())

        override fun getCourses(): Flow<List<Course>> = flowOf(emptyList())
        override fun getCoursesForSchool(schoolId: String): Flow<List<Course>> = flowOf(emptyList())
        override fun getCourseById(courseId: String): Flow<Course?> = course
        override fun getLessonsForCourse(courseId: String): Flow<List<Lesson>> = lessons
        override fun getLessonById(lessonId: String): Flow<Lesson?> = flowOf(null)
        override fun getNextLesson(courseId: String, currentLessonId: String): Flow<Lesson?> = flowOf(null)
        override fun getPagedCourses(request: PageRequest): Flow<PagedData<Course>> {
            requests += request
            return flowOf(pages(request))
        }
        override suspend fun searchCourses(query: String): List<Course> = emptyList()
        override suspend fun markLessonCompleted(lessonId: String, courseId: String): RtiqaResult<Unit> {
            completionCalls++
            return completeResult
        }
        override suspend fun updateLessonProgress(lessonId: String, courseId: String, progressPercent: Float) = RtiqaResult.Success(Unit)
        override suspend fun saveCourse(course: Course) = RtiqaResult.Success(Unit)
        override suspend fun deleteCourse(courseId: String) = RtiqaResult.Success(Unit)
        override suspend fun saveLesson(lesson: Lesson) = RtiqaResult.Success(Unit)
        override suspend fun enrollInCourse(courseId: String) = enrollResult
        override suspend fun toggleBookmark(courseId: String, isBookmarked: Boolean) = bookmarkResult
        override suspend fun toggleCourseDownload(courseId: String, isDownloaded: Boolean) = RtiqaResult.Success(Unit)
        override suspend fun syncCourses() = syncResult
    }

    private class FakeDownloadManager : DownloadManagerContract {
        override fun observeCourseDownloadProgress(courseId: String): Flow<Float> = flowOf(0f)
        override suspend fun downloadCourse(courseId: String) = RtiqaResult.Success(Unit)
        override suspend fun deleteCourseDownload(courseId: String) = RtiqaResult.Success(Unit)
    }

    private class FakeUserRepository : UserRepositoryContract {
        override fun getUserProfile(): Flow<UserProfile?> = flowOf(null)
        override suspend fun updateUserProfile(profile: UserProfile) = RtiqaResult.Success(Unit)
        override suspend fun addXp(amount: Int) = RtiqaResult.Success(Unit)
        override suspend fun incrementStreak() = RtiqaResult.Success(Unit)
    }

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun listViewModel(repository: FakeCourseRepository) = CoursesListViewModel(
        GetPagedCoursesUseCase(repository), SearchCoursesUseCase(repository),
        DownloadCourseUseCase(FakeDownloadManager()), ToggleBookmarkUseCase(repository),
        SyncCoursesUseCase(repository)
    )

    private fun detailViewModel(repository: FakeCourseRepository) = CourseDetailViewModel(
        GetCourseDetailUseCase(repository), GetLessonsForCourseUseCase(repository),
        DownloadCourseUseCase(FakeDownloadManager()), EnrollCourseUseCase(repository),
        ToggleBookmarkUseCase(repository), CompleteLessonUseCase(repository, FakeUserRepository())
    )

    private fun course(id: String) = Course(id, id, "", "", 1, 1)
    private fun lesson(id: String, completed: Boolean = false) = Lesson(id, "course", id, "", 1, completed)

    @Test fun syncSuccess_showsSuccess() = runTest {
        val vm = listViewModel(FakeCourseRepository())
        val events = mutableListOf<CoursesListUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiEvent.collect { events += it } }
        advanceUntilIdle(); vm.onAction(CoursesListUiAction.SyncRequested); advanceUntilIdle()
        assertTrue(events.any { it is CoursesListUiEvent.ShowMessage && it.message.contains("بنجاح") })
    }

    @Test fun syncFailure_doesNotShowSuccess_andSurfacesError() = runTest {
        val repo = FakeCourseRepository().apply { syncResult = RtiqaResult.Error(RtiqaError.NetworkError("sync failed")) }
        val vm = listViewModel(repo); val events = mutableListOf<CoursesListUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiEvent.collect { events += it } }
        advanceUntilIdle(); vm.onAction(CoursesListUiAction.SyncRequested); advanceUntilIdle()
        assertEquals("sync failed", vm.currentState.errorMessage)
        assertFalse(events.filterIsInstance<CoursesListUiEvent.ShowMessage>().any { it.message.contains("بنجاح") })
    }

    @Test fun bookmarkFailure_surfacesError() = runTest {
        val repo = FakeCourseRepository().apply { bookmarkResult = RtiqaResult.Error(RtiqaError.DatabaseError("bookmark failed")) }
        val vm = listViewModel(repo); val events = mutableListOf<CoursesListUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiEvent.collect { events += it } }
        advanceUntilIdle(); vm.onAction(CoursesListUiAction.BookmarkToggled("c", true)); advanceUntilIdle()
        assertTrue(events.any { it is CoursesListUiEvent.ShowMessage && it.message == "bookmark failed" })
    }

    @Test fun courseClick_emitsExactlyOneNavigationEvent() = runTest {
        val vm = listViewModel(FakeCourseRepository()); val events = mutableListOf<CoursesListUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiEvent.collect { events += it } }
        advanceUntilIdle(); vm.onAction(CoursesListUiAction.CourseClicked("c")); advanceUntilIdle()
        assertEquals(1, events.filterIsInstance<CoursesListUiEvent.NavigateToCourseDetail>().size)
    }

    @Test fun searchAndCategoryChanges_resetToPageOne() = runTest {
        val repo = FakeCourseRepository(); val vm = listViewModel(repo); advanceUntilIdle()
        vm.onAction(CoursesListUiAction.SearchQueryChanged("query")); advanceTimeBy(301); advanceUntilIdle()
        assertEquals(1, repo.requests.last().page)
        vm.onAction(CoursesListUiAction.CategoryFilterSelected("AI")); advanceUntilIdle()
        assertEquals(1, repo.requests.last().page)
    }

    @Test fun nextPage_doesNotDuplicateItems() = runTest {
        val repo = FakeCourseRepository().apply {
            pages = { if (it.page == 1) PagedData(listOf(course("a")), 1, 2, 2, true) else PagedData(listOf(course("a"), course("b")), 2, 2, 2, false) }
        }
        val vm = listViewModel(repo); advanceUntilIdle(); vm.onAction(CoursesListUiAction.LoadNextPage); advanceUntilIdle()
        assertEquals(listOf("a", "b"), vm.currentState.courses.map { it.id })
    }

    @Test fun nextPage_doesNotAdvanceWhenNoNextPage() = runTest {
        val repo = FakeCourseRepository(); val vm = listViewModel(repo); advanceUntilIdle()
        vm.onAction(CoursesListUiAction.LoadNextPage); advanceUntilIdle()
        assertEquals(1, vm.currentState.currentPage); assertEquals(1, repo.requests.size)
    }

    @Test fun detailActions_surfaceResults_andNavigateOnce() = runTest {
        val repo = FakeCourseRepository().apply {
            course.value = course("course")
            lessons.value = listOf(lesson("lesson"))
            enrollResult = RtiqaResult.Error(RtiqaError.NetworkError("enroll failed"))
            bookmarkResult = RtiqaResult.Error(RtiqaError.DatabaseError("bookmark failed"))
            completeResult = RtiqaResult.Error(RtiqaError.NetworkError("complete failed"))
        }
        val vm = detailViewModel(repo); val events = mutableListOf<CourseDetailUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiEvent.collect { events += it } }
        vm.onAction(CourseDetailUiAction.LoadCourseDetail("course")); advanceUntilIdle()
        vm.onAction(CourseDetailUiAction.EnrollClicked); vm.onAction(CourseDetailUiAction.BookmarkToggled)
        vm.onAction(CourseDetailUiAction.MarkLessonCompleted("lesson"))
        vm.onAction(CourseDetailUiAction.LessonClicked("lesson")); vm.onAction(CourseDetailUiAction.StartQuizClicked); advanceUntilIdle()
        val messages = events.filterIsInstance<CourseDetailUiEvent.ShowToast>().map { it.message }
        assertTrue(messages.containsAll(listOf("enroll failed", "bookmark failed", "complete failed")))
        assertFalse(messages.any { it.contains("إكمال الدرس") })
        assertEquals(1, events.filterIsInstance<CourseDetailUiEvent.NavigateToLessonViewer>().size)
        assertEquals(1, events.filterIsInstance<CourseDetailUiEvent.NavigateToQuiz>().size)
    }

    @Test fun enrollSuccess_showsSuccess() = runTest {
        val repo = FakeCourseRepository().apply { course.value = course("course") }
        val vm = detailViewModel(repo)
        val events = mutableListOf<CourseDetailUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiEvent.collect { events += it } }
        vm.onAction(CourseDetailUiAction.LoadCourseDetail("course")); advanceUntilIdle()
        vm.onAction(CourseDetailUiAction.EnrollClicked); advanceUntilIdle()
        assertTrue(events.any { it is CourseDetailUiEvent.ShowToast && it.message.contains("بنجاح") })
    }

    @Test fun completedLesson_doesNotSubmitDuplicateCompletion() = runTest {
        val repo = FakeCourseRepository().apply { course.value = course("course"); lessons.value = listOf(lesson("lesson", true)) }
        val vm = detailViewModel(repo); vm.onAction(CourseDetailUiAction.LoadCourseDetail("course")); advanceUntilIdle()
        vm.onAction(CourseDetailUiAction.MarkLessonCompleted("lesson")); advanceUntilIdle()
        assertEquals(0, repo.completionCalls)
    }
}
