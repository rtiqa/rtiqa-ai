import re

with open("app/src/main/java/com/rtiqa/mobile/ui/navigation/RtiqaNavGraph.kt", "r") as f:
    content = f.read()

route_code = """
            composable(com.rtiqa.feature.lessons.LessonRoutes.VIEWER) { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                val courseId = "course_demo" // fallback for demo since not passed in route
                val viewModel: com.rtiqa.feature.lessons.LessonViewerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.rtiqa.feature.lessons.LessonViewerViewModelFactory(
                        completeLessonUseCase = appDiContainer.domainUseCasesContainer.completeLessonUseCase,
                        getLessonDetailUseCase = appDiContainer.domainUseCasesContainer.getLessonDetailUseCase,
                        getNextLessonUseCase = appDiContainer.domainUseCasesContainer.getNextLessonUseCase,
                        saveLessonProgressUseCase = appDiContainer.domainUseCasesContainer.saveLessonProgressUseCase
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
                    onStartQuiz = { /* No-op for now based on requirements */ }
                )
            }
"""

if "LessonRoutes.VIEWER" not in content:
    content = content.replace('composable("academic_platform") {', route_code + '\n            composable("academic_platform") {')
    with open("app/src/main/java/com/rtiqa/mobile/ui/navigation/RtiqaNavGraph.kt", "w") as f:
        f.write(content)
