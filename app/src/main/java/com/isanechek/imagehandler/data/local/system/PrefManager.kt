package com.isanechek.imagehandler.data.local.system

import android.content.SharedPreferences
import androidx.core.content.edit

interface PrefManager {
    fun isFirstStart(): Boolean
    fun setFirstStartIsDone()
    var overlayPath: String
    var sampleImagePath: String
    var logoPath: String
    fun isShowWarningDialog(key: String): Boolean
    fun markDoneWaningShowDialog(key: String)
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
    override var sampleImagePath: String
        get() = preferences.getString(SAMPLE_IMAGE_PATH_KEY, "") ?: ""
        set(value) {
            preferences.edit {
                putString(SAMPLE_IMAGE_PATH_KEY, value)
            }
        }
    override var logoPath: String
        get() = preferences.getString(LOGO_PATH_KEY, "") ?: ""
        set(value) {
            preferences.edit {
                putString(LOGO_PATH_KEY, value)
            }
        }

    override fun isShowWarningDialog(key: String): Boolean {
        return preferences.getBoolean(key, true)
    }

    override fun markDoneWaningShowDialog(key: String) {
        preferences.edit {
            putBoolean(key, false)
        }
    }

    companion object {
        private const val FIRST_START_KEY = "first.start"
        private const val SAMPLE_IMAGE_PATH_KEY = "sipk"
        private const val LOGO_PATH_KEY = "lpk"
    }

}