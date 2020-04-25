package com.isanechek.imagehandler.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = ImageHandlerEntity.TABLE_NAME, primaryKeys = ["id"])
data class ImageHandlerEntity(
    val id: String,
    val title: String,
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
        const val TABLE_NAME = "image_handler"
        const val LABEL_IS_DONE = "label_is_done"
        const val LABEL_IS_FAIL = "label_is_fail"
    }
}