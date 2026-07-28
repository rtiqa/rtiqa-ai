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
import com.rtiqa.core.domain.usecase.SearchCoursesUseCase
import com.rtiqa.core.domain.usecase.SubmitQuizResultUseCase
import com.rtiqa.core.domain.usecase.SyncOfflineDataUseCase
import com.rtiqa.core.domain.usecase.UpdateUserProfileUseCase
import com.rtiqa.core.domain.usecase.UpdateUserStreakUseCase

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
    offlineSync: OfflineSyncContract
) {
    val loginUseCase by lazy { LoginUseCase(authRepository) }
    val registerUseCase by lazy { RegisterUseCase(authRepository) }
    val logoutUseCase by lazy { LogoutUseCase(authRepository) }
    val observeUserSessionUseCase by lazy { ObserveUserSessionUseCase(authRepository) }

    val getCoursesUseCase by lazy { GetCoursesUseCase(courseRepository) }
    val getPagedCoursesUseCase by lazy { GetPagedCoursesUseCase(courseRepository) }
    val getCourseDetailUseCase by lazy { GetCourseDetailUseCase(courseRepository) }
    val getLessonsForCourseUseCase by lazy { GetLessonsForCourseUseCase(courseRepository) }
    val completeLessonUseCase by lazy { CompleteLessonUseCase(courseRepository, userRepository) }
    val searchCoursesUseCase by lazy { SearchCoursesUseCase(courseRepository) }
    val downloadCourseUseCase by lazy { DownloadCourseUseCase(downloadManager) }

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
}
