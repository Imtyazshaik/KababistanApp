package com.example.kababistanadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// Simple theme definition
@Composable
fun KababistanAdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFFFF9800),
            secondary = androidx.compose.ui.graphics.Color(0xFFE65100)
        ),
        content = content
    )
}

class AdminMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KababistanAdminTheme {
                AdminAppNavigation()
            }
        }
    }
}

@Composable
fun AdminAppNavigation() {
    val navController = rememberNavController()
    
    // As per user request: "I dont need any login here It should open dashboard directly"
    val startDestination = "admin_home"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("admin_login") {
            AdminLoginScreen(navController = navController)
        }
        composable("admin_home") {
            AdminManagerScreen(navController = navController)
        }
    }
}
