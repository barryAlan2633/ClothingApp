package com.example.clothingapp.ui.detail

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.clothingapp.data.*
import com.example.clothingapp.ui.components.FullScreenImageViewer
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    navController: NavController,
    viewModel: ItemDetailViewModel,
    itemId: Int
) {
    val item by viewModel.item.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    
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
    
    // Show full screen image if requested
    if (showFullScreenImage) {
        FullScreenImageViewer(
            imageUri = currentItem.imageUri,
            onClose = { showFullScreenImage = false }
        )
        return
    }
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentItem.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFavorite() }
                    ) {
                        Icon(
                            imageVector = if (currentItem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (currentItem.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (currentItem.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate("edit_item/${currentItem.id}") }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
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
            // Image (clickable for full screen)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clickable { showFullScreenImage = true }
            ) {
                Box {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(currentItem.imageUri)),
                        contentDescription = currentItem.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    
                    // Hint overlay
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(
                            text = "Tap to view full size",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            
            // Basic Info Card
            InfoCard(title = "Basic Information") {
                InfoRow("Name", currentItem.name)
                InfoRow("Categories", currentItem.categories.joinToString(", ") { it.displayName })
                InfoRow("Size", currentItem.size)
                currentItem.brand?.let { InfoRow("Brand", it) }
            }
            
            // Style & Appearance Card
            InfoCard(title = "Style & Appearance") {
                InfoRow("Primary Color", currentItem.color)
                currentItem.secondaryColor?.let { InfoRow("Secondary Color", it) }
                currentItem.pattern?.let { InfoRow("Pattern", it) }
                InfoRow("Fabric Type", currentItem.fabricType.name.replace("_", " "))
                InfoRow("Style", currentItem.style.name.replace("_", " "))
                InfoRow("Dress Codes", currentItem.dressCodes.joinToString(", ") { it.name.replace("_", " ") })
            }
            
            // Usage Stats Card
            InfoCard(title = "Usage Statistics") {
                InfoRow("Times Worn", currentItem.wearCount.toString())
                currentItem.lastWorn?.let { 
                    InfoRow("Last Worn", SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it))
                }
                currentItem.purchasePrice?.let { 
                    InfoRow("Purchase Price", "$${String.format("%.2f", it)}")
                }
                currentItem.purchaseDate?.let { 
                    InfoRow("Purchase Date", SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it))
                }
            }
            
            // Notes Card
            currentItem.notes?.let { notes ->
                InfoCard(title = "Notes") {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.markAsWorn() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mark as Worn Today")
                }
                
                OutlinedButton(
                    onClick = { navController.navigate("edit_item/${currentItem.id}") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Item")
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete \"${currentItem.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteItem()
                        showDeleteDialog = false
                        navController.popBackStack()
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