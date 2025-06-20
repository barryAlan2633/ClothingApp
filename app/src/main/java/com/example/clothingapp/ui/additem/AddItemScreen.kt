package com.example.clothingapp.ui.additem

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.clothingapp.data.*
import com.example.clothingapp.utils.ColorExtractor
import com.example.clothingapp.ui.components.ColorPickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    navController: NavController,
    imageUri: String,
    viewModel: AddItemViewModel
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var name by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf(setOf<ClothingCategory>()) }
    var color by remember { mutableStateOf("") }
    var secondaryColor by remember { mutableStateOf("") }
    var isExtractingColors by remember { mutableStateOf(false) }
    var extractedColors by remember { mutableStateOf<com.example.clothingapp.utils.ExtractedColors?>(null) }
    var processedImageUri by remember { mutableStateOf(imageUri) }
    var pattern by remember { mutableStateOf("") }
    var fabricType by remember { mutableStateOf(FabricType.COTTON) }
    var size by remember { mutableStateOf("") }
    var style by remember { mutableStateOf(ClothingStyle.CASUAL) }
    var dressCodes by remember { mutableStateOf(setOf<DressCode>()) }
    var brand by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showFabricMenu by remember { mutableStateOf(false) }
    var showStyleMenu by remember { mutableStateOf(false) }
    var showDressCodeMenu by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var pickingForSecondary by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Clothing Item") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val item = ClothingItem(
                                    name = name,
                                    imageUri = imageUri,
                                    categories = categories.toList(),
                                    color = color,
                                    secondaryColor = secondaryColor.ifEmpty { null },
                                    pattern = pattern.ifEmpty { null },
                                    fabricType = fabricType,
                                    size = size,
                                    style = style,
                                    dressCodes = dressCodes.toList(),
                                    brand = brand.ifEmpty { null },
                                    notes = notes.ifEmpty { null }
                                )
                                viewModel.saveItem(item)
                                navController.popBackStack()
                                navController.popBackStack()
                            }
                        },
                        enabled = name.isNotBlank() && size.isNotBlank() && color.isNotBlank() && dressCodes.isNotEmpty() && categories.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        // Show color picker dialog
        if (showColorPicker) {
            ColorPickerDialog(
                imageUri = imageUri,
                onColorSelected = { colorName ->
                    if (pickingForSecondary) {
                        secondaryColor = colorName
                    } else {
                        color = colorName
                    }
                },
                onDismiss = { showColorPicker = false }
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse(imageUri)),
                    contentDescription = "Captured clothing",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            // Color Extraction Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Extract Colors from Image",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    isExtractingColors = true
                                    extractedColors = ColorExtractor.extractColorsFromImage(context, Uri.parse(imageUri))
                                    extractedColors?.let { colors ->
                                        color = colors.primary
                                        secondaryColor = colors.secondary ?: ""
                                    }
                                    isExtractingColors = false
                                }
                            },
                            enabled = !isExtractingColors
                        ) {
                            if (isExtractingColors) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Extract Colors")
                            }
                        }
                    }
                    
                    if (extractedColors != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Detected:")
                            ColorChip(
                                color = extractedColors!!.primary,
                                label = "Primary"
                            )
                            extractedColors!!.secondary?.let { secondaryColorName ->
                                ColorChip(
                                    color = secondaryColorName,
                                    label = "Secondary"
                                )
                            }
                        }
                    }
                }
            }
            
            // Auto-extract colors on first load
            LaunchedEffect(imageUri) {
                if (color.isEmpty()) {
                    isExtractingColors = true
                    extractedColors = ColorExtractor.extractColorsFromImage(context, Uri.parse(imageUri))
                    extractedColors?.let { colors ->
                        color = colors.primary
                        secondaryColor = colors.secondary ?: ""
                    }
                    isExtractingColors = false
                }
            }
            
            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name *") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Category Selection
            Column {
                Text(
                    text = "Categories *",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(ClothingCategory.values().toList()) { category ->
                        FilterChip(
                            selected = category in categories,
                            onClick = {
                                categories = if (category in categories) {
                                    categories - category
                                } else {
                                    categories + category
                                }
                            },
                            label = { Text(category.displayName) }
                        )
                    }
                }
            }
            
            // Color Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Primary Color *") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                pickingForSecondary = false
                                showColorPicker = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Pick color")
                        }
                    }
                )
                OutlinedTextField(
                    value = secondaryColor,
                    onValueChange = { secondaryColor = it },
                    label = { Text("Secondary Color") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                pickingForSecondary = true
                                showColorPicker = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Pick color")
                        }
                    }
                )
            }
            
            // Pattern Input
            OutlinedTextField(
                value = pattern,
                onValueChange = { pattern = it },
                label = { Text("Pattern (e.g., striped, floral)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Fabric Type Dropdown
            ExposedDropdownMenuBox(
                expanded = showFabricMenu,
                onExpandedChange = { showFabricMenu = !showFabricMenu }
            ) {
                OutlinedTextField(
                    value = fabricType.name,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Fabric Type *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFabricMenu) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showFabricMenu,
                    onDismissRequest = { showFabricMenu = false }
                ) {
                    FabricType.values().forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name) },
                            onClick = {
                                fabricType = item
                                showFabricMenu = false
                            }
                        )
                    }
                }
            }
            
            // Size Input
            OutlinedTextField(
                value = size,
                onValueChange = { size = it },
                label = { Text("Size *") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Style Dropdown
            ExposedDropdownMenuBox(
                expanded = showStyleMenu,
                onExpandedChange = { showStyleMenu = !showStyleMenu }
            ) {
                OutlinedTextField(
                    value = style.name,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Style *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStyleMenu) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showStyleMenu,
                    onDismissRequest = { showStyleMenu = false }
                ) {
                    ClothingStyle.values().forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name) },
                            onClick = {
                                style = item
                                showStyleMenu = false
                            }
                        )
                    }
                }
            }
            
            // Dress Codes Selection
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Dress Codes *",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(DressCode.values().toList()) { dressCode ->
                        FilterChip(
                            selected = dressCode in dressCodes,
                            onClick = {
                                dressCodes = if (dressCode in dressCodes) {
                                    dressCodes - dressCode
                                } else {
                                    dressCodes + dressCode
                                }
                            },
                            label = { Text(dressCode.name.replace("_", " ")) }
                        )
                    }
                }
            }
            
            // Brand Input
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Notes Input
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}

@Composable
fun ColorChip(
    color: String,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(getColorForName(color))
        )
        Text(
            text = color,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun getColorForName(colorName: String): Color {
    return when (colorName.lowercase()) {
        "red" -> Color.Red
        "blue" -> Color.Blue
        "green" -> Color.Green
        "yellow" -> Color.Yellow
        "orange" -> Color(0xFFFFA500)
        "purple" -> Color(0xFF800080)
        "pink" -> Color(0xFFFFB6C1)
        "brown" -> Color(0xFFA52A2A)
        "black" -> Color.Black
        "white" -> Color.White
        "gray", "grey" -> Color.Gray
        "navy" -> Color(0xFF000080)
        "teal" -> Color(0xFF008080)
        "maroon" -> Color(0xFF800000)
        "cyan" -> Color.Cyan
        "magenta" -> Color.Magenta
        "light blue" -> Color(0xFFADD8E6)
        "light green" -> Color(0xFF90EE90)
        "light pink" -> Color(0xFFFFB6C1)
        "dark gray" -> Color(0xFF404040)
        "light gray" -> Color(0xFFD3D3D3)
        "cream" -> Color(0xFFFFFFE0)
        "beige" -> Color(0xFFF5F5DC)
        else -> Color.Gray
    }
}