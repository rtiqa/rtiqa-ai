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
import com.rtiqa.core.domain.repository.ClassRepository
import com.rtiqa.core.domain.repository.SchoolManagementCoreRepository

import com.rtiqa.core.domain.usecase.GetGradeLevelsForSchoolUseCase
import com.rtiqa.core.domain.usecase.SaveGradeLevelUseCase
import com.rtiqa.core.domain.usecase.DeleteGradeLevelUseCase
import com.rtiqa.core.domain.usecase.GetTeacherAssignmentsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetTeacherAssignmentsForTeacherUseCase
import com.rtiqa.core.domain.usecase.SaveTeacherAssignmentUseCase
import com.rtiqa.core.domain.usecase.DeleteTeacherAssignmentUseCase
import com.rtiqa.core.domain.usecase.GetEnrollmentsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetEnrollmentsForClassUseCase
import com.rtiqa.core.domain.usecase.SaveStudentEnrollmentUseCase
import com.rtiqa.core.domain.usecase.DeleteStudentEnrollmentUseCase
import com.rtiqa.core.domain.usecase.EvaluateUserPermissionUseCase
import com.rtiqa.core.domain.usecase.CheckSchoolAccessUseCase

import com.rtiqa.core.domain.usecase.GetOrganizationsUseCase
import com.rtiqa.core.domain.usecase.SaveOrganizationUseCase
import com.rtiqa.core.domain.usecase.DeleteOrganizationUseCase
import com.rtiqa.core.domain.usecase.GetBranchesUseCase
import com.rtiqa.core.domain.usecase.SaveBranchUseCase
import com.rtiqa.core.domain.usecase.GetAcademicYearsUseCase
import com.rtiqa.core.domain.usecase.SaveAcademicYearUseCase
import com.rtiqa.core.domain.usecase.DeleteAcademicYearUseCase
import com.rtiqa.core.domain.usecase.GetSemestersUseCase
import com.rtiqa.core.domain.usecase.SaveSemesterUseCase
import com.rtiqa.core.domain.usecase.GetDepartmentsUseCase
import com.rtiqa.core.domain.usecase.SaveDepartmentUseCase
import com.rtiqa.core.domain.usecase.GetMajorsUseCase
import com.rtiqa.core.domain.usecase.SaveMajorUseCase
import com.rtiqa.core.domain.usecase.GetSectionsUseCase
import com.rtiqa.core.domain.usecase.SaveSectionUseCase
import com.rtiqa.core.domain.usecase.DeleteSectionUseCase
import com.rtiqa.core.domain.usecase.GetSubjectsUseCase
import com.rtiqa.core.domain.usecase.SaveSubjectUseCase
import com.rtiqa.core.domain.usecase.DeleteSubjectUseCase
import com.rtiqa.core.domain.usecase.GetStudyPlansUseCase
import com.rtiqa.core.domain.usecase.SaveStudyPlanUseCase
import com.rtiqa.core.domain.usecase.GetEnterpriseMembersUseCase
import com.rtiqa.core.domain.usecase.SaveEnterpriseMemberUseCase
import com.rtiqa.core.domain.usecase.DeleteEnterpriseMemberUseCase
import com.rtiqa.core.domain.usecase.GetSchoolsUseCase
import com.rtiqa.core.domain.usecase.GetSchoolByIdUseCase
import com.rtiqa.core.domain.usecase.SaveSchoolUseCase
import com.rtiqa.core.domain.usecase.DeleteSchoolUseCase
import com.rtiqa.core.domain.usecase.GetStudentsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetTeachersForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetUsersForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetSectionsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetSubjectsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetCoursesForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetAssessmentsForSchoolUseCase

import com.rtiqa.core.domain.usecase.GetClassesForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetClassByIdUseCase
import com.rtiqa.core.domain.usecase.SaveClassUseCase
import com.rtiqa.core.domain.usecase.DeleteClassUseCase
import com.rtiqa.core.domain.usecase.ValidateClassNameUniquenessUseCase
import com.rtiqa.core.domain.usecase.ReorderClassesUseCase
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
    academicRepository: AcademicRepository? = null,
    classRepository: ClassRepository? = null,
    schoolManagementCoreRepository: SchoolManagementCoreRepository? = null
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
    val enrollCourseUseCase by lazy { com.rtiqa.core.domain.usecase.EnrollCourseUseCase(courseRepository) }
    val toggleBookmarkUseCase by lazy { com.rtiqa.core.domain.usecase.ToggleBookmarkUseCase(courseRepository) }
    val syncCoursesUseCase by lazy { com.rtiqa.core.domain.usecase.SyncCoursesUseCase(courseRepository) }
    val getLessonDetailUseCase by lazy { com.rtiqa.core.domain.usecase.GetLessonDetailUseCase(courseRepository) }
    val getNextLessonUseCase by lazy { com.rtiqa.core.domain.usecase.GetNextLessonUseCase(courseRepository) }
    val saveLessonProgressUseCase by lazy { com.rtiqa.core.domain.usecase.SaveLessonProgressUseCase(courseRepository) }

    val getQuizzesForCourseUseCase by lazy { com.rtiqa.core.domain.usecase.GetQuizzesForCourseUseCase(quizRepository) }
    val getQuizForCourseUseCase by lazy { GetQuizForCourseUseCase(quizRepository) }
    val getQuizDetailUseCase by lazy { com.rtiqa.core.domain.usecase.GetQuizDetailUseCase(quizRepository) }
    val getQuizHistoryUseCase by lazy { com.rtiqa.core.domain.usecase.GetQuizHistoryUseCase(quizRepository) }
    val evaluateQuizAnswersUseCase by lazy { com.rtiqa.core.domain.usecase.EvaluateQuizAnswersUseCase() }
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
    val deleteAcademicYearUseCase by lazy { enterpriseRepositoryInstance?.let { DeleteAcademicYearUseCase(it) } }
    val getSemestersUseCase by lazy { enterpriseRepositoryInstance?.let { GetSemestersUseCase(it) } }
    val saveSemesterUseCase by lazy { enterpriseRepositoryInstance?.let { SaveSemesterUseCase(it) } }
    val getDepartmentsUseCase by lazy { enterpriseRepositoryInstance?.let { GetDepartmentsUseCase(it) } }
    val saveDepartmentUseCase by lazy { enterpriseRepositoryInstance?.let { SaveDepartmentUseCase(it) } }
    val getMajorsUseCase by lazy { enterpriseRepositoryInstance?.let { GetMajorsUseCase(it) } }
    val saveMajorUseCase by lazy { enterpriseRepositoryInstance?.let { SaveMajorUseCase(it) } }
    val getSectionsUseCase by lazy { enterpriseRepositoryInstance?.let { GetSectionsUseCase(it) } }
    val saveSectionUseCase by lazy { enterpriseRepositoryInstance?.let { SaveSectionUseCase(it) } }
    val deleteSectionUseCase by lazy { enterpriseRepositoryInstance?.let { DeleteSectionUseCase(it) } }
    val getSubjectsUseCase by lazy { enterpriseRepositoryInstance?.let { GetSubjectsUseCase(it) } }
    val saveSubjectUseCase by lazy { enterpriseRepositoryInstance?.let { SaveSubjectUseCase(it) } }
    val deleteSubjectUseCase by lazy { enterpriseRepositoryInstance?.let { DeleteSubjectUseCase(it) } }
    val getStudyPlansUseCase by lazy { enterpriseRepositoryInstance?.let { GetStudyPlansUseCase(it) } }
    val saveStudyPlanUseCase by lazy { enterpriseRepositoryInstance?.let { SaveStudyPlanUseCase(it) } }
    val getEnterpriseMembersUseCase by lazy { enterpriseRepositoryInstance?.let { GetEnterpriseMembersUseCase(it) } }
    val saveEnterpriseMemberUseCase by lazy { enterpriseRepositoryInstance?.let { SaveEnterpriseMemberUseCase(it) } }
    val deleteEnterpriseMemberUseCase by lazy { enterpriseRepositoryInstance?.let { DeleteEnterpriseMemberUseCase(it) } }

    val getSchoolsUseCase by lazy { enterpriseRepositoryInstance?.let { GetSchoolsUseCase(it) } }
    val getSchoolByIdUseCase by lazy { enterpriseRepositoryInstance?.let { GetSchoolByIdUseCase(it) } }
    val saveSchoolUseCase by lazy { enterpriseRepositoryInstance?.let { SaveSchoolUseCase(it) } }
    val deleteSchoolUseCase by lazy { enterpriseRepositoryInstance?.let { DeleteSchoolUseCase(it) } }
    val getStudentsForSchoolUseCase by lazy { enterpriseRepositoryInstance?.let { GetStudentsForSchoolUseCase(it) } }
    val getTeachersForSchoolUseCase by lazy { enterpriseRepositoryInstance?.let { GetTeachersForSchoolUseCase(it) } }
    val getUsersForSchoolUseCase by lazy { enterpriseRepositoryInstance?.let { GetUsersForSchoolUseCase(it) } }
    val getSectionsForSchoolUseCase by lazy { enterpriseRepositoryInstance?.let { GetSectionsForSchoolUseCase(it) } }
    val getSubjectsForSchoolUseCase by lazy { enterpriseRepositoryInstance?.let { GetSubjectsForSchoolUseCase(it) } }
    val getCoursesForSchoolUseCase by lazy { GetCoursesForSchoolUseCase(courseRepository) }
    val getAssessmentsForSchoolUseCase by lazy { academicRepositoryInstance?.let { GetAssessmentsForSchoolUseCase(it) } }

    // Classes Management UseCases
    val classRepositoryInstance: ClassRepository? = classRepository
    val getClassesForSchoolUseCase by lazy { classRepositoryInstance?.let { GetClassesForSchoolUseCase(it) } }
    val getClassByIdUseCase by lazy { classRepositoryInstance?.let { GetClassByIdUseCase(it) } }
    val saveClassUseCase by lazy { classRepositoryInstance?.let { SaveClassUseCase(it) } }
    val deleteClassUseCase by lazy { classRepositoryInstance?.let { DeleteClassUseCase(it) } }
    val validateClassNameUniquenessUseCase by lazy { classRepositoryInstance?.let { ValidateClassNameUniquenessUseCase(it) } }
    val reorderClassesUseCase by lazy { classRepositoryInstance?.let { ReorderClassesUseCase(it) } }

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

    // School Management Core UseCases
    val schoolManagementCoreRepositoryInstance: SchoolManagementCoreRepository? = schoolManagementCoreRepository
    val getGradeLevelsForSchoolUseCase by lazy { schoolManagementCoreRepositoryInstance?.let { GetGradeLevelsForSchoolUseCase(it) } }
    val saveGradeLevelUseCase by lazy { schoolManagementCoreRepositoryInstance?.let { SaveGradeLevelUseCase(it) } }
    val deleteGradeLevelUseCase by lazy { schoolManagementCoreRepositoryInstance?.let { DeleteGradeLevelUseCase(it) } }
    val getTeacherAssignmentsForSchoolUseCase by lazy { schoolManagementCoreRepositoryInstance?.let { GetTeacherAssignmentsForSchoolUseCase(it) } }
    val getTeacherAssignmentsForTeacherUseCase by lazy { schoolManagementCoreRepositoryInstance?.let { GetTeacherAssignmentsForTeacherUseCase(it) } }
    val saveTeacherAssignmentUseCase by lazy { schoolManagementCoreRepositoryInstance?.let { SaveTeacherAssignmentUseCase(it) } }
    val deleteTeacherAssignmentUseCase by lazy { schoolManagementCoreRepositoryInstance?.let { DeleteTeacherAssignmentUseCase(it) } }
    val getEnrollmentsForSchoolUseCase by lazy { schoolManagementCoreRepositoryInstance?.let { GetEnrollmentsForSchoolUseCase(it) } }
    val getEnrollmentsForClassUseCase by lazy { schoolManagementCoreRepositoryInstance?.let { GetEnrollmentsForClassUseCase(it) } }
    val saveStudentEnrollmentUseCase by lazy { schoolManagementCoreRepositoryInstance?.let { SaveStudentEnrollmentUseCase(it) } }
    val deleteStudentEnrollmentUseCase by lazy { schoolManagementCoreRepositoryInstance?.let { DeleteStudentEnrollmentUseCase(it) } }
    val evaluateUserPermissionUseCase by lazy { EvaluateUserPermissionUseCase() }
    val checkSchoolAccessUseCase by lazy { CheckSchoolAccessUseCase() }
}
