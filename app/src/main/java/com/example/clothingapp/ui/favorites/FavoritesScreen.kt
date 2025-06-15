package com.example.clothingapp.ui.favorites

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.clothingapp.data.ClothingItem
import com.example.clothingapp.data.OutfitWithItems
import com.example.clothingapp.ui.components.ClothingImageCard
import com.example.clothingapp.ui.components.CharacterOutfitLayout
import com.example.clothingapp.ui.wardrobe.WardrobeViewModel
import com.example.clothingapp.ui.outfits.OutfitsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    wardrobeViewModel: WardrobeViewModel,
    outfitsViewModel: OutfitsViewModel
) {
    val clothingItems by wardrobeViewModel.clothingItems.collectAsState()
    val outfits by outfitsViewModel.outfits.collectAsState()
    val favoriteItems = clothingItems.filter { it.isFavorite }
    val favoriteOutfits = outfits.filter { it.outfit.isFavorite }
    
    LaunchedEffect(Unit) {
        outfitsViewModel.loadOutfits()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        if (favoriteItems.isEmpty() && favoriteOutfits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "No favorites yet",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Tap the heart icon on items and outfits to add them to favorites",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Browse Wardrobe")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Favorite Outfits Section
                if (favoriteOutfits.isNotEmpty()) {
                    item {
                        Text(
                            text = "Favorite Outfits",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.height(((favoriteOutfits.size + 1) / 2 * 200).dp),
                            contentPadding = PaddingValues(0.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            userScrollEnabled = false
                        ) {
                            items(favoriteOutfits) { outfitWithItems ->
                                FavoriteOutfitItem(
                                    outfitWithItems = outfitWithItems,
                                    onOutfitClick = { navController.navigate("outfit_detail/${outfitWithItems.outfit.id}") }
                                )
                            }
                        }
                    }
                }
                
                // Favorite Items Section  
                if (favoriteItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Favorite Items",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.height(((favoriteItems.size + 1) / 2 * 200).dp),
                            contentPadding = PaddingValues(0.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            userScrollEnabled = false
                        ) {
                            items(favoriteItems) { item ->
                                FavoriteGridItem(
                                    item = item,
                                    onItemClick = { navController.navigate("item_detail/${item.id}") },
                                    onFavoriteClick = { wardrobeViewModel.toggleFavorite(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteGridItem(
    item: ClothingItem,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        onClick = onItemClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box {
                ClothingImageCard(
                    painter = rememberAsyncImagePainter(Uri.parse(item.imageUri)),
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
                // Status indicators
                if (item.isDirty || item.needsRepair) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (item.isDirty) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "Dirty",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        }
                        if (item.needsRepair) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Build,
                                        contentDescription = "Needs Repair",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        }
                    }
                }
                
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Remove from favorites",
                        tint = MaterialTheme.colorScheme.error
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

@Composable
fun FavoriteOutfitItem(
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
                hat = outfitWithItems.hat,
                top = outfitWithItems.top,
                bottom = outfitWithItems.bottom,
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
                    maxLines = 1
                )
                Text(
                    text = "Worn ${outfitWithItems.outfit.wearCount} times",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Show if any items need attention
                val items = listOfNotNull(
                    outfitWithItems.hat,
                    outfitWithItems.top, 
                    outfitWithItems.bottom, 
                    outfitWithItems.footwear
                ) + outfitWithItems.jewelry + outfitWithItems.accessories
                val needsAttention = items.any { it.isDirty || it.needsRepair }
                if (needsAttention) {
                    Text(
                        text = "Needs attention",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}