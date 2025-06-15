package com.example.clothingapp.ui.outfits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clothingapp.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class OutfitDetailViewModel(
    private val clothingDao: ClothingDao,
    private val outfitDao: OutfitDao
) : ViewModel() {
    
    private val _outfitWithItems = MutableStateFlow<OutfitWithItems?>(null)
    val outfitWithItems: StateFlow<OutfitWithItems?> = _outfitWithItems.asStateFlow()
    
    fun loadOutfit(outfitId: Int) {
        viewModelScope.launch {
            val outfit = outfitDao.getOutfitById(outfitId)
            if (outfit != null) {
                val hat = outfit.hatId?.let { clothingDao.getItemById(it) }
                val top = outfit.topId?.let { clothingDao.getItemById(it) }
                val bottom = outfit.bottomId?.let { clothingDao.getItemById(it) }
                val footwear = outfit.footwearId?.let { clothingDao.getItemById(it) }
                val jewelry = outfit.jewelryIds.mapNotNull { clothingDao.getItemById(it) }
                val accessories = outfit.accessoryIds.mapNotNull { clothingDao.getItemById(it) }
                
                _outfitWithItems.value = OutfitWithItems(
                    outfit = outfit,
                    hat = hat,
                    top = top,
                    bottom = bottom,
                    footwear = footwear,
                    jewelry = jewelry,
                    accessories = accessories
                )
            }
        }
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            _outfitWithItems.value?.let { current ->
                val newFavoriteStatus = !current.outfit.isFavorite
                outfitDao.updateFavoriteStatus(current.outfit.id, newFavoriteStatus)
                _outfitWithItems.value = current.copy(
                    outfit = current.outfit.copy(isFavorite = newFavoriteStatus)
                )
            }
        }
    }
    
    fun deleteOutfit() {
        viewModelScope.launch {
            _outfitWithItems.value?.let { current ->
                outfitDao.deleteOutfit(current.outfit)
            }
        }
    }
    
    fun markAsWorn() {
        viewModelScope.launch {
            _outfitWithItems.value?.let { current ->
                val now = Date()
                
                // Update outfit wear info
                outfitDao.updateWearInfo(current.outfit.id, now)
                
                // Update each clothing item wear info and mark as dirty
                val allItems = listOfNotNull(current.hat, current.top, current.bottom, current.footwear) + current.jewelry + current.accessories
                allItems.forEach { item ->
                    clothingDao.updateWearInfo(item.id, now)
                    clothingDao.updateDirtyStatus(item.id, true)
                }
                
                // Reload the outfit to reflect changes
                loadOutfit(current.outfit.id)
            }
        }
    }
    
    fun updateItemDirtyStatus(itemId: Int, isDirty: Boolean) {
        viewModelScope.launch {
            clothingDao.updateDirtyStatus(itemId, isDirty)
            // Reload to reflect changes
            _outfitWithItems.value?.outfit?.id?.let { loadOutfit(it) }
        }
    }
    
    fun updateItemRepairStatus(itemId: Int, needsRepair: Boolean) {
        viewModelScope.launch {
            clothingDao.updateRepairStatus(itemId, needsRepair)
            // Reload to reflect changes
            _outfitWithItems.value?.outfit?.id?.let { loadOutfit(it) }
        }
    }
}