package com.example.clothingapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingDao {
    @Query("SELECT * FROM clothing_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ClothingItem>>
    
    @Query("SELECT * FROM clothing_items WHERE categories LIKE '%' || :category || '%' ORDER BY createdAt DESC")
    fun getItemsByCategory(category: ClothingCategory): Flow<List<ClothingItem>>
    
    @Query("SELECT * FROM clothing_items WHERE id = :id")
    suspend fun getItemById(id: Int): ClothingItem?
    
    @Query("SELECT * FROM clothing_items WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteItems(): Flow<List<ClothingItem>>
    
    @Query("SELECT * FROM clothing_items WHERE color LIKE :color OR secondaryColor LIKE :color ORDER BY createdAt DESC")
    fun getItemsByColor(color: String): Flow<List<ClothingItem>>
    
    @Query("SELECT * FROM clothing_items WHERE dressCodes LIKE '%' || :dressCode || '%' ORDER BY createdAt DESC")
    fun getItemsByDressCode(dressCode: DressCode): Flow<List<ClothingItem>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ClothingItem)
    
    @Update
    suspend fun updateItem(item: ClothingItem)
    
    @Delete
    suspend fun deleteItem(item: ClothingItem)
    
    @Query("UPDATE clothing_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)
    
    @Query("UPDATE clothing_items SET lastWorn = :date, wearCount = wearCount + 1 WHERE id = :id")
    suspend fun updateWearInfo(id: Int, date: java.util.Date)
    
    @Query("UPDATE clothing_items SET isDirty = :isDirty WHERE id = :id")
    suspend fun updateDirtyStatus(id: Int, isDirty: Boolean)
    
    @Query("UPDATE clothing_items SET needsRepair = :needsRepair WHERE id = :id")
    suspend fun updateRepairStatus(id: Int, needsRepair: Boolean)
}