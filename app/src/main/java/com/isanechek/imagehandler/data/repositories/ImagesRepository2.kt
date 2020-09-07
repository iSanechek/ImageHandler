package com.isanechek.imagehandler.data.repositories

import android.content.Context
import com.isanechek.imagehandler.data.models.ExecuteResult
import com.isanechek.imagehandler.data.models.Image
import kotlinx.coroutines.flow.Flow

interface ImagesRepository2 {

    suspend fun loadLastImages(context: Context, folderName: String): Flow<ExecuteResult<List<Image>>>

}