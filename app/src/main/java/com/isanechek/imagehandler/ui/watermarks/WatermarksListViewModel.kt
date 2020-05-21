package com.isanechek.imagehandler.ui.watermarks

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isanechek.imagehandler.App
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.data.local.database.dao.WatermarkDao
import com.isanechek.imagehandler.data.local.database.entity.WatermarkChoicesImageEntity
import com.isanechek.imagehandler.data.local.system.FilesManager
import com.isanechek.imagehandler.data.local.system.PrefManager
import com.isanechek.imagehandler.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatermarksListViewModel(
    application: Application,
    private val watermarkDao: WatermarkDao,
    private val filesManager: FilesManager,
    private val prefManager: PrefManager
) : AndroidViewModel(application) {

    val data = watermarkDao.findAllLive()

    fun addWatermark(uri: Uri) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                val path = FileUtils.getPath(getApplication<App>().applicationContext, uri) ?: ""
                // add check empty path
                if (filesManager.checkFileExists(path)) {
                    prefManager.overlayPath = path

                    val id = filesManager.getFileName(path)
                    watermarkDao.insert(
                        WatermarkChoicesImageEntity(
                            id = id,
                            title = id,
                            addTime = System.currentTimeMillis(),
                            isSelected = false,
                            path = path
                        )
                    )
                } else debugLog { "Watermark file not find!" }
            }
        }
    }

    fun selectWatermark(id: String) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                watermarkDao.select(id)
            }
        }
    }

}