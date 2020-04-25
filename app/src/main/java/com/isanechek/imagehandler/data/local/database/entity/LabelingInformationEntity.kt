package com.isanechek.imagehandler.data.local.database.entity

import androidx.room.Entity

@Entity(tableName = "labeling_information", primaryKeys = ["id"])
data class LabelingInformationEntity(val id: String, val coordinateX: Int, val coordinateY: Int, val labelPath: String)