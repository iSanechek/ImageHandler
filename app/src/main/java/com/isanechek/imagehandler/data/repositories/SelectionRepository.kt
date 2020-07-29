package com.isanechek.imagehandler.data.repositories

import android.content.Context
import com.isanechek.imagehandler.data.local.database.dao.GalleryDao
import com.isanechek.imagehandler.data.local.database.entity.GalleryImage
import com.isanechek.imagehandler.data.local.system.gallery.GalleryManager
import com.isanechek.imagehandler.data.models.ExecuteResult
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.debugLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface SelectionRepository {
    suspend fun loadLastImg(
        context: Context,
        folder: String,
        isUpdate: Boolean
    ): Flow<ExecuteResult<List<Image>>>

    suspend fun loadImagesIds(ids: List<Long>): List<Image>
}

class SelectionRepositoryImpl(
    private val galleryManager: GalleryManager,
    private val galleryDao: GalleryDao
) : SelectionRepository {

    override suspend fun loadLastImg(
        context: Context,
        folder: String,
        isUpdate: Boolean
    ): Flow<ExecuteResult<List<Image>>> = flow {
        emit(ExecuteResult.Progress)


        if (isUpdate) {
            val result = galleryManager.loadLastImages(context, folder, 99, 0)
            if (result.isNotEmpty()) {
                galleryDao.updateImages(result.map {
                    GalleryImage(
                        it.id,
                        it.path,
                        it.path,
                        it.addDate,
                        it.folderName
                    )
                }.toList())
                emit(ExecuteResult.Done(result))
            } else {
                emit(ExecuteResult.Error("Not find images!"))
            }

        } else {
            if (galleryDao.getImagesSize() == 0) {
                val result = galleryManager.loadLastImages(context, folder, 99, 0)
                if (result.isNotEmpty()) {
                    galleryDao.insertImages(result.map {
                        GalleryImage(
                            it.id,
                            it.path,
                            it.path,
                            it.addDate,
                            it.folderName
                        )
                    }.toList())
                    emit(ExecuteResult.Done(result))
                } else {
                    emit(ExecuteResult.Error("Not find images!"))
                }
            } else {
                val cache = galleryDao.loadImages()
                debugLog { "CACHE SIZE ${cache.size}" }
                emit(ExecuteResult.Done(cache.map {
                    Image(
                        it.id,
                        it.path,
                        it.name,
                        it.addTime,
                        it.folderName
                    )
                }.toList()))
            }
        }
    }

    override suspend fun loadImagesIds(ids: List<Long>): List<Image> = galleryDao.loadImagesIds(ids)
        .map { Image(it.id, it.path, it.name, it.addTime, it.folderName) }.toList()

}