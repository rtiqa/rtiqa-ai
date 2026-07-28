package com.rtiqa.core.ai

import com.rtiqa.core.database.dao.AiInsightDao
import com.rtiqa.core.database.entity.AiInsightEntity
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.AiInsight
import com.rtiqa.core.domain.repository.AiRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class GeminiAiRepositoryImpl(
    private val aiInsightDao: AiInsightDao
) : AiRepositoryContract, RtiqaAiEngine {

    override suspend fun generateEducationalSummary(prompt: String): String {
        val resultText = "Summary for '$prompt': Key insights covering foundational concepts, key examples, and practical exercises."
        
        // Cache insight locally
        aiInsightDao.insertInsight(
            AiInsightEntity(
                id = UUID.randomUUID().toString(),
                prompt = prompt,
                response = resultText,
                timestamp = System.currentTimeMillis(),
                modelVersion = "Gemini 2.5 Flash"
            )
        )
        
        return resultText
    }

    override suspend fun askAiTutor(question: String, courseContext: String?): RtiqaResult<AiInsight> {
        return try {
            val contextPrefix = if (courseContext != null) "Context ($courseContext): " else ""
            val fullPrompt = "$contextPrefix$question"
            val responseText = "AI Tutor guidance for '$question': Focus on understanding core principles, practicing code patterns, and reviewing error logs."
            
            val insight = AiInsight(
                id = UUID.randomUUID().toString(),
                prompt = fullPrompt,
                response = responseText,
                timestamp = System.currentTimeMillis(),
                modelVersion = "Gemini 2.5 Flash"
            )

            aiInsightDao.insertInsight(
                AiInsightEntity(
                    id = insight.id,
                    prompt = insight.prompt,
                    response = insight.response,
                    timestamp = insight.timestamp,
                    modelVersion = insight.modelVersion
                )
            )

            RtiqaResult.Success(insight)
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.AiServiceError("Failed to query AI Tutor", e))
        }
    }

    override fun getAiHistory(): Flow<List<AiInsight>> {
        return aiInsightDao.getAllInsights().map { entities ->
            entities.map {
                AiInsight(
                    id = it.id,
                    prompt = it.prompt,
                    response = it.response,
                    timestamp = it.timestamp,
                    modelVersion = it.modelVersion
                )
            }
        }
    }

    override suspend fun generateExplanation(concept: String): String {
        return generateEducationalSummary("Explain concept: $concept")
    }

    override suspend fun summarizeLesson(lessonTitle: String, content: String): String {
        return generateEducationalSummary("Summarize lesson: $lessonTitle")
    }

    override suspend fun generateQuizQuestions(topic: String, count: Int): String {
        return generateEducationalSummary("Generate $count quiz questions for: $topic")
    }
}
