package com.isanechek.imagehandler.ui.overlay

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.isanechek.imagehandler.App
import com.isanechek.imagehandler._drawable
import com.isanechek.imagehandler.data.local.database.dao.WatermarkDao
import com.isanechek.imagehandler.debugLog
import kotlinx.coroutines.Dispatchers

class OverlayViewModel(
    application: Application,
    private val watermarkDao: WatermarkDao
) : AndroidViewModel(application) {

    fun overlayBitmap(): LiveData<Bitmap?> = liveData(Dispatchers.IO) {
        val watermark = watermarkDao.findSelected()
        if (watermark != null) {
            debugLog { "WATERMARK $watermark" }

            val bitmap = BitmapFactory.decodeFile(watermark.path)
            emit(bitmap)
        } else emit(null)
    }

}