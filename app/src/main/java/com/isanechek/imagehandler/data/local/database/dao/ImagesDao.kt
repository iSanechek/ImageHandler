package com.isanechek.imagehandler.data.local.database.dao

import androidx.room.*
import com.isanechek.imagehandler.ui.handler.ImageItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ImagesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<ImageItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: ImageItem)

    @Query("SELECT * FROM image_table")
    suspend fun load(): List<ImageItem>

    @Update
    suspend fun updateResultPaths(items: List<ImageItem>)

    @Update
    suspend fun updateResultPath(items: ImageItem)

    @Delete
    suspend fun remove(items: ImageItem)

    @Query("DELETE FROM image_table")
    suspend fun clear()

    @Query("SELECT * FROM image_table WHERE result_path IS NOT NULL")
    fun loadResult(): Flow<List<ImageItem>>

    @Query("SELECT * FROM image_table")
    fun loadAsFlow(): Flow<List<ImageItem>>

    @Update
    suspend fun updateData(items: List<ImageItem>)

    @Transaction
    suspend fun update(items: List<ImageItem>) {
        clear()
        insert(items)

    }
}