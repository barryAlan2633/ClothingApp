package com.example.clothingapp.ui.crop

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import android.widget.Toast
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.clothingapp.utils.CropRect
import com.example.clothingapp.utils.ImageCropper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    navController: NavController,
    imageUri: String,
    onImageCropped: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var isCropping by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    
    // Initialize crop points to form a rectangle in the center
    var topLeft by remember { mutableStateOf(Offset.Zero) }
    var topRight by remember { mutableStateOf(Offset.Zero) }
    var bottomLeft by remember { mutableStateOf(Offset.Zero) }
    var bottomRight by remember { mutableStateOf(Offset.Zero) }
    
    // Initialize crop rectangle when image size is known (only once)
    LaunchedEffect(imageSize) {
        if (imageSize.width > 0 && imageSize.height > 0 && !isInitialized) {
            val padding = with(density) { 50.dp.toPx() }
            topLeft = Offset(padding, padding)
            topRight = Offset(imageSize.width - padding, padding)
            bottomLeft = Offset(padding, imageSize.height - padding)
            bottomRight = Offset(imageSize.width - padding, imageSize.height - padding)
            isInitialized = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop Image") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                // Validate crop rectangle
                                val rect = CropRect(topLeft, topRight, bottomLeft, bottomRight).toRectangle()
                                if (rect.width() <= 0 || rect.height() <= 0) {
                                    Toast.makeText(context, "Invalid crop area. Please adjust the corners.", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                
                                isCropping = true
                                val cropRect = CropRect(topLeft, topRight, bottomLeft, bottomRight)
                                try {
                                    val croppedUri = ImageCropper.cropImage(
                                        context = context,
                                        imageUri = Uri.parse(imageUri),
                                        cropRect = cropRect,
                                        imageWidth = imageSize.width,
                                        imageHeight = imageSize.height
                                    )
                                    isCropping = false
                                    
                                    if (croppedUri != null) {
                                        Toast.makeText(context, "Image cropped successfully", Toast.LENGTH_SHORT).show()
                                        onImageCropped(croppedUri.toString())
                                    } else {
                                        // Cropping failed, show error or fallback
                                        Toast.makeText(context, "Cropping failed", Toast.LENGTH_SHORT).show()
                                        println("Cropping failed: returned null")
                                    }
                                } catch (e: Exception) {
                                    isCropping = false
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    println("Cropping error: ${e.message}")
                                    e.printStackTrace()
                                }
                            }
                        },
                        enabled = !isCropping
                    ) {
                        if (isCropping) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Crop")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Box {
                // Background image
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(Uri.parse(imageUri))
                            .size(coil.size.Size.ORIGINAL)
                            .build()
                    ),
                    contentDescription = "Image to crop",
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { layoutCoordinates ->
                            imageSize = layoutCoordinates.size
                        },
                    contentScale = ContentScale.Inside
                )
                
                // Crop overlay
                if (imageSize.width > 0 && imageSize.height > 0) {
                    CropOverlay(
                        imageSize = imageSize,
                        topLeft = topLeft,
                        topRight = topRight,
                        bottomLeft = bottomLeft,
                        bottomRight = bottomRight,
                        onTopLeftChange = { topLeft = it },
                        onTopRightChange = { topRight = it },
                        onBottomLeftChange = { bottomLeft = it },
                        onBottomRightChange = { bottomRight = it }
                    )
                }
            }
            
            // Instructions
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "Drag the corner points to adjust the crop area",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun CropOverlay(
    imageSize: IntSize,
    topLeft: Offset,
    topRight: Offset,
    bottomLeft: Offset,
    bottomRight: Offset,
    onTopLeftChange: (Offset) -> Unit,
    onTopRightChange: (Offset) -> Unit,
    onBottomLeftChange: (Offset) -> Unit,
    onBottomRightChange: (Offset) -> Unit
) {
    val density = LocalDensity.current
    val handleRadius = 12.dp
    val handleRadiusPx = with(density) { handleRadius.toPx() }
    
    // Track phantom positions
    var phantomTopLeft by remember { mutableStateOf<Offset?>(null) }
    var phantomTopRight by remember { mutableStateOf<Offset?>(null) }
    var phantomBottomLeft by remember { mutableStateOf<Offset?>(null) }
    var phantomBottomRight by remember { mutableStateOf<Offset?>(null) }
    
    Box(
        modifier = Modifier
            .width(with(density) { imageSize.width.toDp() })
            .height(with(density) { imageSize.height.toDp() })
    ) {
        // Draw crop rectangle and overlay
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Draw semi-transparent overlay outside crop area
            val cropPath = Path().apply {
                moveTo(topLeft.x, topLeft.y)
                lineTo(topRight.x, topRight.y)
                lineTo(bottomRight.x, bottomRight.y)
                lineTo(bottomLeft.x, bottomLeft.y)
                close()
            }
            
            // Draw darkened overlay
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = size
            )
            
            // Draw crop rectangle outline
            drawPath(
                path = cropPath,
                color = Color.White,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        // Phantom handles
        phantomTopLeft?.let { pos ->
            PhantomHandle(
                modifier = Modifier.offset(
                    x = with(density) { (pos.x - handleRadiusPx).toDp() },
                    y = with(density) { (pos.y - handleRadiusPx).toDp() }
                )
            )
        }
        phantomTopRight?.let { pos ->
            PhantomHandle(
                modifier = Modifier.offset(
                    x = with(density) { (pos.x - handleRadiusPx).toDp() },
                    y = with(density) { (pos.y - handleRadiusPx).toDp() }
                )
            )
        }
        phantomBottomLeft?.let { pos ->
            PhantomHandle(
                modifier = Modifier.offset(
                    x = with(density) { (pos.x - handleRadiusPx).toDp() },
                    y = with(density) { (pos.y - handleRadiusPx).toDp() }
                )
            )
        }
        phantomBottomRight?.let { pos ->
            PhantomHandle(
                modifier = Modifier.offset(
                    x = with(density) { (pos.x - handleRadiusPx).toDp() },
                    y = with(density) { (pos.y - handleRadiusPx).toDp() }
                )
            )
        }
        
        // Draggable corner handles
        DraggableHandle(
            position = topLeft,
            imageSize = imageSize,
            onPositionChange = onTopLeftChange,
            onPhantomPositionChange = { phantomTopLeft = it },
            modifier = Modifier.offset(
                x = with(density) { (topLeft.x - handleRadiusPx).toDp() },
                y = with(density) { (topLeft.y - handleRadiusPx).toDp() }
            )
        )
        
        DraggableHandle(
            position = topRight,
            imageSize = imageSize,
            onPositionChange = onTopRightChange,
            onPhantomPositionChange = { phantomTopRight = it },
            modifier = Modifier.offset(
                x = with(density) { (topRight.x - handleRadiusPx).toDp() },
                y = with(density) { (topRight.y - handleRadiusPx).toDp() }
            )
        )
        
        DraggableHandle(
            position = bottomLeft,
            imageSize = imageSize,
            onPositionChange = onBottomLeftChange,
            onPhantomPositionChange = { phantomBottomLeft = it },
            modifier = Modifier.offset(
                x = with(density) { (bottomLeft.x - handleRadiusPx).toDp() },
                y = with(density) { (bottomLeft.y - handleRadiusPx).toDp() }
            )
        )
        
        DraggableHandle(
            position = bottomRight,
            imageSize = imageSize,
            onPositionChange = onBottomRightChange,
            onPhantomPositionChange = { phantomBottomRight = it },
            modifier = Modifier.offset(
                x = with(density) { (bottomRight.x - handleRadiusPx).toDp() },
                y = with(density) { (bottomRight.y - handleRadiusPx).toDp() }
            )
        )
    }
}

@Composable
fun PhantomHandle(
    modifier: Modifier = Modifier
) {
    val handleSize = 24.dp
    
    Box(
        modifier = modifier
            .size(handleSize)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                .align(Alignment.Center)
        )
    }
}

@Composable
fun DraggableHandle(
    position: Offset,
    imageSize: IntSize,
    onPositionChange: (Offset) -> Unit,
    onPhantomPositionChange: ((Offset?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val handleSize = 24.dp
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    
    Box(
        modifier = modifier
            .size(handleSize)
            .clip(CircleShape)
            .background(if (isDragging) Color.White.copy(alpha = 0.6f) else Color.White)
            .pointerInput(position) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragOffset = Offset.Zero
                        onPhantomPositionChange?.invoke(position)
                    },
                    onDragEnd = {
                        isDragging = false
                        val finalPosition = Offset(
                            x = (position.x + dragOffset.x).coerceIn(0f, imageSize.width.toFloat()),
                            y = (position.y + dragOffset.y).coerceIn(0f, imageSize.height.toFloat())
                        )
                        onPositionChange(finalPosition)
                        onPhantomPositionChange?.invoke(null)
                        dragOffset = Offset.Zero
                    },
                    onDrag = { change, _ ->
                        dragOffset += Offset(change.position.x - change.previousPosition.x, change.position.y - change.previousPosition.y)
                        val phantomPosition = Offset(
                            x = (position.x + dragOffset.x).coerceIn(0f, imageSize.width.toFloat()),
                            y = (position.y + dragOffset.y).coerceIn(0f, imageSize.height.toFloat())
                        )
                        onPhantomPositionChange?.invoke(phantomPosition)
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .align(Alignment.Center)
        )
    }
}