package com.rtiqa.core.network.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// --- Auth & Users ---

@JsonClass(generateAdapter = true)
data class RestLoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String // Plain text over TLS per contract
)

@JsonClass(generateAdapter = true)
data class RestRegisterRequest(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RestAuthUserDto(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String,
    @Json(name = "name") val name: String,
    @Json(name = "language") val language: String?,
    @Json(name = "is_dark_mode") val isDarkMode: Boolean?
)

@JsonClass(generateAdapter = true)
data class RestLoginResponse(
    @Json(name = "token") val token: String,
    @Json(name = "user") val user: RestAuthUserDto,
    @Json(name = "activeTenantId") val activeTenantId: String?
)

@JsonClass(generateAdapter = true)
data class RestUpdateUserProfileRequest(
    @Json(name = "name") val name: String?,
    @Json(name = "language") val language: String?,
    @Json(name = "is_dark_mode") val isDarkMode: Boolean?
)

// --- Organizations & Academic ---

@JsonClass(generateAdapter = true)
data class RestOrganizationDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class RestMembershipDto(
    @Json(name = "id") val id: String,
    @Json(name = "organization_id") val organizationId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "role") val role: String
)

@JsonClass(generateAdapter = true)
data class RestAcademicYearDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    @Json(name = "is_active") val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class RestTermDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class RestGradeLevelDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "level_order") val levelOrder: Int
)

@JsonClass(generateAdapter = true)
data class RestSubjectDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String
)

// --- Courses & Lessons ---

@JsonClass(generateAdapter = true)
data class RestClassroomDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class RestCourseDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String?
)

@JsonClass(generateAdapter = true)
data class RestLessonDto(
    @Json(name = "id") val id: String,
    @Json(name = "course_id") val courseId: String,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String?
)

// --- Assignments ---

@JsonClass(generateAdapter = true)
data class RestAssignmentDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "due_date") val dueDate: String?
)

@JsonClass(generateAdapter = true)
data class RestSubmissionRequestDto(
    @Json(name = "content") val content: String,
    @Json(name = "attachments") val attachments: List<String>?
)

@JsonClass(generateAdapter = true)
data class RestSubmissionResponseDto(
    @Json(name = "id") val id: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class RestAssessmentDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String
)

@JsonClass(generateAdapter = true)
data class RestGradeDto(
    @Json(name = "id") val id: String,
    @Json(name = "score") val score: Float
)

// --- AI Contract ---

@JsonClass(generateAdapter = true)
data class RestAiConversationRequestDto(
    @Json(name = "course_id") val courseId: String?,
    @Json(name = "context") val context: String?
)

@JsonClass(generateAdapter = true)
data class RestAiConversationResponseDto(
    @Json(name = "id") val id: String
)

@JsonClass(generateAdapter = true)
data class RestAiMessageRequestDto(
    @Json(name = "prompt") val prompt: String
)

@JsonClass(generateAdapter = true)
data class RestAiMessageResponseDto(
    @Json(name = "id") val id: String,
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

// --- Storage Contract ---

@JsonClass(generateAdapter = true)
data class RestPresignedUploadRequestDto(
    @Json(name = "filename") val filename: String,
    @Json(name = "content_type") val contentType: String,
    @Json(name = "size") val size: Long
)

@JsonClass(generateAdapter = true)
data class RestPresignedUploadResponseDto(
    @Json(name = "upload_url") val uploadUrl: String,
    @Json(name = "object_key") val objectKey: String
)

@JsonClass(generateAdapter = true)
data class RestStorageObjectDto(
    @Json(name = "id") val id: String,
    @Json(name = "url") val url: String
)

// --- Sync Contract ---

@JsonClass(generateAdapter = true)
data class RestSyncOperationDto(
    @Json(name = "id") val id: String, // UUID for idempotency
    @Json(name = "action_type") val actionType: String,
    @Json(name = "payload") val payload: String, // JSON payload string matching typed DTO
    @Json(name = "created_at") val createdAt: Long
)

@JsonClass(generateAdapter = true)
data class RestSyncPushRequestDto(
    @Json(name = "operations") val operations: List<RestSyncOperationDto>
)

@JsonClass(generateAdapter = true)
data class RestSyncPushResponseDto(
    @Json(name = "success_ids") val successIds: List<String>,
    @Json(name = "failed_ids") val failedIds: List<String>
)

@JsonClass(generateAdapter = true)
data class RestSyncPullResponseDto(
    @Json(name = "server_timestamp") val serverTimestamp: Long,
    @Json(name = "updates") val updates: List<Map<String, Any>> // Example shape
)

// ==========================================
// RETROFIT API CONTRACT
// ==========================================

interface RestApiService {

    // --- Authentication ---
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: RestLoginRequest): Response<RestLoginResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RestRegisterRequest): Response<RestLoginResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Unit>

    // --- Users ---
    @GET("api/v1/users/me")
    suspend fun getUserProfile(): Response<RestAuthUserDto>

    @PUT("api/v1/users/me")
    suspend fun updateUserProfile(@Body request: RestUpdateUserProfileRequest): Response<RestAuthUserDto>

    // --- Organizations & Roles ---
    @GET("api/v1/organizations")
    suspend fun getOrganizations(): Response<List<RestOrganizationDto>>

    @GET("api/v1/organizations/{orgId}/memberships")
    suspend fun getMemberships(@Path("orgId") orgId: String): Response<List<RestMembershipDto>>

    // --- Academics ---
    @GET("api/v1/academic/years")
    suspend fun getAcademicYears(@Query("active") active: Boolean? = null): Response<List<RestAcademicYearDto>>

    @GET("api/v1/academic/terms")
    suspend fun getTerms(): Response<List<RestTermDto>>

    @GET("api/v1/academic/grade-levels")
    suspend fun getGradeLevels(): Response<List<RestGradeLevelDto>>

    @GET("api/v1/academic/subjects")
    suspend fun getSubjects(): Response<List<RestSubjectDto>>

    // --- Courses ---
    @GET("api/v1/classrooms")
    suspend fun getClassrooms(): Response<List<RestClassroomDto>>

    @GET("api/v1/courses")
    suspend fun getCourses(
        @Query("subjectId") subjectId: String? = null,
        @Query("gradeLevel") gradeLevel: String? = null
    ): Response<List<RestCourseDto>>

    @GET("api/v1/courses/{courseId}/lessons")
    suspend fun getLessons(@Path("courseId") courseId: String): Response<List<RestLessonDto>>

    @POST("api/v1/progress/lessons/{lessonId}/complete")
    suspend fun completeLesson(
        @Path("lessonId") lessonId: String,
        @Header("Idempotency-Key") idempotencyKey: String
    ): Response<Unit>

    // --- Assignments ---
    @GET("api/v1/assignments")
    suspend fun getAssignments(
        @Query("courseId") courseId: String? = null,
        @Query("status") status: String? = null
    ): Response<List<RestAssignmentDto>>

    @POST("api/v1/assignments/{id}/submissions")
    suspend fun submitAssignment(
        @Path("id") assignmentId: String,
        @Body request: RestSubmissionRequestDto,
        @Header("Idempotency-Key") idempotencyKey: String
    ): Response<RestSubmissionResponseDto>

    // --- Assessments ---
    @GET("api/v1/assessments")
    suspend fun getAssessments(): Response<List<RestAssessmentDto>>

    @POST("api/v1/assessments/{id}/grades")
    suspend fun submitGrade(
        @Path("id") assessmentId: String,
        @Body request: RestGradeDto,
        @Header("Idempotency-Key") idempotencyKey: String
    ): Response<Unit>

    // --- AI ---
    @POST("api/v1/ai/conversations")
    suspend fun createAiConversation(@Body request: RestAiConversationRequestDto): Response<RestAiConversationResponseDto>

    @POST("api/v1/ai/conversations/{id}/messages")
    suspend fun sendAiMessage(
        @Path("id") conversationId: String,
        @Body request: RestAiMessageRequestDto
    ): Response<RestAiMessageResponseDto>

    // --- Storage ---
    @POST("api/v1/storage/presigned-url")
    suspend fun getPresignedUploadUrl(@Body request: RestPresignedUploadRequestDto): Response<RestPresignedUploadResponseDto>

    // --- Sync ---
    @POST("api/v1/sync/push")
    suspend fun pushSyncPayload(@Body request: RestSyncPushRequestDto): Response<RestSyncPushResponseDto>

    @GET("api/v1/sync/pull")
    suspend fun pullSyncPayload(@Query("since_timestamp") sinceTimestamp: Long): Response<RestSyncPullResponseDto>
}
