package com.isanechek.imagehandler.data.local.system

import android.content.SharedPreferences
import androidx.core.content.edit

interface PrefManager {
    fun isFirstStart(): Boolean
    fun setFirstStartIsDone()
    var overlayPath: String
}

class PrefManagerImpl(private val preferences: SharedPreferences) : PrefManager {

    override fun isFirstStart(): Boolean = preferences.getBoolean(FIRST_START_KEY, true)

    override fun setFirstStartIsDone() {
        preferences.edit {
            putBoolean(FIRST_START_KEY, false)
        }
    }

    override var overlayPath: String
        get() = preferences.getString("overlay_path", "") ?: ""
        set(value) {
            preferences.edit {
                putString("overlay_path", value)
            }
        }

    companion object {
        private const val FIRST_START_KEY = "first.start"
    }

}