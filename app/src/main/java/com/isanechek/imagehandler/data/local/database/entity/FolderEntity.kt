package com.isanechek.imagehandler.data.local.database.entity

import androidx.room.Entity

@Entity(tableName = "folders", primaryKeys = ["id"])
data class FolderEntity(val id: String, val name: String, val covers: String, val modification: Long)