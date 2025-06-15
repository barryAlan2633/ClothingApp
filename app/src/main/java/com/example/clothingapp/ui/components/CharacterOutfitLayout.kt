package com.example.clothingapp.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import coil.compose.rememberAsyncImagePainter
import com.example.clothingapp.data.ClothingItem
import com.example.clothingapp.data.MainCategory

@Composable
fun CharacterOutfitLayout(
    hat: ClothingItem? = null,
    top: ClothingItem? = null,
    bottom: ClothingItem? = null,
    footwear: ClothingItem? = null,
    jewelry: List<ClothingItem> = emptyList(),
    accessories: List<ClothingItem> = emptyList(),
    onItemClick: ((ClothingItem) -> Unit)? = null,
    onSlotClick: ((MainCategory) -> Unit)? = null,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    useAspectRatio: Boolean = true
) {
    val itemSize = if (isCompact) 60.dp else 80.dp
    val spacing = if (isCompact) 8.dp else 20.dp
    
    Box(
        modifier = if (useAspectRatio) {
            modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .padding(spacing)
        } else {
            modifier.padding(spacing)
        },
        contentAlignment = Alignment.Center
    ) {
        // Character silhouette placeholder (optional)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Gray.copy(alpha = 0.1f),
                    RoundedCornerShape(20.dp)
                )
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier.fillMaxHeight()
        ) {
            // TOP ROW: Hat
            OutfitSlot(
                item = hat,
                category = MainCategory.HAT,
                onItemClick = onItemClick,
                onSlotClick = onSlotClick,
                size = itemSize,
                label = if (!isCompact) "Hat" else null
            )
            
            // MIDDLE ROW: Jewelry (left), Top (center), Accessories (right)
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Jewelry
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing / 2),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (jewelry.isNotEmpty() || onSlotClick != null) {
                        if (jewelry.isNotEmpty()) {
                            jewelry.take(2).forEach { item ->
                                OutfitSlot(
                                    item = item,
                                    category = MainCategory.JEWELRY,
                                    onItemClick = onItemClick,
                                    onSlotClick = null,
                                    size = (itemSize * 0.7f),
                                    showLabel = false
                                )
                            }
                        } else {
                            OutfitSlot(
                                item = null,
                                category = MainCategory.JEWELRY,
                                onItemClick = onItemClick,
                                onSlotClick = onSlotClick,
                                size = (itemSize * 0.7f),
                                label = if (!isCompact) "Jewelry" else null
                            )
                        }
                    }
                }
                
                // Center - Top
                OutfitSlot(
                    item = top,
                    category = MainCategory.TOP,
                    onItemClick = onItemClick,
                    onSlotClick = onSlotClick,
                    size = (itemSize * 1.2f),
                    label = if (!isCompact) "Top" else null
                )
                
                // Right side - Accessories
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing / 2),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (accessories.isNotEmpty() || onSlotClick != null) {
                        if (accessories.isNotEmpty()) {
                            accessories.take(2).forEach { item ->
                                OutfitSlot(
                                    item = item,
                                    category = MainCategory.ACCESSORIES,
                                    onItemClick = onItemClick,
                                    onSlotClick = null,
                                    size = (itemSize * 0.7f),
                                    showLabel = false
                                )
                            }
                        } else {
                            OutfitSlot(
                                item = null,
                                category = MainCategory.ACCESSORIES,
                                onItemClick = onItemClick,
                                onSlotClick = onSlotClick,
                                size = (itemSize * 0.7f),
                                label = if (!isCompact) "Accessories" else null
                            )
                        }
                    }
                }
            }
            
            // BOTTOM SECTION: Bottom
            OutfitSlot(
                item = bottom,
                category = MainCategory.BOTTOM,
                onItemClick = onItemClick,
                onSlotClick = onSlotClick,
                size = itemSize,
                label = if (!isCompact) "Bottom" else null
            )
            
            // Add flexible space when not using aspect ratio to push shoes to bottom
            if (!useAspectRatio) {
                Spacer(modifier = Modifier.weight(1f))
            }
            
            // FEET: Footwear
            OutfitSlot(
                item = footwear,
                category = MainCategory.FOOTWEAR,
                onItemClick = onItemClick,
                onSlotClick = onSlotClick,
                size = itemSize,
                label = if (!isCompact) "Shoes" else null
            )
        }
    }
}

@Composable
fun OutfitSlot(
    item: ClothingItem?,
    category: MainCategory,
    onItemClick: ((ClothingItem) -> Unit)?,
    onSlotClick: ((MainCategory) -> Unit)?,
    size: Dp,
    label: String? = null,
    showLabel: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (item != null) Color.Transparent 
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    if (item != null && onItemClick != null) {
                        onItemClick(item)
                    } else if (onSlotClick != null) {
                        onSlotClick(category)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (item != null) {
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
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (item.isDirty) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        MaterialTheme.colorScheme.error,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Dirty",
                                    modifier = Modifier.size(8.dp),
                                    tint = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
                        if (item.needsRepair) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        MaterialTheme.colorScheme.error,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = "Needs Repair",
                                    modifier = Modifier.size(8.dp),
                                    tint = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
                    }
                }
            } else {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add ${category.displayName}",
                    modifier = Modifier.size((size * 0.4f)),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        
        if (showLabel && label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}