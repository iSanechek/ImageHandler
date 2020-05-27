package com.isanechek.imagehandler.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.isanechek.imagehandler.data.local.database.dao.ImagesDao
import com.isanechek.imagehandler.ui.handler.ImageItem

@Database(entities = [ImageItem::class], version = 2, exportSchema = false)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun imagesDao(): ImagesDao
}