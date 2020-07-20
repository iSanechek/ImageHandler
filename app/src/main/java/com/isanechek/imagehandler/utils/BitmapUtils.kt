package com.isanechek.imagehandler.utils

import android.graphics.*
import com.isanechek.imagehandler.aspectRatio
import com.isanechek.imagehandler.debugLog
import java.io.FileNotFoundException

object BitmapUtils {

    const val ASPECT_RATIO_1_1 = 0
    const val ASPECT_RATIO_16_9 = 1
    const val ASPECT_RATIO_9_16 = 2
    const val ASPECT_RATIO_UNKNOWN = 3

    fun getCornerRoundedBitmap(srcBitmap: Bitmap, cornerRadius: Int): Bitmap {
        val dstBitmap =
            Bitmap.createBitmap(srcBitmap.width, srcBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dstBitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        val rectF = RectF(0f, 0f, srcBitmap.width.toFloat(), srcBitmap.height.toFloat())
        canvas.drawRoundRect(rectF, cornerRadius.toFloat(), cornerRadius.toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(srcBitmap, 0f, 0f, paint)
        return dstBitmap
    }

    fun getBitmapRatio(path: String): Int {
        debugLog { "getBitmapRatio $path" }
        val bitmap = BitmapFactory.decodeFile(path)
        return checkAspectRatio(bitmap.width, bitmap.height)
    }

    private fun checkAspectRatio(width: Int, height: Int): Int = when {
        width > height -> ASPECT_RATIO_16_9
        height > width -> ASPECT_RATIO_9_16
        width == height -> ASPECT_RATIO_1_1
        else -> ASPECT_RATIO_UNKNOWN
    }

    fun decodeBitmap(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            debugLog { "DECODE BITMAP $path ERROR ${e.message}" }
            null
        }
    }
}