package com.isanechek.imagehandler.data.repositories

import android.content.Context
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.data.models.ExecuteResult
import com.isanechek.imagehandler.data.models.ProgressState
import kotlinx.coroutines.flow.Flow

interface SelectCityRepository {
    val cities: Flow<List<City>>
    val selectedCity: Flow<City>
    suspend fun loadCities(context: Context): Flow<ProgressState>
    suspend fun updateCity(city: City): Flow<ProgressState>
    suspend fun updateSelectedCity(city: City): Flow<ProgressState>
    suspend fun generateLogo(context: Context, cityName: String): Flow<ExecuteResult<String>>
}