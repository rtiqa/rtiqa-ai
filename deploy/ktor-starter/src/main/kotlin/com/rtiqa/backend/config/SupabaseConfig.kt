package com.rtiqa.backend.config

import java.net.URI

data class SupabaseConfig(
    val url: String,
    val publishableKey: String
) {
    companion object {
        fun fromEnvironment(): SupabaseConfig {
            val rawUrl = System.getenv("SUPABASE_URL")
            if (rawUrl.isNullOrBlank()) {
                throw IllegalStateException("Configuration error: SUPABASE_URL environment variable is required and cannot be blank.")
            }
            val trimmedUrl = rawUrl.trim()
            try {
                val uri = URI(trimmedUrl)
                if (uri.scheme?.lowercase() != "https" || uri.host.isNullOrBlank()) {
                    throw IllegalStateException("Configuration error: SUPABASE_URL must be a valid absolute HTTPS URL.")
                }
            } catch (e: Exception) {
                if (e is IllegalStateException) throw e
                throw IllegalStateException("Configuration error: SUPABASE_URL is not a valid URL: ${e.message}")
            }

            val publishableKey = System.getenv("SUPABASE_PUBLISHABLE_KEY")
            if (publishableKey.isNullOrBlank()) {
                throw IllegalStateException("Configuration error: SUPABASE_PUBLISHABLE_KEY environment variable is required and cannot be blank.")
            }

            return SupabaseConfig(trimmedUrl, publishableKey)
        }
    }
}
