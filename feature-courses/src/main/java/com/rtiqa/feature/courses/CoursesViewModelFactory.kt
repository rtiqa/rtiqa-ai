package com.rtiqa.feature.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rtiqa.core.data.di.AppDiContainer

class CoursesListViewModelFactory(
    private val appDiContainer: AppDiContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoursesListViewModel::class.java)) {
            val useCases = appDiContainer.domainUseCasesContainer
            return CoursesListViewModel(
                getPagedCoursesUseCase = useCases.getPagedCoursesUseCase,
                searchCoursesUseCase = useCases.searchCoursesUseCase,
                downloadCourseUseCase = useCases.downloadCourseUseCase,
                toggleBookmarkUseCase = useCases.toggleBookmarkUseCase,
                syncCoursesUseCase = useCases.syncCoursesUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}

class CourseDetailViewModelFactory(
    private val appDiContainer: AppDiContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseDetailViewModel::class.java)) {
            val useCases = appDiContainer.domainUseCasesContainer
            return CourseDetailViewModel(
                getCourseDetailUseCase = useCases.getCourseDetailUseCase,
                getLessonsForCourseUseCase = useCases.getLessonsForCourseUseCase,
                downloadCourseUseCase = useCases.downloadCourseUseCase,
                enrollCourseUseCase = useCases.enrollCourseUseCase,
                toggleBookmarkUseCase = useCases.toggleBookmarkUseCase,
                completeLessonUseCase = useCases.completeLessonUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}
