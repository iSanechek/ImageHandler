package com.isanechek.imagehandler.ui.crop

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Message
import androidx.lifecycle.*
import com.isanechek.imagehandler.App
import com.isanechek.imagehandler.data.local.database.dao.ImagesDao
import com.isanechek.imagehandler.data.local.database.entity.ImageItem
import com.isanechek.imagehandler.data.local.system.FilesManager
import com.isanechek.imagehandler.data.local.system.PrefManager
import com.isanechek.imagehandler.data.models.ExecuteResult
import com.isanechek.imagehandler.data.repositories.WatermarkPhotosRepositoryImpl
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.utils.BitmapUtils
import com.isanechek.imagehandler.utils.TrackerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CropViewModel(
    application: Application,
    private val imagesDao: ImagesDao,
    private val filesManager: FilesManager,
    private val prefManager: PrefManager,
    private val trackerUtils: TrackerUtils
) : AndroidViewModel(application) {

    private val _stateProgress = MutableLiveData<Boolean>()
    val stateProgress: LiveData<Boolean>
        get() = _stateProgress

    private val _stateToast = MutableLiveData<String>()
    val stateToast: LiveData<String>
        get() = _stateToast

    private var cacheFolder: String =
        getApplication<App>().filesDir.absolutePath + File.separator + WatermarkPhotosRepositoryImpl.HIDE_FOLDER_NAME

    fun loadItem(id: String): LiveData<ImageItem?> = liveData {
        emit(imagesDao.loadItem(id))
    }

    private val _updateState = MutableLiveData<ExecuteResult<String>>()
    val updateState: LiveData<ExecuteResult<String>>
        get() = _updateState

    fun updateItem(item: ImageItem, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            _updateState.postValue(ExecuteResult.Progress)
            if (filesManager.createFolderIfEmpty(cacheFolder)) {
                val (isOk, path) = filesManager.saveFile(bitmap, cacheFolder, item.id)
                if (isOk) {
                    imagesDao.updateOriginalPath(
                        item.copy(
                            originalPath = path,
                            aspectRationOriginal = BitmapUtils.getBitmapRatio(path)
                        )
                    )
                    _updateState.postValue(ExecuteResult.Done("Все готово! Возвращаемся к списку.:)"))
                } else {
                    trackerUtils.sendEvent(TAG, "Update Item Method Save File Error!")
                    _updateState.postValue(ExecuteResult.Error("Упс. Не могу сохранить файл. Попробуйте позже!"))
                }
            } else {
                _updateState.postValue(ExecuteResult.Error("Не могу создать папку для кэширования!"))
                trackerUtils.sendEvent(TAG, "Update Item Method Not Created Cache Folder!")
            }
        }
    }

    private var testResultDone = false
    private val _bitmap = MutableLiveData<Bitmap>()
    val rBitmap: LiveData<Bitmap>
        get() = _bitmap

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
        val oX = overlayBitmap.width
        val oY = overlayBitmap.height
        val bX = backgroundBitmap.width
        val bY = backgroundBitmap.height

        val resultBitmap = Bitmap.createBitmap(
            bX,
            bY,
            backgroundBitmap.config
        )

        debugLog { "OVERLAY SIZE W $oX x H $oY" }

        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(backgroundBitmap, Matrix(), null)

        var left = 0f
        var top = 0f

        if (oX < oY) {
            debugLog { "" }
            val rOY = oY / 2
            left = bY / 2 - rOY.toFloat()
        } else if (oX > oY) {
            val rOX = oX / 2
            top = bX / 2 - rOX.toFloat()
        }

        canvas.drawBitmap(overlayBitmap, left, top, null)
        return resultBitmap
    }

    override fun onCleared() {
        super.onCleared()
        testResultDone = false
    }

    fun showProgress() {
        _stateProgress.value = false
    }

    fun hideProgress() {
        _stateProgress.value = true
    }

    fun showToast(message: String) {
        _stateToast.value = message
    }

    companion object {
        private const val TAG = "CropViewModel"
    }
}