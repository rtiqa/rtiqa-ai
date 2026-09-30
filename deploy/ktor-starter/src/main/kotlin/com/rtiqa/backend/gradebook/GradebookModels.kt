package com.rtiqa.backend.gradebook

import kotlinx.serialization.Serializable

@Serializable
data class GradebookStudentDto(
    val studentId: String,
    val displayName: String,
    val studentNumber: String? = null
)

@Serializable
data class GradebookAssessmentDto(
    val assessmentId: String,
    val title: String,
    val maxScore: Double,
    val weight: Double? = null
)

@Serializable
data class GradebookScoreDto(
    val studentId: String,
    val assessmentId: String,
    val score: Double? = null
)

@Serializable
data class ClassGradebookDto(
    val classId: String,
    val students: List<GradebookStudentDto>,
    val assessments: List<GradebookAssessmentDto>,
    val scores: List<GradebookScoreDto>
)

sealed class GetClassGradebookResult {
    data class Success(
        val gradebook: ClassGradebookDto
    ) : GetClassGradebookResult()

    object ClassNotFound : GetClassGradebookResult()
}
