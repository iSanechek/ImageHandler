package com.isanechek.imagehandler.ui.base

sealed class ItemClickCallback<T> {
    data class Click<T>(val data: T) : ItemClickCallback<T>()
    data class LongClick<T>(val data: T) : ItemClickCallback<T>()
}