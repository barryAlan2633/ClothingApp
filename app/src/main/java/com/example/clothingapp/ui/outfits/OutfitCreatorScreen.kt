package com.example.clothingapp.ui.outfits

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.clothingapp.data.ClothingCategory
import com.example.clothingapp.data.ClothingItem
import com.example.clothingapp.data.MainCategory
import com.example.clothingapp.ui.components.CharacterOutfitLayout
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitCreatorScreen(
    navController: NavController,
    viewModel: OutfitCreatorViewModel
) {
    val scope = rememberCoroutineScope()
    
    val allItems by viewModel.allItems.collectAsState()
    
    var selectedHat by remember { mutableStateOf<ClothingItem?>(null) }
    var selectedTop by remember { mutableStateOf<ClothingItem?>(null) }
    var selectedBottom by remember { mutableStateOf<ClothingItem?>(null) }
    var selectedFootwear by remember { mutableStateOf<ClothingItem?>(null) }
    var selectedJewelry by remember { mutableStateOf<List<ClothingItem>>(emptyList()) }
    var selectedAccessories by remember { mutableStateOf<List<ClothingItem>>(emptyList()) }
    
    var showItemPicker by remember { mutableStateOf(false) }
    var currentPickerCategory by remember { mutableStateOf<MainCategory?>(null) }
    
    var outfitName by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadClothingItems()
    }
    
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            kotlinx.coroutines.delay(2000)
            showSuccessMessage = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Outfit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("outfits") }) {
                        Icon(Icons.Default.List, contentDescription = "View Outfits")
                    }
                    IconButton(
                        onClick = { showSaveDialog = true },
                        enabled = selectedTop != null || selectedBottom != null || selectedFootwear != null || selectedHat != null || selectedJewelry.isNotEmpty() || selectedAccessories.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save Outfit")
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
            modifier = Modifier.fillMaxSize()
        ) {
            if (showSuccessMessage) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Outfit saved successfully!",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Character outfit layout for selection
            CharacterOutfitLayout(
                hat = selectedHat,
                top = selectedTop,
                bottom = selectedBottom,
                footwear = selectedFootwear,
                jewelry = selectedJewelry,
                accessories = selectedAccessories,
                onSlotClick = { category ->
                    currentPickerCategory = category
                    showItemPicker = true
                },
                useAspectRatio = false,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding(), start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        }
    }
    
    // Save Outfit Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Outfit") },
            text = {
                Column {
                    Text("Enter a name for this outfit:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = outfitName,
                        onValueChange = { outfitName = it },
                        label = { Text("Outfit Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val success = viewModel.saveOutfit(
                                name = outfitName.ifEmpty { "Outfit ${System.currentTimeMillis()}" },
                                hatId = selectedHat?.id,
                                topId = selectedTop?.id,
                                bottomId = selectedBottom?.id,
                                footwearId = selectedFootwear?.id,
                                jewelryIds = selectedJewelry.map { it.id },
                                accessoryIds = selectedAccessories.map { it.id }
                            )
                            showSaveDialog = false
                            if (success) {
                                outfitName = ""
                                showSuccessMessage = true
                            }
                        }
                    },
                    enabled = outfitName.isNotBlank() || selectedTop != null || selectedBottom != null || selectedFootwear != null || selectedHat != null || selectedJewelry.isNotEmpty() || selectedAccessories.isNotEmpty()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Item picker dialog
    if (showItemPicker && currentPickerCategory != null) {
        ItemPickerDialog(
            category = currentPickerCategory!!,
            allItems = allItems,
            selectedItems = when (currentPickerCategory) {
                MainCategory.HAT -> listOfNotNull(selectedHat)
                MainCategory.TOP -> listOfNotNull(selectedTop)
                MainCategory.BOTTOM -> listOfNotNull(selectedBottom)
                MainCategory.FOOTWEAR -> listOfNotNull(selectedFootwear)
                MainCategory.JEWELRY -> selectedJewelry
                MainCategory.ACCESSORIES -> selectedAccessories
                null -> emptyList()
            },
            onItemSelected = { item ->
                when (currentPickerCategory) {
                    MainCategory.HAT -> selectedHat = item
                    MainCategory.TOP -> selectedTop = item
                    MainCategory.BOTTOM -> selectedBottom = item
                    MainCategory.FOOTWEAR -> selectedFootwear = item
                    MainCategory.JEWELRY -> selectedJewelry = selectedJewelry + item
                    MainCategory.ACCESSORIES -> selectedAccessories = selectedAccessories + item
                    null -> {}
                }
            },
            onItemRemoved = { item ->
                when (currentPickerCategory) {
                    MainCategory.HAT -> selectedHat = null
                    MainCategory.TOP -> selectedTop = null
                    MainCategory.BOTTOM -> selectedBottom = null
                    MainCategory.FOOTWEAR -> selectedFootwear = null
                    MainCategory.JEWELRY -> selectedJewelry = selectedJewelry - item
                    MainCategory.ACCESSORIES -> selectedAccessories = selectedAccessories - item
                    null -> {}
                }
            },
            onDismiss = { showItemPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemPickerDialog(
    category: MainCategory,
    allItems: List<ClothingItem>,
    selectedItems: List<ClothingItem>,
    onItemSelected: (ClothingItem) -> Unit,
    onItemRemoved: (ClothingItem) -> Unit,
    onDismiss: () -> Unit
) {
    val filteredItems = allItems.filter { item ->
        item.categories.any { it.mainCategory == category }
    }
    
    val allowMultiple = category == MainCategory.JEWELRY || category == MainCategory.ACCESSORIES
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top app bar
                TopAppBar(
                    title = { Text("Select ${category.displayName}") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "No ${category.displayName.lowercase()} available",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Add some items to your wardrobe first",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Add "None" option for single-select categories
                        if (!allowMultiple) {
                            item {
                                Card(
                                    onClick = {
                                        selectedItems.firstOrNull()?.let { onItemRemoved(it) }
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .border(
                                            BorderStroke(
                                                2.dp,
                                                if (selectedItems.isEmpty()) MaterialTheme.colorScheme.primary else Color.Transparent
                                            ),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedItems.isEmpty()) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "None",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (selectedItems.isEmpty()) 
                                                MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        
                        items(filteredItems) { item ->
                            val isSelected = selectedItems.contains(item)
                            Card(
                                onClick = {
                                    if (isSelected) {
                                        onItemRemoved(item)
                                    } else {
                                        if (!allowMultiple) {
                                            selectedItems.firstOrNull()?.let { onItemRemoved(it) }
                                        }
                                        onItemSelected(item)
                                    }
                                    if (!allowMultiple) {
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .border(
                                        BorderStroke(
                                            2.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                        ),
                                        RoundedCornerShape(12.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column {
                                    Image(
                                        painter = rememberAsyncImagePainter(item.imageUri),
                                        contentDescription = item.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    Text(
                                        text = item.name,
                                        modifier = Modifier.padding(8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        color = if (isSelected) 
                                            MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}