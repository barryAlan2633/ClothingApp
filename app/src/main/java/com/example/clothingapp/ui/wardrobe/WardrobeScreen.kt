package com.example.clothingapp.ui.wardrobe

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.clothingapp.data.ClothingItem
import com.example.clothingapp.data.ClothingCategory
import com.example.clothingapp.data.MainCategory
import com.example.clothingapp.ui.components.ClothingImageCard
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(
    navController: NavController,
    viewModel: WardrobeViewModel
) {
    val clothingItems by viewModel.clothingItems.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var selectedMainCategory by remember { mutableStateOf<MainCategory?>(null) }
    var selectedSubCategory by remember { mutableStateOf<ClothingCategory?>(null) }
    var showDirtyOnly by remember { mutableStateOf(false) }
    var showCleanOnly by remember { mutableStateOf(false) }
    var showNeedsRepairOnly by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            navController.navigate("crop/${Uri.encode(it.toString())}")
        }
    }
    
    // Filter the items based on search and filters
    val filteredItems = remember(clothingItems, searchQuery, selectedMainCategory, selectedSubCategory, showDirtyOnly, showCleanOnly, showNeedsRepairOnly) {
        clothingItems.filter { item ->
            val matchesSearch = searchQuery.isEmpty() || 
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.color.contains(searchQuery, ignoreCase = true) ||
                item.brand?.contains(searchQuery, ignoreCase = true) == true
            
            val matchesCategory = when {
                selectedSubCategory != null -> item.categories.contains(selectedSubCategory)
                selectedMainCategory != null -> item.categories.any { it.mainCategory == selectedMainCategory }
                else -> true
            }
            
            val matchesStatus = when {
                showDirtyOnly -> item.isDirty
                showCleanOnly -> !item.isDirty && !item.needsRepair
                showNeedsRepairOnly -> item.needsRepair
                else -> true
            }
            
            matchesSearch && matchesCategory && matchesStatus
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search clothes...") },
                            trailingIcon = {
                                IconButton(onClick = { 
                                    searchQuery = ""
                                    showSearch = false 
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("My Wardrobe")
                    }
                },
                actions = {
                    if (!showSearch) {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Filter",
                                tint = if (selectedMainCategory != null || selectedSubCategory != null || showDirtyOnly || showCleanOnly || showNeedsRepairOnly) 
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { navController.navigate("favorites") }) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "View Favorites",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showImageSourceDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Full-screen filter dialog
            if (showFilterDialog) {
                FilterDialog(
                    selectedMainCategory = selectedMainCategory,
                    selectedSubCategory = selectedSubCategory,
                    showDirtyOnly = showDirtyOnly,
                    showCleanOnly = showCleanOnly,
                    showNeedsRepairOnly = showNeedsRepairOnly,
                    onMainCategorySelected = { 
                        selectedMainCategory = it
                        selectedSubCategory = null
                    },
                    onSubCategorySelected = { selectedSubCategory = it },
                    onDirtyOnlyChanged = { 
                        showDirtyOnly = it
                        if (it) {
                            showCleanOnly = false
                            showNeedsRepairOnly = false
                        }
                    },
                    onCleanOnlyChanged = { 
                        showCleanOnly = it
                        if (it) {
                            showDirtyOnly = false
                            showNeedsRepairOnly = false
                        }
                    },
                    onNeedsRepairOnlyChanged = { 
                        showNeedsRepairOnly = it
                        if (it) {
                            showDirtyOnly = false
                            showCleanOnly = false
                        }
                    },
                    onClearFilters = {
                        selectedMainCategory = null
                        selectedSubCategory = null
                        showDirtyOnly = false
                        showCleanOnly = false
                        showNeedsRepairOnly = false
                    },
                    onDismiss = { showFilterDialog = false }
                )
            }
            
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            if (clothingItems.isEmpty()) "Your wardrobe is empty" else "No items match your filters",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (clothingItems.isEmpty()) {
                            Button(onClick = { navController.navigate("camera") }) {
                                Text("Add Your First Item")
                            }
                        } else {
                            Button(onClick = { 
                                selectedMainCategory = null
                                selectedSubCategory = null
                                showDirtyOnly = false
                                showCleanOnly = false
                                showNeedsRepairOnly = false
                                searchQuery = ""
                            }) {
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            } else {
                // Group items by main category, with items appearing in multiple categories if needed
                val groupedItems = remember(filteredItems) {
                    val groups = mutableMapOf<MainCategory, MutableList<ClothingItem>>()
                    filteredItems.forEach { item ->
                        val mainCategories = item.categories.map { it.mainCategory }.distinct()
                        mainCategories.forEach { mainCategory ->
                            groups.getOrPut(mainCategory) { mutableListOf() }.add(item)
                        }
                    }
                    groups.toMap()
                }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    groupedItems.forEach { (mainCategory, items) ->
                        item(key = mainCategory) {
                            CategorySection(
                                categoryName = mainCategory.displayName,
                                items = items,
                                onItemClick = { item -> navController.navigate("item_detail/${item.id}") },
                                onFavoriteClick = { item -> viewModel.toggleFavorite(item) },
                                onDirtyStatusChange = { item, isDirty -> viewModel.updateDirtyStatus(item, isDirty) },
                                onRepairStatusChange = { item, needsRepair -> viewModel.updateRepairStatus(item, needsRepair) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Image source selection dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Add New Item") },
            text = { Text("Choose how you'd like to add an image for your new clothing item") },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            navController.navigate("camera")
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera")
                    }
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        }
                    ) {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CategorySection(
    categoryName: String,
    items: List<ClothingItem>,
    onItemClick: (ClothingItem) -> Unit,
    onFavoriteClick: (ClothingItem) -> Unit,
    onDirtyStatusChange: (ClothingItem, Boolean) -> Unit,
    onRepairStatusChange: (ClothingItem, Boolean) -> Unit
) {
    Column {
        Text(
            text = categoryName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height((items.size / 2 + items.size % 2) * 220.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(items) { item ->
                ClothingGridItem(
                    item = item,
                    onItemClick = { onItemClick(item) },
                    onFavoriteClick = { onFavoriteClick(item) },
                    onDirtyStatusChange = { isDirty -> onDirtyStatusChange(item, isDirty) },
                    onRepairStatusChange = { needsRepair -> onRepairStatusChange(item, needsRepair) }
                )
            }
        }
    }
}

@Composable
fun ClothingGridItem(
    item: ClothingItem,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDirtyStatusChange: (Boolean) -> Unit,
    onRepairStatusChange: (Boolean) -> Unit
) {
    Card(
        onClick = onItemClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box {
                ClothingImageCard(
                    painter = rememberAsyncImagePainter(item.imageUri.toUri()),
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
                
                // Status indicators row at top start
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Dirty status indicator (always shown)
                    Card(
                        onClick = { onDirtyStatusChange(!item.isDirty) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isDirty) 
                                MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = if (item.isDirty) "Mark as clean" else "Mark as dirty",
                                modifier = Modifier.size(20.dp),
                                tint = if (item.isDirty) 
                                    MaterialTheme.colorScheme.onError 
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    // Repair status indicator (always shown)
                    Card(
                        onClick = { onRepairStatusChange(!item.needsRepair) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.needsRepair) 
                                MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = if (item.needsRepair) "Mark as repaired" else "Mark as needs repair",
                                modifier = Modifier.size(20.dp),
                                tint = if (item.needsRepair) 
                                    MaterialTheme.colorScheme.onError 
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (item.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                Text(
                    text = "${item.categories.firstOrNull()?.displayName ?: "No Category"} • ${item.color}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    selectedMainCategory: MainCategory?,
    selectedSubCategory: ClothingCategory?,
    showDirtyOnly: Boolean,
    showCleanOnly: Boolean,
    showNeedsRepairOnly: Boolean,
    onMainCategorySelected: (MainCategory?) -> Unit,
    onSubCategorySelected: (ClothingCategory?) -> Unit,
    onDirtyOnlyChanged: (Boolean) -> Unit,
    onCleanOnlyChanged: (Boolean) -> Unit,
    onNeedsRepairOnlyChanged: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
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
                // TopTop app bar
                TopAppBar(
                    title = { Text("Filter Options") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Clear, contentDescription = "Close")
                        }
                    },
                    actions = {
                        TextButton(onClick = onClearFilters) {
                            Text("Clear All")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                // Filter content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        // Main Category filter
                        Text(
                            text = "Main Category",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedMainCategory == null,
                                    onClick = { onMainCategorySelected(null) },
                                    label = { Text("All") }
                                )
                            }
                            items(MainCategory.entries) { mainCategory ->
                                FilterChip(
                                    selected = selectedMainCategory == mainCategory,
                                    onClick = { 
                                        onMainCategorySelected(
                                            if (selectedMainCategory == mainCategory) null else mainCategory
                                        )
                                    },
                                    label = { Text(mainCategory.displayName) }
                                )
                            }
                        }
                    }
                    
                    // Sub Category filter (only show if main category is selected)
                    if (selectedMainCategory != null) {
                        item {
                            Text(
                                text = "Subcategory",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val subcategories = ClothingCategory.values().filter { 
                                it.mainCategory == selectedMainCategory 
                            }
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedSubCategory == null,
                                        onClick = { onSubCategorySelected(null) },
                                        label = { Text("All ${selectedMainCategory.displayName}") }
                                    )
                                }
                                items(subcategories) { category ->
                                    FilterChip(
                                        selected = selectedSubCategory == category,
                                        onClick = { 
                                            onSubCategorySelected(
                                                if (selectedSubCategory == category) null else category
                                            )
                                        },
                                        label = { Text(category.displayName) }
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        // Status filters
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = showDirtyOnly,
                                onClick = { onDirtyOnlyChanged(!showDirtyOnly) },
                                label = { Text("Show Dirty Items Only") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            FilterChip(
                                selected = showCleanOnly,
                                onClick = { onCleanOnlyChanged(!showCleanOnly) },
                                label = { Text("Show Clean Items Only") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            FilterChip(
                                selected = showNeedsRepairOnly,
                                onClick = { onNeedsRepairOnlyChanged(!showNeedsRepairOnly) },
                                label = { Text("Show Items Needing Repair Only") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        // Apply button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Apply Filters")
                        }
                    }
                }
            }
        }
    }
}