package com.rtiqa.core.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed interface RtiqaResult<out T> {
    data class Success<T>(val data: T) : RtiqaResult<T>
    data class Error(val exception: Throwable, val message: String? = exception.localizedMessage) : RtiqaResult<Nothing>
    data object Loading : RtiqaResult<Nothing>
}

fun <T> Flow<T>.asRtiqaResult(): Flow<RtiqaResult<T>> {
    return this
        .map<T, RtiqaResult<T>> { RtiqaResult.Success(it) }
        .onStart { emit(RtiqaResult.Loading) }
        .catch { emit(RtiqaResult.Error(it)) }
}
