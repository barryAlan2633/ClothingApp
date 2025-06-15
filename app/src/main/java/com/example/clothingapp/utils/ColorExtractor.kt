package com.example.clothingapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.sqrt

data class ExtractedColors(
    val primary: String,
    val secondary: String? = null,
    val isDarkDominant: Boolean = false
)

object ColorExtractor {
    
    suspend fun extractColorsFromImage(
        context: Context,
        imageUri: Uri
    ): ExtractedColors? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap == null) return@withContext null
            
            // Resize bitmap for faster processing
            val resizedBitmap = if (bitmap.width > 150 || bitmap.height > 150) {
                val ratio = minOf(150f / bitmap.width, 150f / bitmap.height)
                val newWidth = (bitmap.width * ratio).toInt()
                val newHeight = (bitmap.height * ratio).toInt()
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }
            
            val palette = Palette.from(resizedBitmap).generate()
            
            val colors = extractDominantColors(palette)
            
            // Clean up bitmaps
            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }
            bitmap.recycle()
            
            colors
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun extractDominantColors(palette: Palette): ExtractedColors {
        val colors = mutableListOf<Pair<Int, String>>()
        
        // Get all available swatches with their populations
        palette.vibrantSwatch?.let { swatch ->
            colors.add(swatch.population to colorToString(swatch.rgb))
        }
        palette.mutedSwatch?.let { swatch ->
            colors.add(swatch.population to colorToString(swatch.rgb))
        }
        palette.darkVibrantSwatch?.let { swatch ->
            colors.add(swatch.population to colorToString(swatch.rgb))
        }
        palette.darkMutedSwatch?.let { swatch ->
            colors.add(swatch.population to colorToString(swatch.rgb))
        }
        palette.lightVibrantSwatch?.let { swatch ->
            colors.add(swatch.population to colorToString(swatch.rgb))
        }
        palette.lightMutedSwatch?.let { swatch ->
            colors.add(swatch.population to colorToString(swatch.rgb))
        }
        
        // Sort by population (most dominant first)
        colors.sortByDescending { it.first }
        
        // Remove duplicates and very similar colors
        val uniqueColors = mutableListOf<String>()
        for (color in colors) {
            val colorName = color.second
            if (uniqueColors.none { isSimilarColor(it, colorName) }) {
                uniqueColors.add(colorName)
            }
        }
        
        val primary = uniqueColors.firstOrNull() ?: "Unknown"
        val secondary = uniqueColors.getOrNull(1)
        
        // Determine if dark colors are dominant
        val isDarkDominant = palette.darkVibrantSwatch != null || palette.darkMutedSwatch != null
        
        return ExtractedColors(
            primary = primary,
            secondary = secondary,
            isDarkDominant = isDarkDominant
        )
    }
    
    private fun colorToString(rgb: Int): String {
        val color = Color(rgb)
        return when {
            // Basic colors
            isColorSimilar(color, Color.Red) -> "Red"
            isColorSimilar(color, Color.Blue) -> "Blue"
            isColorSimilar(color, Color.Green) -> "Green"
            isColorSimilar(color, Color.Yellow) -> "Yellow"
            isColorSimilar(color, Color.Magenta) -> "Pink"
            isColorSimilar(color, Color.Cyan) -> "Cyan"
            isColorSimilar(color, Color.Black) -> "Black"
            isColorSimilar(color, Color.White) -> "White"
            isColorSimilar(color, Color.Gray) -> "Gray"
            
            // Extended colors
            isColorSimilar(color, Color(0xFFFFA500)) -> "Orange" // Orange
            isColorSimilar(color, Color(0xFF800080)) -> "Purple" // Purple
            isColorSimilar(color, Color(0xFFA52A2A)) -> "Brown" // Brown
            isColorSimilar(color, Color(0xFFFFB6C1)) -> "Light Pink" // Light Pink
            isColorSimilar(color, Color(0xFF90EE90)) -> "Light Green" // Light Green
            isColorSimilar(color, Color(0xFFADD8E6)) -> "Light Blue" // Light Blue
            isColorSimilar(color, Color(0xFF000080)) -> "Navy" // Navy
            isColorSimilar(color, Color(0xFF800000)) -> "Maroon" // Maroon
            isColorSimilar(color, Color(0xFF008080)) -> "Teal" // Teal
            isColorSimilar(color, Color(0xFFFFFFE0)) -> "Cream" // Cream
            isColorSimilar(color, Color(0xFFF5F5DC)) -> "Beige" // Beige
            
            // Determine by HSV values
            else -> {
                val hsv = rgbToHsv(color.red, color.green, color.blue)
                categorizeByHSV(hsv[0], hsv[1], hsv[2])
            }
        }
    }
    
    private fun isColorSimilar(color1: Color, color2: Color, threshold: Float = 0.3f): Boolean {
        val distance = sqrt(
            (color1.red - color2.red) * (color1.red - color2.red) +
            (color1.green - color2.green) * (color1.green - color2.green) +
            (color1.blue - color2.blue) * (color1.blue - color2.blue)
        )
        return distance < threshold
    }
    
    private fun isSimilarColor(color1: String, color2: String): Boolean {
        return color1.equals(color2, ignoreCase = true)
    }
    
    private fun rgbToHsv(r: Float, g: Float, b: Float): FloatArray {
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        
        val hsv = FloatArray(3)
        
        // Hue
        hsv[0] = when {
            delta == 0f -> 0f
            max == r -> 60f * (((g - b) / delta) % 6f)
            max == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }
        if (hsv[0] < 0) hsv[0] += 360f
        
        // Saturation
        hsv[1] = if (max == 0f) 0f else delta / max
        
        // Value
        hsv[2] = max
        
        return hsv
    }
    
    private fun categorizeByHSV(hue: Float, saturation: Float, value: Float): String {
        return when {
            value < 0.2f -> "Black"
            value > 0.8f && saturation < 0.2f -> "White"
            saturation < 0.3f -> when {
                value < 0.4f -> "Dark Gray"
                value > 0.7f -> "Light Gray"
                else -> "Gray"
            }
            else -> when (hue.toInt()) {
                in 0..15, in 345..360 -> "Red"
                in 16..45 -> "Orange"
                in 46..75 -> "Yellow"
                in 76..165 -> "Green"
                in 166..255 -> "Blue"
                in 256..285 -> "Purple"
                in 286..344 -> "Pink"
                else -> "Unknown"
            }
        }
    }
}