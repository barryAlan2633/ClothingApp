package com.example.clothingapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromClothingCategory(value: ClothingCategory): String {
        return value.name
    }
    
    @TypeConverter
    fun toClothingCategory(value: String): ClothingCategory {
        return ClothingCategory.valueOf(value)
    }
    
    @TypeConverter
    fun fromFabricType(value: FabricType): String {
        return value.name
    }
    
    @TypeConverter
    fun toFabricType(value: String): FabricType {
        return FabricType.valueOf(value)
    }
    
    @TypeConverter
    fun fromClothingStyle(value: ClothingStyle): String {
        return value.name
    }
    
    @TypeConverter
    fun toClothingStyle(value: String): ClothingStyle {
        return ClothingStyle.valueOf(value)
    }
    
    @TypeConverter
    fun fromDressCodeList(dressCodes: List<DressCode>): String {
        return dressCodes.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toDressCodeList(dressCodesString: String): List<DressCode> {
        if (dressCodesString.isEmpty()) return emptyList()
        return dressCodesString.split(",").map { DressCode.valueOf(it) }
    }
    
    @TypeConverter
    fun fromClothingCategoryList(categories: List<ClothingCategory>): String {
        return categories.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toClothingCategoryList(categoriesString: String): List<ClothingCategory> {
        if (categoriesString.isEmpty()) return emptyList()
        return categoriesString.split(",").map { ClothingCategory.valueOf(it) }
    }
    
    @TypeConverter
    fun fromIntList(intList: List<Int>): String {
        return intList.joinToString(",")
    }

    @TypeConverter
    fun toIntList(intString: String): List<Int> {
        if (intString.isEmpty()) return emptyList()
        return intString.split(",").map { it.toInt() }
    }
}

@Database(
    entities = [ClothingItem::class, Outfit::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ClothingDatabase : RoomDatabase() {
    abstract fun clothingDao(): ClothingDao
    abstract fun outfitDao(): OutfitDao
    
    companion object {
        @Volatile
        private var INSTANCE: ClothingDatabase? = null
        
        fun getDatabase(context: Context): ClothingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClothingDatabase::class.java,
                    "clothing_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}