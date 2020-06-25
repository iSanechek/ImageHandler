package com.isanechek.imagehandler.ui.city

import android.app.Application
import androidx.lifecycle.*
import com.isanechek.imagehandler.data.local.database.dao.CitiesDao
import com.isanechek.imagehandler.data.local.database.entity.CityEntity
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SelectViewModel(application: Application, private val citiesDao: CitiesDao) :
    AndroidViewModel(application) {

    private val listCities = listOf("Сызрань", "Энгельс")
    private val _stateProgress = MutableLiveData<Boolean>()
    private val progressState: LiveData<Boolean>
        get() = _stateProgress

    private val _stateError = MutableLiveData<String>()
    val errorState: LiveData<String>
        get() = _stateError

    val data: Flow<List<City>>
        get() = citiesDao.loadAllCities().map { items ->
            items.map { item ->
                City(
                    id = item.id,
                    name = item.name,
                    isSelected = item.isSelected
                )
            }
        }

    fun loadCities2() {
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
                            item.isSelected
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

    fun saveCity(city: String) {
        viewModelScope.launch {
            val item = citiesDao.loadCityFromName(city)
            if (item == null) {
                citiesDao.insert(CityEntity(UUID.randomUUID().toString(), city, false))
            } else {
                _stateError.value = "Город уже добавлен!"
            }
        }
    }

    fun updateCity(city: City) {
        viewModelScope.launch {
            citiesDao.updateSelected(CityEntity(city.id, city.name, city.isSelected))
        }
    }

    fun removeCity(city: City) {
        viewModelScope.launch {
            citiesDao.removeCity(CityEntity(city.id, city.name, city.isSelected))
        }
    }

    private fun mapCities(): List<City> {
        val temp = mutableListOf<City>()

        listCities.forEach { item ->
            temp.add(City(UUID.randomUUID().toString(), item, false))
        }

        return temp
    }
}