package com.example.ui.navigation

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
import com.example.ui.components.OfflineModeBanner
import com.example.ui.components.RtiqaBottomBar
import com.example.ui.screens.AiTutorScreen
import com.example.ui.screens.CourseDetailScreen
import com.example.ui.screens.CoursesScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LessonPlayerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.viewmodel.AiTutorViewModel
import com.example.ui.viewmodel.CourseViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.QuizViewModel
import com.rtiqa.feature.auth.SplashScreen
import com.rtiqa.feature.auth.WelcomeScreen

@Composable
fun RtiqaApp(
    mainViewModel: MainViewModel = viewModel(),
    courseViewModel: CourseViewModel = viewModel(),
    aiTutorViewModel: AiTutorViewModel = viewModel(),
    quizViewModel: QuizViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
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
                        navController.navigate("welcome") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            composable("welcome") {
                WelcomeScreen(
                    onNavigateToLogin = {
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    },
                    onContinueAsGuest = {
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
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
                    isArabic = isArabic
                )
            }

            composable("settings") {
                SettingsScreen(
                    userProfile = userProfile,
                    isOnline = isOnline,
                    onBack = { navController.popBackStack() },
                    onToggleLanguage = { mainViewModel.toggleLanguage() },
                    onToggleTheme = { mainViewModel.toggleTheme() },
                    isArabic = isArabic
                )
            }
        }
    }
}
