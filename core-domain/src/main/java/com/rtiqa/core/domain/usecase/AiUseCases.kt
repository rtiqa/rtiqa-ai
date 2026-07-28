package com.rtiqa.core.domain.usecase

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.AiInsight
import com.rtiqa.core.domain.repository.AiRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.flow.Flow

/**
 * Use case to query AI tutor with an educational question and optional context.
 */
class AskAiTutorUseCase(
    private val aiRepository: AiRepositoryContract
) {
    suspend operator fun invoke(question: String, courseContext: String? = null): RtiqaResult<AiInsight> {
        val trimmed = question.trim()
        if (trimmed.length < 3) {
            return RtiqaResult.Error(RtiqaError.ValidationError(listOf("Question is too short. Minimum 3 characters required.")))
        }
        return aiRepository.askAiTutor(trimmed, courseContext)
    }
}

/**
 * Use case to generate an AI educational summary for a course or topic.
 */
class GenerateAiSummaryUseCase(
    private val aiRepository: AiRepositoryContract
) {
    suspend operator fun invoke(prompt: String): RtiqaResult<String> {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) {
            return RtiqaResult.Error(RtiqaError.ValidationError(listOf("Prompt cannot be empty.")))
        }
        return try {
            val summary = aiRepository.generateEducationalSummary(trimmed)
            RtiqaResult.Success(summary)
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.AiServiceError("Failed to generate AI summary", e))
        }
    }
}

/**
 * Use case to observe historical AI interactions.
 */
class GetAiHistoryUseCase(
    private val aiRepository: AiRepositoryContract
) {
    operator fun invoke(): Flow<List<AiInsight>> = aiRepository.getAiHistory()
}
