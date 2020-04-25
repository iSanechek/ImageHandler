package com.isanechek.imagehandler.data.local.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isanechek.imagehandler.d
import com.isanechek.imagehandler.data.local.database.entity.ImageEntity
import com.isanechek.imagehandler.data.local.database.entity.WatermarkChoicesImageEntity
import com.isanechek.imagehandler.data.local.database.entity.WatermarkImageResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatermarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatermarkChoicesImageEntity)

    @Query("SELECT * FROM watermark_info ORDER BY add_time DESC")
    fun findAllLive(): LiveData<List<WatermarkChoicesImageEntity>>

    @Query("SELECT * FROM watermark_info ORDER BY add_time DESC")
    suspend fun findAll(): List<WatermarkChoicesImageEntity>

    @Query("SELECT * FROM watermark_info WHERE is_selected = 1 LIMIT 1")
    suspend fun findSelected(): WatermarkChoicesImageEntity?

    @Query("UPDATE watermark_info SET is_selected =:isSelected WHERE id =:id")
    suspend fun update(id: String, isSelected: Boolean)

    @Query("SELECT * FROM watermark_info WHERE id =:id")
    suspend fun findOne(id: String): WatermarkChoicesImageEntity?

    @Query("SELECT id =:id FROM watermark_info LIMIT 1")
    suspend fun findId(id: String): String

    @Transaction
    suspend fun select(id: String) {
        val findId = findId(id)
        if (findId.isNotEmpty()) {
            update(id, true)
        } else d { "$id not find!" }


        val data = findAll()
        if (data.isNotEmpty()) {
            data.filter { it.id != id }.forEach { item ->
                d { "Item $item" }
                if (item.isSelected) {
                    update(item.id, false)
                }
            }
        } else d { "Watermark data is null" }
    }


    /*----------------------------------*/

    @Query("SELECT * FROM images")
    fun loadChoicesImages(): LiveData<List<ImageEntity>>

    @Query("SELECT * FROM images")
    fun loadChoicesImagesData(): List<ImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChoicesImages(data: List<ImageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChoicesImage(imageEntity: ImageEntity)

    @Query("DELETE FROM images WHERE id =:id")
    suspend fun removeChoicesImage(id: Long)

    @Query("DELETE FROM images")
    suspend fun clearChoicesImages()

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateImage(imageEntity: ImageEntity)

    /*=====================================*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatermarkResult(watermarkImageResultEntity: WatermarkImageResultEntity)

    @Query("SELECT * FROM watermark_image_result")
    fun loadWatermarkResult(): Flow<List<WatermarkImageResultEntity>>

    @Query("SELECT * FROM watermark_image_result")
    suspend fun loadWatermarkResultData(): List<WatermarkImageResultEntity>

    @Query("DELETE FROM watermark_image_result WHERE id =:id")
    suspend fun removeWatermarkImageResult(id: Long)

    @Query("DELETE FROM watermark_image_result WHERE id =(:ids)")
    suspend fun clearWatermarkResult(ids: List<Long>)

    @Update
    suspend fun updateWatermarkResultImage(watermarkImageResultEntity: WatermarkImageResultEntity)
}