package com.rtiqa.core.domain.repository

import com.rtiqa.core.domain.model.AcademicLesson
import com.rtiqa.core.domain.model.AchievementBadge
import com.rtiqa.core.domain.model.Assessment
import com.rtiqa.core.domain.model.AssessmentAttempt
import com.rtiqa.core.domain.model.Assignment
import com.rtiqa.core.domain.model.AssignmentSubmission
import com.rtiqa.core.domain.model.CurriculumModule
import com.rtiqa.core.domain.model.GradebookRecord
import com.rtiqa.core.domain.model.LearningPath
import com.rtiqa.core.domain.model.OfflineContentDownload
import com.rtiqa.core.domain.model.Prerequisite
import com.rtiqa.core.domain.model.QuestionBankItem
import com.rtiqa.core.domain.model.SmartRecommendation
import com.rtiqa.core.domain.model.StudentProgress
import kotlinx.coroutines.flow.Flow

interface AcademicRepository {
    // Modules & Lessons (Curriculum Management)
    fun getModulesForCourse(courseId: String): Flow<List<CurriculumModule>>
    suspend fun saveModule(module: CurriculumModule)
    fun getLessonsForModule(moduleId: String): Flow<List<AcademicLesson>>
    suspend fun saveLesson(lesson: AcademicLesson)

    // Assignments, Projects, Labs
    fun getAssignmentsForCourse(courseId: String): Flow<List<Assignment>>
    suspend fun saveAssignment(assignment: Assignment)
    fun getSubmissions(assignmentId: String, studentId: String): Flow<List<AssignmentSubmission>>
    suspend fun submitAssignment(submission: AssignmentSubmission)
    suspend fun gradeSubmission(submissionId: String, score: Int, feedback: String)

    // Question Bank & Assessments (Quizzes, Exams, Labs)
    fun getQuestionsForCourse(courseId: String): Flow<List<QuestionBankItem>>
    suspend fun saveQuestion(question: QuestionBankItem)
    fun getAssessmentsForCourse(courseId: String): Flow<List<Assessment>>
    fun getAssessmentsForSchool(schoolId: String): Flow<List<Assessment>>
    suspend fun saveAssessment(assessment: Assessment)
    fun getAttempts(assessmentId: String, studentId: String): Flow<List<AssessmentAttempt>>
    suspend fun submitAssessmentAttempt(attempt: AssessmentAttempt)

    // Gradebook & Academic Records
    fun getGradebookForStudent(studentId: String, orgId: String): Flow<List<GradebookRecord>>
    suspend fun saveGradebookRecord(record: GradebookRecord)

    // Student Progress Tracking
    fun getStudentProgress(studentId: String, courseId: String): Flow<StudentProgress?>
    suspend fun updateStudentProgress(progress: StudentProgress)

    // Achievements & Badges
    fun getBadgesForStudent(studentId: String): Flow<List<AchievementBadge>>
    suspend fun unlockBadge(badge: AchievementBadge)

    // Learning Paths & Prerequisites
    fun getLearningPaths(orgId: String): Flow<List<LearningPath>>
    suspend fun saveLearningPath(path: LearningPath)
    fun getPrerequisites(targetCourseId: String): Flow<List<Prerequisite>>
    suspend fun savePrerequisite(prerequisite: Prerequisite)

    // AI Smart Recommendations
    fun getRecommendationsForStudent(studentId: String): Flow<List<SmartRecommendation>>
    suspend fun saveRecommendation(recommendation: SmartRecommendation)

    // Offline Engine & Sync
    fun getOfflineDownloads(courseId: String): Flow<List<OfflineContentDownload>>
    suspend fun saveOfflineDownload(download: OfflineContentDownload)
}
