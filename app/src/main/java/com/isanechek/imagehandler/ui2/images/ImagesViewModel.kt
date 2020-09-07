package com.isanechek.imagehandler.ui2.images

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.isanechek.imagehandler.App
import com.isanechek.imagehandler.data.models.ExecuteResult
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.data.models.UiState
import com.isanechek.imagehandler.data.repositories.ImagesRepository2
import com.isanechek.imagehandler.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class ImagesViewModel(application: Application, private val imagesRepository: ImagesRepository2) :
    AndroidViewModel(application) {

    private val context: Context = getApplication<App>()

    private val _uiState = MutableLiveData<UiState<List<Image>>>()
    val uiState: LiveData<UiState<List<Image>>>
        get() = _uiState

    fun setHomeState(state: UiState<List<Image>>) {
        _uiState.value = state
    }

    fun loadLastImages() {
        viewModelScope.launch {
            imagesRepository.loadLastImages(context, "")
                .flowOn(Dispatchers.IO)
                .catch {
                    debugLog { "loadLastImages error ${it.message}" }
                    setHomeState(UiState.Error("Load images error!"))
                }
                .collect { result ->
                    when(result) {
                        is ExecuteResult.Error -> setHomeState(UiState.Error(result.message))
                        is ExecuteResult.Done -> setHomeState(UiState.Done(result.data))
                        is ExecuteResult.Progress -> setHomeState(UiState.Progress)
                        else -> Unit
                    }
                }
        }
    }

    fun refreshLastImages() {

    }

}