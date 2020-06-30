package com.isanechek.imagehandler.ui.city

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.*
import com.isanechek.imagehandler.data.local.database.dao.CitiesDao
import com.isanechek.imagehandler.data.local.database.entity.CityEntity
import com.isanechek.imagehandler.data.local.system.FilesManager
import com.isanechek.imagehandler.data.local.system.OverlayManager
import com.isanechek.imagehandler.data.local.system.PrefManager
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SelectViewModel(
    application: Application,
    private val citiesDao: CitiesDao,
    private val filesManager: FilesManager,
    private val overlayManager: OverlayManager,
    private val prefManager: PrefManager
) : AndroidViewModel(application) {

    private val listCities = listOf("Сызрань", "Энгельс")
    private val _stateProgress = MutableLiveData<Boolean>()
    private val progressState: LiveData<Boolean>
        get() = _stateProgress

    private val _stateScreen = MutableLiveData<Int>()
    val stateScreen: LiveData<Int>
        get() = _stateScreen

    private val _stateError = MutableLiveData<String>()
    val errorState: LiveData<String>
        get() = _stateError

    val data: Flow<List<City>>
        get() = citiesDao.loadAllCities().map { items ->
            items.map { item ->
                City(
                    id = item.id,
                    name = item.name,
                    isSelected = item.isSelected,
                    overlayPath = item.overlayPath
                )
            }
        }

    val city: Flow<CityEntity?>
        get() = citiesDao.loadSelectedCity()

    init {
        loadCities2()
    }

    fun goToScreen(screen: Int) {
        _stateScreen.value = screen
    }

    private fun loadCities2() {
        viewModelScope.launch {
            val count = citiesDao.count()
            if (count == 0) {
                debugLog { "CITIES COUNT 0" }
                _stateProgress.value = true
                val data = withContext(Dispatchers.IO) { mapCities() }
                if (data.isNotEmpty()) {
                    citiesDao.insert(data.map { item ->
                        CityEntity(
                            item.id,
                            item.name,
                            item.isSelected,
                            item.overlayPath
                        )
                    })
                    _stateProgress.value = false
                } else {
                    _stateProgress.value = false
                    _stateError.value = "При загрузке списка городов произошла ошибка."
                }
            } else debugLog { "CITIES COUNT $count" }
        }
    }

    fun loadSelectedCity(): LiveData<Boolean> = liveData(Dispatchers.IO) {
        citiesDao.loadSelectedCity().collect {
            debugLog { "SELECT CITY $it" }
            if (it != null) {
                emit(true)
            } else emit(false)
        }
    }

    fun updateCity(city: City) {
        viewModelScope.launch {
            citiesDao.updateSelected(
                CityEntity(
                    city.id,
                    city.name,
                    city.isSelected,
                    city.overlayPath
                )
            )
        }
    }

    fun saveOverlay(overlayPath: String) {
        prefManager.overlayPath = overlayPath
        prefManager.setFirstStartIsDone()
    }

    fun removeCity(city: City) {
        viewModelScope.launch {
            citiesDao.removeCity(CityEntity(city.id, city.name, city.isSelected, city.overlayPath))
        }
    }

    fun loadPreview(overlayPath: String): LiveData<Bitmap> = liveData(Dispatchers.IO) {
        val samplePath = prefManager.sampleImagePath
        debugLog { "SP -> $samplePath" }
        val result = overlayManager.overlay(getApplication(), samplePath, overlayPath)
        emit(result)
    }

    private suspend fun mapCities(): List<City> {
        val temp = mutableListOf<City>()

        val logos = filesManager.loadImagesFromAssets(getApplication())

        debugLog { "SIZE PATHS ${logos.size}" }
        if (logos.isEmpty()) return emptyList()

        val samplePath = logos.find { it.contains("sample", ignoreCase = true) }
        debugLog { "Sample path $samplePath" }
        if (!samplePath.isNullOrEmpty()) {
            debugLog { "BOOOOM" }
            prefManager.sampleImagePath = samplePath
        }

        listCities.forEach { item ->
            val path = when(item) {
                "Сызрань" -> logos.find { it.contains("syzran", ignoreCase = true) } ?: ""
                "Энгельс" -> logos.find { it.contains("engels", ignoreCase = true) } ?: ""
                else -> ""
            }
            debugLog { "CITY $item PATH $path" }
            temp.add(City(UUID.randomUUID().toString(), item, false, path))
        }

        return temp
    }
}