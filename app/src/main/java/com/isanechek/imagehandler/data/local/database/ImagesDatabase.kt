package com.isanechek.imagehandler.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.isanechek.imagehandler.data.local.database.dao.GalleryDao
import com.isanechek.imagehandler.data.local.database.dao.WatermarkDao
import com.isanechek.imagehandler.data.local.database.entity.*

@Database(
    entities = [
        (ImageHandlerEntity::class),
        (LabelingInformationEntity::class),
        (WatermarkChoicesImageEntity::class),
        (AlbumEntity::class),
        (ImageEntity::class),
        (WatermarkImageResultEntity::class),
        (GalleryImage::class),
        (FolderEntity::class)
    ],
    version = 11,
    exportSchema = false
)
abstract class ImagesDatabase : RoomDatabase() {
    abstract fun watermarkDao(): WatermarkDao
    abstract fun galleryDao(): GalleryDao
}