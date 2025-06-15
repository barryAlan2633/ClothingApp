package com.example.clothingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "outfits")
data class Outfit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val hatId: Int? = null,
    val topId: Int?,
    val bottomId: Int?,
    val footwearId: Int?,
    val jewelryIds: List<Int> = emptyList(),
    val accessoryIds: List<Int> = emptyList(),
    val createdAt: Date = Date(),
    val isFavorite: Boolean = false,
    val wearCount: Int = 0,
    val lastWorn: Date? = null
)

data class OutfitWithItems(
    val outfit: Outfit,
    val hat: ClothingItem? = null,
    val top: ClothingItem?,
    val bottom: ClothingItem?,
    val footwear: ClothingItem?,
    val jewelry: List<ClothingItem> = emptyList(),
    val accessories: List<ClothingItem> = emptyList()
) {
    // Backward compatibility properties
    @Deprecated("Use footwear instead", ReplaceWith("footwear"))
    val shoe: ClothingItem? get() = footwear
}