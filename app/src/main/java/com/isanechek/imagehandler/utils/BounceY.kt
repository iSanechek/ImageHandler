package com.isanechek.imagehandler.utils

import kotlin.math.pow

class BounceY(bouncedWidth: Float) {
    private val mBouncedWidth: Float

    val time1: Float

    fun calulateY(x: Int): Float {
        return if (x <= time1) {
            (mBouncedWidth - 2 * mBouncedWidth * (x - TIME).toDouble().pow(2.0) / TIME.toDouble().pow(2.0)).toFloat()
        } else 0f
    }

    companion object {
        const val TIME = 250f
    }

    init {
        time1 = 1.2071f * TIME
        mBouncedWidth = bouncedWidth
    }
}