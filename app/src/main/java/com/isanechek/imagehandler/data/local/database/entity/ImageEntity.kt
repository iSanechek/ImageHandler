package com.isanechek.imagehandler.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "images", primaryKeys = ["id"])
data class ImageEntity(
    val id: Long,
    val name: String,
    val path: String,
    @ColumnInfo(name = "add_time") val addTime: Long
)