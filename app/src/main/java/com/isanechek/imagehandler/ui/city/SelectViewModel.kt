package com.isanechek.imagehandler.ui.city

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.debugLog
import kotlinx.coroutines.Dispatchers
import java.util.*

class SelectViewModel(application: Application) : AndroidViewModel(application) {

    private val listCities = listOf("Сызрань", "Энгельс")
    private val _stateProgress = MutableLiveData<Boolean>()
    private val progressState: LiveData<Boolean>
        get() = _stateProgress

    private val _stateError = MutableLiveData<String>()
    val errorState: LiveData<String>
        get() = _stateError

    fun loadCities(): LiveData<List<City>> = liveData(Dispatchers.IO) {
        _stateProgress.postValue(true)
        val data = mapCities()
        if (data.isNotEmpty()) {
            _stateProgress.postValue(false)
            emit(data)
        } else {
            _stateProgress.postValue(false)
            _stateError.postValue("При загрузке списка городов произошла ошибка.")
        }
    }

    fun loadSelectedCity(): LiveData<String> = liveData {
        emit("")
    }

    fun saveSelectedCity(city: String) {

    }

    private fun mapCities(): List<City> {
        val temp = mutableListOf<City>()

        listCities.forEach { item ->
            debugLog { "CITY $item" }
            temp.add(City(UUID.randomUUID().toString(), item, false))
        }

        return temp
    }
}