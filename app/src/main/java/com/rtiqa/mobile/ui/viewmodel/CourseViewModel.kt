package com.rtiqa.mobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rtiqa.mobile.data.local.RtiqaDatabase
import com.rtiqa.mobile.data.repository.CourseRepository
import com.rtiqa.mobile.domain.model.Course
import com.rtiqa.mobile.domain.model.CourseCategory
import com.rtiqa.mobile.domain.model.Lesson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = RtiqaDatabase.getDatabase(application)
    private val courseRepository = CourseRepository(
        database.courseDao(),
        database.lessonDao(),
        database.syncQueueDao()
    )

    private val _selectedCategory = MutableStateFlow(CourseCategory.ALL)
    val selectedCategory: StateFlow<CourseCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredCourses: StateFlow<List<Course>> = combine(
        courseRepository.allCourses,
        _selectedCategory,
        _searchQuery
    ) { courses, category, query ->
        courses.filter { course ->
            val matchesCategory = category == CourseCategory.ALL || course.category == category.displayName
            val matchesQuery = query.isEmpty() ||
                    course.title.contains(query, ignoreCase = true) ||
                    course.titleAr.contains(query) ||
                    course.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bookmarkedCourses: StateFlow<List<Course>> = courseRepository.bookmarkedCourses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val enrolledCourses: StateFlow<List<Course>> = courseRepository.enrolledCourses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val completedLessonsCount: StateFlow<Int> = courseRepository.completedLessonsCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val passedQuizzesCount: StateFlow<Int> = courseRepository.passedQuizzesCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val downloadedCourses: StateFlow<List<Course>> = courseRepository.downloadedCourses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val downloadedLessons: StateFlow<List<Lesson>> = courseRepository.downloadedLessons.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedCourseId = MutableStateFlow("c_ai_101")
    val selectedCourseId: StateFlow<String> = _selectedCourseId.asStateFlow()

    private val _selectedLessonId = MutableStateFlow("l_ai_1")
    val selectedLessonId: StateFlow<String> = _selectedLessonId.asStateFlow()

    fun selectCategory(category: CourseCategory) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCourse(courseId: String) {
        _selectedCourseId.value = courseId
    }

    fun selectLesson(lessonId: String) {
        _selectedLessonId.value = lessonId
    }

    fun getLessonsForCourse(courseId: String): StateFlow<List<Lesson>> {
        return courseRepository.getLessonsForCourse(courseId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun toggleEnrollment(courseId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            courseRepository.toggleEnrollment(courseId, !currentStatus)
        }
    }

    fun toggleBookmark(courseId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            courseRepository.toggleBookmark(courseId, !currentStatus)
        }
    }

    fun toggleCourseDownload(courseId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            courseRepository.toggleCourseDownload(courseId, !currentStatus)
        }
    }

    fun toggleLessonCompletion(lessonId: String, courseId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            courseRepository.toggleLessonCompletion(lessonId, courseId, !currentStatus)
        }
    }

    fun markLessonQuizPassed(lessonId: String, courseId: String, isPassed: Boolean = true) {
        viewModelScope.launch {
            courseRepository.updateQuizPassed(lessonId, courseId, isPassed)
        }
    }

    fun toggleLessonDownload(lessonId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            courseRepository.toggleLessonDownload(lessonId, !currentStatus)
        }
    }
}
