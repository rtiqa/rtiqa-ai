package com.rtiqa.core.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun emailValidator_validEmail_returnsValid() {
        val result = EmailValidator.validate("student@rtiqa.com")
        assertTrue(result.isValid())
    }

    @Test
    fun emailValidator_invalidEmail_returnsInvalid() {
        val result = EmailValidator.validate("student@rtiqa")
        assertFalse(result.isValid())
    }

    @Test
    fun passwordValidator_validPassword_returnsValid() {
        val result = PasswordValidator.validate("SecurePass123")
        assertTrue(result.isValid())
    }

    @Test
    fun passwordValidator_shortPassword_returnsInvalid() {
        val result = PasswordValidator.validate("pass1")
        assertFalse(result.isValid())
    }

    @Test
    fun quizSubmissionValidator_validScore_returnsValid() {
        val result = QuizSubmissionValidator.validate(score = 8, totalQuestions = 10)
        assertTrue(result.isValid())
    }

    @Test
    fun quizSubmissionValidator_invalidScore_returnsInvalid() {
        val result = QuizSubmissionValidator.validate(score = 12, totalQuestions = 10)
        assertFalse(result.isValid())
    }
}
