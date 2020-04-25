package com.isanechek.imagehandler.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "watermark_info", primaryKeys = ["id"])
data class WatermarkChoicesImageEntity(
    val id: String,
    val title: String,
    val path: String,
    @ColumnInfo(name = "add_time") val addTime: Long,
    @ColumnInfo(name = "is_selected") val isSelected: Boolean
)