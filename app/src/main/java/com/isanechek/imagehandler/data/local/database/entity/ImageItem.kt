package com.isanechek.imagehandler.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "image_table", primaryKeys = ["id"])
data class ImageItem(
    val id: String,
    val name: String,
    @ColumnInfo(name = "original_path") val originalPath: String,
    @ColumnInfo(name = "result_path") val resultPath: String,
    @ColumnInfo(name = "public_path") val publicPath: String,
    @ColumnInfo(name = "overlay_status") val overlayStatus: String,
    @ColumnInfo(name = "aspect_ration_original") val aspectRationOriginal: Int,
    @ColumnInfo(name = "aspect_ration_result") val aspectRationResult: Int,
    @ColumnInfo(name = "select_aspect_ration") val selectedAspectRation: Int
) {

    companion object {
        const val OVERLAY_FAIL = "fail"
        const val OVERLAY_DONE = "done"
        const val OVERLAY_NONE = "none"
    }
}