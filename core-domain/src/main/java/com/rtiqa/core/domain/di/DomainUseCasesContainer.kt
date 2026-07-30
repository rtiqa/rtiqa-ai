package com.rtiqa.core.domain.di

import com.rtiqa.core.domain.repository.AiRepositoryContract
import com.rtiqa.core.domain.repository.AuthRepositoryContract
import com.rtiqa.core.domain.repository.CourseRepositoryContract
import com.rtiqa.core.domain.repository.DownloadManagerContract
import com.rtiqa.core.domain.repository.OfflineSyncContract
import com.rtiqa.core.domain.repository.QuizRepositoryContract
import com.rtiqa.core.domain.repository.UserRepositoryContract
import com.rtiqa.core.domain.usecase.AskAiTutorUseCase
import com.rtiqa.core.domain.usecase.CompleteLessonUseCase
import com.rtiqa.core.domain.usecase.DownloadCourseUseCase
import com.rtiqa.core.domain.usecase.GenerateAiSummaryUseCase
import com.rtiqa.core.domain.usecase.GetAiHistoryUseCase
import com.rtiqa.core.domain.usecase.GetCourseDetailUseCase
import com.rtiqa.core.domain.usecase.GetCoursesUseCase
import com.rtiqa.core.domain.usecase.GetLessonsForCourseUseCase
import com.rtiqa.core.domain.usecase.GetPagedCoursesUseCase
import com.rtiqa.core.domain.usecase.GetQuizForCourseUseCase
import com.rtiqa.core.domain.usecase.GetUserProfileUseCase
import com.rtiqa.core.domain.usecase.LoginUseCase
import com.rtiqa.core.domain.usecase.LogoutUseCase
import com.rtiqa.core.domain.usecase.ObserveSyncStatusUseCase
import com.rtiqa.core.domain.usecase.ObserveUserSessionUseCase
import com.rtiqa.core.domain.usecase.RegisterUseCase
import com.rtiqa.core.domain.usecase.ResetPasswordUseCase
import com.rtiqa.core.domain.usecase.SearchCoursesUseCase
import com.rtiqa.core.domain.usecase.SubmitQuizResultUseCase
import com.rtiqa.core.domain.usecase.SyncOfflineDataUseCase
import com.rtiqa.core.domain.usecase.UpdateUserProfileUseCase
import com.rtiqa.core.domain.usecase.UpdateUserStreakUseCase

import com.rtiqa.core.domain.usecase.DeleteCourseUseCase
import com.rtiqa.core.domain.usecase.SaveCourseUseCase

import com.rtiqa.core.domain.repository.EnterpriseRepository
import com.rtiqa.core.domain.repository.AcademicRepository

import com.rtiqa.core.domain.usecase.GetOrganizationsUseCase
import com.rtiqa.core.domain.usecase.SaveOrganizationUseCase
import com.rtiqa.core.domain.usecase.DeleteOrganizationUseCase
import com.rtiqa.core.domain.usecase.GetBranchesUseCase
import com.rtiqa.core.domain.usecase.SaveBranchUseCase
import com.rtiqa.core.domain.usecase.GetAcademicYearsUseCase
import com.rtiqa.core.domain.usecase.SaveAcademicYearUseCase
import com.rtiqa.core.domain.usecase.GetSemestersUseCase
import com.rtiqa.core.domain.usecase.SaveSemesterUseCase
import com.rtiqa.core.domain.usecase.GetDepartmentsUseCase
import com.rtiqa.core.domain.usecase.SaveDepartmentUseCase
import com.rtiqa.core.domain.usecase.GetMajorsUseCase
import com.rtiqa.core.domain.usecase.SaveMajorUseCase
import com.rtiqa.core.domain.usecase.GetSectionsUseCase
import com.rtiqa.core.domain.usecase.SaveSectionUseCase
import com.rtiqa.core.domain.usecase.GetSubjectsUseCase
import com.rtiqa.core.domain.usecase.SaveSubjectUseCase
import com.rtiqa.core.domain.usecase.GetStudyPlansUseCase
import com.rtiqa.core.domain.usecase.SaveStudyPlanUseCase
import com.rtiqa.core.domain.usecase.GetEnterpriseMembersUseCase
import com.rtiqa.core.domain.usecase.SaveEnterpriseMemberUseCase
import com.rtiqa.core.domain.usecase.DeleteEnterpriseMemberUseCase

import com.rtiqa.core.domain.usecase.GetModulesUseCase
import com.rtiqa.core.domain.usecase.SaveModuleUseCase
import com.rtiqa.core.domain.usecase.GetAcademicLessonsUseCase
import com.rtiqa.core.domain.usecase.SaveAcademicLessonUseCase
import com.rtiqa.core.domain.usecase.GetAssignmentsUseCase
import com.rtiqa.core.domain.usecase.SaveAssignmentUseCase
import com.rtiqa.core.domain.usecase.SubmitAssignmentUseCase
import com.rtiqa.core.domain.usecase.GetAssessmentsUseCase
import com.rtiqa.core.domain.usecase.SaveAssessmentUseCase
import com.rtiqa.core.domain.usecase.SubmitAssessmentAttemptUseCase
import com.rtiqa.core.domain.usecase.GetGradebookUseCase
import com.rtiqa.core.domain.usecase.GetStudentProgressUseCase
import com.rtiqa.core.domain.usecase.GetBadgesUseCase
import com.rtiqa.core.domain.usecase.GetLearningPathsUseCase
import com.rtiqa.core.domain.usecase.GetSmartRecommendationsUseCase
import com.rtiqa.core.domain.usecase.GetOfflineDownloadsUseCase
import com.rtiqa.core.domain.usecase.SaveOfflineDownloadUseCase
import com.rtiqa.core.domain.usecase.SaveGradebookRecordUseCase
import com.rtiqa.core.domain.usecase.SaveLearningPathUseCase
import com.rtiqa.core.domain.usecase.UnlockBadgeUseCase
import com.rtiqa.core.domain.usecase.SavePrerequisiteUseCase
import com.rtiqa.core.domain.usecase.SaveQuestionUseCase

/**
 * Dependency container aggregating domain UseCases provided to presentation ViewModels.
 */
class DomainUseCasesContainer(
    authRepository: AuthRepositoryContract,
    courseRepository: CourseRepositoryContract,
    userRepository: UserRepositoryContract,
    quizRepository: QuizRepositoryContract,
    aiRepository: AiRepositoryContract,
    downloadManager: DownloadManagerContract,
    offlineSync: OfflineSyncContract,
    enterpriseRepository: EnterpriseRepository? = null,
    academicRepository: AcademicRepository? = null
) {
    val loginUseCase by lazy { LoginUseCase(authRepository) }
    val registerUseCase by lazy { RegisterUseCase(authRepository) }
    val resetPasswordUseCase by lazy { ResetPasswordUseCase(authRepository) }
    val logoutUseCase by lazy { LogoutUseCase(authRepository) }
    val observeUserSessionUseCase by lazy { ObserveUserSessionUseCase(authRepository) }

    val getCoursesUseCase by lazy { GetCoursesUseCase(courseRepository) }
    val getPagedCoursesUseCase by lazy { GetPagedCoursesUseCase(courseRepository) }
    val getCourseDetailUseCase by lazy { GetCourseDetailUseCase(courseRepository) }
    val getLessonsForCourseUseCase by lazy { GetLessonsForCourseUseCase(courseRepository) }
    val completeLessonUseCase by lazy { CompleteLessonUseCase(courseRepository, userRepository) }
    val searchCoursesUseCase by lazy { SearchCoursesUseCase(courseRepository) }
    val downloadCourseUseCase by lazy { DownloadCourseUseCase(downloadManager) }
    val saveCourseUseCase by lazy { SaveCourseUseCase(courseRepository) }
    val deleteCourseUseCase by lazy { DeleteCourseUseCase(courseRepository) }

    val getQuizForCourseUseCase by lazy { GetQuizForCourseUseCase(quizRepository) }
    val submitQuizResultUseCase by lazy { SubmitQuizResultUseCase(quizRepository, userRepository) }

    val askAiTutorUseCase by lazy { AskAiTutorUseCase(aiRepository) }
    val generateAiSummaryUseCase by lazy { GenerateAiSummaryUseCase(aiRepository) }
    val getAiHistoryUseCase by lazy { GetAiHistoryUseCase(aiRepository) }

    val syncOfflineDataUseCase by lazy { SyncOfflineDataUseCase(offlineSync) }
    val observeSyncStatusUseCase by lazy { ObserveSyncStatusUseCase(offlineSync) }

    val getUserProfileUseCase by lazy { GetUserProfileUseCase(userRepository) }
    val updateUserProfileUseCase by lazy { UpdateUserProfileUseCase(userRepository) }
    val updateUserStreakUseCase by lazy { UpdateUserStreakUseCase(userRepository) }

    // Enterprise UseCases
    val enterpriseRepositoryInstance: EnterpriseRepository? = enterpriseRepository
    val getOrganizationsUseCase by lazy { enterpriseRepositoryInstance?.let { GetOrganizationsUseCase(it) } }
    val saveOrganizationUseCase by lazy { enterpriseRepositoryInstance?.let { SaveOrganizationUseCase(it) } }
    val deleteOrganizationUseCase by lazy { enterpriseRepositoryInstance?.let { DeleteOrganizationUseCase(it) } }
    val getBranchesUseCase by lazy { enterpriseRepositoryInstance?.let { GetBranchesUseCase(it) } }
    val saveBranchUseCase by lazy { enterpriseRepositoryInstance?.let { SaveBranchUseCase(it) } }
    val getAcademicYearsUseCase by lazy { enterpriseRepositoryInstance?.let { GetAcademicYearsUseCase(it) } }
    val saveAcademicYearUseCase by lazy { enterpriseRepositoryInstance?.let { SaveAcademicYearUseCase(it) } }
    val getSemestersUseCase by lazy { enterpriseRepositoryInstance?.let { GetSemestersUseCase(it) } }
    val saveSemesterUseCase by lazy { enterpriseRepositoryInstance?.let { SaveSemesterUseCase(it) } }
    val getDepartmentsUseCase by lazy { enterpriseRepositoryInstance?.let { GetDepartmentsUseCase(it) } }
    val saveDepartmentUseCase by lazy { enterpriseRepositoryInstance?.let { SaveDepartmentUseCase(it) } }
    val getMajorsUseCase by lazy { enterpriseRepositoryInstance?.let { GetMajorsUseCase(it) } }
    val saveMajorUseCase by lazy { enterpriseRepositoryInstance?.let { SaveMajorUseCase(it) } }
    val getSectionsUseCase by lazy { enterpriseRepositoryInstance?.let { GetSectionsUseCase(it) } }
    val saveSectionUseCase by lazy { enterpriseRepositoryInstance?.let { SaveSectionUseCase(it) } }
    val getSubjectsUseCase by lazy { enterpriseRepositoryInstance?.let { GetSubjectsUseCase(it) } }
    val saveSubjectUseCase by lazy { enterpriseRepositoryInstance?.let { SaveSubjectUseCase(it) } }
    val getStudyPlansUseCase by lazy { enterpriseRepositoryInstance?.let { GetStudyPlansUseCase(it) } }
    val saveStudyPlanUseCase by lazy { enterpriseRepositoryInstance?.let { SaveStudyPlanUseCase(it) } }
    val getEnterpriseMembersUseCase by lazy { enterpriseRepositoryInstance?.let { GetEnterpriseMembersUseCase(it) } }
    val saveEnterpriseMemberUseCase by lazy { enterpriseRepositoryInstance?.let { SaveEnterpriseMemberUseCase(it) } }
    val deleteEnterpriseMemberUseCase by lazy { enterpriseRepositoryInstance?.let { DeleteEnterpriseMemberUseCase(it) } }

    // Academic Platform UseCases
    val academicRepositoryInstance: AcademicRepository? = academicRepository
    val getModulesUseCase by lazy { academicRepositoryInstance?.let { GetModulesUseCase(it) } }
    val saveModuleUseCase by lazy { academicRepositoryInstance?.let { SaveModuleUseCase(it) } }
    val getAcademicLessonsUseCase by lazy { academicRepositoryInstance?.let { GetAcademicLessonsUseCase(it) } }
    val saveAcademicLessonUseCase by lazy { academicRepositoryInstance?.let { SaveAcademicLessonUseCase(it) } }
    val getAssignmentsUseCase by lazy { academicRepositoryInstance?.let { GetAssignmentsUseCase(it) } }
    val saveAssignmentUseCase by lazy { academicRepositoryInstance?.let { SaveAssignmentUseCase(it) } }
    val submitAssignmentUseCase by lazy { academicRepositoryInstance?.let { SubmitAssignmentUseCase(it) } }
    val getAssessmentsUseCase by lazy { academicRepositoryInstance?.let { GetAssessmentsUseCase(it) } }
    val saveAssessmentUseCase by lazy { academicRepositoryInstance?.let { SaveAssessmentUseCase(it) } }
    val submitAssessmentAttemptUseCase by lazy { academicRepositoryInstance?.let { SubmitAssessmentAttemptUseCase(it) } }
    val getGradebookUseCase by lazy { academicRepositoryInstance?.let { GetGradebookUseCase(it) } }
    val getStudentProgressUseCase by lazy { academicRepositoryInstance?.let { GetStudentProgressUseCase(it) } }
    val getBadgesUseCase by lazy { academicRepositoryInstance?.let { GetBadgesUseCase(it) } }
    val getLearningPathsUseCase by lazy { academicRepositoryInstance?.let { GetLearningPathsUseCase(it) } }
    val getSmartRecommendationsUseCase by lazy { academicRepositoryInstance?.let { GetSmartRecommendationsUseCase(it) } }
    val getOfflineDownloadsUseCase by lazy { academicRepositoryInstance?.let { GetOfflineDownloadsUseCase(it) } }
    val saveOfflineDownloadUseCase by lazy { academicRepositoryInstance?.let { SaveOfflineDownloadUseCase(it) } }
    val saveGradebookRecordUseCase by lazy { academicRepositoryInstance?.let { SaveGradebookRecordUseCase(it) } }
    val saveLearningPathUseCase by lazy { academicRepositoryInstance?.let { SaveLearningPathUseCase(it) } }
    val unlockBadgeUseCase by lazy { academicRepositoryInstance?.let { UnlockBadgeUseCase(it) } }
    val savePrerequisiteUseCase by lazy { academicRepositoryInstance?.let { SavePrerequisiteUseCase(it) } }
    val saveQuestionUseCase by lazy { academicRepositoryInstance?.let { SaveQuestionUseCase(it) } }
}
