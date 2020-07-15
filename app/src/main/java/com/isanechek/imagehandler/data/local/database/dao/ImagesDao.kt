package com.isanechek.imagehandler.data.local.database.dao

import androidx.room.*
import com.isanechek.imagehandler.data.local.database.entity.ImageItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ImagesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<ImageItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: ImageItem)

    @Query("SELECT * FROM image_table")
    suspend fun load(): List<ImageItem>

    @Query("SELECT * FROM image_table WHERE id =:id")
    suspend fun loadItem(id: String): ImageItem?

    @Update
    suspend fun updateResultPaths(items: List<ImageItem>)

    @Update
    suspend fun updateResultPath(items: ImageItem)

    @Update
    suspend fun updateOriginalPath(item: ImageItem)

    @Query("UPDATE image_table SET original_path = :path WHERE id =:id")
    suspend fun updateOriginalPath(id: String, path: String)

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