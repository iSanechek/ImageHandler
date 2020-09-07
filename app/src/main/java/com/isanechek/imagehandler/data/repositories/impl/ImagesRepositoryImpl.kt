package com.isanechek.imagehandler.data.repositories.impl

import android.content.Context
import com.isanechek.imagehandler.data.local.system.gallery.GalleryManager
import com.isanechek.imagehandler.data.models.ExecuteResult
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.data.repositories.ImagesRepository2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ImagesRepositoryImpl(private val galleryManager: GalleryManager) : ImagesRepository2 {

    override suspend fun loadLastImages(context: Context, folderName: String): Flow<ExecuteResult<List<Image>>> = flow {
        emit(ExecuteResult.Progress)
        val data = galleryManager.loadLastImages(context, "", 30, 0)
        if (data.isNotEmpty()) {
            emit(ExecuteResult.Done(data))
        } else {
            emit(ExecuteResult.Error("Fail"))
        }
    }

}