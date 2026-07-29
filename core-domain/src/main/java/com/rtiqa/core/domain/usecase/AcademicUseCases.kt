package com.rtiqa.core.domain.usecase

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

class GetModulesUseCase(private val repository: AcademicRepository) {
    operator fun invoke(courseId: String): Flow<List<CurriculumModule>> = repository.getModulesForCourse(courseId)
}

class SaveModuleUseCase(private val repository: AcademicRepository) {
    suspend operator fun invoke(module: CurriculumModule) = repository.saveModule(module)
}

class GetAcademicLessonsUseCase(private val repository: AcademicRepository) {
    operator fun invoke(moduleId: String): Flow<List<AcademicLesson>> = repository.getLessonsForModule(moduleId)
}

class SaveAcademicLessonUseCase(private val repository: AcademicRepository) {
    suspend operator fun invoke(lesson: AcademicLesson) = repository.saveLesson(lesson)
}

class GetAssignmentsUseCase(private val repository: AcademicRepository) {
    operator fun invoke(courseId: String): Flow<List<Assignment>> = repository.getAssignmentsForCourse(courseId)
}

class SaveAssignmentUseCase(private val repository: AcademicRepository) {
    suspend operator fun invoke(assignment: Assignment) = repository.saveAssignment(assignment)
}

class SubmitAssignmentUseCase(private val repository: AcademicRepository) {
    suspend operator fun invoke(submission: AssignmentSubmission) = repository.submitAssignment(submission)
}

class GetAssessmentsUseCase(private val repository: AcademicRepository) {
    operator fun invoke(courseId: String): Flow<List<Assessment>> = repository.getAssessmentsForCourse(courseId)
}

class SaveAssessmentUseCase(private val repository: AcademicRepository) {
    suspend operator fun invoke(assessment: Assessment) = repository.saveAssessment(assessment)
}

class SubmitAssessmentAttemptUseCase(private val repository: AcademicRepository) {
    suspend operator fun invoke(attempt: AssessmentAttempt) = repository.submitAssessmentAttempt(attempt)
}

class GetGradebookUseCase(private val repository: AcademicRepository) {
    operator fun invoke(studentId: String, orgId: String): Flow<List<GradebookRecord>> =
        repository.getGradebookForStudent(studentId, orgId)
}

class GetStudentProgressUseCase(private val repository: AcademicRepository) {
    operator fun invoke(studentId: String, courseId: String): Flow<StudentProgress?> =
        repository.getStudentProgress(studentId, courseId)
}

class GetBadgesUseCase(private val repository: AcademicRepository) {
    operator fun invoke(studentId: String): Flow<List<AchievementBadge>> =
        repository.getBadgesForStudent(studentId)
}

class GetLearningPathsUseCase(private val repository: AcademicRepository) {
    operator fun invoke(orgId: String): Flow<List<LearningPath>> =
        repository.getLearningPaths(orgId)
}

class GetSmartRecommendationsUseCase(private val repository: AcademicRepository) {
    operator fun invoke(studentId: String): Flow<List<SmartRecommendation>> =
        repository.getRecommendationsForStudent(studentId)
}

class GetOfflineDownloadsUseCase(private val repository: AcademicRepository) {
    operator fun invoke(courseId: String): Flow<List<OfflineContentDownload>> =
        repository.getOfflineDownloads(courseId)
}

class SaveOfflineDownloadUseCase(private val repository: AcademicRepository) {
    suspend operator fun invoke(download: OfflineContentDownload) =
        repository.saveOfflineDownload(download)
}
