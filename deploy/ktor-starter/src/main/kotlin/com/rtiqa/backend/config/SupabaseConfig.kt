package com.rtiqa.backend.config

data class SupabaseConfig(
    val url: String,
    val anonKey: String
) {
    companion object {
        fun fromEnvironment(): SupabaseConfig {
            val url = System.getenv("SUPABASE_URL") ?: "https://example.supabase.co"
            val anonKey = System.getenv("SUPABASE_ANON_KEY") ?: "dummy-anon-key"

            return SupabaseConfig(url, anonKey)
        }
    }
}
