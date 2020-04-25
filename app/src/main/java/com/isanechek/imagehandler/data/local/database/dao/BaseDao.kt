package com.isanechek.imagehandler.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<T>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOne(item: T)

    @RawQuery
    suspend fun findOne(query: SupportSQLiteQuery): T

    @RawQuery
    suspend fun deleteOne(query: SupportSQLiteQuery): T
}