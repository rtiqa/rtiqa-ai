package com.rtiqa.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rtiqa.core.database.entity.AcademicLessonEntity
import com.rtiqa.core.database.entity.AchievementBadgeEntity
import com.rtiqa.core.database.entity.AssessmentAttemptEntity
import com.rtiqa.core.database.entity.AssessmentEntity
import com.rtiqa.core.database.entity.AssignmentEntity
import com.rtiqa.core.database.entity.AssignmentSubmissionEntity
import com.rtiqa.core.database.entity.CurriculumModuleEntity
import com.rtiqa.core.database.entity.GradebookRecordEntity
import com.rtiqa.core.database.entity.LearningPathEntity
import com.rtiqa.core.database.entity.OfflineContentDownloadEntity
import com.rtiqa.core.database.entity.PrerequisiteEntity
import com.rtiqa.core.database.entity.QuestionBankEntity
import com.rtiqa.core.database.entity.SmartRecommendationEntity
import com.rtiqa.core.database.entity.StudentProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicDao {
    // Curriculum Modules & Lessons
    @Query("SELECT * FROM curriculum_modules WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getModulesForCourse(courseId: String): Flow<List<CurriculumModuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: CurriculumModuleEntity)

    @Query("SELECT * FROM academic_lessons WHERE moduleId = :moduleId ORDER BY orderIndex ASC")
    fun getLessonsForModule(moduleId: String): Flow<List<AcademicLessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: AcademicLessonEntity)

    // Assignments, Projects, Labs
    @Query("SELECT * FROM assignments WHERE courseId = :courseId")
    fun getAssignmentsForCourse(courseId: String): Flow<List<AssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Query("SELECT * FROM assignment_submissions WHERE assignmentId = :assignmentId AND studentId = :studentId")
    fun getSubmissions(assignmentId: String, studentId: String): Flow<List<AssignmentSubmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: AssignmentSubmissionEntity)

    @Query("UPDATE assignment_submissions SET score = :score, feedback = :feedback, status = 'GRADED' WHERE id = :submissionId")
    suspend fun updateSubmissionGrade(submissionId: String, score: Int, feedback: String)

    // Question Bank & Assessments (Quizzes, Exams, Labs)
    @Query("SELECT * FROM question_bank WHERE courseId = :courseId")
    fun getQuestionsForCourse(courseId: String): Flow<List<QuestionBankEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionBankEntity)

    @Query("SELECT * FROM assessments WHERE courseId = :courseId")
    fun getAssessmentsForCourse(courseId: String): Flow<List<AssessmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: AssessmentEntity)

    @Query("SELECT * FROM assessment_attempts WHERE assessmentId = :assessmentId AND studentId = :studentId")
    fun getAttempts(assessmentId: String, studentId: String): Flow<List<AssessmentAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessmentAttempt(attempt: AssessmentAttemptEntity)

    // Gradebook & Academic Record
    @Query("SELECT * FROM gradebook_records WHERE studentId = :studentId AND orgId = :orgId")
    fun getGradebookForStudent(studentId: String, orgId: String): Flow<List<GradebookRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGradebookRecord(record: GradebookRecordEntity)

    // Progress Tracking
    @Query("SELECT * FROM student_progress WHERE studentId = :studentId AND courseId = :courseId LIMIT 1")
    fun getStudentProgress(studentId: String, courseId: String): Flow<StudentProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentProgress(progress: StudentProgressEntity)

    // Achievements & Badges
    @Query("SELECT * FROM achievement_badges WHERE studentId = :studentId")
    fun getBadgesForStudent(studentId: String): Flow<List<AchievementBadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: AchievementBadgeEntity)

    // Learning Paths & Prerequisites
    @Query("SELECT * FROM learning_paths WHERE orgId = :orgId")
    fun getLearningPaths(orgId: String): Flow<List<LearningPathEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningPath(path: LearningPathEntity)

    @Query("SELECT * FROM prerequisites WHERE targetCourseId = :targetCourseId")
    fun getPrerequisites(targetCourseId: String): Flow<List<PrerequisiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrerequisite(prerequisite: PrerequisiteEntity)

    // Smart Recommendations
    @Query("SELECT * FROM smart_recommendations WHERE studentId = :studentId")
    fun getRecommendationsForStudent(studentId: String): Flow<List<SmartRecommendationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: SmartRecommendationEntity)

    // Offline Downloads
    @Query("SELECT * FROM offline_downloads WHERE courseId = :courseId")
    fun getOfflineDownloads(courseId: String): Flow<List<OfflineContentDownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineDownload(download: OfflineContentDownloadEntity)
}
