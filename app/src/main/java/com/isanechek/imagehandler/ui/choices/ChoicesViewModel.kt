package com.isanechek.imagehandler.ui.choices

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.isanechek.imagehandler.d
import com.isanechek.imagehandler.data.models.*
import com.isanechek.imagehandler.data.repositories.ChoicesRepository
import com.isanechek.imagehandler.data.repositories.WatermarkPhotosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class ChoicesViewModel(
    application: Application,
    private val repository: WatermarkPhotosRepository,
    private val choicesRepository: ChoicesRepository
) : AndroidViewModel(application) {

    private val context: Context = getApplication()

    private val _showElevation = MutableLiveData<Boolean>()
    val showElevation: LiveData<Boolean>
        get() = _showElevation

    private val _toolbarTitle = MutableLiveData<String>()
    val toolbarTitle: LiveData<String>
        get() = _toolbarTitle

    val choice: LiveData<ChoicesResult<List<Folder>>>
        get() = choicesRepository.loadFolders(context)

    private val _showToolbarProgress = MutableLiveData<Boolean>()
    val toolbarProgressState: LiveData<Boolean>
        get() = _showToolbarProgress

    private val loadImagesWithUpdate = MutableLiveData<Boolean>()
    val imagesData: LiveData<GalleryImageResult>
        get() = Transformations.switchMap(loadImagesWithUpdate) { isUpdate ->
            repository.loadImages(context, isUpdate)
        }

    private val loadFolders = MutableLiveData<Boolean>()
    val folders: LiveData<ChoicesResult<List<Album>>>
        get() = Transformations.switchMap(loadFolders) { isUpdate ->
            choicesRepository.loadFolders(context, isUpdate)
        }

    init {
        _toolbarTitle.value = ""
    }

    fun setToolbarTitle(title: String) {
        _toolbarTitle.value = title
    }

    fun loadFolders() {
        loadFolders.value = false
    }

    fun updateFolders() {
        loadFolders.value = true
    }

    fun showToolbarProgress() {
        _showToolbarProgress.value = false
    }

    fun hideToolbarProgress() {
        _showToolbarProgress.value = true
    }

    fun loadFolders(isRefresh: Boolean) {

    }

    fun loadImages(isRefresh: Boolean = false) {
        loadImagesWithUpdate.value = isRefresh
    }

    fun showToolbarElevation(isShow: Boolean) {
        _showElevation.value = isShow
    }
}