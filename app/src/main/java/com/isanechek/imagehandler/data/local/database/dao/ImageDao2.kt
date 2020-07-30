package com.isanechek.imagehandler.data.local.database.dao

import androidx.room.*
import com.isanechek.imagehandler.data.local.database.entity.ImageEntity

@Dao
interface ImageDao2 {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(items: List<ImageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(item: ImageEntity)

    @Query("SELECT * FROM images WHERE id IN (:ids)")
    suspend fun load(ids: List<Long>): List<ImageEntity>

    @Query("SELECT * FROM images")
    suspend fun load(): List<ImageEntity>

    @Query("DELETE FROM images")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM images")
    suspend fun getCount(): Int

    @Transaction
    suspend fun updateImages(items: List<ImageEntity>) {
        clear()
        save(items)
    }

}