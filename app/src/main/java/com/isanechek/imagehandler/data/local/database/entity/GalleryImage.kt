package com.isanechek.imagehandler.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "gallery_images", primaryKeys = ["id"])
data class GalleryImage(
    val id: Long,
    val name: String,
    val path: String,
    @ColumnInfo(name = "add_time") val addTime: Long,
    @ColumnInfo(name = "folder_name") val folderName: String
)