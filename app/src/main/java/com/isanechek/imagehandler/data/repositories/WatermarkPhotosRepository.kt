package com.isanechek.imagehandler.data.repositories

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.isanechek.imagehandler.PRIVATE_APP_FOLDER_NAME
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.data.local.database.dao.GalleryDao
import com.isanechek.imagehandler.data.local.database.dao.WatermarkDao
import com.isanechek.imagehandler.data.local.database.entity.AlbumEntity
import com.isanechek.imagehandler.data.local.database.entity.GalleryImage
import com.isanechek.imagehandler.data.local.database.entity.WatermarkImageResultEntity
import com.isanechek.imagehandler.data.local.system.FilesManager
import com.isanechek.imagehandler.data.local.system.MediaStoreManager
import com.isanechek.imagehandler.data.local.system.OverlayManager
import com.isanechek.imagehandler.data.local.system.gallery.GalleryImagesDataSourceFactory
import com.isanechek.imagehandler.data.local.system.gallery.GalleryManager
import com.isanechek.imagehandler.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

interface WatermarkPhotosRepository {
    val progressState: LiveData<ProgressState>
    fun loadImagesPaging(
        context: Context,
        request: GalleryRequest,
        scope: CoroutineScope
    ): LiveData<PagedList<Image>>
    fun loadImagesFlow(context: Context, request: GalleryRequest): Flow<GalleryImageResult>
    fun loadImages(context: Context, isUpdate: Boolean): LiveData<GalleryImageResult>
    fun loadImages(folderName: String): LiveData<GalleryImageResult>
    suspend fun data(): Flow<List<WatermarkImageResult>>
    suspend fun handleWatermark(context: Context): Flow<String>
    suspend fun saveWatermarkResult(context: Context): Flow<String>
    suspend fun removeAll(context: Context): Flow<String>
    suspend fun removeItem(context: Context, id: Long): Flow<String>
    suspend fun saveFindImages(data: Set<Image>)
    fun loadFolders(context: Context, isUpdate: Boolean): Flow<List<Album>>
}

class WatermarkPhotosRepositoryImpl(
    private val watermarkDao: WatermarkDao,
    private val filesManager: FilesManager,
    private val overlayManager: OverlayManager,
    private val galleryManager: GalleryManager,
    private val mediaStoreManager: MediaStoreManager,
    private val galleryDao: GalleryDao
) : WatermarkPhotosRepository {

    private val _progressState = MutableLiveData<ProgressState>()
    private val imagesConfig = PagedList
        .Config
        .Builder()
        .setEnablePlaceholders(false)
        .setPageSize(10)
        .build()

    override val progressState: LiveData<ProgressState>
        get() = _progressState

    override fun loadImagesPaging(
        context: Context,
        request: GalleryRequest,
        scope: CoroutineScope
    ): LiveData<PagedList<Image>> {
        val imagesDataSource = GalleryImagesDataSourceFactory(
            context,
            scope,
            galleryManager,
            request) { progressState ->

        }
        return LivePagedListBuilder(imagesDataSource, imagesConfig).build()
    }

    override fun loadImagesFlow(
        context: Context,
        request: GalleryRequest
    ): Flow<GalleryImageResult> = flow {

    }

    override fun loadImages(
        context: Context,
        isUpdate: Boolean
    ): LiveData<GalleryImageResult> = liveData(Dispatchers.IO) {
        val count = galleryDao.getImagesSize()
        if (count == 0) {
            emit(GalleryImageResult.Load)
            val result = galleryManager.loadLastImages(context, "", LIMIT_LOAD_SIZE, 0)
            emit(GalleryImageResult.Done(result))
            galleryDao.insertImages(result.map { it.toEntity() }.toList())
        } else {
            val cache = galleryDao.loadImages().map { it.toModel() }.toList()
            if (!isUpdate) {
                emit(GalleryImageResult.Done(cache))
            } else {
                emit(GalleryImageResult.Update(cache))
                val result = galleryManager.loadLastImages(context, "", LIMIT_LOAD_SIZE, 0)
                if (result.isNotEmpty()) {
                    galleryDao.updateImages(result.map { it.toEntity() }.toList())
                    emit(GalleryImageResult.Done(result))
                } else {
                    emit(GalleryImageResult.Done(cache))
                }
            }
        }
    }

    override fun loadImages(
        folderName: String
    ): LiveData<GalleryImageResult> = liveData(Dispatchers.IO) {
        emit(GalleryImageResult.Load)
        val data = galleryDao.loadImagesFromAlbum(folderName)
        if (data.isNotEmpty()) {
            emit(GalleryImageResult.Done(data.map { it.toModel() }.toList()))
        } else {
            emit(GalleryImageResult.Error(emptyList<Image>(), "empty list"))
        }
    }

    override suspend fun data(): Flow<List<WatermarkImageResult>> =
        watermarkDao.loadWatermarkResult().map { it.map { item -> item.toModel() } }

    override suspend fun handleWatermark(context: Context): Flow<String> = flow {
        val watermarkItem = watermarkDao.findSelected()
        if (watermarkItem == null) {
            emit("Watermark not find!")
            return@flow
        }
        val rootFolderPath =
            context.filesDir.absolutePath + File.separator + HIDE_FOLDER_NAME
        if (filesManager.createFolderIfEmpty(rootFolderPath)) {

            val data = watermarkDao.loadChoicesImagesData()
            if (data.isNotEmpty()) {
                for (item in data) {
                    if (filesManager.checkFileExists(item.path)) {

                        val (isOk, copyPath) = filesManager.copyFile(
                            item.path,
                            rootFolderPath,
                            item.name
                        )

                        if (isOk) {
                            val watermarkBitmap =
                                overlayManager.overlay(context, copyPath, watermarkItem.path)
                            val (isDone, resultPath) = filesManager.saveFile(
                                watermarkBitmap,
                                rootFolderPath,
                                item.name
                            )

                            val timestamp = System.nanoTime()
                            val resultItem = WatermarkImageResultEntity(
                                id = item.id,
                                name = item.name,
                                publicPath = "",
                                resultPath = if (isDone) resultPath else "",
                                message = if (isDone) "" else "Save result error!",
                                status = if (isDone) WatermarkImageResultEntity.STATUS_DONE else WatermarkImageResultEntity.STATUS_FAIL,
                                copyPath = copyPath,
                                originalPath = item.path,
                                lastUpdate = timestamp,
                                createTime = timestamp
                            )

                            watermarkDao.insertWatermarkResult(resultItem)
                        } else {

                            val timestamp = System.nanoTime()
                            val resultItem = WatermarkImageResultEntity(
                                id = item.id,
                                name = item.name,
                                publicPath = "",
                                resultPath = "",
                                message = "Copy file error!",
                                status = WatermarkImageResultEntity.STATUS_FAIL,
                                copyPath = copyPath,
                                originalPath = item.path,
                                lastUpdate = timestamp,
                                createTime = timestamp
                            )
                            watermarkDao.insertWatermarkResult(resultItem)
                        }
                    } else {
                        debugLog { "Original file is null!" }
                        val timestamp = System.nanoTime()
                        val resultItem = WatermarkImageResultEntity(
                            id = item.id,
                            name = item.name,
                            publicPath = "",
                            resultPath = "",
                            message = "Original file not find!",
                            status = WatermarkImageResultEntity.STATUS_FAIL,
                            copyPath = "",
                            originalPath = item.path,
                            lastUpdate = timestamp,
                            createTime = timestamp
                        )
                        watermarkDao.insertWatermarkResult(resultItem)
                    }
                }

                watermarkDao.clearChoicesImages()
                emit("done")
            } else emit("empty_data")


        } else {
            debugLog { "Root folder not create!" }
            emit("fail_create_root_folder")
        }
    }

    override suspend fun saveWatermarkResult(context: Context): Flow<String> = flow {
        val data = watermarkDao.loadWatermarkResultData()
        if (data.isNotEmpty()) {
            val rootFolderPath =
                context.filesDir.absolutePath + File.separator + PRIVATE_APP_FOLDER_NAME
            if (filesManager.createFolderIfEmpty(rootFolderPath)) {
                for (item in data) {
                    if (filesManager.checkFileExists(item.resultPath)) {
                        val bitmap = BitmapFactory.decodeFile(item.resultPath)
                        val (isOk, value) = mediaStoreManager.saveBitmapInGallery(
                            context = context,
                            fileName = item.name,
                            bitmap = bitmap,
                            folderName = PUBLIC_FOLDER_NAME,
                            timestamp = item.lastUpdate
                        )
                        val result = item.copy(
                            publicPath = if (isOk) value else "",
                            message = if (isOk) "" else value
                        )
                        watermarkDao.updateWatermarkResultImage(result)
                    }
                }
                emit("done")
            } else emit("fail_exists_root_folder")
        } else emit("data_empty")

    }

    override suspend fun removeAll(context: Context): Flow<String> = flow {
        val data = watermarkDao.loadWatermarkResultData()
        if (data.isNotEmpty()) {
            val temp = mutableListOf<Long>()
            data.filter { item ->
                when {
                    item.copyPath.isNotEmpty() -> filesManager.deleteFile(item.copyPath)
                    else -> true
                }
            }.filter { item ->
                when {
                    item.resultPath.isNotEmpty() -> filesManager.deleteFile(item.resultPath)
                    else -> true
                }
            }.filter {
                when {
                    it.publicPath.isNotEmpty() -> filesManager.deleteFile(it.publicPath)
                    else -> true
                }
            }.forEach { i -> temp.add(i.id) }

            if (temp.isNotEmpty()) {
                watermarkDao.clearWatermarkResult(temp)
            }

        } else emit("empty_data")
    }

    override suspend fun removeItem(context: Context, id: Long): Flow<String> = flow {

    }

    override suspend fun saveFindImages(data: Set<Image>) = withContext(Dispatchers.IO) {
        galleryDao.insertImages(data.map { it.toEntity() }.toList())
    }


    override fun loadFolders(context: Context, isUpdate: Boolean): Flow<List<Album>> = flow {
        val cache = galleryDao.loadAlbums()
        debugLog { "folders cache ${cache.size}" }
        if (isUpdate) {
            if (cache.isNotEmpty()) {
                val newData = galleryManager.loadAlbums(context, 0)
                if (newData.isNotEmpty()) {
                    galleryDao.update(newData.map { it.toEntity() }.toList())
                } else {
                    galleryDao.clearAlbums()
                }
            }
        } else {
            debugLog { "sg" }
            if (cache.isNotEmpty()) {
                emit(cache.map { it.toModel() }.toList())
            } else {
                debugLog { "hi" }
                val newData = galleryManager.loadAlbums(context, 0)
                debugLog { "New data ${newData.size}" }
                if (newData.isNotEmpty()) {
                    debugLog { "pm" }
                    emit(newData)
                    galleryDao.insertAlbums(newData.map { it.toEntity() }.toList())
                } else {
                    debugLog { "vm" }
                    emit(cache.map { it.toModel() }.toList())
                }
            }
        }
    }

    private fun Album.toEntity(): AlbumEntity {
        return AlbumEntity(
            id = this.id,
            title = this.name,
            coverPath = this.images.first().path,
            lastModification = this.lastModification,
            path = this.path
        )
    }

    private fun AlbumEntity.toModel(): Album = Album(
        id = this.id,
        lastModification = this.lastModification,
        name = this.title,
        addDate = 0,
        path = this.path,
        images = mutableListOf(Image(0, this.path, "", 0, ""))
    )

    private fun Image.toEntity(): GalleryImage = GalleryImage(
        id = this.id,
        name = this.name,
        path = this.path,
        addTime = this.addDate,
        folderName = this.folderName
    )

    private fun GalleryImage.toModel(): Image = Image(
        id = this.id,
        path = this.path,
        addDate = this.addTime,
        name = this.name,
        folderName = this.folderName
    )

    private fun WatermarkImageResultEntity.toModel(): WatermarkImageResult =
        WatermarkImageResult(
            id,
            name,
            originalPath,
            copyPath,
            resultPath,
            publicPath,
            createTime,
            lastUpdate,
            status,
            message
        )

    companion object {
        const val HIDE_FOLDER_NAME = "image_cache"
        private const val PUBLIC_FOLDER_NAME = "ImageHandler"
        private const val LIMIT_LOAD_SIZE = 102
    }
}