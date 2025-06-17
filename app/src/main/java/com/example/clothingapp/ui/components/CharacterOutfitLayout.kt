package com.example.clothingapp.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalConfiguration
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
    hats: List<ClothingItem> = emptyList(),
    tops: List<ClothingItem> = emptyList(),
    bottoms: List<ClothingItem> = emptyList(),
    footwear: List<ClothingItem> = emptyList(),
    jewelry: List<ClothingItem> = emptyList(),
    accessories: List<ClothingItem> = emptyList(),
    onItemClick: ((ClothingItem) -> Unit)? = null,
    onSlotClick: ((MainCategory) -> Unit)? = null,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    useAspectRatio: Boolean = true,
    // Backward compatibility
    hat: ClothingItem? = null,
    top: ClothingItem? = null,
    bottom: ClothingItem? = null
) {
    // Convert single items to lists for backward compatibility
    val finalHats = if (hats.isEmpty() && hat != null) listOf(hat) else hats
    val finalTops = if (tops.isEmpty() && top != null) listOf(top) else tops
    val finalBottoms = if (bottoms.isEmpty() && bottom != null) listOf(bottom) else bottoms
    val itemSize = if (isCompact) 60.dp else 80.dp
    val spacing = if (isCompact) 8.dp else 20.dp
    
    // Calculate available width for center items (accounting for side columns)
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val sideColumnWidth = itemSize * 0.5f + spacing
    val centerWidth = screenWidth - (spacing * 4) // Total width minus padding
    
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
            modifier = if (useAspectRatio) Modifier.fillMaxHeight() else Modifier
        ) {
            // TOP ROW: Hats
            HorizontalItemSlot(
                items = finalHats,
                category = MainCategory.HAT,
                onItemClick = onItemClick,
                onSlotClick = onSlotClick,
                size = itemSize,
                label = if (!isCompact) "Hat" else null,
                maxWidth = centerWidth,
                spacing = spacing / 2
            )
            
            // MIDDLE ROW: Jewelry (left), Top (center), Accessories (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Jewelry (fixed width)
                Box(
                    modifier = Modifier.width(itemSize * 0.5f + spacing),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(spacing / 2),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    if (jewelry.isNotEmpty()) {
                        jewelry.forEach { item ->
                            OutfitSlot(
                                item = item,
                                category = MainCategory.JEWELRY,
                                onItemClick = onItemClick,
                                onSlotClick = null,
                                size = (itemSize * 0.5f),
                                showLabel = false
                            )
                        }
                        // Add button to add more jewelry
                        if (onSlotClick != null) {
                            Box(
                                modifier = Modifier
                                    .size(itemSize * 0.5f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSlotClick(MainCategory.JEWELRY) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add more jewelry",
                                    modifier = Modifier.size((itemSize * 0.5f) * 0.4f),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        OutfitSlot(
                            item = null,
                            category = MainCategory.JEWELRY,
                            onItemClick = onItemClick,
                            onSlotClick = onSlotClick,
                            size = (itemSize * 0.5f),
                            label = if (!isCompact) "Jewelry" else null
                        )
                    }
                    }
                }
                
                // Center - Tops (flexible width, takes remaining space)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalItemSlot(
                        items = finalTops,
                        category = MainCategory.TOP,
                        onItemClick = onItemClick,
                        onSlotClick = onSlotClick,
                        size = itemSize,
                        label = if (!isCompact) "Top" else null,
                        maxWidth = centerWidth - (itemSize * 1.0f + spacing * 2),
                        spacing = spacing / 2
                    )
                }
                
                // Right side - Accessories (fixed width)
                Box(
                    modifier = Modifier.width(itemSize * 0.5f + spacing),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(spacing / 2),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    if (accessories.isNotEmpty()) {
                        accessories.forEach { item ->
                            OutfitSlot(
                                item = item,
                                category = MainCategory.ACCESSORIES,
                                onItemClick = onItemClick,
                                onSlotClick = null,
                                size = (itemSize * 0.5f),
                                showLabel = false
                            )
                        }
                        // Add button to add more accessories
                        if (onSlotClick != null) {
                            Box(
                                modifier = Modifier
                                    .size(itemSize * 0.5f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSlotClick(MainCategory.ACCESSORIES) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add more accessories",
                                    modifier = Modifier.size((itemSize * 0.5f) * 0.4f),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        OutfitSlot(
                            item = null,
                            category = MainCategory.ACCESSORIES,
                            onItemClick = onItemClick,
                            onSlotClick = onSlotClick,
                            size = (itemSize * 0.5f),
                            label = if (!isCompact) "Accessories" else null
                        )
                    }
                    }
                }
            }
            
            // BOTTOM SECTION: Bottoms
            HorizontalItemSlot(
                items = finalBottoms,
                category = MainCategory.BOTTOM,
                onItemClick = onItemClick,
                onSlotClick = onSlotClick,
                size = itemSize,
                label = if (!isCompact) "Bottom" else null,
                maxWidth = centerWidth,
                spacing = spacing / 2
            )
            
            // Add some spacing before shoes section
            Spacer(modifier = Modifier.height(spacing))
            
            // FEET: Footwear
            HorizontalItemSlot(
                items = footwear,
                category = MainCategory.FOOTWEAR,
                onItemClick = onItemClick,
                onSlotClick = onSlotClick,
                size = itemSize,
                label = if (!isCompact) "Shoes" else null,
                maxWidth = centerWidth,
                spacing = spacing / 2
            )
        }
    }
}

@Composable
fun MultiItemSlot(
    items: List<ClothingItem>,
    category: MainCategory,
    onItemClick: ((ClothingItem) -> Unit)?,
    onSlotClick: ((MainCategory) -> Unit)?,
    size: Dp,
    label: String? = null,
    isCompact: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (items.isEmpty()) {
            // Show empty slot
            OutfitSlot(
                item = null,
                category = category,
                onItemClick = onItemClick,
                onSlotClick = onSlotClick,
                size = size,
                label = label,
                showLabel = true
            )
        } else {
            // Show stacked items with slight offset
            Box(
                modifier = Modifier.clickable {
                    // Allow clicking on the slot to add more items
                    onSlotClick?.invoke(category)
                }
            ) {
                items.forEachIndexed { index, item ->
                    val offset = (index * 4).dp
                    OutfitSlot(
                        item = item,
                        category = category,
                        onItemClick = onItemClick,
                        onSlotClick = onSlotClick,
                        size = size,
                        label = if (index == 0) label else null,
                        showLabel = index == 0,
                        modifier = Modifier.offset(x = offset, y = offset)
                    )
                }
                
                // Add a small indicator showing count if more than 1 item
                if (items.size > 1) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-8).dp)
                            .size(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = items.size.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                
                // Add a small "+" indicator in the bottom right to show more items can be added
                if (onSlotClick != null) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 8.dp, y = 8.dp)
                            .size(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add more ${category.displayName}",
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalItemSlot(
    items: List<ClothingItem>,
    category: MainCategory,
    onItemClick: ((ClothingItem) -> Unit)?,
    onSlotClick: ((MainCategory) -> Unit)?,
    size: Dp,
    label: String? = null,
    maxWidth: Dp,
    spacing: Dp = 8.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (items.isEmpty()) {
            // Show empty slot
            OutfitSlot(
                item = null,
                category = category,
                onItemClick = onItemClick,
                onSlotClick = onSlotClick,
                size = size,
                showLabel = false
            )
        } else {
            // Calculate how many items can fit per row (minimum 2)
            val naturalItemsPerRow = (maxWidth / (size + spacing)).toInt()
            val itemsPerRow = naturalItemsPerRow.coerceAtLeast(2)
            
            // If we can't fit 2 items naturally, reduce the item size
            val adjustedSize = if (naturalItemsPerRow < 2) {
                // Calculate size that allows 2 items to fit
                (maxWidth - spacing) / 2.2f
            } else {
                size
            }
            
            val rows = items.chunked(itemsPerRow)
            
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                rows.forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowItems.forEach { item ->
                            OutfitSlot(
                                item = item,
                                category = category,
                                onItemClick = onItemClick,
                                onSlotClick = null,
                                size = adjustedSize,
                                showLabel = false
                            )
                        }
                    }
                }
                
                // Add button to add more items
                if (onSlotClick != null) {
                    Box(
                        modifier = Modifier
                            .size(adjustedSize)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSlotClick(category) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add more ${category.displayName}",
                            modifier = Modifier.size(adjustedSize * 0.4f),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
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
    showLabel: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
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