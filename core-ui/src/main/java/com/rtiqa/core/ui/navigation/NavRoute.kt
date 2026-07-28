package com.rtiqa.core.ui.navigation

/**
 * Type-safe navigation destination constants for the Rtiqa application.
 */
object NavRoute {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val AUTH_LOGIN = "auth/login"
    const val AUTH_REGISTER = "auth/register"
    const val HOME_DASHBOARD = "home/dashboard"
    const val COURSES_LIST = "courses/list"
    const val COURSE_DETAIL = "courses/detail/{courseId}"
    const val LESSON_VIEWER = "lessons/viewer/{lessonId}"
    const val QUIZ_PLAY = "quiz/play/{courseId}"
    const val AI_TUTOR = "ai/tutor"
    const val OFFLINE_DOWNLOADS = "offline/downloads"
    const val PROFILE_ME = "profile/me"
    const val SETTINGS_MAIN = "settings/main"
    const val ADMIN_DASHBOARD = "admin/dashboard"

    fun createCourseDetailRoute(courseId: String): String = "courses/detail/$courseId"
    fun createLessonViewerRoute(lessonId: String): String = "lessons/viewer/$lessonId"
    fun createQuizPlayRoute(courseId: String): String = "quiz/play/$courseId"
}
