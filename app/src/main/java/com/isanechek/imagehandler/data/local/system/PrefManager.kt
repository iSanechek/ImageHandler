package com.isanechek.imagehandler.data.local.system

import android.content.SharedPreferences
import androidx.core.content.edit

interface PrefManager {
    fun isFirstStart(): Boolean
    fun setFirstStartIsDone()
}

class PrefManagerImpl(private val preferences: SharedPreferences) : PrefManager {

    override fun isFirstStart(): Boolean = preferences.getBoolean(FIRST_START_KEY, true)

    override fun setFirstStartIsDone() {
        preferences.edit {
            putBoolean(FIRST_START_KEY, false)
        }
    }

    companion object {
        private const val FIRST_START_KEY = "first.start"
    }

}