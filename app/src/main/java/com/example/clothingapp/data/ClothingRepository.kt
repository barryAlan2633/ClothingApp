package com.example.clothingapp.data

import kotlinx.coroutines.flow.Flow
import java.util.Date

class ClothingRepository(private val clothingDao: ClothingDao) {
    
    fun getAllItems(): Flow<List<ClothingItem>> = clothingDao.getAllItems()
    
    fun getItemsByCategory(category: ClothingCategory): Flow<List<ClothingItem>> = 
        clothingDao.getItemsByCategory(category)
    
    suspend fun getItemById(id: Int): ClothingItem? = clothingDao.getItemById(id)
    
    fun getFavoriteItems(): Flow<List<ClothingItem>> = clothingDao.getFavoriteItems()
    
    fun getItemsByColor(color: String): Flow<List<ClothingItem>> = 
        clothingDao.getItemsByColor("%$color%")
    
    fun getItemsByDressCode(dressCode: DressCode): Flow<List<ClothingItem>> = 
        clothingDao.getItemsByDressCode(dressCode)
    
    suspend fun insertItem(item: ClothingItem) {
        clothingDao.insertItem(item)
    }
    
    suspend fun updateItem(item: ClothingItem) {
        clothingDao.updateItem(item.copy(updatedAt = Date()))
    }
    
    suspend fun deleteItem(item: ClothingItem) {
        clothingDao.deleteItem(item)
    }
    
    suspend fun toggleFavorite(id: Int, isFavorite: Boolean) {
        clothingDao.updateFavoriteStatus(id, isFavorite)
    }
    
    suspend fun markAsWorn(id: Int) {
        clothingDao.updateWearInfo(id, Date())
    }
}