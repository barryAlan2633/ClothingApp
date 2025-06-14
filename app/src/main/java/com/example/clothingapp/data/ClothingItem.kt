package com.example.clothingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "clothing_items")
data class ClothingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val imageUri: String,
    val category: ClothingCategory,
    val color: String,
    val secondaryColor: String? = null,
    val pattern: String? = null,
    val fabricType: FabricType,
    val size: String,
    val style: ClothingStyle,
    val dressCode: DressCode,
    val brand: String? = null,
    val purchasePrice: Double? = null,
    val purchaseDate: Date? = null,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val lastWorn: Date? = null,
    val wearCount: Int = 0,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class ClothingCategory {
    SHOES,
    TOP,
    BOTTOM,
    OUTERWEAR,
    DRESS,
    ACCESSORIES,
    UNDERWEAR,
    ACTIVEWEAR,
    SWIMWEAR,
    SLEEPWEAR,
    OTHER
}

enum class FabricType {
    COTTON,
    POLYESTER,
    WOOL,
    SILK,
    LINEN,
    DENIM,
    LEATHER,
    SYNTHETIC,
    BLEND,
    OTHER
}

enum class ClothingStyle {
    CASUAL,
    FORMAL,
    BUSINESS,
    SPORTY,
    STREETWEAR,
    VINTAGE,
    BOHEMIAN,
    MINIMALIST,
    PREPPY,
    OTHER
}

enum class DressCode {
    CASUAL,
    BUSINESS_CASUAL,
    BUSINESS_FORMAL,
    COCKTAIL,
    BLACK_TIE,
    ATHLETIC,
    LOUNGEWEAR,
    OTHER
}