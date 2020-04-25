package com.isanechek.imagehandler.data.models

data class Image(
    val id: Long,
    val path: String,
    val name: String,
    val addDate: Long,
    val folderName: String
)