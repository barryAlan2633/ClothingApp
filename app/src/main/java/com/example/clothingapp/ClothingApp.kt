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
import com.example.clothingapp.data.ClothingDao
import com.example.clothingapp.data.ClothingItem
import com.example.clothingapp.data.ClothingCategory
import com.example.clothingapp.data.FabricType
import com.example.clothingapp.data.ClothingStyle
import com.example.clothingapp.data.DressCode
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
import com.example.clothingapp.ui.favorites.FavoritesScreen
import com.example.clothingapp.ui.outfits.OutfitCreatorScreen
import com.example.clothingapp.ui.outfits.OutfitCreatorViewModel
import com.example.clothingapp.ui.outfits.OutfitsScreen
import com.example.clothingapp.ui.outfits.OutfitsViewModel
import com.example.clothingapp.ui.outfits.OutfitDetailScreen
import com.example.clothingapp.ui.outfits.OutfitDetailViewModel
import kotlinx.coroutines.flow.first

@Composable
fun ClothingApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val database = remember { ClothingDatabase.getDatabase(context) }
    val repository = remember { ClothingRepository(database.clothingDao()) }
    
    // Add test data on first launch
    LaunchedEffect(Unit) {
        addTestDataIfNeeded(database.clothingDao())
    }
    
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
        
        composable("favorites") {
            val wardrobeViewModel: WardrobeViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return WardrobeViewModel(repository) as T
                    }
                }
            )
            val outfitsViewModel: OutfitsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return OutfitsViewModel(database.clothingDao(), database.outfitDao()) as T
                    }
                }
            )
            FavoritesScreen(navController, wardrobeViewModel, outfitsViewModel)
        }
        
        composable("outfit_creator") {
            val outfitCreatorViewModel: OutfitCreatorViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return OutfitCreatorViewModel(database.clothingDao(), database.outfitDao()) as T
                    }
                }
            )
            OutfitCreatorScreen(navController, outfitCreatorViewModel)
        }
        
        composable("outfits") {
            val outfitsViewModel: OutfitsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return OutfitsViewModel(database.clothingDao(), database.outfitDao()) as T
                    }
                }
            )
            OutfitsScreen(navController, outfitsViewModel)
        }
        
        composable(
            "outfit_detail/{outfitId}",
            arguments = listOf(navArgument("outfitId") { type = NavType.IntType })
        ) { backStackEntry ->
            val outfitId = backStackEntry.arguments?.getInt("outfitId") ?: 0
            val outfitDetailViewModel: OutfitDetailViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return OutfitDetailViewModel(database.clothingDao(), database.outfitDao()) as T
                    }
                }
            )
            OutfitDetailScreen(
                navController = navController,
                viewModel = outfitDetailViewModel,
                outfitId = outfitId
            )
        }
    }
}

private suspend fun addTestDataIfNeeded(dao: ClothingDao) {
    // Check if we already have items to avoid duplicates
    val existingItems = dao.getAllItems().first()
    if (existingItems.isEmpty()) {
        // Add test data
        val testItems = listOf(
            // HATS
            ClothingItem(
                name = "Black Beanie",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_beanie",
                categories = listOf(ClothingCategory.BEANIE),
                color = "Black",
                fabricType = FabricType.WOOL,
                size = "One Size",
                style = ClothingStyle.CASUAL,
                dressCodes = listOf(DressCode.CASUAL)
            ),
            ClothingItem(
                name = "Baseball Cap",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_cap",
                categories = listOf(ClothingCategory.CAP),
                color = "Navy",
                fabricType = FabricType.COTTON,
                size = "One Size",
                style = ClothingStyle.SPORTY,
                dressCodes = listOf(DressCode.CASUAL, DressCode.ATHLETIC)
            ),
            ClothingItem(
                name = "Winter Hat",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_winter_hat",
                categories = listOf(ClothingCategory.HAT),
                color = "Gray",
                fabricType = FabricType.WOOL,
                size = "One Size",
                style = ClothingStyle.CASUAL,
                dressCodes = listOf(DressCode.CASUAL)
            ),
            
            // TOPS
            ClothingItem(
                name = "White Undershirt",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_undershirt",
                categories = listOf(ClothingCategory.UNDERSHIRT),
                color = "White",
                fabricType = FabricType.COTTON,
                size = "L",
                style = ClothingStyle.CASUAL,
                dressCodes = listOf(DressCode.CASUAL, DressCode.BUSINESS_CASUAL, DressCode.BUSINESS_FORMAL)
            ),
            ClothingItem(
                name = "Blue Button-Up Shirt",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_shirt",
                categories = listOf(ClothingCategory.SHIRT),
                color = "Blue",
                fabricType = FabricType.COTTON,
                size = "L",
                style = ClothingStyle.BUSINESS,
                dressCodes = listOf(DressCode.BUSINESS_CASUAL, DressCode.BUSINESS_FORMAL)
            ),
            ClothingItem(
                name = "Gray Sweater",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_sweater",
                categories = listOf(ClothingCategory.SWEATER),
                color = "Gray",
                fabricType = FabricType.WOOL,
                size = "L",
                style = ClothingStyle.CASUAL,
                dressCodes = listOf(DressCode.CASUAL, DressCode.BUSINESS_CASUAL)
            ),
            
            // BOTTOMS
            ClothingItem(
                name = "White Boxer Briefs",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_underwear",
                categories = listOf(ClothingCategory.UNDERWEAR),
                color = "White",
                fabricType = FabricType.COTTON,
                size = "L",
                style = ClothingStyle.CASUAL,
                dressCodes = listOf(DressCode.CASUAL, DressCode.BUSINESS_CASUAL, DressCode.BUSINESS_FORMAL, DressCode.ATHLETIC)
            ),
            ClothingItem(
                name = "Dark Blue Jeans",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_jeans",
                categories = listOf(ClothingCategory.JEANS),
                color = "Dark Blue",
                fabricType = FabricType.DENIM,
                size = "32x32",
                style = ClothingStyle.CASUAL,
                dressCodes = listOf(DressCode.CASUAL, DressCode.BUSINESS_CASUAL)
            ),
            ClothingItem(
                name = "Black Dress Pants",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_dress_pants",
                categories = listOf(ClothingCategory.PANTS),
                color = "Black",
                fabricType = FabricType.WOOL,
                size = "32x32",
                style = ClothingStyle.BUSINESS,
                dressCodes = listOf(DressCode.BUSINESS_CASUAL, DressCode.BUSINESS_FORMAL)
            ),
            
            // FOOTWEAR
            ClothingItem(
                name = "White Crew Socks",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_socks",
                categories = listOf(ClothingCategory.SOCKS),
                color = "White",
                fabricType = FabricType.COTTON,
                size = "L",
                style = ClothingStyle.CASUAL,
                dressCodes = listOf(DressCode.CASUAL, DressCode.ATHLETIC)
            ),
            ClothingItem(
                name = "Black Dress Shoes",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_dress_shoes",
                categories = listOf(ClothingCategory.DRESS_SHOES),
                color = "Black",
                fabricType = FabricType.LEATHER,
                size = "10",
                style = ClothingStyle.BUSINESS,
                dressCodes = listOf(DressCode.BUSINESS_CASUAL, DressCode.BUSINESS_FORMAL)
            ),
            ClothingItem(
                name = "White Sneakers",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_sneakers",
                categories = listOf(ClothingCategory.SNEAKERS),
                color = "White",
                fabricType = FabricType.SYNTHETIC,
                size = "10",
                style = ClothingStyle.SPORTY,
                dressCodes = listOf(DressCode.CASUAL, DressCode.ATHLETIC)
            ),
            
            // JEWELRY
            ClothingItem(
                name = "Silver Watch",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_watch",
                categories = listOf(ClothingCategory.WATCH),
                color = "Silver",
                fabricType = FabricType.OTHER,
                size = "One Size",
                style = ClothingStyle.BUSINESS,
                dressCodes = listOf(DressCode.BUSINESS_CASUAL, DressCode.BUSINESS_FORMAL)
            ),
            ClothingItem(
                name = "Gold Wedding Ring",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_ring",
                categories = listOf(ClothingCategory.RING),
                color = "Gold",
                fabricType = FabricType.OTHER,
                size = "9",
                style = ClothingStyle.FORMAL,
                dressCodes = listOf(DressCode.CASUAL, DressCode.BUSINESS_CASUAL, DressCode.BUSINESS_FORMAL)
            ),
            ClothingItem(
                name = "Silver Chain Necklace",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_necklace",
                categories = listOf(ClothingCategory.NECKLACE),
                color = "Silver",
                fabricType = FabricType.OTHER,
                size = "One Size",
                style = ClothingStyle.CASUAL,
                dressCodes = listOf(DressCode.CASUAL)
            ),
            
            // ACCESSORIES
            ClothingItem(
                name = "Black Leather Belt",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_belt",
                categories = listOf(ClothingCategory.BELT),
                color = "Black",
                fabricType = FabricType.LEATHER,
                size = "34",
                style = ClothingStyle.FORMAL,
                dressCodes = listOf(DressCode.BUSINESS_CASUAL, DressCode.BUSINESS_FORMAL)
            ),
            ClothingItem(
                name = "Brown Leather Wallet",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_wallet",
                categories = listOf(ClothingCategory.WALLET),
                color = "Brown",
                fabricType = FabricType.LEATHER,
                size = "One Size",
                style = ClothingStyle.FORMAL,
                dressCodes = listOf(DressCode.CASUAL, DressCode.BUSINESS_CASUAL, DressCode.BUSINESS_FORMAL)
            ),
            ClothingItem(
                name = "Black Sunglasses",
                imageUri = "android.resource://com.example.clothingapp/drawable/test_sunglasses",
                categories = listOf(ClothingCategory.SUNGLASSES),
                color = "Black",
                fabricType = FabricType.SYNTHETIC,
                size = "One Size",
                style = ClothingStyle.CASUAL,
                dressCodes = listOf(DressCode.CASUAL, DressCode.ATHLETIC)
            )
        )
        
        testItems.forEach { item ->
            dao.insertItem(item)
        }
    }
}