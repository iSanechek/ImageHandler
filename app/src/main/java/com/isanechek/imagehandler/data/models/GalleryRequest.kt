package com.isanechek.imagehandler.data.models

data class GalleryRequest(
    val folderName: String,
    val mediaType: MediaType,
    val withPaging: Boolean,
    val isUpdate: Boolean
)