package com.rtiqa.core.domain.model

data class GradebookStudent(
    val studentId: String,
    val displayName: String,
    val studentNumber: String? = null
)

data class GradebookAssessment(
    val assessmentId: String,
    val title: String,
    val maxScore: Double,
    val weight: Double? = null
)

data class GradebookScore(
    val studentId: String,
    val assessmentId: String,
    val score: Double?
)

data class ClassGradebook(
    val classId: String,
    val students: List<GradebookStudent>,
    val assessments: List<GradebookAssessment>,
    val scores: List<GradebookScore>
)
