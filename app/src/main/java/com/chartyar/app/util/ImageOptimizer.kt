package com.chartyar.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object ImageOptimizer {

    private const val MAX_WIDTH = 1920
    private const val MAX_HEIGHT = 1920
    private const val QUALITY = 85

    fun optimizeForOcr(bitmap: Bitmap): Bitmap {
        return try {
            val width = bitmap.width
            val height = bitmap.height

            if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
                return bitmap
            }

            val ratio = minOf(
                MAX_WIDTH.toFloat() / width,
                MAX_HEIGHT.toFloat() / height
            )

            val newWidth = (width * ratio).toInt()
            val newHeight = (height * ratio).toInt()

            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } catch (e: Exception) {
            bitmap
        }
    }

    fun compressBitmap(bitmap: Bitmap, quality: Int = QUALITY): ByteArray? {
        return try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    fun decodeWithSampling(data: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(data, 0, data.size, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            BitmapFactory.decodeByteArray(data, 0, data.size, options)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun getBitmapSize(bitmap: Bitmap): Long {
        return try {
            bitmap.allocationByteCount.toLong()
        } catch (e: Exception) {
            0L
        }
    }
}