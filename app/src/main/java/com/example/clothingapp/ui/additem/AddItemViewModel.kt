package com.example.clothingapp.ui.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clothingapp.data.ClothingItem
import com.example.clothingapp.data.ClothingRepository
import kotlinx.coroutines.launch

class AddItemViewModel(
    private val repository: ClothingRepository
) : ViewModel() {
    
    fun saveItem(item: ClothingItem) {
        viewModelScope.launch {
            repository.insertItem(item)
        }
    }
}