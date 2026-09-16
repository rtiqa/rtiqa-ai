import re

with open("feature-lessons/src/main/java/com/rtiqa/feature/lessons/LessonViewerViewModel.kt", "r") as f:
    content = f.read()

factory_code = """
class LessonViewerViewModelFactory(
    private val completeLessonUseCase: CompleteLessonUseCase,
    private val getLessonDetailUseCase: GetLessonDetailUseCase? = null,
    private val getNextLessonUseCase: GetNextLessonUseCase? = null,
    private val saveLessonProgressUseCase: SaveLessonProgressUseCase? = null
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LessonViewerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LessonViewerViewModel(
                completeLessonUseCase,
                getLessonDetailUseCase,
                getNextLessonUseCase,
                saveLessonProgressUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
"""

if "LessonViewerViewModelFactory" not in content:
    content += "\n" + factory_code
    with open("feature-lessons/src/main/java/com/rtiqa/feature/lessons/LessonViewerViewModel.kt", "w") as f:
        f.write(content)
