package com.rtiqa.core.network.model
import com.rtiqa.core.network.api.RestLoginRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test
class RestDtoSerializationTest {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    @Test
    fun `DTO serialization and deserialization works`() {
        val adapter = moshi.adapter(RestLoginRequest::class.java)
        val request = RestLoginRequest("test@rtiqa.com", "secure123")
        val json = adapter.toJson(request)
        val expectedJson = """{"email":"test@rtiqa.com","password":"secure123"}"""
        assertEquals(expectedJson, json)
        val deserialized = adapter.fromJson(json)
        assertEquals(request.email, deserialized?.email)
        assertEquals(request.password, deserialized?.password)
    }
}
