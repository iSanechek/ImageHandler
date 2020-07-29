package com.isanechek.imagehandler.ui.handler.choices

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.isanechek.imagehandler.data.models.ExecuteResult
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.data.repositories.SelectionRepository
import com.isanechek.imagehandler.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class SelectImgViewModel(
    application: Application,
    private val repository: SelectionRepository
) : AndroidViewModel(application) {

    private val _selectedImages = MutableLiveData<List<Image>>()
    val selected: LiveData<List<Image>>
        get() = _selectedImages

    private val _progressState = MutableLiveData<Boolean>()
    val progressState: LiveData<Boolean>
        get() = _progressState

    private val _images = MutableLiveData<List<Image>>()
    val images: LiveData<List<Image>>
        get() = _images

    fun loadLastImg() {
        debugLog { "INIT" }
        viewModelScope.launch {
            repository.loadLastImg(getApplication(), "", false)
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
            val data = repository.loadImagesIds(ids)
            if (data.isNotEmpty()) {
                _selectedImages.value = data
            }
        }
    }
}