package com.isanechek.imagehandler.utils

import android.graphics.*
import com.isanechek.imagehandler.aspectRatio
import com.isanechek.imagehandler.debugLog
import java.io.FileNotFoundException

object BitmapUtils {

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
        return aspectRatio(bitmap.width, bitmap.height)
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