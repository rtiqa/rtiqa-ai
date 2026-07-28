package com.rtiqa.core.domain.model

/**
 * Domain entity representing an educational course.
 */
data class Course(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val totalLessons: Int,
    val durationMinutes: Int,
    val iconUrl: String? = null,
    val isDownloaded: Boolean = false,
    val progressPercent: Float = 0f
) {
    /**
     * Business rule: Checks whether the course is 100% completed.
     */
    fun isFullyCompleted(): Boolean = progressPercent >= 1.0f

    /**
     * Business rule: Returns formatted display string for estimated duration.
     */
    fun getFormattedDuration(): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}

/**
 * Domain entity representing a lesson within a course.
 */
data class Lesson(
    val id: String,
    val courseId: String,
    val title: String,
    val content: String,
    val order: Int,
    val isCompleted: Boolean = false,
    val audioUrl: String? = null
)

/**
 * Domain entity representing an assessment quiz.
 */
data class Quiz(
    val id: String,
    val courseId: String,
    val title: String,
    val questions: List<Question>,
    val passingScorePercent: Int = 70
) {
    /**
     * Calculates the score percentage for a list of submitted user answer indices.
     */
    fun calculateScorePercent(userAnswers: Map<String, Int>): Int {
        if (questions.isEmpty()) return 0
        var correctCount = 0
        questions.forEach { question ->
            val userAnswer = userAnswers[question.id]
            if (userAnswer != null && userAnswer == question.correctAnswerIndex) {
                correctCount++
            }
        }
        return ((correctCount.toFloat() / questions.size) * 100).toInt()
    }

    /**
     * Evaluates if score percent meets or exceeds passing score threshold.
     */
    fun isPassed(scorePercent: Int): Boolean = scorePercent >= passingScorePercent
}

/**
 * Domain entity representing a single question in a quiz.
 */
data class Question(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String? = null
)
