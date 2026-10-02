package com.rtiqa.feature.courses

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.PageRequest
import com.rtiqa.core.domain.usecase.DownloadCourseUseCase
import com.rtiqa.core.domain.usecase.GetPagedCoursesUseCase
import com.rtiqa.core.domain.usecase.SearchCoursesUseCase
import com.rtiqa.core.domain.usecase.SyncCoursesUseCase
import com.rtiqa.core.domain.usecase.ToggleBookmarkUseCase
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.ui.base.BaseViewModel
import com.rtiqa.core.ui.base.ViewUiAction
import com.rtiqa.core.ui.base.ViewUiEvent
import com.rtiqa.core.ui.base.ViewUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class CoursesListUiState(
    val courses: List<Course> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val totalCount: Int = 0,
    val currentPage: Int = 1,
    val hasNextPage: Boolean = false,
    val errorMessage: String? = null
) : ViewUiState

sealed interface CoursesListUiAction : ViewUiAction {
    data class CategoryFilterSelected(val category: String?) : CoursesListUiAction
    data class SearchQueryChanged(val query: String) : CoursesListUiAction
    data class DownloadCourseRequested(val courseId: String) : CoursesListUiAction
    data class BookmarkToggled(val courseId: String, val isBookmarked: Boolean) : CoursesListUiAction
    data class CourseClicked(val courseId: String) : CoursesListUiAction
    object SyncRequested : CoursesListUiAction
    object LoadNextPage : CoursesListUiAction
}

sealed interface CoursesListUiEvent : ViewUiEvent {
    data class NavigateToCourseDetail(val courseId: String) : CoursesListUiEvent
    data class ShowMessage(val message: String) : CoursesListUiEvent
}

class CoursesListViewModel(
    private val getPagedCoursesUseCase: GetPagedCoursesUseCase,
    private val searchCoursesUseCase: SearchCoursesUseCase,
    private val downloadCourseUseCase: DownloadCourseUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val syncCoursesUseCase: SyncCoursesUseCase
) : BaseViewModel<CoursesListUiState, CoursesListUiAction, CoursesListUiEvent>(CoursesListUiState()) {

    private var searchJob: Job? = null
    private var loadJob: Job? = null

    init {
        loadCourses()
    }

    private fun loadCourses() {
        loadJob?.cancel()
        setState { copy(isLoading = true) }
        val request = PageRequest(
            page = currentState.currentPage,
            pageSize = 20,
            searchQuery = currentState.searchQuery.ifBlank { null },
            filterCategory = currentState.selectedCategory
        )
        loadJob = getPagedCoursesUseCase(request)
            .onEach { paged ->
                setState {
                    val updatedCourses = if (paged.page == 1) {
                        paged.items
                    } else {
                        (courses + paged.items).distinctBy { it.id }
                    }
                    copy(
                        courses = updatedCourses,
                        totalCount = paged.totalItems,
                        currentPage = paged.page,
                        hasNextPage = paged.hasNextPage,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: CoursesListUiAction) {
        when (action) {
            is CoursesListUiAction.CategoryFilterSelected -> {
                setState { copy(selectedCategory = action.category, currentPage = 1) }
                loadCourses()
            }
            is CoursesListUiAction.SearchQueryChanged -> {
                setState { copy(searchQuery = action.query, currentPage = 1) }
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(300)
                    loadCourses()
                }
            }
            is CoursesListUiAction.DownloadCourseRequested -> downloadCourse(action.courseId)
            is CoursesListUiAction.BookmarkToggled -> toggleBookmark(action.courseId, action.isBookmarked)
            is CoursesListUiAction.CourseClicked -> sendEvent(CoursesListUiEvent.NavigateToCourseDetail(action.courseId))
            is CoursesListUiAction.SyncRequested -> syncCourses()
            is CoursesListUiAction.LoadNextPage -> {
                if (currentState.hasNextPage && !currentState.isLoading) {
                    setState { copy(currentPage = currentPage + 1) }
                    loadCourses()
                }
            }
        }
    }

    private fun toggleBookmark(courseId: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            when (val result = toggleBookmarkUseCase(courseId, isBookmarked)) {
                is RtiqaResult.Success -> Unit
                is RtiqaResult.Error -> {
                    setState { copy(errorMessage = result.error.message) }
                    sendEvent(CoursesListUiEvent.ShowMessage(result.error.message))
                }
                is RtiqaResult.Loading -> Unit
            }
        }
    }

    private fun syncCourses() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            when (val result = syncCoursesUseCase()) {
                is RtiqaResult.Success -> {
                    setState { copy(isLoading = false, errorMessage = null) }
                    sendEvent(CoursesListUiEvent.ShowMessage("تمت مزامنة المقررات بنجاح"))
                }
                is RtiqaResult.Error -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                    sendEvent(CoursesListUiEvent.ShowMessage(result.error.message))
                }
                is RtiqaResult.Loading -> setState { copy(isLoading = true) }
            }
        }
    }

    private fun downloadCourse(courseId: String) {
        viewModelScope.launch {
            when (val result = downloadCourseUseCase(courseId)) {
                is com.rtiqa.core.domain.result.RtiqaResult.Success -> {
                    sendEvent(CoursesListUiEvent.ShowMessage("بدأ تحميل حزمة الدورة بنجاح!"))
                }
                is com.rtiqa.core.domain.result.RtiqaResult.Error -> {
                    sendEvent(CoursesListUiEvent.ShowMessage(result.error.message))
                }
                is com.rtiqa.core.domain.result.RtiqaResult.Loading -> {
                    sendEvent(CoursesListUiEvent.ShowMessage("جاري بدء التحميل..."))
                }
            }
        }
    }
}
