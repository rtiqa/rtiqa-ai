package com.rtiqa.core.data.di

import android.content.Context
import com.rtiqa.core.ai.GeminiAiRepositoryImpl
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.data.firestore.FirestoreSyncManager
import com.rtiqa.core.data.repository.AuthRepositoryImpl
import com.rtiqa.core.data.repository.CourseRepositoryImpl
import com.rtiqa.core.data.repository.DownloadManagerImpl
import com.rtiqa.core.data.repository.PermissionManagerImpl
import com.rtiqa.core.data.repository.QuizRepositoryImpl
import com.rtiqa.core.data.repository.UserRepositoryImpl
import com.rtiqa.core.data.sync.OfflineSyncManager
import com.rtiqa.core.database.RtiqaDatabase
import com.rtiqa.core.di.RtiqaCoreDiContainer
import com.rtiqa.core.domain.di.DomainUseCasesContainer
import com.rtiqa.core.domain.repository.AiRepositoryContract
import com.rtiqa.core.domain.repository.AuthRepositoryContract
import com.rtiqa.core.domain.repository.CourseRepositoryContract
import com.rtiqa.core.domain.repository.DownloadManagerContract
import com.rtiqa.core.domain.repository.OfflineSyncContract
import com.rtiqa.core.domain.repository.PermissionContract
import com.rtiqa.core.domain.repository.QuizRepositoryContract
import com.rtiqa.core.domain.repository.UserRepositoryContract
import com.rtiqa.core.network.RetrofitNetworkClient
import com.rtiqa.core.network.api.RtiqaApiService
import com.rtiqa.core.network.monitor.ConnectivityManagerNetworkMonitor
import com.rtiqa.core.network.monitor.NetworkMonitor
import com.rtiqa.core.ui.navigation.AppNavigator

/**
 * Root Application DI Container providing dependency graph binding across all core and feature modules.
 */
class AppDiContainer(val context: Context) {

    val coreDiContainer: RtiqaCoreDiContainer by lazy {
        RtiqaCoreDiContainer(context)
    }

    val database: RtiqaDatabase by lazy {
        RtiqaDatabase.getInstance(context)
    }

    val preferencesDataStore: RtiqaPreferencesDataStore by lazy {
        RtiqaPreferencesDataStore(context)
    }

    val okHttpClient by lazy {
        RetrofitNetworkClient.createOkHttpClient(
            securityManager = coreDiContainer.securityManager,
            isDebug = true
        )
    }

    val apiService: RtiqaApiService by lazy {
        RetrofitNetworkClient.createApiService(okHttpClient)
    }

    val networkMonitor: NetworkMonitor by lazy {
        ConnectivityManagerNetworkMonitor(context)
    }

    val firestoreSyncManager: FirestoreSyncManager by lazy {
        FirestoreSyncManager()
    }

    val offlineSyncManager: OfflineSyncManager by lazy {
        OfflineSyncManager(
            apiService = apiService,
            courseDao = database.courseDao(),
            syncDao = database.syncDao()
        )
    }

    val offlineSyncContract: OfflineSyncContract by lazy {
        offlineSyncManager
    }

    val authRepository: AuthRepositoryContract by lazy {
        AuthRepositoryImpl(
            apiService = apiService,
            userProfileDao = database.userProfileDao(),
            preferencesDataStore = preferencesDataStore,
            securityManager = coreDiContainer.securityManager,
            firestoreSyncManager = firestoreSyncManager
        )
    }

    val downloadManager: DownloadManagerContract by lazy {
        DownloadManagerImpl(
            courseDao = database.courseDao()
        )
    }

    val permissionManager: PermissionContract by lazy {
        PermissionManagerImpl(
            context = context
        )
    }

    val courseRepository: CourseRepositoryContract by lazy {
        CourseRepositoryImpl(
            courseDao = database.courseDao(),
            lessonDao = database.lessonDao(),
            firestoreSyncManager = firestoreSyncManager,
            currentUserIdProvider = { authRepository.getCurrentUserId() }
        )
    }

    val userRepository: UserRepositoryContract by lazy {
        UserRepositoryImpl(
            userProfileDao = database.userProfileDao(),
            firestoreSyncManager = firestoreSyncManager
        )
    }

    val quizRepository: QuizRepositoryContract by lazy {
        QuizRepositoryImpl(
            academicDao = database.academicDao(),
            offlineSyncManager = offlineSyncManager,
            firestoreSyncManager = firestoreSyncManager,
            currentUserIdProvider = { authRepository.getCurrentUserId() }
        )
    }

    val aiRepository: AiRepositoryContract by lazy {
        GeminiAiRepositoryImpl(
            aiInsightDao = database.aiInsightDao()
        )
    }

    val enterpriseRepository: com.rtiqa.core.domain.repository.EnterpriseRepository by lazy {
        com.rtiqa.core.data.repository.EnterpriseRepositoryImpl(
            enterpriseDao = database.enterpriseDao()
        )
    }

    val academicRepository: com.rtiqa.core.domain.repository.AcademicRepository by lazy {
        com.rtiqa.core.data.repository.AcademicRepositoryImpl(
            academicDao = database.academicDao()
        )
    }

    val domainUseCasesContainer: DomainUseCasesContainer by lazy {
        DomainUseCasesContainer(
            authRepository = authRepository,
            courseRepository = courseRepository,
            userRepository = userRepository,
            quizRepository = quizRepository,
            aiRepository = aiRepository,
            downloadManager = downloadManager,
            offlineSync = offlineSyncContract,
            enterpriseRepository = enterpriseRepository,
            academicRepository = academicRepository
        )
    }

    val appNavigator: AppNavigator by lazy {
        AppNavigator()
    }
}
