package com.rtiqa.core.domain.result

import com.rtiqa.core.domain.error.RtiqaError

/**
 * Generic domain result wrapper for reactive and asynchronous business operations.
 */
sealed interface RtiqaResult<out T> {

    data class Success<out T>(val data: T) : RtiqaResult<T>

    data class Error(val error: RtiqaError) : RtiqaResult<Nothing>

    object Loading : RtiqaResult<Nothing>

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrElse(defaultValue: @UnsafeVariance T): T {
        return (this as? Success)?.data ?: defaultValue
    }

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading
}

inline fun <T, R> RtiqaResult<T>.map(transform: (T) -> R): RtiqaResult<R> {
    return when (this) {
        is RtiqaResult.Success -> RtiqaResult.Success(transform(data))
        is RtiqaResult.Error -> RtiqaResult.Error(error)
        is RtiqaResult.Loading -> RtiqaResult.Loading
    }
}
