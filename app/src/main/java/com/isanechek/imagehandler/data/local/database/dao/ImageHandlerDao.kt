package com.isanechek.imagehandler.data.local.database.dao

import androidx.paging.DataSource
import androidx.room.*
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.data.local.database.entity.ImageHandlerEntity

@Dao
interface ImageHandlerDao : BaseDao<ImageHandlerEntity> {

    @Query("SELECT * FROM image_handler ORDER BY last_update DESC")
    fun loadAll(): DataSource.Factory<Int, ImageHandlerEntity>

    @Query("DELETE FROM image_handler")
    fun clearAll()

    @Query("SELECT * FROM image_handler ORDER BY last_update DESC")
    suspend fun loadData(): List<ImageHandlerEntity>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: ImageHandlerEntity)

    @Transaction
    suspend fun update(items: List<ImageHandlerEntity>) {
        items.forEach { item ->
            debugLog { "I $item" }
            update(item)
        }
    }

    @Query("DELETE FROM image_handler WHERE id IN (:ids)")
    suspend fun remove(ids: List<String>)
}