package com.isanechek.imagehandler.data.models

data class WatermarkImageResult(
    val id: Long,
    val name: String,
    val originalPath: String,
    val copyPath: String,
    val resultPath: String,
    val publicPath: String,
    val createTime: Long,
    val lastUpdate: Long,
    val status: String,
    val message: String
)