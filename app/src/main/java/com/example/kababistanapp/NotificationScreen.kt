package com.example.kababistanapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kababistanapp.ui.theme.PrimaryColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController, cartViewModel: CartViewModel) {
    val previousOrders by cartViewModel.previousOrders.collectAsState()
    
    // Convert orders into notification items
    val notifications = remember(previousOrders) {
        previousOrders.map { order ->
            val (title, message, icon, color) = when (order.status.lowercase()) {
                "pending" -> listOf(
                    "Order Received",
                    "Your order ${order.id} is pending confirmation from Kababistan.",
                    Icons.Default.Info,
                    Color(0xFF1976D2)
                )
                "accepted" -> listOf(
                    "Order Confirmed ✅",
                    "Great news! Your order ${order.id} has been accepted and is now being prepared.",
                    Icons.Default.CheckCircle,
                    Color(0xFF2E7D32)
                )
                "ready" -> listOf(
                    "Ready to Pick Up",
                    "Your delicious meal ${order.id} is ready! Please head to the counter.",
                    Icons.Default.CheckCircle,
                    Color(0xFF2E7D32)
                )
                "completed" -> listOf(
                    "Order Picked Up",
                    "Thank you for ordering from Kababistan ❤️. Enjoy your meal!",
                    Icons.Default.CheckCircle,
                    Color(0xFF2E7D32)
                )
                "rejected", "cancelled" -> listOf(
                    "Order Update",
                    "Your order ${order.id} was rejected or cancelled. Tap for help.",
                    Icons.Default.Warning,
                    Color.Red
                )
                else -> listOf(
                    "Status Update",
                    "Status of order ${order.id}: ${order.status}",
                    Icons.Default.Notifications,
                    PrimaryColor
                )
            }
            
            NotificationItem(
                id = order.id,
                title = title as String,
                message = message as String,
                time = order.timestamp,
                icon = icon as androidx.compose.ui.graphics.vector.ImageVector,
                color = color as Color,
                orderId = order.id
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = LightBlueUI
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No notifications yet", color = Color.Gray, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(notification) {
                        navController.navigate("orders")
                    }
                }
            }
        }
    }
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: Long,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val orderId: String
)

@Composable
fun NotificationCard(notification: NotificationItem, onClick: () -> Unit) {
    val timeFormat = remember { SimpleDateFormat("h:mm a, dd MMM", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(notification.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(notification.icon, contentDescription = null, tint = notification.color, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = notification.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = notification.message, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = timeFormat.format(Date(notification.time)),
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
