package com.rtiqa.mobile.ui.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.rtiqa.core.data.di.AppDiContainer
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rtiqa.mobile.ui.components.OfflineModeBanner
import com.rtiqa.mobile.ui.components.RtiqaBottomBar
import com.rtiqa.mobile.ui.screens.AiTutorScreen
import com.rtiqa.mobile.ui.screens.DownloadsScreen
import com.rtiqa.mobile.ui.screens.HomeScreen
import com.rtiqa.mobile.ui.screens.LessonPlayerScreen
import com.rtiqa.mobile.ui.screens.ProfileScreen
import com.rtiqa.mobile.ui.screens.QuizScreen
import com.rtiqa.mobile.ui.screens.SettingsScreen
import com.rtiqa.mobile.ui.viewmodel.AiTutorViewModel
import com.rtiqa.mobile.ui.viewmodel.CourseViewModel
import com.rtiqa.mobile.ui.viewmodel.MainViewModel
import com.rtiqa.mobile.ui.viewmodel.HomeDashboardViewModelFactory
import com.rtiqa.mobile.ui.viewmodel.OfflineDownloadsViewModelFactory
import com.rtiqa.mobile.ui.viewmodel.ProfileViewModelFactory
import com.rtiqa.mobile.ui.viewmodel.QuizViewModel
import com.rtiqa.feature.home.HomeDashboardViewModel
import com.rtiqa.feature.offline.OfflineDownloadsViewModel
import com.rtiqa.feature.profile.ProfileViewModel
import com.rtiqa.feature.courses.CoursesListScreen
import com.rtiqa.feature.courses.CourseDetailScreen as FeatureCourseDetailScreen
import com.rtiqa.feature.courses.CoursesListViewModel
import com.rtiqa.feature.courses.CourseDetailViewModel
import com.rtiqa.feature.courses.CoursesListViewModelFactory
import com.rtiqa.feature.courses.CourseDetailViewModelFactory
import com.rtiqa.feature.auth.ForgotPasswordScreen
import com.rtiqa.feature.auth.LoginScreen
import com.rtiqa.feature.auth.LoginViewModel
import com.rtiqa.feature.auth.LoginViewModelFactory
import com.rtiqa.feature.auth.RegisterScreen
import com.rtiqa.feature.auth.RegisterViewModel
import com.rtiqa.feature.auth.RegisterViewModelFactory
import com.rtiqa.feature.auth.SplashScreen
import com.rtiqa.feature.auth.WelcomeScreen

import com.rtiqa.feature.admin.AdminDashboardViewModel
import com.rtiqa.feature.admin.AdminScreen
import com.rtiqa.feature.admin.classes.ClassesScreen
import com.rtiqa.feature.admin.classes.ClassesViewModel
import com.rtiqa.feature.admin.classes.ClassesViewModelFactory
import com.rtiqa.feature.admin.school.SchoolsScreen
import com.rtiqa.feature.admin.school.SchoolViewModel
import com.rtiqa.feature.admin.school.SchoolViewModelFactory
import com.rtiqa.feature.admin.users.UsersScreen
import com.rtiqa.feature.admin.users.UserManagementViewModel
import com.rtiqa.feature.admin.users.UserViewModelFactory
import com.rtiqa.feature.admin.teacher.TeacherDashboardScreen
import com.rtiqa.feature.admin.teacher.TeacherDashboardViewModel
import com.rtiqa.feature.admin.teacher.TeacherDashboardUiAction

@Composable
fun RtiqaApp(
    mainViewModel: MainViewModel = viewModel(),
    courseViewModel: CourseViewModel = viewModel(),
    aiTutorViewModel: AiTutorViewModel = viewModel(),
    quizViewModel: QuizViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val appDiContainer = remember(context) { AppDiContainer(context.applicationContext) }
    val scope = rememberCoroutineScope()

    val activeSession by appDiContainer.authRepository.observeUserSession().collectAsState(initial = null)
    val userProfile by mainViewModel.userProfile.collectAsState()
    val isOnline by mainViewModel.isOnline.collectAsState()

    val courses by courseViewModel.filteredCourses.collectAsState()
    val bookmarkedCourses by courseViewModel.bookmarkedCourses.collectAsState()
    val selectedCategory by courseViewModel.selectedCategory.collectAsState()
    val searchQuery by courseViewModel.searchQuery.collectAsState()

    val chatMessages by aiTutorViewModel.messages.collectAsState()
    val aiInputText by aiTutorViewModel.inputText.collectAsState()
    val isAiLoading by aiTutorViewModel.isLoading.collectAsState()
    val aiErrorMessage by aiTutorViewModel.errorMessage.collectAsState()

    val quizUiState by quizViewModel.uiState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val isArabic = userProfile.language == "ar"
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
        topBar = {
            OfflineModeBanner(isOnline = isOnline, isArabic = isArabic)
        },
        bottomBar = {
            if (currentRoute in listOf("home", "courses", "ai_tutor", "quiz", "downloads", "profile")) {
                RtiqaBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    isArabic = isArabic
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                SplashScreen(
                    onSplashFinished = {
                        val destination = if (activeSession != null) "home" else "welcome"
                        navController.navigate(destination) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            composable("welcome") {
                WelcomeScreen(
                    onNavigateToLogin = {
                        navController.navigate("login")
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    },
                    onContinueAsGuest = {
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                )
            }

            composable("login") {
                val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(appDiContainer.authRepository))
                LoginScreen(
                    viewModel = loginViewModel,
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                    onBack = { navController.popBackStack() },
                    isArabic = isArabic
                )
            }

            composable("register") {
                val registerViewModel: RegisterViewModel = viewModel(factory = RegisterViewModelFactory(appDiContainer.authRepository))
                RegisterScreen(
                    viewModel = registerViewModel,
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.navigate("login") },
                    onBack = { navController.popBackStack() },
                    isArabic = isArabic
                )
            }

            composable("forgot_password") {
                val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(appDiContainer.authRepository))
                ForgotPasswordScreen(
                    viewModel = loginViewModel,
                    onBack = { navController.popBackStack() },
                    isArabic = isArabic
                )
            }

            composable("home") {
                val homeViewModel: HomeDashboardViewModel = viewModel(
                    factory = HomeDashboardViewModelFactory(appDiContainer)
                )
                val homeUiState by homeViewModel.uiState.collectAsState()

                HomeScreen(
                    userProfile = homeUiState.userProfile,
                    courses = homeUiState.featuredCourses,
                    currentLesson = homeUiState.currentLesson,
                    completedLessonsCount = homeUiState.completedLessonsCount,
                    passedQuizzesCount = homeUiState.passedQuizzesCount,
                    onCourseClick = { courseId ->
                        navController.navigate("course_detail/$courseId")
                    },
                    onLessonClick = { lessonId ->
                        navController.navigate("lesson_player/$lessonId")
                    },
                    onNavigate = { route -> navController.navigate(route) },
                    onToggleBookmark = { id, status ->
                        scope.launch {
                            appDiContainer.domainUseCasesContainer.toggleBookmarkUseCase(id, !status)
                        }
                    },
                    onToggleDownload = { id, status ->
                        scope.launch {
                            if (status) {
                                appDiContainer.domainUseCasesContainer.deleteCourseDownloadUseCase(id)
                            } else {
                                appDiContainer.domainUseCasesContainer.downloadCourseUseCase(id)
                            }
                        }
                    },
                    onToggleLanguage = { mainViewModel.toggleLanguage() },
                    isArabic = isArabic
                )
            }

            composable("courses") {
                val coursesListViewModel: CoursesListViewModel = viewModel(
                    factory = CoursesListViewModelFactory(appDiContainer)
                )
                CoursesListScreen(
                    viewModel = coursesListViewModel,
                    onNavigateToDetail = { courseId ->
                        navController.navigate("course_detail/$courseId")
                    }
                )
            }

            composable(
                route = "course_detail/{courseId}",
                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
            ) { backStack ->
                val courseId = backStack.arguments?.getString("courseId") ?: ""
                val courseDetailViewModel: CourseDetailViewModel = viewModel(
                    key = "course_detail_$courseId",
                    factory = CourseDetailViewModelFactory(appDiContainer)
                )
                FeatureCourseDetailScreen(
                    courseId = courseId,
                    viewModel = courseDetailViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToLesson = { lessonId ->
                        navController.navigate("lesson_player/$lessonId")
                    },
                    onNavigateToQuiz = { cId ->
                        val targetCourseId = if (cId.isNotBlank()) cId else courseId
                        if (targetCourseId.isNotBlank()) {
                            navController.navigate("quiz/play/$targetCourseId")
                        } else {
                            navController.navigate("quiz")
                        }
                    }
                )
            }

            composable(
                route = "lesson_player/{lessonId}",
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
            ) { backStack ->
                val lessonId = backStack.arguments?.getString("lessonId") ?: ""
                val allLessons by courseViewModel.allLessons.collectAsState()
                val matchingLesson = allLessons.find { it.id == lessonId }
                val courseId = matchingLesson?.courseId
                val isQuizPassed = matchingLesson?.isQuizPassed == true

                val viewerViewModel: com.rtiqa.feature.lessons.LessonViewerViewModel = viewModel(
                    key = "lesson_viewer_$lessonId",
                    factory = com.rtiqa.feature.lessons.LessonViewerViewModelFactory(
                        completeLessonUseCase = appDiContainer.domainUseCasesContainer.completeLessonUseCase,
                        getLessonDetailUseCase = appDiContainer.domainUseCasesContainer.getLessonDetailUseCase,
                        getNextLessonUseCase = appDiContainer.domainUseCasesContainer.getNextLessonUseCase,
                        saveLessonProgressUseCase = appDiContainer.domainUseCasesContainer.saveLessonProgressUseCase,
                        getLessonsForCourseUseCase = appDiContainer.domainUseCasesContainer.getLessonsForCourseUseCase
                    )
                )

                androidx.compose.runtime.LaunchedEffect(lessonId, courseId) {
                    if (lessonId.isNotBlank() && !courseId.isNullOrBlank()) {
                        viewerViewModel.onAction(
                            com.rtiqa.feature.lessons.LessonViewerUiAction.InitializeLesson(
                                lessonId = lessonId,
                                courseId = courseId
                            )
                        )
                    }
                }

                val uiState by viewerViewModel.uiState.collectAsState()

                val effectiveUiState = if (courseId.isNullOrBlank()) {
                    uiState.copy(isLoading = true)
                } else {
                    uiState
                }

                com.rtiqa.feature.lessons.LessonDetailsScreen(
                    uiState = effectiveUiState,
                    onAction = { viewerViewModel.onAction(it) },
                    onBack = { navController.popBackStack() },
                    onStartQuiz = {
                        val targetCourseId = courseId ?: ""
                        if (targetCourseId.isNotBlank()) {
                            navController.navigate("quiz/play/$targetCourseId?lessonId=$lessonId")
                        } else {
                            navController.navigate("quiz")
                        }
                    },
                    onNavigateToLesson = { targetLessonId ->
                        navController.navigate("lesson_player/$targetLessonId")
                    },
                    isQuizPassed = isQuizPassed,
                    durationMinutes = matchingLesson?.durationMinutes
                )
            }

            composable(
                route = "quiz/play/{courseId}?lessonId={lessonId}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType },
                    navArgument("lessonId") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    }
                )
            ) { backStack ->
                val courseId = backStack.arguments?.getString("courseId") ?: ""
                val lessonId = backStack.arguments?.getString("lessonId") ?: ""
                val allLessons by courseViewModel.allLessons.collectAsState()
                val wasAlreadyPassed = allLessons.find { it.id == lessonId }?.isQuizPassed == true

                val quizPlayViewModel: com.rtiqa.feature.quiz.QuizPlayViewModel = viewModel(
                    key = "quiz_play_${courseId}_$lessonId",
                    factory = com.rtiqa.feature.quiz.QuizPlayViewModelFactory(
                        getQuizForCourseUseCase = appDiContainer.domainUseCasesContainer.getQuizForCourseUseCase,
                        submitQuizResultUseCase = appDiContainer.domainUseCasesContainer.submitQuizResultUseCase,
                        evaluateQuizAnswersUseCase = appDiContainer.domainUseCasesContainer.evaluateQuizAnswersUseCase
                    )
                )

                androidx.compose.runtime.LaunchedEffect(courseId, lessonId) {
                    if (courseId.isNotBlank()) {
                        quizPlayViewModel.onAction(
                            com.rtiqa.feature.quiz.QuizPlayUiAction.LoadQuizForCourse(
                                courseId = courseId,
                                lessonId = lessonId
                            )
                        )
                    }
                }

                val quizState by quizPlayViewModel.uiState.collectAsState()

                com.rtiqa.feature.quiz.QuizPlayScreen(
                    uiState = quizState,
                    onAction = { quizPlayViewModel.onAction(it) },
                    onBack = { navController.popBackStack() },
                    onQuizCompleted = { isPassed, xpEarned ->
                        if (isPassed && lessonId.isNotBlank()) {
                            courseViewModel.markLessonQuizPassed(lessonId, courseId, true)
                            scope.launch {
                                appDiContainer.courseRepository.markLessonCompleted(lessonId, courseId)
                            }
                        }
                        if (isPassed && !wasAlreadyPassed && xpEarned > 0) {
                            mainViewModel.addRewards(xpEarned, 0)
                        }
                    },
                    isArabic = isArabic
                )
            }

            composable("ai_tutor") {
                AiTutorScreen(
                    messages = chatMessages,
                    inputText = aiInputText,
                    isLoading = isAiLoading,
                    errorMessage = aiErrorMessage,
                    onDismissError = { aiTutorViewModel.dismissError() },
                    onInputTextChange = { text -> aiTutorViewModel.updateInputText(text) },
                    onSendMessage = { prompt -> aiTutorViewModel.sendMessage(prompt, isArabic) },
                    isArabic = isArabic
                )
            }

            composable("quiz") {
                val selectedCourseId by courseViewModel.selectedCourseId.collectAsState()
                val allCourses by courseViewModel.filteredCourses.collectAsState()
                val effectiveCourseId = selectedCourseId.ifBlank { allCourses.firstOrNull()?.id ?: "" }

                val quizPlayViewModel: com.rtiqa.feature.quiz.QuizPlayViewModel = viewModel(
                    key = "quiz_play_tab_$effectiveCourseId",
                    factory = com.rtiqa.feature.quiz.QuizPlayViewModelFactory(
                        getQuizForCourseUseCase = appDiContainer.domainUseCasesContainer.getQuizForCourseUseCase,
                        submitQuizResultUseCase = appDiContainer.domainUseCasesContainer.submitQuizResultUseCase,
                        evaluateQuizAnswersUseCase = appDiContainer.domainUseCasesContainer.evaluateQuizAnswersUseCase
                    )
                )

                androidx.compose.runtime.LaunchedEffect(effectiveCourseId) {
                    if (effectiveCourseId.isNotBlank()) {
                        quizPlayViewModel.onAction(
                            com.rtiqa.feature.quiz.QuizPlayUiAction.LoadQuizForCourse(
                                courseId = effectiveCourseId
                            )
                        )
                    }
                }

                val quizState by quizPlayViewModel.uiState.collectAsState()

                com.rtiqa.feature.quiz.QuizPlayScreen(
                    uiState = quizState,
                    onAction = { quizPlayViewModel.onAction(it) },
                    onBack = { navController.popBackStack() },
                    onQuizCompleted = { isPassed, xpEarned ->
                        if (xpEarned > 0) {
                            mainViewModel.addRewards(xpEarned, 0)
                        }
                    },
                    isArabic = isArabic
                )
            }

            composable("downloads") {
                val offlineDownloadsViewModel: OfflineDownloadsViewModel = viewModel(
                    factory = OfflineDownloadsViewModelFactory(appDiContainer)
                )
                DownloadsScreen(
                    viewModel = offlineDownloadsViewModel,
                    onNavigateToCourse = { courseId ->
                        navController.navigate("course_detail/$courseId")
                    },
                    isArabic = isArabic
                )
            }

            composable("profile") {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(appDiContainer)
                )
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToAdmin = { navController.navigate("admin_dashboard") },
                    onNavigateToLogin = {
                        navController.navigate("welcome") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    isArabic = isArabic
                )
            }

            composable("admin_dashboard") {
                val adminViewModel: AdminDashboardViewModel = viewModel()
                val adminUiState by adminViewModel.uiState.collectAsState()
                AdminScreen(
                    uiState = adminUiState,
                    onAction = { action -> adminViewModel.onAction(action) },
                    onBack = { navController.popBackStack() },
                    onNavigateToAcademicPlatform = { navController.navigate("academic_platform") },
                    onNavigateToAcademicStructure = { navController.navigate("academic_structure") },
                    onNavigateToSchools = { navController.navigate("schools_management") },
                    onNavigateToUsers = { navController.navigate("users_management") },
                    onNavigateToClasses = { navController.navigate("classes_management") },
                    onNavigateToTeacherDashboard = { navController.navigate("teacher_dashboard") }
                )
            }

            composable("teacher_dashboard") {
                val teacherViewModel: TeacherDashboardViewModel = viewModel()
                val teacherUiState by teacherViewModel.uiState.collectAsState()
                TeacherDashboardScreen(
                    uiState = teacherUiState,
                    uiEvent = teacherViewModel.uiEvent,
                    onAction = { action -> teacherViewModel.onAction(action) },
                    onBack = { navController.popBackStack() },
                    onNavigateToClasses = { navController.navigate("classes_management") },
                    onNavigateToAcademicPlatform = { navController.navigate("academic_platform") }
                )
            }

            composable("classes_management") {
                val classesViewModel: ClassesViewModel = viewModel(
                    factory = ClassesViewModelFactory(
                        getClassesForSchoolUseCase = appDiContainer.domainUseCasesContainer.getClassesForSchoolUseCase!!,
                        saveClassUseCase = appDiContainer.domainUseCasesContainer.saveClassUseCase!!,
                        deleteClassUseCase = appDiContainer.domainUseCasesContainer.deleteClassUseCase!!,
                        reorderClassesUseCase = appDiContainer.domainUseCasesContainer.reorderClassesUseCase!!,
                        getSchoolsUseCase = appDiContainer.domainUseCasesContainer.getSchoolsUseCase!!,
                        preferencesDataStore = appDiContainer.preferencesDataStore
                    )
                )
                val classesUiState by classesViewModel.uiState.collectAsState()
                ClassesScreen(
                    uiState = classesUiState,
                    onAction = { action -> classesViewModel.onAction(action) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("users_management") {
                val userViewModel: UserManagementViewModel = viewModel(
                    factory = UserViewModelFactory(
                        getUsersForSchoolUseCase = appDiContainer.domainUseCasesContainer.getUsersForSchoolUseCase!!,
                        saveEnterpriseMemberUseCase = appDiContainer.domainUseCasesContainer.saveEnterpriseMemberUseCase!!,
                        deleteEnterpriseMemberUseCase = appDiContainer.domainUseCasesContainer.deleteEnterpriseMemberUseCase!!,
                        getSchoolsUseCase = appDiContainer.domainUseCasesContainer.getSchoolsUseCase!!,
                        preferencesDataStore = appDiContainer.preferencesDataStore
                    )
                )
                val userUiState by userViewModel.uiState.collectAsState()
                UsersScreen(
                    uiState = userUiState,
                    onAction = { action -> userViewModel.onAction(action) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("academic_structure") {
                val academicStructureViewModel: com.rtiqa.feature.admin.academic.AcademicStructureViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.rtiqa.feature.admin.academic.AcademicStructureViewModelFactory(
                        getAcademicYearsUseCase = appDiContainer.domainUseCasesContainer.getAcademicYearsUseCase!!,
                        saveAcademicYearUseCase = appDiContainer.domainUseCasesContainer.saveAcademicYearUseCase!!,
                        deleteAcademicYearUseCase = appDiContainer.domainUseCasesContainer.deleteAcademicYearUseCase!!,
                        getSemestersUseCase = appDiContainer.domainUseCasesContainer.getSemestersUseCase!!,
                        saveSemesterUseCase = appDiContainer.domainUseCasesContainer.saveSemesterUseCase!!,
                        deleteSemesterUseCase = appDiContainer.domainUseCasesContainer.deleteSemesterUseCase!!,
                        getGradeLevelsForSchoolUseCase = appDiContainer.domainUseCasesContainer.getGradeLevelsForSchoolUseCase!!,
                        saveGradeLevelUseCase = appDiContainer.domainUseCasesContainer.saveGradeLevelUseCase!!,
                        deleteGradeLevelUseCase = appDiContainer.domainUseCasesContainer.deleteGradeLevelUseCase!!,
                        getDepartmentsUseCase = appDiContainer.domainUseCasesContainer.getDepartmentsUseCase!!,
                        saveDepartmentUseCase = appDiContainer.domainUseCasesContainer.saveDepartmentUseCase!!,
                        deleteDepartmentUseCase = appDiContainer.domainUseCasesContainer.deleteDepartmentUseCase!!,
                        getMajorsUseCase = appDiContainer.domainUseCasesContainer.getMajorsUseCase!!,
                        saveMajorUseCase = appDiContainer.domainUseCasesContainer.saveMajorUseCase!!,
                        deleteMajorUseCase = appDiContainer.domainUseCasesContainer.deleteMajorUseCase!!,
                        getSubjectsForSchoolUseCase = appDiContainer.domainUseCasesContainer.getSubjectsForSchoolUseCase!!,
                        saveSubjectUseCase = appDiContainer.domainUseCasesContainer.saveSubjectUseCase!!,
                        deleteSubjectUseCase = appDiContainer.domainUseCasesContainer.deleteSubjectUseCase!!,
                        getSectionsForSchoolUseCase = appDiContainer.domainUseCasesContainer.getSectionsForSchoolUseCase!!,
                        saveSectionUseCase = appDiContainer.domainUseCasesContainer.saveSectionUseCase!!,
                        deleteSectionUseCase = appDiContainer.domainUseCasesContainer.deleteSectionUseCase!!,
                        getStudyPlansUseCase = appDiContainer.domainUseCasesContainer.getStudyPlansUseCase!!,
                        saveStudyPlanUseCase = appDiContainer.domainUseCasesContainer.saveStudyPlanUseCase!!,
                        deleteStudyPlanUseCase = appDiContainer.domainUseCasesContainer.deleteStudyPlanUseCase!!
                    )
                )
                val academicUiState by academicStructureViewModel.uiState.collectAsState()
                com.rtiqa.feature.admin.academic.AcademicStructureScreen(
                    uiState = academicUiState,
                    onAction = { action -> academicStructureViewModel.onAction(action) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("schools_management") {
                val schoolViewModel: SchoolViewModel = viewModel(
                    factory = SchoolViewModelFactory(
                        getSchoolsUseCase = appDiContainer.domainUseCasesContainer.getSchoolsUseCase!!,
                        saveSchoolUseCase = appDiContainer.domainUseCasesContainer.saveSchoolUseCase!!,
                        deleteSchoolUseCase = appDiContainer.domainUseCasesContainer.deleteSchoolUseCase!!,
                        getStudentsForSchoolUseCase = appDiContainer.domainUseCasesContainer.getStudentsForSchoolUseCase!!,
                        getTeachersForSchoolUseCase = appDiContainer.domainUseCasesContainer.getTeachersForSchoolUseCase!!,
                        getSectionsForSchoolUseCase = appDiContainer.domainUseCasesContainer.getSectionsForSchoolUseCase!!,
                        getSubjectsForSchoolUseCase = appDiContainer.domainUseCasesContainer.getSubjectsForSchoolUseCase!!,
                        getCoursesForSchoolUseCase = appDiContainer.domainUseCasesContainer.getCoursesForSchoolUseCase,
                        getAssessmentsForSchoolUseCase = appDiContainer.domainUseCasesContainer.getAssessmentsForSchoolUseCase,
                        saveEnterpriseMemberUseCase = appDiContainer.domainUseCasesContainer.saveEnterpriseMemberUseCase,
                        saveCourseUseCase = appDiContainer.domainUseCasesContainer.saveCourseUseCase,
                        saveAssessmentUseCase = appDiContainer.domainUseCasesContainer.saveAssessmentUseCase,
                        getGradeLevelsForSchoolUseCase = appDiContainer.domainUseCasesContainer.getGradeLevelsForSchoolUseCase,
                        saveGradeLevelUseCase = appDiContainer.domainUseCasesContainer.saveGradeLevelUseCase,
                        deleteGradeLevelUseCase = appDiContainer.domainUseCasesContainer.deleteGradeLevelUseCase,
                        getClassesForSchoolUseCase = appDiContainer.domainUseCasesContainer.getClassesForSchoolUseCase,
                        saveClassUseCase = appDiContainer.domainUseCasesContainer.saveClassUseCase,
                        deleteClassUseCase = appDiContainer.domainUseCasesContainer.deleteClassUseCase,
                        getAcademicYearsUseCase = appDiContainer.domainUseCasesContainer.getAcademicYearsUseCase,
                        saveAcademicYearUseCase = appDiContainer.domainUseCasesContainer.saveAcademicYearUseCase,
                        deleteAcademicYearUseCase = appDiContainer.domainUseCasesContainer.deleteAcademicYearUseCase,
                        saveSectionUseCase = appDiContainer.domainUseCasesContainer.saveSectionUseCase,
                        deleteSectionUseCase = appDiContainer.domainUseCasesContainer.deleteSectionUseCase,
                        saveSubjectUseCase = appDiContainer.domainUseCasesContainer.saveSubjectUseCase,
                        deleteSubjectUseCase = appDiContainer.domainUseCasesContainer.deleteSubjectUseCase,
                        preferencesDataStore = appDiContainer.preferencesDataStore
                    )
                )
                val schoolUiState by schoolViewModel.uiState.collectAsState()
                SchoolsScreen(
                    uiState = schoolUiState,
                    onAction = { action -> schoolViewModel.onAction(action) },
                    onBack = { navController.popBackStack() }
                )
            }

            
            composable(com.rtiqa.feature.lessons.LessonRoutes.VIEWER) { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                val courseId = "course_demo" // fallback for demo since not passed in route
                val viewModel: com.rtiqa.feature.lessons.LessonViewerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.rtiqa.feature.lessons.LessonViewerViewModelFactory(
                        completeLessonUseCase = appDiContainer.domainUseCasesContainer.completeLessonUseCase,
                        getLessonDetailUseCase = appDiContainer.domainUseCasesContainer.getLessonDetailUseCase,
                        getNextLessonUseCase = appDiContainer.domainUseCasesContainer.getNextLessonUseCase,
                        saveLessonProgressUseCase = appDiContainer.domainUseCasesContainer.saveLessonProgressUseCase,
                        getLessonsForCourseUseCase = appDiContainer.domainUseCasesContainer.getLessonsForCourseUseCase
                    )
                )
                
                // Initialize if needed
                androidx.compose.runtime.LaunchedEffect(lessonId) {
                    viewModel.onAction(com.rtiqa.feature.lessons.LessonViewerUiAction.InitializeLesson(lessonId, courseId))
                }

                val uiState by viewModel.uiState.collectAsState()
                
                com.rtiqa.feature.lessons.LessonDetailsScreen(
                    uiState = uiState,
                    onAction = { viewModel.onAction(it) },
                    onBack = { navController.popBackStack() },
                    onStartQuiz = { /* No-op for now based on requirements */ },
                    onNavigateToLesson = { targetId ->
                        navController.navigate("lesson_player/$targetId")
                    }
                )
            }

            composable("academic_platform") {
                val academicViewModel: com.rtiqa.mobile.ui.viewmodel.AcademicPlatformViewModel = viewModel()
                val academicUiState by academicViewModel.uiState.collectAsState()
                com.rtiqa.mobile.ui.screens.AcademicPlatformScreen(
                    uiState = academicUiState,
                    onAction = { action -> academicViewModel.onAction(action) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    userProfile = userProfile,
                    isOnline = isOnline,
                    onBack = { navController.popBackStack() },
                    onToggleLanguage = { mainViewModel.toggleLanguage() },
                    onToggleTheme = { mainViewModel.toggleTheme() },
                    onToggleOfflineAutoSync = { mainViewModel.toggleOfflineAutoSync() },
                    isArabic = isArabic
                )
            }
        }
    }
}
}
