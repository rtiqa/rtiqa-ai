package com.rtiqa.core.network.api
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
interface RestApiContract {
    @POST("v1/auth/login")
    suspend fun login(@Body request: RestLoginRequest): RestLoginResponse
    
    @GET("v1/users/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): RestUserDto
    
    @GET("v1/courses")
    suspend fun getCourses(): List<RestCourseDto>
}
