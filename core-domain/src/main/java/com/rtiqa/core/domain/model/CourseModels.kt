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
    val progressPercent: Float = 0f,
    val isEnrolled: Boolean = false,
    val isBookmarked: Boolean = false,
    val rating: Float = 4.8f,
    val level: String = "مبتدئ",
    val titleAr: String? = null,
    val descriptionAr: String? = null
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
    val passingScorePercent: Int = 70,
    val durationMinutes: Int = 10,
    val timeLimitSeconds: Int = 600,
    val titleAr: String? = null
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
    val explanation: String? = null,
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val textAr: String? = null,
    val optionsAr: List<String>? = null,
    val explanationAr: String? = null,
    val hint: String? = null,
    val hintAr: String? = null,
    val xpReward: Int = 10
)

/**
 * Domain entity representing a user's completed quiz attempt result.
 */
data class QuizResult(
    val id: String,
    val quizId: String,
    val courseId: String,
    val studentId: String,
    val score: Int,
    val totalQuestions: Int,
    val scorePercent: Int,
    val isPassed: Boolean,
    val completedAt: Long = System.currentTimeMillis()
)
