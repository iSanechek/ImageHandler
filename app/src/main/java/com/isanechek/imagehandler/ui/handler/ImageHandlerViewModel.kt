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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class ImageHandlerViewModel(
    application: Application,
    private val filesManager: FilesManager,
    private val prefManager: PrefManager,
    private val mediaStoreManager: MediaStoreManager
) : AndroidViewModel(application) {

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

    private val dataState = MutableStateFlow(emptyList<ImageItem>())
    val data: Flow<List<ImageItem>>
        get() = dataState

    private val resultState = MutableStateFlow(emptyList<ImageItem>())
    val result: Flow<List<ImageItem>>
        get() = resultState

    fun setData(data: List<String>) {
        dataState.value = mapData(data)
    }

    private var cacheFolder: String =
        getApplication<App>().filesDir.absolutePath + File.separator + WatermarkPhotosRepositoryImpl.HIDE_FOLDER_NAME

    init {
        startClearWorker()
    }

    fun clearData() {
        dataState.value = emptyList()
        progressCountState.value = ""
        if (resultState.value.isNotEmpty() && resultState.value.first().overlayStatus.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                filesManager.clearAll(cacheFolder)
            }
        }
    }

    fun startWork() {
        val data = dataState.value
        if (data.isEmpty()) {
            errorState.value = "Data list is empty!"
            return
        }

        val overlayPath = prefManager.overlayPath
        if (overlayPath.isEmpty()) {
            errorState.value = "Overlay not selected!"
            return
        }

        viewModelScope.launch {
            preparing(data, overlayPath)
        }
    }

    fun saveToSystem() {
        val data = resultState.value
        if (data.isNotEmpty()) {
            viewModelScope.launch {
                saveFiles(data)
            }

        } else debugLog { "RESULT DATA IS EMPTY" }
    }

    private suspend fun saveFiles(data: List<ImageItem>) = withContext(Dispatchers.IO) {
        val publicPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + File.separator + "ImageHandler"
        progressState.postValue(true)
        if (filesManager.createFolderIfEmpty(publicPath)) {
            if (data.isNotEmpty()) {
                val temp = mutableListOf<ImageItem>()
                if (temp.isNotEmpty()) temp.clear()
                data.forEach { item ->
                    val bitmap = BitmapFactory.decodeFile(item.resultPath)
                    val (isOk, path) = mediaStoreManager.saveBitmapInGallery(
                        getApplication<App>(),
                        bitmap,
                        File(item.resultPath).name,
                        "ImageHandler",
                        System.currentTimeMillis()
                    )
                    if (isOk) {
                        temp.add(item.copy(publicPath = path))
                    }
                }
                if (temp.isNotEmpty()) {
                    resultState.value = temp
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
                val temp = mutableListOf<ImageItem>()
                if (temp.isNotEmpty()) temp.clear()
                data.forEachIndexed { index, imageItem ->
                    val resultBitmap = labelingImages(imageItem, overlayPath)
                    val (isOk, path) = filesManager.saveFile(
                        resultBitmap,
                        cacheFolder,
                        "vova_${imageItem.name}"
                    )

                    if (isOk) {
                        temp.add(
                            imageItem.copy(
                                overlayStatus = ImageItem.OVERLAY_DONE,
                                resultPath = path
                            )
                        )
                    } else {
                        temp.add(imageItem.copy(overlayStatus = ImageItem.OVERLAY_FAIL))
                    }


                    progressCountState.postValue(String.format("%d/%d", index.inc(), data.size))
                }
                resultState.value = temp
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

    private fun mapData(data: List<String>): List<ImageItem> =
        data.map { path ->
            ImageItem(
                id = UUID.randomUUID().toString(),
                name = File(path).name,
                originalPath = path,
                overlayStatus = ImageItem.OVERLAY_NONE,
                resultPath = "",
                publicPath = ""
            )
        }

    private fun startClearWorker() {
        val clearCacheWorker = OneTimeWorkRequestBuilder<ClearWorker>()
            .setInputData(workDataOf(ClearWorker.CACHE_PATH_KEY to cacheFolder))
            .build()

        WorkManager.getInstance(getApplication<App>()).enqueue(clearCacheWorker)
    }
}