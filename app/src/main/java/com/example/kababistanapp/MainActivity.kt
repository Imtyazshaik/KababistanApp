package com.example.kababistanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kababistanapp.ui.theme.KababistanAppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KababistanAppTheme {
                RestaurantApp()
            }
        }
    }
}

@Composable
fun RestaurantApp() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    
    // Start with home if logged in, otherwise login
    val currentUser = Firebase.auth.currentUser
    val startDestination = if (currentUser != null) "home" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("signup") {
            SignUpScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController, cartViewModel = cartViewModel)
        }
        composable(
            "food_detail/{name}/{price}/{imageRes}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("price") { type = NavType.StringType },
                navArgument("imageRes") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val price = backStackEntry.arguments?.getString("price") ?: ""
            val imageRes = backStackEntry.arguments?.getInt("imageRes") ?: 0
            FoodDetailScreen(
                navController = navController,
                cartViewModel = cartViewModel,
                itemName = name,
                itemPrice = price,
                imageRes = imageRes
            )
        }
        composable("cart") {
            CartScreen(navController = navController, cartViewModel = cartViewModel)
        }
        composable("table_reservation") {
            TableReservationScreen(navController = navController, cartViewModel = cartViewModel)
        }
        composable("order_confirmation") {
            CheckoutScreen(navController = navController, cartViewModel = cartViewModel)
        }
        composable("profile") {
            ProfileScreen(navController = navController, cartViewModel = cartViewModel)
        }
        composable("orders") {
            OrdersScreen(navController = navController, cartViewModel = cartViewModel)
        }
        composable("help") {
            HelpScreen(navController = navController, cartViewModel = cartViewModel)
        }
    }
}
