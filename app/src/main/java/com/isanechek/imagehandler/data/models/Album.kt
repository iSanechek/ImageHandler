package com.isanechek.imagehandler.data.models

data class Album(
    val id: Long,
    val name: String,
    val path: String,
    val addDate: Long,
    val lastModification: Long,
    val images: MutableList<Image>
)