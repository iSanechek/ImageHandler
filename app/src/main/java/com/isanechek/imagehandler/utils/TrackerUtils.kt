package com.isanechek.imagehandler.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics

interface TrackerUtils {
    fun sendEvent(tag: String, message: String)
    fun sendEventWithException(tag: String, message: String, exception: Exception)
    fun enableCrashAnalytics(enabled: Boolean)
}

class TrackerUtilsImpl : TrackerUtils {

    override fun sendEvent(tag: String, message: String) {

    }

    override fun sendEventWithException(tag: String, message: String, exception: Exception) {

    }

    override fun enableCrashAnalytics(enabled: Boolean) {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
    }

}