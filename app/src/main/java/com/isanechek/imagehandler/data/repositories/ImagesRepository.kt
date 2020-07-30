package com.isanechek.imagehandler.data.repositories

import android.content.Context
import com.isanechek.imagehandler.data.local.database.dao.GalleryDao
import com.isanechek.imagehandler.data.local.database.dao.ImageDao2
import com.isanechek.imagehandler.data.local.database.entity.GalleryImage
import com.isanechek.imagehandler.data.local.system.gallery.GalleryManager
import com.isanechek.imagehandler.data.models.ExecuteResult
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.ext.toEntity
import com.isanechek.imagehandler.ext.toGallery
import com.isanechek.imagehandler.ext.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ImagesRepository {
    suspend fun loadLastImg(
        context: Context,
        folder: String,
        isUpdate: Boolean
    ): Flow<ExecuteResult<List<Image>>>

    suspend fun loadImagesIds(ids: List<Long>): List<Image>
    suspend fun setSelectImagesCache(ids: List<Long>)
    suspend fun getSelectImageCache(): List<Image>
    fun clearSelectImagesCache()
}

class ImagesRepositoryImpl(
    private val galleryManager: GalleryManager,
    private val imageDao: ImageDao2
) : ImagesRepository {

    private val cacheSelectImages = mutableListOf<Image>()

    override suspend fun loadLastImg(
        context: Context,
        folder: String,
        isUpdate: Boolean
    ): Flow<ExecuteResult<List<Image>>> = flow {
        emit(ExecuteResult.Progress)

        if (isUpdate) {
            val result = galleryManager.loadLastImages(context, folder, 99, 0)
            if (result.isNotEmpty()) {
                imageDao.updateImages(result.map { it.toEntity() }.toList())
                emit(ExecuteResult.Done(result))
            } else {
                emit(ExecuteResult.Error("Not find images!"))
            }

        } else {
            if (imageDao.getCount() == 0) {
                val result = galleryManager.loadLastImages(context, folder, 99, 0)
                if (result.isNotEmpty()) {
                    imageDao.save(result.map { it.toEntity() }.toList())
                    emit(ExecuteResult.Done(result))
                } else {
                    emit(ExecuteResult.Error("Not find images!"))
                }
            } else {
                val cache = imageDao.load()
                debugLog { "CACHE SIZE ${cache.size}" }
                emit(ExecuteResult.Done(cache.map {
                    it.toModel()
                }.toList()))
            }
        }
    }

    override suspend fun loadImagesIds(ids: List<Long>): List<Image> = imageDao.load(ids).map { it.toModel() }.toList()

    override suspend fun setSelectImagesCache(ids: List<Long>) {

    }

    override suspend fun getSelectImageCache(): List<Image> = cacheSelectImages

    override fun clearSelectImagesCache() {
        cacheSelectImages.clear()
    }

}