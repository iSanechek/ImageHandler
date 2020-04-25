package com.isanechek.imagehandler.data.local.database.dao

import androidx.room.Dao
import com.isanechek.imagehandler.data.local.database.entity.LabelingInformationEntity

@Dao
interface LabelingDao : BaseDao<LabelingInformationEntity> {
}