package com.isanechek.imagehandler.utils

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment

fun Fragment.isRequestPermission(permissionName: String, callback: (String) -> Unit) {
    if (checkSelfPermission(this.requireContext(), permissionName) != PackageManager.PERMISSION_GRANTED) {
        callback.invoke("request")
    } else {
        callback.invoke("load")
    }
}