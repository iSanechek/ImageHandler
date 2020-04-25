package com.isanechek.imagehandler.data.models

sealed class ExecuteResult<out T: Any> {
    data class Progress(val status: Pair<String, Any>) : ExecuteResult<Nothing>()
    data class Done<out T: Any>(val data: T) : ExecuteResult<T>()
    data class Error(val message: String) : ExecuteResult<String>()
}