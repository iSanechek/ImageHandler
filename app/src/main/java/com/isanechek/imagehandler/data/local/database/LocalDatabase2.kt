package com.isanechek.imagehandler.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.isanechek.imagehandler.data.local.database.dao.ImageDao2
import com.isanechek.imagehandler.data.local.database.entity.ImageEntity

@Database(entities = [(ImageEntity::class)], version = 1, exportSchema = false)
abstract class LocalDatabase2 : RoomDatabase() {
    abstract fun imageDao2(): ImageDao2
}