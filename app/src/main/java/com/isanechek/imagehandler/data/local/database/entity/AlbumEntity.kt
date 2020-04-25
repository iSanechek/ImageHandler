package com.isanechek.imagehandler.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "album", primaryKeys = ["id"])
data class AlbumEntity(
    val id: Long,
    val title: String,
    @ColumnInfo(name = "last_modification") val lastModification: Long,
    val coverPath: String,
    val path: String
)