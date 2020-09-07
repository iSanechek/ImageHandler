package com.isanechek.imagehandler.data.local.system

import android.content.Context

interface AssetsManager {
    suspend fun loadImagesFromAssets(context: Context, folder: String, imageQuality: Int): List<String>
}