package com.example.clothingapp.ui.outfits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clothingapp.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OutfitsViewModel(
    private val clothingDao: ClothingDao,
    private val outfitDao: OutfitDao
) : ViewModel() {
    
    private val _outfits = MutableStateFlow<List<OutfitWithItems>>(emptyList())
    val outfits: StateFlow<List<OutfitWithItems>> = _outfits.asStateFlow()
    
    fun loadOutfits() {
        viewModelScope.launch {
            outfitDao.getAllOutfits().collect { outfitList ->
                val outfitsWithItems = outfitList.map { outfit ->
                    val hats = outfit.hatIds.mapNotNull { clothingDao.getItemById(it) }
                    val tops = outfit.topIds.mapNotNull { clothingDao.getItemById(it) }
                    val bottoms = outfit.bottomIds.mapNotNull { clothingDao.getItemById(it) }
                    val footwear = outfit.footwearIds.mapNotNull { clothingDao.getItemById(it) }
                    val jewelry = outfit.jewelryIds.mapNotNull { clothingDao.getItemById(it) }
                    val accessories = outfit.accessoryIds.mapNotNull { clothingDao.getItemById(it) }
                    
                    OutfitWithItems(
                        outfit = outfit,
                        hats = hats,
                        tops = tops,
                        bottoms = bottoms,
                        footwear = footwear,
                        jewelry = jewelry,
                        accessories = accessories
                    )
                }
                _outfits.value = outfitsWithItems
            }
        }
    }
}