package com.isanechek.imagehandler.data.repositories.impl

import android.content.Context
import com.isanechek.imagehandler.EMPTY_VALUE
import com.isanechek.imagehandler.OVERLAY_CACHE_FOLDER_NAME
import com.isanechek.imagehandler.data.local.database.dao.CitiesDao
import com.isanechek.imagehandler.data.local.database.entity.CityEntity
import com.isanechek.imagehandler.data.local.system.FilesManager
import com.isanechek.imagehandler.data.local.system.OverlayManager
import com.isanechek.imagehandler.data.local.system.PrefManager
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.data.models.ExecuteResult
import com.isanechek.imagehandler.data.models.ProgressState
import com.isanechek.imagehandler.data.repositories.SelectCityRepository
import com.isanechek.imagehandler.ext.isOverZero
import com.isanechek.imagehandler.ext.isZero
import com.isanechek.imagehandler.ext.toEntity
import com.isanechek.imagehandler.ext.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class SelectCityRepositoryImpl(
    private val citiesDao: CitiesDao,
    private val filesManager: FilesManager,
    private val overlayManager: OverlayManager,
    private val prefManager: PrefManager
) : SelectCityRepository {

    private val defaultCitiesList = listOf("Сызрань", "Энгельс")

    override val cities: Flow<List<City>>
        get() = citiesDao.loadAllCities()
            .map { collection ->
                collection.map { item ->
                    item.toModel()
                }
            }
    override val selectedCity: Flow<City>
        get() = citiesDao.loadSelectedCity()
            .map { item ->
                item?.toModel() ?: City.empty()
            }

    override suspend fun loadCities(context: Context): Flow<ProgressState> = flow {
        val count = citiesDao.count()
        if (count.isZero()) {
            emit(ProgressState.Loading)
            val temp = mutableListOf<CityEntity>()
            defaultCitiesList.forEach { city ->
                temp.add(
                    CityEntity(
                        id = city,
                        name = city,
                        isSelected = false,
                        overlayPath = EMPTY_VALUE
                    )
                )
            }

            when {
                temp.isNotEmpty() -> {
                    citiesDao.insert(temp)
                    emit(ProgressState.Done)
                }
                else -> emit(ProgressState.Error(""))
            }
        }
    }

    override suspend fun updateCity(city: City): Flow<ProgressState> = flow {
        emit(ProgressState.Loading)
        val code = citiesDao.updateCity(city.toEntity())
        when {
            code.isOverZero() -> emit(ProgressState.Done)
            else -> emit(ProgressState.Error(""))
        }
    }

    override suspend fun updateSelectedCity(city: City): Flow<ProgressState> = flow {
        emit(ProgressState.Loading)
        val code = citiesDao.updateSelected2(city.copy(isSelected = true).toEntity())
        when {
            code.isOverZero() -> emit(ProgressState.Done)
            else -> emit(ProgressState.Error(""))
        }
    }

    override suspend fun generateLogo(
        context: Context,
        cityName: String
    ): Flow<ExecuteResult<String>> = flow {
        emit(ExecuteResult.Progress)



        val logoPath = filesManager.loadLogoFromAssets(context)
        if (logoPath.isNotEmpty()) {
            val bitmap = overlayManager.drawTextOnLogo(logoPath, cityName)
            if (bitmap != null) {
                val cacheFolder = filesManager.getCacheFolder(context, OVERLAY_CACHE_FOLDER_NAME)
                val fileName = "logo_$cityName.png"
                val (isOk, path) = filesManager.saveFile(bitmap, cacheFolder, fileName)
                if (isOk) {
                    emit(ExecuteResult.Done(path))
                } else {
                    emit(ExecuteResult.Error("Save logo error!"))
                }
            } else {
                emit(ExecuteResult.Error("Bitmap logo is null"))
            }
        } else {
            emit(ExecuteResult.Error("Load logo from assets fail."))
        }
    }
}