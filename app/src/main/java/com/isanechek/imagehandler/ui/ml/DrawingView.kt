package com.isanechek.imagehandler.ui.ml

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import com.google.firebase.ml.vision.objects.FirebaseVisionObject

class DrawingView(context: Context, var visionObjects: List<FirebaseVisionObject>) : View(context) {

    companion object {
        val categoryNames: Map<Int, String> = mapOf(
            FirebaseVisionObject.CATEGORY_UNKNOWN to "Unknown",
            FirebaseVisionObject.CATEGORY_HOME_GOOD to "Home Goods",
            FirebaseVisionObject.CATEGORY_FASHION_GOOD to "Fashion Goods",
            FirebaseVisionObject.CATEGORY_FOOD to "Food",
            FirebaseVisionObject.CATEGORY_PLACE to "Place",
            FirebaseVisionObject.CATEGORY_PLANT to "Plant"
        )
    }

    val MAX_FONT_SIZE = 96F

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pen = Paint()
        pen.textAlign = Paint.Align.LEFT

        for (item in visionObjects) {
            pen.color = Color.WHITE
            pen.strokeWidth = 16F
            pen.style = Paint.Style.STROKE
            val box = item.boundingBox
            canvas.drawRect(box, pen)

            val tags: MutableList<String> = mutableListOf()
            tags.add("Category: ${categoryNames[item.classificationCategory]}")
            if (item.classificationCategory != FirebaseVisionObject.CATEGORY_UNKNOWN) {
                tags.add("Confidence: ${item.classificationConfidence!!.times(100).toInt()}%")
            }

            var tagSize = Rect(0, 0, 0, 0)
            var maxLen = 0
            var index: Int = -1

            for ((idx, tag) in tags.withIndex()) {
                if (maxLen < tag.length) {
                    maxLen = tag.length
                    index = idx
                }
            }

            pen.style = Paint.Style.FILL_AND_STROKE
            pen.color = Color.YELLOW
            pen.strokeWidth = 2F

            pen.textSize = MAX_FONT_SIZE
            pen.getTextBounds(tags[index], 0, tags[index].length, tagSize)
            val fontSize: Float = pen.textSize * box.width() / tagSize.width()

            if (fontSize < pen.textSize) pen.textSize = fontSize

            var margin = (box.width() - tagSize.width()) / 2.0F
            if (margin < 0F) margin = 0F

            for ((idx, txt) in tags.withIndex()) {
                canvas.drawText(
                    txt, box.left + margin,
                    box.top + tagSize.height().times(idx + 1.0F), pen
                )
            }
        }
    }
}