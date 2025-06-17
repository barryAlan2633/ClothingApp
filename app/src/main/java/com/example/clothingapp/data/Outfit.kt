package com.example.clothingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "outfits")
data class Outfit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val hatIds: List<Int> = emptyList(),
    val topIds: List<Int> = emptyList(),
    val bottomIds: List<Int> = emptyList(),
    val footwearIds: List<Int> = emptyList(),
    val jewelryIds: List<Int> = emptyList(),
    val accessoryIds: List<Int> = emptyList(),
    val createdAt: Date = Date(),
    val isFavorite: Boolean = false,
    val wearCount: Int = 0,
    val lastWorn: Date? = null,
    
    // Backward compatibility
    @Deprecated("Use hatIds instead")
    val hatId: Int? = null,
    @Deprecated("Use topIds instead") 
    val topId: Int? = null,
    @Deprecated("Use bottomIds instead")
    val bottomId: Int? = null,
    @Deprecated("Use footwearIds instead")
    val footwearId: Int? = null
)

data class OutfitWithItems(
    val outfit: Outfit,
    val hats: List<ClothingItem> = emptyList(),
    val tops: List<ClothingItem> = emptyList(),
    val bottoms: List<ClothingItem> = emptyList(),
    val footwear: List<ClothingItem> = emptyList(),
    val jewelry: List<ClothingItem> = emptyList(),
    val accessories: List<ClothingItem> = emptyList()
) {
    // Backward compatibility properties
    @Deprecated("Use hats instead", ReplaceWith("hats.firstOrNull()"))
    val hat: ClothingItem? get() = hats.firstOrNull()
    
    @Deprecated("Use tops instead", ReplaceWith("tops.firstOrNull()"))
    val top: ClothingItem? get() = tops.firstOrNull()
    
    @Deprecated("Use bottoms instead", ReplaceWith("bottoms.firstOrNull()"))
    val bottom: ClothingItem? get() = bottoms.firstOrNull()
    
    @Deprecated("Use footwear instead", ReplaceWith("footwear.firstOrNull()"))
    val shoe: ClothingItem? get() = footwear.firstOrNull()
}