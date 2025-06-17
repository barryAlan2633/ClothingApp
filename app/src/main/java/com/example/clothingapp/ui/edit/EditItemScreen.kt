package com.example.clothingapp.ui.edit

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.clothingapp.data.*
import com.example.clothingapp.ui.additem.ColorChip
import com.example.clothingapp.utils.ColorExtractor
import com.example.clothingapp.ui.components.FullScreenImageViewer
import com.example.clothingapp.ui.components.ColorPickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    navController: NavController,
    viewModel: EditItemViewModel,
    itemId: Int
) {
    val item by viewModel.item.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }
    
    if (item == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    val currentItem = item!!
    
    var name by remember { mutableStateOf(currentItem.name) }
    var categories by remember { mutableStateOf(currentItem.categories.toSet()) }
    var color by remember { mutableStateOf(currentItem.color) }
    var secondaryColor by remember { mutableStateOf(currentItem.secondaryColor ?: "") }
    var pattern by remember { mutableStateOf(currentItem.pattern ?: "") }
    var fabricType by remember { mutableStateOf(currentItem.fabricType) }
    var size by remember { mutableStateOf(currentItem.size) }
    var style by remember { mutableStateOf(currentItem.style) }
    var dressCodes by remember { mutableStateOf(currentItem.dressCodes.toSet()) }
    var brand by remember { mutableStateOf(currentItem.brand ?: "") }
    var notes by remember { mutableStateOf(currentItem.notes ?: "") }
    var purchasePrice by remember { mutableStateOf(currentItem.purchasePrice?.toString() ?: "") }
    var imageUri by remember { mutableStateOf(currentItem.imageUri) }
    
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showFabricMenu by remember { mutableStateOf(false) }
    var showStyleMenu by remember { mutableStateOf(false) }
    var showDressCodeMenu by remember { mutableStateOf(false) }
    
    var isExtractingColors by remember { mutableStateOf(false) }
    var extractedColors by remember { mutableStateOf<com.example.clothingapp.utils.ExtractedColors?>(null) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var pickingForSecondary by remember { mutableStateOf(false) }
    
    // Update state when item changes
    LaunchedEffect(currentItem) {
        name = currentItem.name
        categories = currentItem.categories.toSet()
        color = currentItem.color
        secondaryColor = currentItem.secondaryColor ?: ""
        pattern = currentItem.pattern ?: ""
        fabricType = currentItem.fabricType
        size = currentItem.size
        style = currentItem.style
        dressCodes = currentItem.dressCodes.toSet()
        brand = currentItem.brand ?: ""
        notes = currentItem.notes ?: ""
        purchasePrice = currentItem.purchasePrice?.toString() ?: ""
        imageUri = currentItem.imageUri
    }
    
    
    // Show full screen image if requested
    if (showFullScreenImage) {
        FullScreenImageViewer(
            imageUri = currentItem.imageUri,
            onClose = { showFullScreenImage = false }
        )
        return
    }
    
    // Show color picker dialog
    if (showColorPicker) {
        ColorPickerDialog(
            imageUri = currentItem.imageUri,
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Item") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val updatedItem = currentItem.copy(
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
                                    notes = notes.ifEmpty { null },
                                    purchasePrice = purchasePrice.toDoubleOrNull()
                                )
                                viewModel.updateItem(updatedItem)
                                navController.popBackStack()
                            }
                        },
                        enabled = name.isNotBlank() && size.isNotBlank() && color.isNotBlank() && dressCodes.isNotEmpty() && categories.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Preview (read-only)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clickable { showFullScreenImage = true }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse(imageUri)),
                    contentDescription = currentItem.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
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
                            text = "Re-extract Colors from Image",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    isExtractingColors = true
                                    extractedColors = ColorExtractor.extractColorsFromImage(context, Uri.parse(currentItem.imageUri))
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
                                Icon(Icons.Default.Refresh, contentDescription = "Re-extract Colors")
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
                            Icon(Icons.Default.Add, contentDescription = "Pick color")
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
                            Icon(Icons.Default.Add, contentDescription = "Pick color")
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
                    value = fabricType.name.replace("_", " "),
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
                            text = { Text(item.name.replace("_", " ")) },
                            onClick = {
                                fabricType = item
                                showFabricMenu = false
                            }
                        )
                    }
                }
            }
            
            // Size and Brand Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Size *") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Style Dropdown
            ExposedDropdownMenuBox(
                expanded = showStyleMenu,
                onExpandedChange = { showStyleMenu = !showStyleMenu }
            ) {
                OutlinedTextField(
                    value = style.name.replace("_", " "),
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
                            text = { Text(item.name.replace("_", " ")) },
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
            
            // Purchase Price Input
            OutlinedTextField(
                value = purchasePrice,
                onValueChange = { purchasePrice = it },
                label = { Text("Purchase Price") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("$") }
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