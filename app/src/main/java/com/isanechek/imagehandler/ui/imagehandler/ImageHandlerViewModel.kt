package com.isanechek.imagehandler.ui.imagehandler

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Config
import androidx.paging.toLiveData
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.data.local.database.dao.ImageHandlerDao
import com.isanechek.imagehandler.data.local.database.dao.WatermarkDao
import com.isanechek.imagehandler.data.local.database.entity.ImageHandlerEntity
import com.isanechek.imagehandler.data.local.system.FilesManager
import com.isanechek.imagehandler.data.local.system.MediaStoreManager
import com.isanechek.imagehandler.data.local.system.OverlayManager
import com.isanechek.imagehandler.data.models.ExecuteResult
import com.isanechek.imagehandler.data.repositories.WatermarkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ImageHandlerViewModel(
    application: Application,
    private val imagesDao: ImageHandlerDao,
    private val watermarkDao: WatermarkDao,
    private val filesManager: FilesManager,
    private val overlayManager: OverlayManager,
    private val mediaStoreManager: MediaStoreManager,
    private val repository: WatermarkRepository
) : AndroidViewModel(application) {

    private val _scrollRecycler = MutableLiveData<Int>()
    val scrollRecycler: LiveData<Int> = _scrollRecycler

    private val _progressMessage = MutableLiveData<String>()
    val progressMessage: LiveData<String> = _progressMessage

    private val _progressState = MutableLiveData<Boolean>()
    val progressState: LiveData<Boolean> = _progressState

    private val _showToast = MutableLiveData<String>()
    val showToast: LiveData<String> = _showToast

    val data = imagesDao.loadAll()
        .toLiveData(Config(pageSize = 10, prefetchDistance = 10, enablePlaceholders = false))

    fun startAction(path: String?) {
        if (path != null) {
            startAction(listOf(path))
        } else {
            debugLog { "Path null" }
        }
    }

    fun startAction(paths: List<String>) {
        debugLog { "uris size ${paths.size}" }
        viewModelScope.launch(Dispatchers.Main) {

            withContext(Dispatchers.IO) {
                val temp = mutableListOf<ImageHandlerEntity>()
                paths.forEach { path ->
                    temp.add(createModel(path))
                }

                debugLog { "temp size ${temp.size}" }

                if (temp.isNotEmpty()) {
                    imagesDao.insertAll(temp)
                }
            }
        }
    }

    private fun saveData(paths: Set<String>) {

    }


    fun clearAll() {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                removeFiles()
            }
        }
    }

    private suspend fun removeFiles() {
        progressShow()
        val removeIds = mutableListOf<String>()
        val data = imagesDao.loadData()
        if (data.isNotEmpty()) {
            data.filter { item -> when {
                    item.copyPath.isNotEmpty() -> filesManager.deleteFile(item.copyPath)
                    else -> true
                } }
                .filter { item -> when {
                        item.resultPath.isNotEmpty() -> filesManager.deleteFile(item.resultPath)
                        else -> true
                    }
                }
                .forEach { item -> removeIds.add(item.id) }
            if (removeIds.isNotEmpty()) {
                imagesDao.remove(removeIds)
                progressShow(false)
                _showToast.postValue("Удалено ${removeIds.size} обьектов")
            } else {
                progressShow(false)
                _showToast.postValue("Нечего удалять")
            }
        } else {
            progressShow(false)
            _showToast.postValue("Нечего удалять")
        }
    }

    fun startWork() {
        viewModelScope.launch(Dispatchers.Main) {
            handleWatermark()
        }
    }

    @ExperimentalCoroutinesApi
    fun saveAll() {
        viewModelScope.launch(Dispatchers.Main) {
            repository.saveResult(getApplication<Application>())
                .flowOn(Dispatchers.IO)
                .catch {
                    progressShow(false)
                    _showToast.value = it.message
                }.collect { execute ->
                    when (execute) {
                        is ExecuteResult.Progress -> {
                            progressShow()
                            when (execute.status.first) {
                                PROGRESS_STATE_LOAD_DATA_FROM_DB -> progressMsg("Загрузка данных из базы")
                                PROGRESS_STATE_CHECK_PRIVATE_FOLDER -> progressMsg("Проверка папки для кэша")
                                PROGRESS_STATE_EXECUTE_FILES_SIZE -> progressMsg(
                                    String.format(
                                        "%d файла для обработки",
                                        execute.status.second
                                    )
                                )
                                PROGRESS_STATE_CHECK_EXISTS_FILE -> progressMsg(
                                    String.format(
                                        "Проверка целостности %s",
                                        execute.status.second
                                    )
                                )
                                PROGRESS_STATE_SAVE_FILE -> progressMsg(
                                    String.format(
                                        "Сохранение в систему %s ...",
                                        execute.status.second
                                    )
                                )
                                PROGRESS_STATE_SAVE_FILE_DONE -> progressMsg(
                                    String.format(
                                        "Сохранение в систему %s завершено",
                                        execute.status.second
                                    )
                                )
                                PROGRESS_STATE_SAVE_FILE_FAIL -> progressMsg(
                                    String.format(
                                        "Сохранение в систему завершено с ошибкой %s",
                                        execute.status.second
                                    )
                                )
                                PROGRESS_STATE_ADD_SIZE_IN_GALLEY -> progressMsg(
                                    String.format(
                                        "%d файлов сохранине в систему",
                                        execute.status.second
                                    )
                                )
                            }
                        }
                        is ExecuteResult.Done -> {
                            debugLog { "Boooo" }
                            progressShow(false)
                        }
                        is ExecuteResult.Error -> {
                            debugLog { "Error ${execute.message}" }
                            progressShow(false)
                            _showToast.value = execute.message
                        }
                    }
                }
        }
    }

    private fun progressMsg(msg: String) {
        _progressMessage.value = msg
    }

    private suspend fun handleWatermark() = withContext(Dispatchers.IO) {
        progressShow()
        val data = imagesDao.loadData()
        if (data.isNotEmpty()) {
            for (i in data.indices - 1) {
                if (i % 2 == 0) {
                    _scrollRecycler.postValue(i)
                }


                val item = data[i]
                if (filesManager.checkFileExists(item.originalPath)) {
                    val context = getApplication<Application>().applicationContext
                    val rootFolderPath = context.filesDir.absolutePath + File.separator + "app_data"
                    if (filesManager.createFolderIfEmpty(rootFolderPath)) {
                        val (isOk, path) = filesManager.copyFile(
                            item.originalPath,
                            rootFolderPath,
                            item.title
                        )
                        if (isOk) {
                            val i = item.copy(copyPath = path)
                            val watermarkItem = watermarkDao.findSelected()
                            if (watermarkItem != null) {
                                val watermarkBitmap =
                                    overlayManager.overlay(context, path, watermarkItem.path)
                                val saveResult = filesManager.saveFile(
                                    watermarkBitmap,
                                    rootFolderPath,
                                    item.title
                                )
                                if (saveResult.first) {
//                                    temp.add(i.copy(resultPath = saveResult.second))
                                    update(i.copy(resultPath = saveResult.second))

                                } else {
                                    debugLog { "Save watermark error!" }
                                }



                            } else debugLog { "Watermark item not find!" }
                        } else update(item.copy(copyPath = "fail"))

                    } else debugLog { "Root folder not create!" }

                } else debugLog { "Original file is null!" }
            }
            progressShow(false)
            showToast("Обработано ${data.size}")
        } else {
            debugLog { "Data is empty!" }
            _progressState.postValue(false)
        }
    }

    private suspend fun update(item: ImageHandlerEntity) {
        imagesDao.update(item)
    }

    private fun createModel(path: String): ImageHandlerEntity {
        debugLog { "original path $path" }
        val id = filesManager.getFileName(path)
        debugLog { "id $id" }
        val title = filesManager.getFileName(path)
        debugLog { "title $title" }
        val time = System.currentTimeMillis()
        return ImageHandlerEntity(
            id = id,
            title = title,
            createTime = time,
            lastUpdate = time,
            originalPath = path,
            resultPath = "",
            copyPath = "",
            status = "",
            message = "",
            publicPath = ""
        )
    }

    private fun progressShow(show: Boolean = true) {
        _progressState.postValue(show)
    }

    private fun showToast(message: String) {
        _showToast.postValue(message)
    }
}