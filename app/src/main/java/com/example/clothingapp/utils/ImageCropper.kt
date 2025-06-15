package com.example.clothingapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

data class CropRect(
    val topLeft: Offset,
    val topRight: Offset,
    val bottomLeft: Offset,
    val bottomRight: Offset
) {
    fun toRectangle(): android.graphics.Rect {
        val left = min(min(topLeft.x, topRight.x), min(bottomLeft.x, bottomRight.x)).toInt()
        val top = min(min(topLeft.y, topRight.y), min(bottomLeft.y, bottomRight.y)).toInt()
        val right = max(max(topLeft.x, topRight.x), max(bottomLeft.x, bottomRight.x)).toInt()
        val bottom = max(max(topLeft.y, topRight.y), max(bottomLeft.y, bottomRight.y)).toInt()
        
        return android.graphics.Rect(left, top, right, bottom)
    }
}

object ImageCropper {
    suspend fun cropImage(
        context: Context,
        imageUri: Uri,
        cropRect: CropRect,
        imageWidth: Int,
        imageHeight: Int
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) return@withContext null
            
            // Get image orientation
            val exif = ExifInterface(context.contentResolver.openInputStream(imageUri)!!)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            
            // Apply orientation correction
            val rotatedBitmap = rotateBitmap(originalBitmap, orientation)
            
            // Calculate scale factors between display size and actual image size
            val scaleX = rotatedBitmap.width.toFloat() / imageWidth.toFloat()
            val scaleY = rotatedBitmap.height.toFloat() / imageHeight.toFloat()
            
            // Convert crop rectangle to actual image coordinates
            val actualCropRect = android.graphics.Rect(
                (cropRect.toRectangle().left * scaleX).toInt(),
                (cropRect.toRectangle().top * scaleY).toInt(),
                (cropRect.toRectangle().right * scaleX).toInt(),
                (cropRect.toRectangle().bottom * scaleY).toInt()
            )
            
            // Ensure crop rectangle is within image bounds
            val safeLeft = max(0, actualCropRect.left)
            val safeTop = max(0, actualCropRect.top)
            val safeRight = min(rotatedBitmap.width, actualCropRect.right)
            val safeBottom = min(rotatedBitmap.height, actualCropRect.bottom)
            
            val cropWidth = safeRight - safeLeft
            val cropHeight = safeBottom - safeTop
            
            if (cropWidth <= 0 || cropHeight <= 0) {
                return@withContext null
            }
            
            // Crop the image
            val croppedBitmap = Bitmap.createBitmap(
                rotatedBitmap,
                safeLeft,
                safeTop,
                cropWidth,
                cropHeight
            )
            
            // Save cropped image
            val croppedFile = File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(croppedFile)
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            
            // Clean up
            if (rotatedBitmap != originalBitmap) {
                rotatedBitmap.recycle()
            }
            originalBitmap.recycle()
            croppedBitmap.recycle()
            
            Uri.fromFile(croppedFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        
        return try {
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }
            rotatedBitmap
        } catch (e: OutOfMemoryError) {
            bitmap
        }
    }
}