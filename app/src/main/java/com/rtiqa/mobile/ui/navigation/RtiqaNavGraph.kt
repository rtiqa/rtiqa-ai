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
import com.rtiqa.mobile.ui.viewmodel.QuizViewModel
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
    val downloadedCourses by courseViewModel.downloadedCourses.collectAsState()
    val downloadedLessons by courseViewModel.downloadedLessons.collectAsState()

    val selectedCategory by courseViewModel.selectedCategory.collectAsState()
    val searchQuery by courseViewModel.searchQuery.collectAsState()

    val chatMessages by aiTutorViewModel.messages.collectAsState()
    val aiInputText by aiTutorViewModel.inputText.collectAsState()
    val isAiLoading by aiTutorViewModel.isLoading.collectAsState()

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
                val enrolledCourses by courseViewModel.enrolledCourses.collectAsState()
                val completedLessonsCount by courseViewModel.completedLessonsCount.collectAsState()
                val passedQuizzesCount by courseViewModel.passedQuizzesCount.collectAsState()

                HomeScreen(
                    userProfile = userProfile,
                    courses = courses,
                    enrolledCourses = enrolledCourses,
                    completedLessonsCount = completedLessonsCount,
                    passedQuizzesCount = passedQuizzesCount,
                    onCourseClick = { courseId ->
                        courseViewModel.selectCourse(courseId)
                        navController.navigate("course_detail/$courseId")
                    },
                    onLessonClick = { lessonId ->
                        navController.navigate("lesson_player/$lessonId")
                    },
                    onNavigate = { route -> navController.navigate(route) },
                    onToggleBookmark = { id, status -> courseViewModel.toggleBookmark(id, status) },
                    onToggleDownload = { id, status -> courseViewModel.toggleCourseDownload(id, status) },
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
                        navController.navigate("quiz")
                    }
                )
            }

            composable(
                route = "lesson_player/{lessonId}",
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
            ) { backStack ->
                val lessonId = backStack.arguments?.getString("lessonId") ?: "l_ai_1"
                val courseLessons by courseViewModel.getLessonsForCourse(courseViewModel.selectedCourseId.value).collectAsState()
                val selectedLesson = courseLessons.find { it.id == lessonId } ?: courseLessons.firstOrNull()
                val currentIndex = courseLessons.indexOfFirst { it.id == lessonId }
                val nextLesson = if (currentIndex != -1 && currentIndex + 1 < courseLessons.size) courseLessons[currentIndex + 1] else null

                LessonPlayerScreen(
                    lesson = selectedLesson,
                    onBack = { navController.popBackStack() },
                    onToggleComplete = { status ->
                        selectedLesson?.let {
                            courseViewModel.toggleLessonCompletion(it.id, it.courseId, status)
                        }
                    },
                    onAskAiAboutLesson = { prompt ->
                        aiTutorViewModel.sendMessage(prompt, isArabic)
                        navController.navigate("ai_tutor")
                    },
                    onNextLesson = if (nextLesson != null) {
                        { navController.navigate("lesson_player/${nextLesson.id}") }
                    } else null,
                    onStartQuiz = { navController.navigate("quiz") },
                    hasNextLesson = nextLesson != null,
                    isArabic = isArabic
                )
            }

            composable("ai_tutor") {
                AiTutorScreen(
                    messages = chatMessages,
                    inputText = aiInputText,
                    isLoading = isAiLoading,
                    onInputTextChange = { text -> aiTutorViewModel.updateInputText(text) },
                    onSendMessage = { prompt -> aiTutorViewModel.sendMessage(prompt, isArabic) },
                    isArabic = isArabic
                )
            }

            composable("quiz") {
                val courseLessons by courseViewModel.getLessonsForCourse(courseViewModel.selectedCourseId.value).collectAsState()
                val selectedLessonId by courseViewModel.selectedLessonId.collectAsState()
                val selectedLesson = courseLessons.find { it.id == selectedLessonId } ?: courseLessons.firstOrNull()

                QuizScreen(
                    uiState = quizUiState,
                    question = quizViewModel.currentQuestion,
                    onSelectOption = { idx -> quizViewModel.selectOption(idx) },
                    onSubmitAnswer = { quizViewModel.submitAnswer() },
                    onNextQuestion = { quizViewModel.nextQuestion() },
                    onToggleHint = { quizViewModel.toggleHint() },
                    onRestartQuiz = { quizViewModel.restartQuiz() },
                    onClaimRewards = { xp, coins ->
                        mainViewModel.addRewards(xp, coins)
                        if (quizUiState.isPassed) {
                            selectedLesson?.let {
                                courseViewModel.markLessonQuizPassed(it.id, it.courseId, true)
                            }
                        }
                        navController.popBackStack()
                    },
                    isArabic = isArabic
                )
            }

            composable("downloads") {
                DownloadsScreen(
                    downloadedCourses = downloadedCourses,
                    downloadedLessons = downloadedLessons,
                    onLessonClick = { lessonId ->
                        courseViewModel.selectLesson(lessonId)
                        navController.navigate("lesson_player/$lessonId")
                    },
                    isArabic = isArabic
                )
            }

            composable("profile") {
                ProfileScreen(
                    userProfile = userProfile,
                    onNavigateToAdmin = { navController.navigate("admin_dashboard") },
                    onLogout = {
                        scope.launch {
                            appDiContainer.authRepository.logout()
                            navController.navigate("welcome") {
                                popUpTo("home") { inclusive = true }
                            }
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
                    onNavigateToSchools = { navController.navigate("schools_management") },
                    onNavigateToUsers = { navController.navigate("users_management") },
                    onNavigateToClasses = { navController.navigate("classes_management") }
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
