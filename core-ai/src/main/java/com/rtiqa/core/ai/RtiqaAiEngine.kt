package com.rtiqa.core.ai

interface RtiqaAiEngine {
    suspend fun generateExplanation(concept: String): String
    suspend fun summarizeLesson(lessonTitle: String, content: String): String
    suspend fun generateQuizQuestions(topic: String, count: Int): String
}
