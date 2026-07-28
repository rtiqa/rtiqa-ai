package com.example.domain.model

data class Course(
    val id: String,
    val title: String,
    val titleAr: String,
    val category: String,
    val categoryAr: String,
    val description: String,
    val descriptionAr: String,
    val rating: Float,
    val durationMinutes: Int,
    val totalLessons: Int,
    val enrolledCount: Int,
    val imageResName: String,
    val level: String, // Beginner, Intermediate, Advanced
    val tags: List<String>,
    val progressPercent: Float = 0f,
    val isBookmarked: Boolean = false,
    val isDownloaded: Boolean = false
)

enum class CourseCategory(val displayName: String, val displayNameAr: String) {
    ALL("All Categories", "جميع التصنيفات"),
    AI_DATA("AI & Data Science", "الذكاء الاصطناعي والبيانات"),
    COMPUTER_SCIENCE("Computer Science", "علوم الحاسوب"),
    MATH("Mathematics", "الرياضيات"),
    LANGUAGES("Languages", "اللغات والترجمة"),
    PHYSICS("Physics & Engineering", "الفيزياء والهندسة")
}
