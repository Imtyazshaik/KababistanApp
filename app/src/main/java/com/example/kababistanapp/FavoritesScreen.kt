package com.example.kababistanapp

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kababistanapp.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(navController: NavController, cartViewModel: CartViewModel) {
    val favorites by cartViewModel.favorites.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()

    // Complete list of menu items from HomeScreen to filter against
    val allMenuItems = remember {
        listOf(
            MenuItem("Bolani", "7.99", R.drawable.boloni, "Appetizer"),
            MenuItem("Mantu(Beef/Chicken)", "13.99", R.drawable.mantu, "Appetizer"),
            MenuItem("Borani Banjan", "8.99", R.drawable.borani_banjan1, "Appetizer"),
            MenuItem("Beef Tikka kabab", "19.99", R.drawable.beef_takka, "Main Dishes"),
            MenuItem("Chicken Tikka kabab with Rice", "17.99", R.drawable.chicken_takka, "Main Dishes"),
            MenuItem("Lamb Tikka kabab", "18.99", R.drawable.beef_takka, "Main Dishes"),
            MenuItem("Qabuli Palaw With Baby Goat", "18.99", R.drawable.kabulipilau, "Main Dishes"),
            MenuItem("Shinnwari Karahi(Baby Goat)", "35.00", R.drawable.shinwari_karahi, "Main Dishes"),
            MenuItem("Shinnwari Karahi(Beef Tikka)", "38.99", R.drawable.shinwari_karahi, "Main Dishes"),
            MenuItem("Chapli kabab", "25.00", R.drawable.chapli_kabab, "Main Dishes"),
            MenuItem("Afghan Shami Chicken Kabab", "17.99", R.drawable.afghan_shami, "Main Dishes"),
            MenuItem("Afghan Burger", "13.99", R.drawable.afghan_burger, "Main Dishes"),
            MenuItem("Double Combo(Chicken/Beef)", "26.99", R.drawable.double_combo, "Combo Platter"),
            MenuItem("Triple Combo(Chicken/beef/Chapli)", "31.99", R.drawable.triple_combo, "Combo Platter"),
            MenuItem("Chicken Nuggets with Hand Cut Fries", "7.99", R.drawable.chicken_nuggetes, "Kids Meal"),
            MenuItem("Chocolate Ice Cream Cone", "3.49", R.drawable.chocolate_icecreem, "Dessert"),
            MenuItem("Vanilla Ice Cream", "3.49", R.drawable.vanilla_icecream, "Dessert"),
            MenuItem("Twist Ice Cream", "3.49", R.drawable.twist_icecream, "Dessert"),
            MenuItem("Shirpera", "4.99", R.drawable.sheerpera_img, "Dessert"),
            MenuItem("Afghan Cream Roll (3 pieces)", "6.99", R.drawable.afghan_cream, "Dessert"),
            MenuItem("Water", "1.99", R.drawable.water_bottle, "Drinks"),
            MenuItem("Sparkling Water", "2.99", R.drawable.sparkling_water, "Drinks"),
            MenuItem("Green/Black Tea (Cup)", "1.99", R.drawable.black_tea, "Drinks"),
            MenuItem("Green/Black Tea (Pot)", "3.99", R.drawable.green_tea, "Drinks"),
            MenuItem("Doogh(Yogurt Drink)", "3.49", R.drawable.curd_img, "Drinks")
        )
    }

    val favoriteItems = allMenuItems.filter { it.name in favorites }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Favorites", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = LightBlueUI)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        if (favoriteItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FavoriteBorder, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No favorites yet", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favoriteItems) { item ->
                    val currentItem = cartItems.find { it.name == item.name }
                    val quantity = currentItem?.quantity ?: 0
                    
                    FavoriteFoodItemCard(
                        item = item,
                        quantity = quantity,
                        onToggleFavorite = { cartViewModel.toggleFavorite(item.name) },
                        onAddToCart = { cartViewModel.addToCart(item.name, item.price, item.imageRes) },
                        onRemoveFromCart = { currentItem?.let { cartViewModel.decreaseQuantity(it) } },
                        onClick = {
                            val encodedName = Uri.encode(item.name)
                            navController.navigate("food_detail/$encodedName/${item.price}/${item.imageRes}")
                        }
                    )
                }
            }
        }
    }
}
