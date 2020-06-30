package com.isanechek.imagehandler.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "is_selected") val isSelected: Boolean,
    @ColumnInfo(name = "overlay_path") val overlayPath: String
)