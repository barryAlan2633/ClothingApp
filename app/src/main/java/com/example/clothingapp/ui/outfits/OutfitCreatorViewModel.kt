package com.example.clothingapp.ui.outfits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clothingapp.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OutfitCreatorViewModel(
    private val clothingDao: ClothingDao,
    private val outfitDao: OutfitDao
) : ViewModel() {
    
    private val _allItems = MutableStateFlow<List<ClothingItem>>(emptyList())
    val allItems: StateFlow<List<ClothingItem>> = _allItems.asStateFlow()
    
    fun loadClothingItems() {
        viewModelScope.launch {
            clothingDao.getAllItems().collect { allItems ->
                _allItems.value = allItems
            }
        }
    }
    
    suspend fun saveOutfit(
        name: String,
        hatIds: List<Int>,
        topIds: List<Int>,
        bottomIds: List<Int>,
        footwearIds: List<Int>,
        jewelryIds: List<Int>,
        accessoryIds: List<Int>
    ): Boolean {
        return try {
            // At least one item must be selected
            if (hatIds.isEmpty() && topIds.isEmpty() && bottomIds.isEmpty() && footwearIds.isEmpty() && jewelryIds.isEmpty() && accessoryIds.isEmpty()) {
                return false
            }
            
            val outfit = Outfit(
                name = name,
                hatIds = hatIds,
                topIds = topIds,
                bottomIds = bottomIds,
                footwearIds = footwearIds,
                jewelryIds = jewelryIds,
                accessoryIds = accessoryIds
            )
            
            outfitDao.insertOutfit(outfit)
            true
        } catch (e: Exception) {
            false
        }
    }
}