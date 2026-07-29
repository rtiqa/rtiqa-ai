package com.rtiqa.core.data.repository

import com.rtiqa.core.data.mapper.toDomain
import com.rtiqa.core.data.mapper.toEntity
import com.rtiqa.core.database.dao.AcademicDao
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
import com.rtiqa.core.domain.repository.AcademicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AcademicRepositoryImpl(
    private val academicDao: AcademicDao
) : AcademicRepository {

    override fun getModulesForCourse(courseId: String): Flow<List<CurriculumModule>> =
        academicDao.getModulesForCourse(courseId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveModule(module: CurriculumModule) {
        academicDao.insertModule(module.toEntity())
    }

    override fun getLessonsForModule(moduleId: String): Flow<List<AcademicLesson>> =
        academicDao.getLessonsForModule(moduleId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveLesson(lesson: AcademicLesson) {
        academicDao.insertLesson(lesson.toEntity())
    }

    override fun getAssignmentsForCourse(courseId: String): Flow<List<Assignment>> =
        academicDao.getAssignmentsForCourse(courseId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveAssignment(assignment: Assignment) {
        academicDao.insertAssignment(assignment.toEntity())
    }

    override fun getSubmissions(assignmentId: String, studentId: String): Flow<List<AssignmentSubmission>> =
        academicDao.getSubmissions(assignmentId, studentId).map { list -> list.map { it.toDomain() } }

    override suspend fun submitAssignment(submission: AssignmentSubmission) {
        academicDao.insertSubmission(submission.toEntity())
    }

    override suspend fun gradeSubmission(submissionId: String, score: Int, feedback: String) {
        academicDao.updateSubmissionGrade(submissionId, score, feedback)
    }

    override fun getQuestionsForCourse(courseId: String): Flow<List<QuestionBankItem>> =
        academicDao.getQuestionsForCourse(courseId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveQuestion(question: QuestionBankItem) {
        academicDao.insertQuestion(question.toEntity())
    }

    override fun getAssessmentsForCourse(courseId: String): Flow<List<Assessment>> =
        academicDao.getAssessmentsForCourse(courseId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveAssessment(assessment: Assessment) {
        academicDao.insertAssessment(assessment.toEntity())
    }

    override fun getAttempts(assessmentId: String, studentId: String): Flow<List<AssessmentAttempt>> =
        academicDao.getAttempts(assessmentId, studentId).map { list -> list.map { it.toDomain() } }

    override suspend fun submitAssessmentAttempt(attempt: AssessmentAttempt) {
        academicDao.insertAssessmentAttempt(attempt.toEntity())
    }

    override fun getGradebookForStudent(studentId: String, orgId: String): Flow<List<GradebookRecord>> =
        academicDao.getGradebookForStudent(studentId, orgId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveGradebookRecord(record: GradebookRecord) {
        academicDao.insertGradebookRecord(record.toEntity())
    }

    override fun getStudentProgress(studentId: String, courseId: String): Flow<StudentProgress?> =
        academicDao.getStudentProgress(studentId, courseId).map { it?.toDomain() }

    override suspend fun updateStudentProgress(progress: StudentProgress) {
        academicDao.insertStudentProgress(progress.toEntity())
    }

    override fun getBadgesForStudent(studentId: String): Flow<List<AchievementBadge>> =
        academicDao.getBadgesForStudent(studentId).map { list -> list.map { it.toDomain() } }

    override suspend fun unlockBadge(badge: AchievementBadge) {
        academicDao.insertBadge(badge.toEntity())
    }

    override fun getLearningPaths(orgId: String): Flow<List<LearningPath>> =
        academicDao.getLearningPaths(orgId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveLearningPath(path: LearningPath) {
        academicDao.insertLearningPath(path.toEntity())
    }

    override fun getPrerequisites(targetCourseId: String): Flow<List<Prerequisite>> =
        academicDao.getPrerequisites(targetCourseId).map { list -> list.map { it.toDomain() } }

    override suspend fun savePrerequisite(prerequisite: Prerequisite) {
        academicDao.insertPrerequisite(prerequisite.toEntity())
    }

    override fun getRecommendationsForStudent(studentId: String): Flow<List<SmartRecommendation>> =
        academicDao.getRecommendationsForStudent(studentId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveRecommendation(recommendation: SmartRecommendation) {
        academicDao.insertRecommendation(recommendation.toEntity())
    }

    override fun getOfflineDownloads(courseId: String): Flow<List<OfflineContentDownload>> =
        academicDao.getOfflineDownloads(courseId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveOfflineDownload(download: OfflineContentDownload) {
        academicDao.insertOfflineDownload(download.toEntity())
    }
}
