package com.rtiqa.core.domain.validation

/**
 * Pure Kotlin email format validator.
 */
object EmailValidator {
    private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$".toRegex()

    fun validate(email: String): ValidationResult {
        val trimmed = email.trim()
        val errors = mutableListOf<String>()

        if (trimmed.isEmpty()) {
            errors.add("Email address cannot be empty.")
        } else if (!EMAIL_REGEX.matches(trimmed)) {
            errors.add("Invalid email format.")
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}

/**
 * Pure Kotlin password complexity validator.
 */
object PasswordValidator {
    fun validate(password: String): ValidationResult {
        val errors = mutableListOf<String>()

        if (password.length < 8) {
            errors.add("Password must be at least 8 characters long.")
        }
        if (!password.any { it.isDigit() }) {
            errors.add("Password must contain at least one digit.")
        }
        if (!password.any { it.isLetter() }) {
            errors.add("Password must contain at least one letter.")
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}

/**
 * Registration form submission validator.
 */
object RegistrationValidator {
    fun validate(name: String, email: String, password: String): ValidationResult {
        val errors = mutableListOf<String>()

        if (name.trim().length < 2) {
            errors.add("Name must be at least 2 characters.")
        }

        val emailResult = EmailValidator.validate(email)
        if (!emailResult.isValid()) {
            errors.addAll(emailResult.getErrorsOrEmpty())
        }

        val passwordResult = PasswordValidator.validate(password)
        if (!passwordResult.isValid()) {
            errors.addAll(passwordResult.getErrorsOrEmpty())
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}

/**
 * Quiz answer submission validator.
 */
object QuizSubmissionValidator {
    fun validate(score: Int, totalQuestions: Int): ValidationResult {
        val errors = mutableListOf<String>()

        if (totalQuestions <= 0) {
            errors.add("Total questions must be greater than zero.")
        }
        if (score < 0 || score > totalQuestions) {
            errors.add("Score must be between 0 and total questions count.")
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}
