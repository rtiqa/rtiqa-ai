package com.rtiqa.core.domain.validation

/**
 * Domain validation result representation.
 */
sealed interface ValidationResult {

    object Valid : ValidationResult

    data class Invalid(val errors: List<String>) : ValidationResult

    fun isValid(): Boolean = this is Valid

    fun getErrorsOrEmpty(): List<String> = (this as? Invalid)?.errors ?: emptyList()
}
