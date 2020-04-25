package com.isanechek.imagehandler.ui.overlay

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.isanechek.imagehandler.d
import com.isanechek.imagehandler.data.models.GalleryImageResult
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.data.models.WatermarkImageResult
import com.isanechek.imagehandler.data.repositories.WatermarkPhotosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class OverlayViewModel(
    application: Application,
    private val repository: WatermarkPhotosRepository
) : AndroidViewModel(application) {

    private val context: Context = getApplication()

    fun loadImages(isUpdate: Boolean = false): LiveData<GalleryImageResult> = liveData {
        val result = repository.loadImages(context, isUpdate)
        emitSource(result)
    }


    fun loadResult(): LiveData<List<WatermarkImageResult>> = liveData {
        repository.data()
            .flowOn(Dispatchers.IO)
            .catch {
                d { "Load data error ${it.message}" }
            }
            .collect { data ->
                emit(data)
            }
    }

    fun saveData(): LiveData<Boolean> = liveData {
        emit(true)
        repository.saveWatermarkResult(context)
            .flowOn(Dispatchers.IO)
            .catch {
                d { "Save file error ${it.message}" }
                emit(false)
            }
            .collect {
                d { "Save file state $it" }
                emit(false)
            }
    }
}
