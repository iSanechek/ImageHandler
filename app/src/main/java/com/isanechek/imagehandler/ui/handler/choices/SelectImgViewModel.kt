package com.isanechek.imagehandler.ui.handler.choices

import android.app.Application
import androidx.lifecycle.*
import com.isanechek.imagehandler.data.models.ExecuteResult
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.data.repositories.ImagesRepository
import com.isanechek.imagehandler.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class SelectImgViewModel(
    application: Application,
    private val repository: ImagesRepository
) : AndroidViewModel(application) {

    val selected: LiveData<List<Image>>
        get() = liveData {
            emit(repository.getSelectImageCache())
        }

    private val _progressState = MutableLiveData<Boolean>()
    val progressState: LiveData<Boolean>
        get() = _progressState

    private val _images = MutableLiveData<List<Image>>()
    val images: LiveData<List<Image>>
        get() = _images

    fun loadLastImg(refresh: Boolean) {
        debugLog { "INIT" }
        viewModelScope.launch {
            repository.loadLastImg(getApplication(), "", refresh)
                .flowOn(Dispatchers.IO)
                .catch {
                    debugLog { "ERROR ${it.message}" }
                }
                .collect { data ->
                    when(data) {
                        is ExecuteResult.Progress -> {
                            debugLog { "PROGRESS" }
                            _progressState.value = true
                        }
                        is ExecuteResult.Error -> {
                            _progressState.value = false
                            debugLog { "ERROR ${data.message}" }
                        }
                        is ExecuteResult.Done -> {
                            _progressState.value = false
                            _images.value = data.data
                            debugLog { "DONE ${data.data.size}" }
                        }
                        else -> Unit
                    }
                }
        }
    }

    fun setSelectedImages(ids: List<Long>) {
        viewModelScope.launch {
            repository.setSelectImagesCache(ids)
        }
    }

    fun clearCache() {
        repository.clearSelectImagesCache()
    }
}