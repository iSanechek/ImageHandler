package com.isanechek.imagehandler.ui2.logo.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.isanechek.imagehandler.App
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.data.models.ExecuteResult
import com.isanechek.imagehandler.data.models.UiState
import com.isanechek.imagehandler.data.repositories.SelectCityRepository
import com.isanechek.imagehandler.ext.io
import com.isanechek.imagehandler.ext.main
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class CreateLogoViewModel(
    application: Application,
    private val repository: SelectCityRepository
) : AndroidViewModel(application) {

    val selectedCity: LiveData<City>
        get() = repository.selectedCity.asLiveData(io)

    private val _uiState = MutableLiveData<UiState<String>>()
    val uiState: LiveData<UiState<String>>
        get() = _uiState

    fun generateLogo(city: City) {
        viewModelScope.launch(main) {
            repository.generateLogo(getApplication<App>(), city.name)
                .flowOn(io)
                .catch { error -> setState(UiState.Error(error.message ?: "")) }
                .collect { result ->
                    when (result) {
                        is ExecuteResult.Error -> setState(UiState.Error(result.message))
                        is ExecuteResult.Done -> setState(UiState.Done(result.data))
                        is ExecuteResult.Progress -> setState(UiState.Progress)
                        is ExecuteResult.ProgressWithStatus -> Unit
                    }
                }
        }
    }

    private fun setState(state: UiState<String>) {
        _uiState.value = state
    }

}