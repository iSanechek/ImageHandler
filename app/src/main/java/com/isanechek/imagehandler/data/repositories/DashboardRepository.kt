package com.isanechek.imagehandler.data.repositories

import com.isanechek.imagehandler.data.local.system.PrefManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface DashboardRepository {
    val toolbarTitle: Flow<String>
    fun setSelectRatio(ratio: Int)
}

class DashboardRepositoryImpl(private val prefManager: PrefManager) : DashboardRepository {

    override val toolbarTitle: Flow<String>
        get() = flow {
            emit("Добро\nПожаловать")
        }

    override fun setSelectRatio(ratio: Int) {
        prefManager.setSelectRation(ratio)
    }

}