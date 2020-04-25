package com.isanechek.imagehandler.data.models

sealed class MediaType {
    object Image : MediaType()
    object Video : MediaType()
    object Other : MediaType()
}