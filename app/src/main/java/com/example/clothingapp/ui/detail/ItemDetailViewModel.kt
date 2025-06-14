package com.example.clothingapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clothingapp.data.ClothingItem
import com.example.clothingapp.data.ClothingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItemDetailViewModel(
    private val repository: ClothingRepository
) : ViewModel() {
    
    private val _item = MutableStateFlow<ClothingItem?>(null)
    val item: StateFlow<ClothingItem?> = _item.asStateFlow()
    
    fun loadItem(itemId: Int) {
        viewModelScope.launch {
            _item.value = repository.getItemById(itemId)
        }
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            _item.value?.let { currentItem ->
                val updatedItem = currentItem.copy(isFavorite = !currentItem.isFavorite)
                repository.updateItem(updatedItem)
                _item.value = updatedItem
            }
        }
    }
    
    fun markAsWorn() {
        viewModelScope.launch {
            _item.value?.let { currentItem ->
                repository.markAsWorn(currentItem.id)
                // Reload item to get updated wear count and last worn date
                loadItem(currentItem.id)
            }
        }
    }
    
    fun deleteItem() {
        viewModelScope.launch {
            _item.value?.let { currentItem ->
                repository.deleteItem(currentItem)
            }
        }
    }
}