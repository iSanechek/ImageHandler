package com.isanechek.imagehandler.ui.crop

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.lifecycle.*
import com.isanechek.imagehandler.App
import com.isanechek.imagehandler.data.local.database.dao.ImagesDao
import com.isanechek.imagehandler.data.local.database.entity.ImageItem
import com.isanechek.imagehandler.data.local.system.FilesManager
import com.isanechek.imagehandler.data.local.system.PrefManager
import com.isanechek.imagehandler.data.repositories.WatermarkPhotosRepositoryImpl
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CropViewModel(
    application: Application,
    private val imagesDao: ImagesDao,
    private val filesManager: FilesManager,
    private val prefManager: PrefManager
) : AndroidViewModel(application) {

    private var cacheFolder: String =
        getApplication<App>().filesDir.absolutePath + File.separator + WatermarkPhotosRepositoryImpl.HIDE_FOLDER_NAME

    fun loadItem(id: String): LiveData<ImageItem?> = liveData {
        emit(imagesDao.loadItem(id))
    }

    fun updateItem(item: ImageItem, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            if (filesManager.createFolderIfEmpty(cacheFolder)) {
                val (isOk, path) = filesManager.saveFile(bitmap, cacheFolder, item.id)
                if (isOk) {
                    debugLog { "${item.name} save done" }
                    imagesDao.updateOriginalPath(
                        item.copy(
                            originalPath = path,
                            aspectRationOriginal = BitmapUtils.getBitmapRatio(path)
                        )
                    )
                }
            } else {
                debugLog { "CACHE FOLDER NOT CREATED" }
            }
        }
    }

    private var testResultDone = false
    private val _bitmap = MutableLiveData<Bitmap>()
    val rBitmap: LiveData<Bitmap>
        get() = _bitmap

    private fun saveOverlay(id: String, path: String) {

    }

    fun overlayBitmap(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (filesManager.createFolderIfEmpty(cacheFolder)) {
                if (!testResultDone) {
                    val backPath = prefManager.backgroundPath
                    debugLog { "BACK PATH $backPath" }
                    debugLog { "ORIGINAL PATH $path" }
                    val back = BitmapUtils.decodeBitmap(backPath)
                    val overlay = BitmapUtils.decodeBitmap(path)
                    if (back != null && overlay != null) {
                        val result = overlayBitmap(back, overlay)
                        _bitmap.postValue(result)
                    } else {
                        debugLog { "OVERLAY OR BACK NULL!!!" }
                    }
                }
            }
        }
    }

    private fun overlayBitmap(backgroundBitmap: Bitmap, overlayBitmap: Bitmap): Bitmap {
        val resultBitmap = Bitmap.createBitmap(
            backgroundBitmap.width,
            backgroundBitmap.height,
            backgroundBitmap.config
        )
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(backgroundBitmap, Matrix(), null)
        val oX = overlayBitmap.width / 2
        val x = backgroundBitmap.width / 2 - oX
        canvas.drawBitmap(overlayBitmap, 0f, x.toFloat(), null)
        return resultBitmap
    }

    override fun onCleared() {
        super.onCleared()
        testResultDone = false
    }
}