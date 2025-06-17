package com.example.clothingapp.ui.outfits

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.clothingapp.data.OutfitWithItems
import com.example.clothingapp.ui.components.CharacterOutfitLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitsScreen(
    navController: NavController,
    viewModel: OutfitsViewModel
) {
    val outfits by viewModel.outfits.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var showCleanOutfitsOnly by remember { mutableStateOf(false) }
    var showDirtyOutfitsOnly by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadOutfits()
    }
    
    // Filter outfits based on search and filters
    val filteredOutfits = remember(outfits, searchQuery, showFavoritesOnly, showCleanOutfitsOnly, showDirtyOutfitsOnly) {
        outfits.filter { outfitWithItems ->
            val matchesSearch = searchQuery.isEmpty() || 
                outfitWithItems.outfit.name.contains(searchQuery, ignoreCase = true)
            
            val matchesFavorites = !showFavoritesOnly || outfitWithItems.outfit.isFavorite
            
            val items = outfitWithItems.hats + outfitWithItems.tops + outfitWithItems.bottoms + outfitWithItems.footwear + outfitWithItems.jewelry + outfitWithItems.accessories
            val hasDirtyItems = items.any { it.isDirty || it.needsRepair }
            
            val matchesStatus = when {
                showCleanOutfitsOnly -> !hasDirtyItems
                showDirtyOutfitsOnly -> hasDirtyItems
                else -> true
            }
            
            matchesSearch && matchesFavorites && matchesStatus
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
                            placeholder = { Text("Search outfits...") },
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
                        Text("My Outfits")
                    }
                },
                navigationIcon = {
                    if (!showSearch) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (!showSearch) {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Filter",
                                tint = if (showFavoritesOnly || showCleanOutfitsOnly || showDirtyOutfitsOnly) 
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { navController.navigate("outfit_creator") }) {
                            Icon(Icons.Default.Add, contentDescription = "Create Outfit")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter UI
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Status filters
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = showFavoritesOnly,
                                onClick = { showFavoritesOnly = !showFavoritesOnly },
                                label = { Text("Favorites Only") }
                            )
                            FilterChip(
                                selected = showCleanOutfitsOnly,
                                onClick = { 
                                    showCleanOutfitsOnly = !showCleanOutfitsOnly
                                    if (showCleanOutfitsOnly) showDirtyOutfitsOnly = false
                                },
                                label = { Text("Clean Outfits") }
                            )
                            FilterChip(
                                selected = showDirtyOutfitsOnly,
                                onClick = { 
                                    showDirtyOutfitsOnly = !showDirtyOutfitsOnly
                                    if (showDirtyOutfitsOnly) showCleanOutfitsOnly = false
                                },
                                label = { Text("Needs Attention") }
                            )
                        }
                        
                        // Clear all filters
                        TextButton(
                            onClick = { 
                                showFavoritesOnly = false
                                showCleanOutfitsOnly = false
                                showDirtyOutfitsOnly = false
                            }
                        ) {
                            Text("Clear All Filters")
                        }
                    }
                }
            }
            
            if (filteredOutfits.isEmpty()) {
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
                            if (outfits.isEmpty()) "No outfits created yet" else "No outfits match your filters",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (outfits.isEmpty()) {
                            Button(onClick = { navController.navigate("outfit_creator") }) {
                                Text("Create Your First Outfit")
                            }
                        } else {
                            Button(onClick = { 
                                showFavoritesOnly = false
                                showCleanOutfitsOnly = false
                                showDirtyOutfitsOnly = false
                                searchQuery = ""
                            }) {
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredOutfits) { outfitWithItems ->
                        OutfitGridItem(
                            outfitWithItems = outfitWithItems,
                            onOutfitClick = { navController.navigate("outfit_detail/${outfitWithItems.outfit.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OutfitGridItem(
    outfitWithItems: OutfitWithItems,
    onOutfitClick: () -> Unit
) {
    Card(
        onClick = onOutfitClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Character-style outfit preview
            CharacterOutfitLayout(
                hats = outfitWithItems.hats,
                tops = outfitWithItems.tops,
                bottoms = outfitWithItems.bottoms,
                footwear = outfitWithItems.footwear,
                jewelry = outfitWithItems.jewelry,
                accessories = outfitWithItems.accessories,
                isCompact = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            
            // Outfit info
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = outfitWithItems.outfit.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Worn ${outfitWithItems.outfit.wearCount} times",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}