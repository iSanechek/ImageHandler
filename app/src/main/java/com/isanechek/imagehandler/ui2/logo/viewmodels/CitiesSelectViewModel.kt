package com.isanechek.imagehandler.ui2.logo.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.isanechek.imagehandler.App
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.data.models.ProgressState
import com.isanechek.imagehandler.data.models.UiState
import com.isanechek.imagehandler.data.repositories.SelectCityRepository
import com.isanechek.imagehandler.ext.io
import com.isanechek.imagehandler.ext.main
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class CitiesSelectViewModel(
    application: Application,
    private val repository: SelectCityRepository
) : AndroidViewModel(application) {

    init {
        initLoadData()
    }

    val data: LiveData<List<City>>
        get() = repository.cities.asLiveData(context = Dispatchers.IO)

    private val _uiState = MutableLiveData<UiState<Int>>()
    val uiState: LiveData<UiState<Int>>
        get() = _uiState

    fun setSelectedCity(city: City) {
        viewModelScope.launch(main) {
            repository.updateSelectedCity(city)
                .flowOn(io)
                .catch { error -> setState(UiState.Error(error.message ?: "")) }
                .collect { state -> setProgressState(state, 1) }
        }
    }

    private fun initLoadData() {
        viewModelScope.launch(main) {
            repository.loadCities(getApplication<App>())
                .flowOn(io)
                .catch { error -> setState(UiState.Error(error.message ?: "Init data some error!")) }
                .collect { state -> setProgressState(state, 0) }
        }
    }

    private fun setProgressState(state: ProgressState, type: Int) {
        when(state) {
            is ProgressState.Loading -> setState(UiState.Progress)
            is ProgressState.Done -> setState(UiState.Done(type))
            is ProgressState.Error -> setState(UiState.Error(state.message))
        }
    }

    private fun setState(state: UiState<Int>) {
        _uiState.value = state
    }
}