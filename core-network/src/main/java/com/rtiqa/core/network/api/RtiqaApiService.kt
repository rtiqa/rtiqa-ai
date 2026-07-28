package com.rtiqa.core.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class NetworkUserDto(
    val id: String,
    val email: String,
    val name: String,
    val streakCount: Int,
    val totalXp: Int
)

data class LoginRequestDto(
    val email: String,
    val passwordHash: String
)

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val passwordHash: String
)

data class AuthResponseDto(
    val token: String,
    val user: NetworkUserDto
)

data class NetworkCourseDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val difficulty: String,
    val totalModules: Int,
    val completedModules: Int,
    val progressPercent: Float
)

data class NetworkLessonDto(
    val id: String,
    val courseId: String,
    val title: String,
    val content: String,
    val moduleOrder: Int,
    val estimatedMinutes: Int
)

data class NetworkSyncPayloadDto(
    val userId: String,
    val progressUpdates: List<Map<String, String>>,
    val lastSyncedTimestamp: Long
)

data class NetworkSyncResponseDto(
    val success: Boolean,
    val syncedAt: Long,
    val serverMessage: String
)

/**
 * Production Retrofit interface for Rtiqa remote backend services.
 */
interface RtiqaApiService {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<AuthResponseDto>

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): Response<AuthResponseDto>

    @GET("api/v1/user/profile")
    suspend fun getUserProfile(): Response<NetworkUserDto>

    @GET("api/v1/courses")
    suspend fun getCourses(
        @Query("category") category: String? = null
    ): Response<List<NetworkCourseDto>>

    @GET("api/v1/courses/{courseId}/lessons")
    suspend fun getCourseLessons(
        @Path("courseId") courseId: String
    ): Response<List<NetworkLessonDto>>

    @POST("api/v1/sync")
    suspend fun syncOfflineData(
        @Body payload: NetworkSyncPayloadDto
    ): Response<NetworkSyncResponseDto>
}
