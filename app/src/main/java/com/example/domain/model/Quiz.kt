package com.example.domain.model

data class Quiz(
    val id: String,
    val lessonId: String,
    val courseId: String,
    val title: String,
    val titleAr: String,
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val id: String,
    val questionText: String,
    val questionTextAr: String,
    val options: List<String>,
    val optionsAr: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val explanationAr: String,
    val hint: String,
    val hintAr: String,
    val xpReward: Int = 50
)
