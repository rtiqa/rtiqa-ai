package com.rtiqa.feature.courses

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.PageRequest
import com.rtiqa.core.domain.usecase.DownloadCourseUseCase
import com.rtiqa.core.domain.usecase.GetPagedCoursesUseCase
import com.rtiqa.core.domain.usecase.SearchCoursesUseCase
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
    val errorMessage: String? = null
) : ViewUiState

sealed interface CoursesListUiAction : ViewUiAction {
    data class CategoryFilterSelected(val category: String?) : CoursesListUiAction
    data class SearchQueryChanged(val query: String) : CoursesListUiAction
    data class DownloadCourseRequested(val courseId: String) : CoursesListUiAction
    data class CourseClicked(val courseId: String) : CoursesListUiAction
    object LoadNextPage : CoursesListUiAction
}

sealed interface CoursesListUiEvent : ViewUiEvent {
    data class NavigateToCourseDetail(val courseId: String) : CoursesListUiEvent
    data class ShowMessage(val message: String) : CoursesListUiEvent
}

class CoursesListViewModel(
    private val getPagedCoursesUseCase: GetPagedCoursesUseCase,
    private val searchCoursesUseCase: SearchCoursesUseCase,
    private val downloadCourseUseCase: DownloadCourseUseCase
) : BaseViewModel<CoursesListUiState, CoursesListUiAction, CoursesListUiEvent>(CoursesListUiState()) {

    private var searchJob: Job? = null

    init {
        loadCourses()
    }

    private fun loadCourses() {
        setState { copy(isLoading = true) }
        val request = PageRequest(
            page = currentState.currentPage,
            pageSize = 20,
            searchQuery = currentState.searchQuery.ifBlank { null },
            filterCategory = currentState.selectedCategory
        )
        getPagedCoursesUseCase(request)
            .onEach { paged ->
                setState {
                    copy(
                        courses = paged.items,
                        totalCount = paged.totalItems,
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
            is CoursesListUiAction.CourseClicked -> sendEvent(CoursesListUiEvent.NavigateToCourseDetail(action.courseId))
            is CoursesListUiAction.LoadNextPage -> {
                setState { copy(currentPage = currentPage + 1) }
                loadCourses()
            }
        }
    }

    private fun downloadCourse(courseId: String) {
        viewModelScope.launch {
            when (val result = downloadCourseUseCase(courseId)) {
                is com.rtiqa.core.domain.result.RtiqaResult.Success -> {
                    sendEvent(CoursesListUiEvent.ShowMessage("Course package download started!"))
                }
                is com.rtiqa.core.domain.result.RtiqaResult.Error -> {
                    sendEvent(CoursesListUiEvent.ShowMessage(result.error.message))
                }
                is com.rtiqa.core.domain.result.RtiqaResult.Loading -> {
                    sendEvent(CoursesListUiEvent.ShowMessage("Download initializing..."))
                }
            }
        }
    }
}
