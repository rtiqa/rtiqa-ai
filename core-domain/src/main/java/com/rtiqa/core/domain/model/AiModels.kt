package com.rtiqa.core.domain.model

/**
 * Domain entity representing an AI interaction or insight.
 */
data class AiInsight(
    val id: String,
    val prompt: String,
    val response: String,
    val timestamp: Long,
    val modelVersion: String = "Gemini 2.5 Flash"
)
