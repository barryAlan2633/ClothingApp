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
    val categories: List<ClothingCategory>,
    val color: String,
    val secondaryColor: String? = null,
    val pattern: String? = null,
    val fabricType: FabricType,
    val size: String,
    val style: ClothingStyle,
    val dressCodes: List<DressCode>,
    val brand: String? = null,
    val purchasePrice: Double? = null,
    val purchaseDate: Date? = null,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val lastWorn: Date? = null,
    val wearCount: Int = 0,
    val isDirty: Boolean = false,
    val needsRepair: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class ClothingCategory(val mainCategory: MainCategory, val displayName: String) {
    // HAT category
    HAT(MainCategory.HAT, "Hat"),
    BEANIE(MainCategory.HAT, "Beanie"),
    CAP(MainCategory.HAT, "Cap"),
    BUCKET_HAT(MainCategory.HAT, "Bucket Hat"),
    
    // TOP category
    TOP(MainCategory.TOP, "Top"),
    SHIRT(MainCategory.TOP, "Shirt"),
    T_SHIRT(MainCategory.TOP, "T-Shirt"),
    BLOUSE(MainCategory.TOP, "Blouse"),
    TANK_TOP(MainCategory.TOP, "Tank Top"),
    SWEATER(MainCategory.TOP, "Sweater"),
    HOODIE(MainCategory.TOP, "Hoodie"),
    JACKET(MainCategory.TOP, "Jacket"),
    BLAZER(MainCategory.TOP, "Blazer"),
    COAT(MainCategory.TOP, "Coat"),
    VEST(MainCategory.TOP, "Vest"),
    CARDIGAN(MainCategory.TOP, "Cardigan"),
    POLO(MainCategory.TOP, "Polo"),
    UNDERSHIRT(MainCategory.TOP, "Undershirt"),
    CROP_TOP(MainCategory.TOP, "Crop Top"),
    
    // BOTTOM category
    BOTTOM(MainCategory.BOTTOM, "Bottom"),
    PANTS(MainCategory.BOTTOM, "Pants"),
    JEANS(MainCategory.BOTTOM, "Jeans"),
    SHORTS(MainCategory.BOTTOM, "Shorts"),
    SKIRT(MainCategory.BOTTOM, "Skirt"),
    LEGGINGS(MainCategory.BOTTOM, "Leggings"),
    CHINOS(MainCategory.BOTTOM, "Chinos"),
    SWEATPANTS(MainCategory.BOTTOM, "Sweatpants"),
    CARGO_PANTS(MainCategory.BOTTOM, "Cargo Pants"),
    UNDERWEAR(MainCategory.BOTTOM, "Underwear"),
    
    // FOOTWEAR category
    FOOTWEAR(MainCategory.FOOTWEAR, "Footwear"),
    SHOES(MainCategory.FOOTWEAR, "Shoes"),
    SNEAKERS(MainCategory.FOOTWEAR, "Sneakers"),
    BOOTS(MainCategory.FOOTWEAR, "Boots"),
    SANDALS(MainCategory.FOOTWEAR, "Sandals"),
    HEELS(MainCategory.FOOTWEAR, "Heels"),
    FLATS(MainCategory.FOOTWEAR, "Flats"),
    LOAFERS(MainCategory.FOOTWEAR, "Loafers"),
    ATHLETIC_SHOES(MainCategory.FOOTWEAR, "Athletic Shoes"),
    DRESS_SHOES(MainCategory.FOOTWEAR, "Dress Shoes"),
    SOCKS(MainCategory.FOOTWEAR, "Socks"),
    
    // JEWELRY category
    JEWELRY(MainCategory.JEWELRY, "Jewelry"),
    NECKLACE(MainCategory.JEWELRY, "Necklace"),
    EARRINGS(MainCategory.JEWELRY, "Earrings"),
    BRACELET(MainCategory.JEWELRY, "Bracelet"),
    RING(MainCategory.JEWELRY, "Ring"),
    WATCH(MainCategory.JEWELRY, "Watch"),
    ANKLET(MainCategory.JEWELRY, "Anklet"),
    
    // ACCESSORIES category
    ACCESSORIES(MainCategory.ACCESSORIES, "Accessories"),
    BAG(MainCategory.ACCESSORIES, "Bag"),
    PURSE(MainCategory.ACCESSORIES, "Purse"),
    WALLET(MainCategory.ACCESSORIES, "Wallet"),
    BACKPACK(MainCategory.ACCESSORIES, "Backpack"),
    BELT(MainCategory.ACCESSORIES, "Belt"),
    SCARF(MainCategory.ACCESSORIES, "Scarf"),
    TIE(MainCategory.ACCESSORIES, "Tie"),
    BOW_TIE(MainCategory.ACCESSORIES, "Bow Tie"),
    GLASSES(MainCategory.ACCESSORIES, "Glasses"),
    SUNGLASSES(MainCategory.ACCESSORIES, "Sunglasses"),
    GLOVES(MainCategory.ACCESSORIES, "Gloves"),
    
    // SPECIAL ITEMS (can belong to multiple main categories)
    DRESS(MainCategory.TOP, "Dress"), // Also counts as bottom
    JUMPSUIT(MainCategory.TOP, "Jumpsuit"), // Also counts as bottom
    OVERALLS(MainCategory.TOP, "Overalls"), // Also counts as bottom
    ROMPER(MainCategory.TOP, "Romper"), // Also counts as bottom
    
    // OTHER
    OTHER(MainCategory.ACCESSORIES, "Other")
}

enum class MainCategory(val displayName: String) {
    HAT("Hat"),
    TOP("Top"),
    BOTTOM("Bottom"),
    FOOTWEAR("Footwear"),
    JEWELRY("Jewelry"),
    ACCESSORIES("Accessories")
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

