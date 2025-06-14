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
    fun fromDressCode(value: DressCode): String {
        return value.name
    }
    
    @TypeConverter
    fun toDressCode(value: String): DressCode {
        return DressCode.valueOf(value)
    }
}

@Database(
    entities = [ClothingItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ClothingDatabase : RoomDatabase() {
    abstract fun clothingDao(): ClothingDao
    
    companion object {
        @Volatile
        private var INSTANCE: ClothingDatabase? = null
        
        fun getDatabase(context: Context): ClothingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClothingDatabase::class.java,
                    "clothing_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}