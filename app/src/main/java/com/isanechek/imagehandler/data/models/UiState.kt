package com.isanechek.imagehandler.data.models

sealed class UiState<out T: Any> {
    object Progress : UiState<Nothing>()
    data class Done<out T: Any>(val data: T) : UiState<T>()
    object Permission : UiState<Nothing>()
    data class Error(val message: String): UiState<Nothing>()
}