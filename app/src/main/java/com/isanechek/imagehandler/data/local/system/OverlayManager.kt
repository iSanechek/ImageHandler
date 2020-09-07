package com.isanechek.imagehandler.data.local.system

import android.content.Context
import android.graphics.*
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

    suspend fun drawTextOnLogo(logoPath: String, text: String): Bitmap?

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

    override suspend fun drawTextOnLogo(logoPath: String, text: String): Bitmap? = suspendCancellableCoroutine { c ->
        try {
            var bitmap = BitmapFactory.decodeFile(logoPath)
            var bitmapConfig = bitmap.config
            if (bitmapConfig == null) {
                bitmapConfig = Bitmap.Config.ARGB_8888
            }
            bitmap = bitmap.copy(bitmapConfig, true)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.RED
            paint.textSize = 100.toFloat()
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            val fm = Paint.FontMetrics()
            paint.getFontMetrics(fm)
            paint.setShadowLayer(100.0f, 10.0f, 0.0f, Color.WHITE)
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            val textX = bitmap.width - bounds.width()
            val textY = bitmap.height - bounds.height()
            val x: Int = (textX - 150)
            val y: Int = (textY - 240)
//        val bg = Paint(Paint.ANTI_ALIAS_FLAG)
//        bg.color = Color.WHITE
//        bg.alpha = 25
//        canvas.drawRoundRect(
//            x - 40.toFloat(),
//            x + fm.top + 80,
//            y + paint.measureText(mText) + 40,
//            x + fm.bottom + 15,
//            16f,
//            16f,
//            bg
//        )
            canvas.drawText(text, x.toFloat(), y.toFloat(), paint)
            c.resume(bitmap)
        } catch (ex: Exception) {
            debugLog { "Draw text exception ${ex.message}" }
            c.resume(null)
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