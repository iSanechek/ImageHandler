package com.isanechek.imagehandler.data.repositories

import com.isanechek.imagehandler.data.local.system.PrefManager

interface DashboardRepository {
    fun setSelectRatio(ratio: Int)
}

class DashboardRepositoryImpl(private val prefManager: PrefManager) : DashboardRepository {

    override fun setSelectRatio(ratio: Int) {
        prefManager.setSelectRation(ratio)
    }

}