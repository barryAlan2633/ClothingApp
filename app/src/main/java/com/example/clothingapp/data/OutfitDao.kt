package com.example.clothingapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OutfitDao {
    @Query("SELECT * FROM outfits ORDER BY createdAt DESC")
    fun getAllOutfits(): Flow<List<Outfit>>
    
    @Query("SELECT * FROM outfits WHERE id = :id")
    suspend fun getOutfitById(id: Int): Outfit?
    
    @Query("SELECT * FROM outfits WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteOutfits(): Flow<List<Outfit>>
    
    @Query("SELECT * FROM outfits WHERE topId = :topId AND bottomId = :bottomId AND footwearId = :footwearId LIMIT 1")
    suspend fun findDuplicateOutfit(topId: Int?, bottomId: Int?, footwearId: Int?): Outfit?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutfit(outfit: Outfit): Long
    
    @Update
    suspend fun updateOutfit(outfit: Outfit)
    
    @Delete
    suspend fun deleteOutfit(outfit: Outfit)
    
    @Query("UPDATE outfits SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)
    
    @Query("UPDATE outfits SET lastWorn = :date, wearCount = wearCount + 1 WHERE id = :id")
    suspend fun updateWearInfo(id: Int, date: java.util.Date)
    
    @Query("SELECT * FROM outfits WHERE id = :id")
    fun getOutfitWithItems(id: Int): Flow<Outfit?>
}