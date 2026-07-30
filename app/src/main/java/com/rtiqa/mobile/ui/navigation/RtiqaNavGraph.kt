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
import com.rtiqa.mobile.ui.screens.CourseDetailScreen
import com.rtiqa.mobile.ui.screens.CoursesScreen
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
                HomeScreen(
                    userProfile = userProfile,
                    courses = courses,
                    onCourseClick = { courseId ->
                        courseViewModel.selectCourse(courseId)
                        navController.navigate("course_detail/$courseId")
                    },
                    onNavigate = { route -> navController.navigate(route) },
                    onToggleBookmark = { id, status -> courseViewModel.toggleBookmark(id, status) },
                    onToggleDownload = { id, status -> courseViewModel.toggleCourseDownload(id, status) },
                    onToggleLanguage = { mainViewModel.toggleLanguage() },
                    isArabic = isArabic
                )
            }

            composable("courses") {
                CoursesScreen(
                    courses = courses,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery,
                    onCategorySelect = { cat -> courseViewModel.selectCategory(cat) },
                    onSearchQueryChange = { q -> courseViewModel.updateSearchQuery(q) },
                    onCourseClick = { courseId ->
                        courseViewModel.selectCourse(courseId)
                        navController.navigate("course_detail/$courseId")
                    },
                    onToggleBookmark = { id, status -> courseViewModel.toggleBookmark(id, status) },
                    onToggleDownload = { id, status -> courseViewModel.toggleCourseDownload(id, status) },
                    isArabic = isArabic
                )
            }

            composable(
                route = "course_detail/{courseId}",
                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
            ) { backStack ->
                val courseId = backStack.arguments?.getString("courseId") ?: "c_ai_101"
                val selectedCourse = courses.find { it.id == courseId } ?: courses.firstOrNull()
                val courseLessons by courseViewModel.getLessonsForCourse(courseId).collectAsState()

                CourseDetailScreen(
                    course = selectedCourse,
                    lessons = courseLessons,
                    onBack = { navController.popBackStack() },
                    onLessonClick = { lessonId ->
                        courseViewModel.selectLesson(lessonId)
                        navController.navigate("lesson_player/$lessonId")
                    },
                    onQuizClick = { navController.navigate("quiz") },
                    onToggleDownloadLesson = { lessonId, status ->
                        courseViewModel.toggleLessonDownload(lessonId, status)
                    },
                    isArabic = isArabic
                )
            }

            composable(
                route = "lesson_player/{lessonId}",
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
            ) { backStack ->
                val lessonId = backStack.arguments?.getString("lessonId") ?: "l_ai_1"
                val courseLessons by courseViewModel.getLessonsForCourse(courseViewModel.selectedCourseId.value).collectAsState()
                val selectedLesson = courseLessons.find { it.id == lessonId } ?: courseLessons.firstOrNull()

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
                QuizScreen(
                    uiState = quizUiState,
                    question = quizViewModel.currentQuestion,
                    onSelectOption = { idx -> quizViewModel.selectOption(idx) },
                    onSubmitAnswer = { quizViewModel.submitAnswer() },
                    onNextQuestion = { quizViewModel.nextQuestion() },
                    onToggleHint = { quizViewModel.toggleHint() },
                    onRestartQuiz = { quizViewModel.restartQuiz() },
                    onClaimRewards = { xp, coins -> mainViewModel.addRewards(xp, coins) },
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
                    onNavigateToAcademicPlatform = { navController.navigate("academic_platform") }
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
