package com.example.clothingapp

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.clothingapp.data.ClothingDatabase
import com.example.clothingapp.data.ClothingRepository
import com.example.clothingapp.ui.additem.AddItemScreen
import com.example.clothingapp.ui.additem.AddItemViewModel
import com.example.clothingapp.ui.camera.CameraScreen
import com.example.clothingapp.ui.home.HomeScreen
import com.example.clothingapp.ui.wardrobe.WardrobeScreen
import com.example.clothingapp.ui.wardrobe.WardrobeViewModel
import com.example.clothingapp.ui.detail.ItemDetailScreen
import com.example.clothingapp.ui.detail.ItemDetailViewModel
import com.example.clothingapp.ui.edit.EditItemScreen
import com.example.clothingapp.ui.edit.EditItemViewModel
import com.example.clothingapp.ui.crop.CropScreen

@Composable
fun ClothingApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val database = remember { ClothingDatabase.getDatabase(context) }
    val repository = remember { ClothingRepository(database.clothingDao()) }
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { 
            HomeScreen(navController)
        }
        
        composable("wardrobe") { 
            val wardrobeViewModel: WardrobeViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return WardrobeViewModel(repository) as T
                    }
                }
            )
            WardrobeScreen(navController, wardrobeViewModel)
        }
        
        composable("camera") {
            CameraScreen(
                navController = navController,
                onImageCaptured = { /* Handled in CameraScreen */ }
            )
        }
        
        composable(
            "add_item/{imageUri}",
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val addItemViewModel: AddItemViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AddItemViewModel(repository) as T
                    }
                }
            )
            AddItemScreen(
                navController = navController,
                imageUri = imageUri,
                viewModel = addItemViewModel
            )
        }
        
        composable(
            "item_detail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            val detailViewModel: ItemDetailViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return ItemDetailViewModel(repository) as T
                    }
                }
            )
            ItemDetailScreen(
                navController = navController,
                viewModel = detailViewModel,
                itemId = itemId
            )
        }
        
        composable(
            "edit_item/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            val editViewModel: EditItemViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EditItemViewModel(repository) as T
                    }
                }
            )
            EditItemScreen(
                navController = navController,
                viewModel = editViewModel,
                itemId = itemId
            )
        }
        
        composable(
            "crop/{imageUri}",
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            CropScreen(
                navController = navController,
                imageUri = imageUri,
                onImageCropped = { croppedUri ->
                    navController.navigate("add_item/${Uri.encode(croppedUri)}") {
                        popUpTo("camera") { inclusive = false }
                    }
                }
            )
        }
    }
}