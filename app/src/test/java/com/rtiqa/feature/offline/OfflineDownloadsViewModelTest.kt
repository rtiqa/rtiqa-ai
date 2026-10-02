package com.rtiqa.feature.offline

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.*
import com.rtiqa.core.domain.repository.*
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
class OfflineDownloadsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val courses = MutableStateFlow<List<Course>>(emptyList())
    private val repository = FakeCourseRepository(courses)
    private val sync = FakeOfflineSync()
    private val downloads = FakeDownloadManager()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = OfflineDownloadsViewModel(
        GetCoursesUseCase(repository), SyncOfflineDataUseCase(sync),
        ObserveSyncStatusUseCase(sync), DeleteCourseDownloadUseCase(downloads)
    )

    @Test fun downloadedCourses_comeFromCoreGetCoursesUseCase() = runTest {
        courses.value = listOf(course("core", true), course("not-downloaded", false))
        val vm = viewModel(); advanceUntilIdle()
        assertEquals(listOf("core"), vm.currentState.downloadedCourses.map { it.id })
    }

    @Test fun downloadedCourseClick_emitsExactlyOneNavigationEvent() = runTest {
        val vm = viewModel(); val events = mutableListOf<OfflineDownloadsUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiEvent.collect { events += it } }
        vm.onAction(OfflineDownloadsUiAction.CourseClicked("core")); advanceUntilIdle()
        assertEquals(listOf(OfflineDownloadsUiEvent.NavigateToCourseDetail("core")), events)
    }

    @Test fun removeDownloadFailure_surfacesError() = runTest {
        downloads.deleteResult = RtiqaResult.Error(RtiqaError.DatabaseError("remove failed"))
        val vm = viewModel(); val events = mutableListOf<OfflineDownloadsUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiEvent.collect { events += it } }
        vm.onAction(OfflineDownloadsUiAction.RemoveDownloadClicked("core")); advanceUntilIdle()
        assertEquals("remove failed", vm.currentState.errorMessage)
        assertTrue(events.any { it is OfflineDownloadsUiEvent.ShowToast && it.message == "remove failed" })
    }

    private fun course(id: String, downloaded: Boolean) = Course(id, id, "", "", 1, 10, isDownloaded = downloaded)

    private class FakeCourseRepository(private val source: Flow<List<Course>>) : CourseRepositoryContract {
        override fun getCourses() = source
        override fun getCoursesForSchool(schoolId: String) = flowOf(emptyList<Course>())
        override fun getCourseById(courseId: String) = flowOf<Course?>(null)
        override fun getLessonsForCourse(courseId: String) = flowOf(emptyList<Lesson>())
        override fun getLessonById(lessonId: String) = flowOf<Lesson?>(null)
        override fun getNextLesson(courseId: String, currentLessonId: String) = flowOf<Lesson?>(null)
        override fun getPagedCourses(request: PageRequest) = flowOf(PagedData<Course>(emptyList(), 1, 1, 0, false))
        override suspend fun searchCourses(query: String) = emptyList<Course>()
        override suspend fun markLessonCompleted(lessonId: String, courseId: String) = RtiqaResult.Success(Unit)
        override suspend fun updateLessonProgress(lessonId: String, courseId: String, progressPercent: Float) = RtiqaResult.Success(Unit)
        override suspend fun saveCourse(course: Course) = RtiqaResult.Success(Unit)
        override suspend fun deleteCourse(courseId: String) = RtiqaResult.Success(Unit)
        override suspend fun saveLesson(lesson: Lesson) = RtiqaResult.Success(Unit)
        override suspend fun enrollInCourse(courseId: String) = RtiqaResult.Success(Unit)
        override suspend fun toggleBookmark(courseId: String, isBookmarked: Boolean) = RtiqaResult.Success(Unit)
        override suspend fun toggleCourseDownload(courseId: String, isDownloaded: Boolean) = RtiqaResult.Success(Unit)
        override suspend fun syncCourses() = RtiqaResult.Success(Unit)
    }

    private class FakeOfflineSync : OfflineSyncContract {
        override suspend fun syncRemoteCourses() = RtiqaResult.Success(Unit)
        override suspend fun enqueueOfflineAction(actionType: String, payloadJson: String) = RtiqaResult.Success(Unit)
        override fun observePendingSyncCount() = flowOf(0)
    }

    private class FakeDownloadManager : DownloadManagerContract {
        var deleteResult: RtiqaResult<Unit> = RtiqaResult.Success(Unit)
        override fun observeCourseDownloadProgress(courseId: String) = flowOf(0f)
        override suspend fun downloadCourse(courseId: String) = RtiqaResult.Success(Unit)
        override suspend fun deleteCourseDownload(courseId: String) = deleteResult
    }
}
