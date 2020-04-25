package com.isanechek.imagehandler.data.models

sealed class GalleryImageResult {
    object Load : GalleryImageResult()
    data class Done(val data: List<Image>) : GalleryImageResult()
    data class Update(val data: List<Image>) : GalleryImageResult()
    data class Error(val data: List<Image>, val message: String) : GalleryImageResult()
}