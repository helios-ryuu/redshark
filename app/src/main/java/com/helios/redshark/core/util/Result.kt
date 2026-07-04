package com.helios.redshark.core.util

// File nay chua tien ich va ha tang dung chung trong ung dung.

import com.helios.redshark.core.error.AppException

// Sealed type nay liet ke cac trang thai hop le ma code can xu ly du.
sealed class Result<out T> {
    // Model du lieu nay giu cac truong can truyen giua cac lop.
    data class Success<T>(val data: T) : Result<T>()
    // Model du lieu nay giu cac truong can truyen giua cac lop.
    data class Error(val exception: AppException) : Result<Nothing>()
    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    data object Loading : Result<Nothing>()
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (AppException) -> Unit): Result<T> {
    if (this is Result.Error) action(exception)
    return this
}
