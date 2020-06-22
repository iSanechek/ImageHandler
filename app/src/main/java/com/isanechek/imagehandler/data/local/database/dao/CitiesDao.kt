package com.isanechek.imagehandler.data.local.database.dao

import androidx.room.*
import com.isanechek.imagehandler.data.local.database.entity.CityEntity
import com.isanechek.imagehandler.debugLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CitiesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<CityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CityEntity)

    @Query("SELECT * FROM cities")
    fun loadAllCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities WHERE id =:id")
    fun loadCity(id: String): Flow<CityEntity>

    @Query("SELECT * FROM cities WHERE is_selected = 1")
    fun loadSelectedCity(): Flow<CityEntity>

    @Query("SELECT * FROM cities WHERE is_selected = 1")
    fun loadSelected(): CityEntity?

    @Delete
    suspend fun removeCity(item: CityEntity)

    @Query("SELECT COUNT(*) FROM cities")
    suspend fun count(): Int

    @Query("SELECT * FROM cities WHERE name =:name")
    suspend fun loadCityFromName(name: String): CityEntity?

    @Update
    suspend fun update(cityEntity: CityEntity)

    @Transaction
    suspend fun updateSelected(cityEntity: CityEntity) {
        val item = loadSelected()
        if (item != null) {
            debugLog { "ITEM FIND $item" }
            update(item.copy(isSelected = false))
            update(cityEntity)
        } else {
            update(cityEntity)
        }
    }
}