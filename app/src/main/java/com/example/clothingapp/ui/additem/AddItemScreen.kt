package com.example.clothingapp.ui.additem

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.clothingapp.data.*
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
    
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ClothingCategory.TOP) }
    var color by remember { mutableStateOf("") }
    var secondaryColor by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf("") }
    var fabricType by remember { mutableStateOf(FabricType.COTTON) }
    var size by remember { mutableStateOf("") }
    var style by remember { mutableStateOf(ClothingStyle.CASUAL) }
    var dressCode by remember { mutableStateOf(DressCode.CASUAL) }
    var brand by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showFabricMenu by remember { mutableStateOf(false) }
    var showStyleMenu by remember { mutableStateOf(false) }
    var showDressCodeMenu by remember { mutableStateOf(false) }
    
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
                                    category = category,
                                    color = color,
                                    secondaryColor = secondaryColor.ifEmpty { null },
                                    pattern = pattern.ifEmpty { null },
                                    fabricType = fabricType,
                                    size = size,
                                    style = style,
                                    dressCode = dressCode,
                                    brand = brand.ifEmpty { null },
                                    notes = notes.ifEmpty { null }
                                )
                                viewModel.saveItem(item)
                                navController.popBackStack()
                                navController.popBackStack()
                            }
                        },
                        enabled = name.isNotBlank() && size.isNotBlank() && color.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
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
                    contentScale = ContentScale.Crop
                )
            }
            
            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name *") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = !showCategoryMenu }
            ) {
                OutlinedTextField(
                    value = category.name,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Category *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    ClothingCategory.values().forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name) },
                            onClick = {
                                category = item
                                showCategoryMenu = false
                            }
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
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = secondaryColor,
                    onValueChange = { secondaryColor = it },
                    label = { Text("Secondary Color") },
                    modifier = Modifier.weight(1f)
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
            
            // Dress Code Dropdown
            ExposedDropdownMenuBox(
                expanded = showDressCodeMenu,
                onExpandedChange = { showDressCodeMenu = !showDressCodeMenu }
            ) {
                OutlinedTextField(
                    value = dressCode.name,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Dress Code *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDressCodeMenu) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showDressCodeMenu,
                    onDismissRequest = { showDressCodeMenu = false }
                ) {
                    DressCode.values().forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name) },
                            onClick = {
                                dressCode = item
                                showDressCodeMenu = false
                            }
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