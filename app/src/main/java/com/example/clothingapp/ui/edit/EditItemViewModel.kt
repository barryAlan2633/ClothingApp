package com.example.clothingapp.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clothingapp.data.ClothingItem
import com.example.clothingapp.data.ClothingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditItemViewModel(
    private val repository: ClothingRepository
) : ViewModel() {
    
    private val _item = MutableStateFlow<ClothingItem?>(null)
    val item: StateFlow<ClothingItem?> = _item.asStateFlow()
    
    fun loadItem(itemId: Int) {
        viewModelScope.launch {
            _item.value = repository.getItemById(itemId)
        }
    }
    
    fun updateItem(item: ClothingItem) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }
}