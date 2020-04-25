package com.isanechek.imagehandler.data.models

sealed class ProgressState {
    object Loading : ProgressState()
    object Done : ProgressState()
    data class Error(val message: String): ProgressState()
}