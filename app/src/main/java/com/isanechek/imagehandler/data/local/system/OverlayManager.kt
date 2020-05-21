package com.isanechek.imagehandler.data.local.system

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import com.isanechek.imagehandler.debugLog
import com.watermark.androidwm_light.WatermarkBuilder
import com.watermark.androidwm_light.bean.WatermarkImage
import com.watermark.androidwm_light.bean.WatermarkPosition
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class Points(
    val originalX: Float,
    val originalY: Float,
    val overlayX: Float,
    val overlayY: Float
)

interface OverlayManager {
    suspend fun overlay(
        context: Context,
        originalPath: String,
        overlayPath: String,
        overlayPosition: OverlayPosition
    ): Bitmap

    suspend fun overlay(
        context: Context,
        originalPath: String,
        overlayPath: String,
        marginX: Float,
        marginY: Float
    ): Bitmap

    suspend fun overlay(
        context: Context,
        originalPath: String,
        overlayPath: String
    ): Bitmap

    sealed class OverlayPosition {
        object TopStart : OverlayPosition()
        object TopEnd : OverlayPosition()
        object BottomStart : OverlayPosition()
        object BottomEnd : OverlayPosition()
        object Center : OverlayPosition()
    }
}

class OverlayManagerImpl : OverlayManager {

    override suspend fun overlay(
        context: Context,
        originalPath: String,
        overlayPath: String,
        overlayPosition: OverlayManager.OverlayPosition
    ): Bitmap = suspendCancellableCoroutine { c ->
        try {
            val originalBitmap = BitmapFactory.decodeFile(originalPath)
            val overlayBitmap = BitmapFactory.decodeFile(overlayPath)
            val (marginX, marginY) = defaultPosition(
                Points(
                    originalX = originalBitmap.width.toFloat(),
                    originalY = originalBitmap.height.toFloat(),
                    overlayX = overlayBitmap.width.toFloat(),
                    overlayY = overlayBitmap.height.toFloat()
                )
                , overlayPosition
            )
//            c.resume(handleAction(originalBitmap, overlayBitmap, marginX, marginY))
            c.resume(handleWork(context, originalBitmap, overlayBitmap))
        } catch (ex: Exception) {
            c.resumeWithException(ex)
        }
    }

    override suspend fun overlay(
        context: Context,
        originalPath: String,
        overlayPath: String,
        marginX: Float,
        marginY: Float
    ): Bitmap = suspendCancellableCoroutine { c ->
        try {
            val originalBitmap = BitmapFactory.decodeFile(originalPath)
            val overlayBitmap = BitmapFactory.decodeFile(overlayPath)
//            c.resume(handleAction(originalBitmap, overlayBitmap, marginX, marginY))
            c.resume(handleWork(context, originalBitmap, overlayBitmap))
        } catch (ex: Exception) {
            c.resumeWithException(ex)
        }
    }

    override suspend fun overlay(
        context: Context,
        originalPath: String,
        overlayPath: String
    ): Bitmap = suspendCancellableCoroutine { c ->
        try {
            val originalBitmap = BitmapFactory.decodeFile(originalPath)
            val overlayBitmap = BitmapFactory.decodeFile(overlayPath)
            c.resume(handleAction(originalBitmap, overlayBitmap, (0.01 * originalBitmap.width).toFloat(), (0.65 * originalBitmap.height).toFloat()))
        } catch (ex: Exception) {
            c.resumeWithException(ex)
        }

    }

    private fun handleAction(
        originalBitmap: Bitmap,
        overlayBitmap: Bitmap,
        marginX: Float,
        marginY: Float
    ): Bitmap {
        val resultBitmap = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height,
            originalBitmap.config
        )
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(originalBitmap, Matrix(), null)
        canvas.drawBitmap(resizeBitmap(overlayBitmap, 0.5f, originalBitmap), marginX, marginY, null)
        return resultBitmap
    }

    private fun handleWork(context: Context, originalBitmap: Bitmap, overlayBitmap: Bitmap): Bitmap =
        WatermarkBuilder.create(context, originalBitmap).loadWatermarkImage(
            WatermarkImage(overlayBitmap, WatermarkPosition(0.1, 0.8))
        ).watermark.outputImage

    // x - ширина y - высота
    private fun defaultPosition(
        points: Points,
        overlayPosition: OverlayManager.OverlayPosition
    ): Pair<Float, Float> {
        // bottom start position
        val x = (0.1 * points.originalX).toFloat()
        val y = (0.8 * points.originalY).toFloat()

        debugLog { "original x ${points.originalX} y ${points.originalY}" }
        debugLog { "result x $x y $y" }

        return Pair(x, y)
    }

    private fun resizeBitmap(
        watermarkImg: Bitmap,
        size: Float,
        backgroundImg: Bitmap
    ): Bitmap {
        val bitmapWidth = watermarkImg.width
        val bitmapHeight = watermarkImg.height
        val scale = backgroundImg.width * size / bitmapWidth
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        return Bitmap.createBitmap(
            watermarkImg, 0, 0,
            bitmapWidth, bitmapHeight, matrix, true
        )
    }

}