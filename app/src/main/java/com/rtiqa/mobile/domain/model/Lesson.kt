package com.rtiqa.mobile.domain.model

data class Lesson(
    val id: String,
    val courseId: String,
    val title: String,
    val titleAr: String,
    val durationMinutes: Int,
    val order: Int,
    val videoUrl: String,
    val audioUrl: String,
    val contentMarkdown: String,
    val contentMarkdownAr: String,
    val isCompleted: Boolean = false,
    val isDownloaded: Boolean = false,
    val hasQuiz: Boolean = false,
    val quizId: String? = null,
    val isQuizPassed: Boolean = false
)
