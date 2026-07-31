package com.rtiqa.core.domain.model

enum class AssignmentType {
    WRITTEN, PROJECT, LAB
}

enum class SubmissionStatus {
    PENDING, SUBMITTED, GRADED
}

enum class QuestionType {
    MULTIPLE_CHOICE, MCQ, TRUE_FALSE, CODE
}

enum class AssessmentType {
    QUIZ, FINAL_EXAM, PRACTICAL_LAB
}

enum class DownloadStatus {
    NOT_DOWNLOADED, DOWNLOADING, COMPLETED, ERROR
}

data class CurriculumModule(
    val id: String,
    val courseId: String,
    val orgId: String,
    val title: String,
    val description: String,
    val orderIndex: Int,
    val durationHours: Int
)

data class AcademicLesson(
    val id: String,
    val moduleId: String,
    val courseId: String,
    val title: String,
    val content: String,
    val videoUrl: String? = null,
    val pdfAttachmentUrl: String? = null,
    val durationMinutes: Int = 15,
    val orderIndex: Int = 1,
    val isCompleted: Boolean = false
)

data class Assignment(
    val id: String,
    val lessonId: String,
    val courseId: String,
    val title: String,
    val prompt: String,
    val maxScore: Int = 100,
    val dueDate: String,
    val type: AssignmentType = AssignmentType.WRITTEN
)

data class AssignmentSubmission(
    val id: String,
    val assignmentId: String,
    val studentId: String,
    val submissionContent: String,
    val fileAttachmentUrl: String? = null,
    val status: SubmissionStatus = SubmissionStatus.SUBMITTED,
    val score: Int = 0,
    val feedback: String? = null,
    val submittedAt: Long = System.currentTimeMillis()
)

data class QuestionBankItem(
    val id: String,
    val courseId: String,
    val orgId: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswerIndex: Int,
    val explanation: String? = null,
    val difficultyLevel: String = "MEDIUM",
    val questionType: QuestionType = QuestionType.MCQ
)

data class Assessment(
    val id: String,
    val courseId: String,
    val orgId: String,
    val title: String,
    val type: AssessmentType = AssessmentType.QUIZ,
    val passingScore: Int = 70,
    val timeLimitMinutes: Int = 30,
    val totalQuestions: Int = 10
)

data class AssessmentAttempt(
    val id: String,
    val assessmentId: String,
    val studentId: String,
    val scorePercent: Int,
    val isPassed: Boolean,
    val autoGradedFeedback: String,
    val completedAt: Long = System.currentTimeMillis()
)

data class GradebookRecord(
    val id: String,
    val studentId: String,
    val courseId: String,
    val orgId: String,
    val courseName: String,
    val totalScore: Float,
    val gradeLetter: String,
    val gpaValue: Float,
    val isPassed: Boolean
)

data class StudentProgress(
    val id: String,
    val studentId: String,
    val courseId: String,
    val completedLessonsCount: Int,
    val totalLessonsCount: Int,
    val progressPercent: Float,
    val lastAccessedAt: Long = System.currentTimeMillis()
)

data class AchievementBadge(
    val id: String,
    val studentId: String,
    val badgeName: String,
    val badgeDescription: String,
    val iconName: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

data class LearningPath(
    val id: String,
    val orgId: String,
    val title: String,
    val description: String,
    val category: String,
    val courseIds: List<String>
)

data class Prerequisite(
    val id: String,
    val targetCourseId: String,
    val requiredCourseId: String,
    val requiredCourseTitle: String
)

data class SmartRecommendation(
    val id: String,
    val studentId: String,
    val courseId: String,
    val courseTitle: String,
    val reasonText: String,
    val confidenceScore: Float
)

data class OfflineContentDownload(
    val id: String,
    val courseId: String,
    val lessonId: String,
    val localFilePath: String,
    val status: DownloadStatus = DownloadStatus.COMPLETED,
    val progressPercent: Float = 1.0f
)
