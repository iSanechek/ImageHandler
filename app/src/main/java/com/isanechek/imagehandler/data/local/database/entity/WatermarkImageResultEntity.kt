package com.isanechek.imagehandler.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "watermark_image_result", primaryKeys = ["id"])
data class WatermarkImageResultEntity(
    val id: Long,
    val name: String,
    @ColumnInfo(name = "original_path") val originalPath: String,
    @ColumnInfo(name = "copy_path") val copyPath: String,
    @ColumnInfo(name = "result_path") val resultPath: String,
    @ColumnInfo(name = "public_path") val publicPath: String,
    @ColumnInfo(name = "create_time") val createTime: Long,
    @ColumnInfo(name = "last_update") val lastUpdate: Long,
    val status: String,
    val message: String
) {
    companion object {
        const val STATUS_DONE = "done"
        const val STATUS_FAIL = "fail"
    }
}