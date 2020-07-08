package com.isanechek.imagehandler.ui.handler

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.isanechek.imagehandler.App
import com.isanechek.imagehandler.data.local.database.dao.ImagesDao
import com.isanechek.imagehandler.data.local.system.FilesManager
import com.isanechek.imagehandler.data.local.system.MediaStoreManager
import com.isanechek.imagehandler.data.local.system.PrefManager
import com.isanechek.imagehandler.data.repositories.WatermarkPhotosRepositoryImpl
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.workers.ClearWorker
import com.watermark.androidwm_light.WatermarkBuilder
import com.watermark.androidwm_light.bean.WatermarkImage
import com.watermark.androidwm_light.bean.WatermarkPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ImageHandlerViewModel(
    application: Application,
    private val filesManager: FilesManager,
    private val prefManager: PrefManager,
    private val mediaStoreManager: MediaStoreManager,
    private val imagesDao: ImagesDao
) : AndroidViewModel(application) {

    private var currentId = -1

    private val toastState = MutableLiveData<String>()
    val toast: LiveData<String>
        get() = toastState

    private val progressCountState = MutableLiveData<String>()
    val progressCount: LiveData<String>
        get() = progressCountState

    private val progressState = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean>
        get() = progressState

    private val errorState = MutableLiveData<String>()
    val error: LiveData<String>
        get() = errorState

    val data: Flow<List<ImageItem>>
        get() = imagesDao.loadAsFlow()

    val result: Flow<List<ImageItem>>
        get() = imagesDao.loadResult()

    fun setData(data: List<String>) {
        viewModelScope.launch {
            val res = mapData(data)
//            val r = createData(res)

            imagesDao.insert(res)
        }
    }

    private var cacheFolder: String =
        getApplication<App>().filesDir.absolutePath + File.separator + WatermarkPhotosRepositoryImpl.HIDE_FOLDER_NAME

    init {
        startClearWorker()

//        viewModelScope.launch(Dispatchers.IO) {
//            for (i in 0..MAX_ITEM_COUNT.minus(1)) {
//                imagesDao.insert(ImageItem.logo(i, prefManager.logoPath))
//            }
//
//        }
    }

    fun clearData() {
        progressCountState.value = ""
        viewModelScope.launch(Dispatchers.IO) {
            val data = imagesDao.load()
            if (data.isNotEmpty() && data.first().overlayStatus.isNotEmpty()) {
                filesManager.clearAll(cacheFolder)
                imagesDao.clear()
            }
        }

    }

    fun startWork() {
        viewModelScope.launch {
            val data = imagesDao.load()
            if (data.isEmpty()) {
                errorState.value = "Data list is empty!"
            } else {
                val overlayPath = prefManager.overlayPath
                if (overlayPath.isEmpty()) {
                    errorState.value = "Overlay not selected!"
                } else {
                    preparing(data, overlayPath)
                }
            }
        }
    }

    fun saveToSystem() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = imagesDao.load()
            if (data.isNotEmpty()) {
                saveFiles(data)
            } else debugLog { "RESULT DATA IS EMPTY" }
        }
    }

    private suspend fun saveFiles(data: List<ImageItem>) = withContext(Dispatchers.IO) {
        val publicPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + File.separator + "ImageHandler"
        progressState.postValue(true)
        if (filesManager.createFolderIfEmpty(publicPath)) {
            if (data.isNotEmpty()) {
                val temp = mutableListOf<ImageItem>()
                if (temp.isNotEmpty()) temp.clear()
                progressCountState.postValue("")
                data.forEachIndexed { index, item ->
                    val bitmap = BitmapFactory.decodeFile(item.resultPath)
                    val (isOk, path) = mediaStoreManager.saveBitmapInGallery(
                        getApplication<App>(),
                        bitmap,
                        File(item.resultPath).name,
                        "ImageHandler",
                        System.currentTimeMillis()
                    )
                    if (isOk) {
                        progressCountState.postValue(String.format("%d/%d", index.inc(), data.size))
                        temp.add(item.copy(publicPath = path))
                    }
                }
                if (temp.isNotEmpty()) {
                    imagesDao.updateResultPaths(temp)
                }
            }
            progressState.postValue(false)
            toastState.postValue("Сохранение завершено")
        } else {
            progressState.postValue(false)
            errorState.postValue("Не могу скопировать в систему")
        }
    }

    private suspend fun preparing(data: List<ImageItem>, overlayPath: String) =
        withContext(Dispatchers.IO) {
            progressState.postValue(true)
            if (filesManager.createFolderIfEmpty(cacheFolder)) {
                data.forEachIndexed { index, imageItem ->

                    val resultBitmap = labelingImages(imageItem, overlayPath)
                    val (isOk, path) = filesManager.saveFile(
                        resultBitmap,
                        cacheFolder,
                        "vova_${imageItem.name}"
                    )

                    if (isOk) {
                        imagesDao.updateResultPath(imageItem.copy(
                            overlayStatus = ImageItem.OVERLAY_DONE,
                            resultPath = path
                        ))
                    } else {
                        imagesDao.updateResultPath(imageItem.copy(overlayStatus = ImageItem.OVERLAY_FAIL))
                    }
                    progressCountState.postValue(String.format("%d/%d", index.inc(), data.size))
                }

                progressState.postValue(false)
                toastState.postValue("Обработка данных завершена.")
            } else {
                progressState.postValue(false)
                errorState.value = "Fail create cache folder!"
            }
        }

    private fun labelingImages(item: ImageItem, overlayPath: String): Bitmap {
        val overlayBitmap = BitmapFactory.decodeFile(overlayPath)
        val originalBitmap = BitmapFactory.decodeFile(item.originalPath)

        return handleWork(originalBitmap, overlayBitmap)
    }

    private fun handleWork(originalBitmap: Bitmap, overlayBitmap: Bitmap): Bitmap =
        WatermarkBuilder.create(getApplication<App>(), originalBitmap).loadWatermarkImage(
            WatermarkImage(overlayBitmap, WatermarkPosition(0.005, 0.70))
                .setSize(0.4)
                .setImageAlpha(255)
        ).watermark.outputImage

    private fun mapData(data: List<String>): List<ImageItem> {
        return data.map { path ->
            val file = File(path)
            ImageItem(
                id = file.nameWithoutExtension,
                name = file.name,
                originalPath = path,
                overlayStatus = ImageItem.OVERLAY_NONE,
                resultPath = "",
                publicPath = ""
            )
        }
    }

    private fun startClearWorker() {
        val clearCacheWorker = OneTimeWorkRequestBuilder<ClearWorker>()
            .setInputData(workDataOf(ClearWorker.CACHE_PATH_KEY to cacheFolder))
            .build()

        WorkManager.getInstance(getApplication<App>()).enqueue(clearCacheWorker)
    }

    companion object {
        private const val MAX_ITEM_COUNT = 16
    }
}