package com.example.clothingapp.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun ColorPickerDialog(
    imageUri: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var selectedPosition by remember { mutableStateOf<Offset?>(null) }
    var selectedColor by remember { mutableStateOf<Color?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Load bitmap
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(imageUri))
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap image to pick color",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Divider()
                
                // Image with tap detection
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(imageUri)),
                        contentDescription = "Pick color from image",
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { layoutCoordinates ->
                                imageSize = layoutCoordinates.size
                            }
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    selectedPosition = offset
                                    
                                    // Extract color at tap position
                                    bitmap?.let { bmp ->
                                        val scaleX = bmp.width.toFloat() / imageSize.width.toFloat()
                                        val scaleY = bmp.height.toFloat() / imageSize.height.toFloat()
                                        
                                        val x = (offset.x * scaleX).roundToInt().coerceIn(0, bmp.width - 1)
                                        val y = (offset.y * scaleY).roundToInt().coerceIn(0, bmp.height - 1)
                                        
                                        val pixel = bmp.getPixel(x, y)
                                        selectedColor = Color(pixel)
                                    }
                                }
                            }
                            .drawBehind {
                                // Draw crosshair at selected position
                                selectedPosition?.let { pos ->
                                    drawCrosshair(pos)
                                }
                            },
                        contentScale = ContentScale.Fit
                    )
                }
                
                // Selected color preview and actions
                selectedColor?.let { color ->
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color preview
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            
                            // Color info
                            Column {
                                val colorName = getColorName(color)
                                Text(
                                    text = colorName,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = String.format(
                                        "#%06X",
                                        color.toArgb() and 0xFFFFFF
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    onColorSelected(getColorName(color))
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Select Color")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCrosshair(position: Offset) {
    val crosshairSize = 20.dp.toPx()
    val strokeWidth = 2.dp.toPx()
    
    // White outline
    drawLine(
        color = Color.White,
        start = Offset(position.x - crosshairSize, position.y),
        end = Offset(position.x + crosshairSize, position.y),
        strokeWidth = strokeWidth + 2
    )
    drawLine(
        color = Color.White,
        start = Offset(position.x, position.y - crosshairSize),
        end = Offset(position.x, position.y + crosshairSize),
        strokeWidth = strokeWidth + 2
    )
    
    // Black crosshair
    drawLine(
        color = Color.Black,
        start = Offset(position.x - crosshairSize, position.y),
        end = Offset(position.x + crosshairSize, position.y),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = Color.Black,
        start = Offset(position.x, position.y - crosshairSize),
        end = Offset(position.x, position.y + crosshairSize),
        strokeWidth = strokeWidth
    )
    
    // Circle at center
    drawCircle(
        color = Color.White,
        radius = 8.dp.toPx(),
        center = position,
        style = Stroke(width = strokeWidth + 2)
    )
    drawCircle(
        color = Color.Black,
        radius = 8.dp.toPx(),
        center = position,
        style = Stroke(width = strokeWidth)
    )
}

private fun getColorName(color: Color): String {
    val rgb = color.toArgb()
    val r = (rgb shr 16) and 0xFF
    val g = (rgb shr 8) and 0xFF
    val b = rgb and 0xFF
    
    // Convert to HSL for better color matching
    val max = maxOf(r, g, b).toFloat() / 255f
    val min = minOf(r, g, b).toFloat() / 255f
    val l = (max + min) / 2f
    
    val d = max - min
    val s = if (d == 0f) 0f else d / (1f - kotlin.math.abs(2f * l - 1f))
    
    val h = when {
        d == 0f -> 0f
        max == r.toFloat() / 255f -> ((g - b).toFloat() / 255f / d + if (g < b) 6f else 0f) * 60f
        max == g.toFloat() / 255f -> ((b - r).toFloat() / 255f / d + 2f) * 60f
        else -> ((r - g).toFloat() / 255f / d + 4f) * 60f
    }
    
    // Determine color name based on HSL values
    return when {
        l < 0.1f -> "Black"
        l > 0.9f -> "White"
        s < 0.1f && l < 0.3f -> "Dark Gray"
        s < 0.1f && l < 0.7f -> "Gray"
        s < 0.1f -> "Light Gray"
        h < 15f || h >= 345f -> if (l < 0.5f) "Dark Red" else "Red"
        h < 35f -> if (s > 0.7f) "Orange" else "Brown"
        h < 65f -> if (l < 0.5f) "Dark Yellow" else "Yellow"
        h < 150f -> if (l < 0.5f) "Dark Green" else "Green"
        h < 185f -> "Cyan"
        h < 255f -> if (l < 0.5f) "Dark Blue" else "Blue"
        h < 290f -> "Purple"
        h < 335f -> if (l < 0.5f) "Dark Pink" else "Pink"
        else -> "Red"
    }
}