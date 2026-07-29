package com.rtiqa.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "curriculum_modules")
data class CurriculumModuleEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val orgId: String,
    val title: String,
    val description: String,
    val orderIndex: Int,
    val durationHours: Int
)

@Entity(tableName = "academic_lessons")
data class AcademicLessonEntity(
    @PrimaryKey val id: String,
    val moduleId: String,
    val courseId: String,
    val title: String,
    val content: String,
    val videoUrl: String?,
    val pdfAttachmentUrl: String?,
    val durationMinutes: Int,
    val orderIndex: Int,
    val isCompleted: Boolean
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val courseId: String,
    val title: String,
    val prompt: String,
    val maxScore: Int,
    val dueDate: String,
    val type: String
)

@Entity(tableName = "assignment_submissions")
data class AssignmentSubmissionEntity(
    @PrimaryKey val id: String,
    val assignmentId: String,
    val studentId: String,
    val submissionContent: String,
    val fileAttachmentUrl: String?,
    val status: String,
    val score: Int,
    val feedback: String?,
    val submittedAt: Long
)

@Entity(tableName = "question_bank")
data class QuestionBankEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val orgId: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswerIndex: Int,
    val explanation: String?,
    val difficultyLevel: String,
    val questionType: String
)

@Entity(tableName = "assessments")
data class AssessmentEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val orgId: String,
    val title: String,
    val type: String,
    val passingScore: Int,
    val timeLimitMinutes: Int,
    val totalQuestions: Int
)

@Entity(tableName = "assessment_attempts")
data class AssessmentAttemptEntity(
    @PrimaryKey val id: String,
    val assessmentId: String,
    val studentId: String,
    val scorePercent: Int,
    val isPassed: Boolean,
    val autoGradedFeedback: String,
    val completedAt: Long
)

@Entity(tableName = "gradebook_records")
data class GradebookRecordEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val courseId: String,
    val orgId: String,
    val courseName: String,
    val totalScore: Float,
    val gradeLetter: String,
    val gpaValue: Float,
    val isPassed: Boolean
)

@Entity(tableName = "student_progress")
data class StudentProgressEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val courseId: String,
    val completedLessonsCount: Int,
    val totalLessonsCount: Int,
    val progressPercent: Float,
    val lastAccessedAt: Long
)

@Entity(tableName = "achievement_badges")
data class AchievementBadgeEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val badgeName: String,
    val badgeDescription: String,
    val iconName: String,
    val unlockedAt: Long
)

@Entity(tableName = "learning_paths")
data class LearningPathEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val title: String,
    val description: String,
    val category: String,
    val courseIdsJson: String
)

@Entity(tableName = "prerequisites")
data class PrerequisiteEntity(
    @PrimaryKey val id: String,
    val targetCourseId: String,
    val requiredCourseId: String,
    val requiredCourseTitle: String
)

@Entity(tableName = "smart_recommendations")
data class SmartRecommendationEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val courseId: String,
    val courseTitle: String,
    val reasonText: String,
    val confidenceScore: Float
)

@Entity(tableName = "offline_downloads")
data class OfflineContentDownloadEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val lessonId: String,
    val localFilePath: String,
    val status: String,
    val progressPercent: Float
)
