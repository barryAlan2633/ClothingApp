package com.example.clothingapp.ui.outfits

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.clothingapp.data.ClothingItem
import com.example.clothingapp.data.DressCode
import com.example.clothingapp.ui.components.CharacterOutfitLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitDetailScreen(
    navController: NavController,
    viewModel: OutfitDetailViewModel,
    outfitId: Int
) {
    val outfitWithItems by viewModel.outfitWithItems.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(outfitId) {
        viewModel.loadOutfit(outfitId)
    }
    
    if (outfitWithItems == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    val outfit = outfitWithItems!!.outfit
    val items = outfitWithItems!!.hats + 
        outfitWithItems!!.tops + 
        outfitWithItems!!.bottoms + 
        outfitWithItems!!.footwear + 
        outfitWithItems!!.jewelry + 
        outfitWithItems!!.accessories
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(outfit.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("outfit_editor/${outfitId}") }
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Outfit"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleFavorite() }
                    ) {
                        Icon(
                            imageVector = if (outfit.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (outfit.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (outfit.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
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
            // Character-style Outfit Preview
            CharacterOutfitLayout(
                hats = outfitWithItems!!.hats,
                tops = outfitWithItems!!.tops,
                bottoms = outfitWithItems!!.bottoms,
                footwear = outfitWithItems!!.footwear,
                jewelry = outfitWithItems!!.jewelry,
                accessories = outfitWithItems!!.accessories,
                onItemClick = { item ->
                    navController.navigate("item_detail/${item.id}")
                },
                useAspectRatio = false,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Outfit Stats Card
            InfoCard(title = "Outfit Statistics") {
                InfoRow("Times Worn", outfit.wearCount.toString())
                outfit.lastWorn?.let { 
                    InfoRow("Last Worn", SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it))
                }
                InfoRow("Created", SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(outfit.createdAt))
            }
            
            // Dress Codes Card
            val allDressCodes = items.flatMap { it.dressCodes }.distinct()
            if (allDressCodes.isNotEmpty()) {
                InfoCard(title = "Dress Codes") {
                    Text(
                        text = allDressCodes.joinToString(", ") { it.name.replace("_", " ") },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Item Status Card
            val dirtyItems = items.filter { it.isDirty }
            val repairItems = items.filter { it.needsRepair }
            
            if (dirtyItems.isNotEmpty() || repairItems.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Attention Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        if (dirtyItems.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Dirty items: ${dirtyItems.joinToString { it.name }}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        if (repairItems.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Needs repair: ${repairItems.joinToString { it.name }}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // Individual Items Status
            InfoCard(title = "Item Details") {
                items.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("item_detail/${item.id}") }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${item.categories.firstOrNull()?.displayName ?: "No Category"} • ${item.color}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dirty toggle
                                IconToggleButton(
                                    checked = item.isDirty,
                                    onCheckedChange = { isDirty ->
                                        scope.launch {
                                            viewModel.updateItemDirtyStatus(item.id, isDirty)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = if (item.isDirty) "Mark as clean" else "Mark as dirty",
                                        tint = if (item.isDirty) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // Repair toggle
                                IconToggleButton(
                                    checked = item.needsRepair,
                                    onCheckedChange = { needsRepair ->
                                        scope.launch {
                                            viewModel.updateItemRepairStatus(item.id, needsRepair)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Build,
                                        contentDescription = if (item.needsRepair) "Mark as repaired" else "Mark as needs repair",
                                        tint = if (item.needsRepair) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Action Button
            Button(
                onClick = { 
                    scope.launch {
                        viewModel.markAsWorn()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = dirtyItems.isEmpty() && repairItems.isEmpty()
            ) {
                Text("Mark Outfit as Worn Today")
            }
            
            if (dirtyItems.isNotEmpty() || repairItems.isNotEmpty()) {
                Text(
                    text = "Clean and repair all items before wearing this outfit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Outfit") },
            text = { Text("Are you sure you want to delete \"${outfit.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.deleteOutfit()
                            showDeleteDialog = false
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun OutfitItemCard(
    item: ClothingItem?,
    label: String,
    modifier: Modifier = Modifier,
    onItemClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        onClick = onItemClick ?: {}
    ) {
        if (item != null) {
            Box {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse(item.imageUri)),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Status indicators
                if (item.isDirty || item.needsRepair) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (item.isDirty) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Dirty",
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(16.dp),
                                    tint = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
                        if (item.needsRepair) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = "Needs Repair",
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(16.dp),
                                    tint = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
                    }
                }
                
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No $label",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}