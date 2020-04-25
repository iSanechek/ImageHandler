package com.isanechek.imagehandler.data.models

sealed class ChoicesResult<out T: Any> {
    object Load : ChoicesResult<Nothing>()
    data class Done<out T: Any>(val data: T) : ChoicesResult<T>()
    data class Update<out T: Any>(val data: T) : ChoicesResult<T>()
    data class Error<out T: Any>(val data: T, val message: String) : ChoicesResult<T>()
}