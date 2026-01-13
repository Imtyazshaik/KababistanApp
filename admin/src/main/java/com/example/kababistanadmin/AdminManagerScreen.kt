package com.example.kababistanadmin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManagerScreen(navController: NavController, adminViewModel: AdminViewModel = viewModel()) {
    val allOrders by adminViewModel.allOrders.collectAsState()
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedMode by remember { mutableIntStateOf(0) } // 0: All, 1: Orders, 2: Reservations
    
    val tabs = listOf("New", "Active", "History")
    val modes = listOf("All", "Orders", "Reservations")
    
    val modeFilteredOrders = when (selectedMode) {
        1 -> allOrders.filter { it.type == "Pick up" || it.type == "Delivery" }
        2 -> allOrders.filter { it.type == "Reservation" }
        else -> allOrders
    }

    val filteredOrders = when (selectedTab) {
        0 -> modeFilteredOrders.filter { 
            it.status.contains("New", ignoreCase = true) || it.status.contains("pending", ignoreCase = true) 
        }
        1 -> modeFilteredOrders.filter { it.status != "Completed" && it.status != "Cancelled" && it.status != "Delivered" && it.status != "Picked up" && !it.status.contains("New", ignoreCase = true) && !it.status.contains("pending", ignoreCase = true) }
        else -> modeFilteredOrders.filter { it.status == "Completed" || it.status == "Cancelled" || it.status == "Delivered" || it.status == "Picked up" }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("Kababistan Manager", fontWeight = FontWeight.Bold)
                        Text("Admin Dashboard", fontSize = 12.sp, fontWeight = FontWeight.Normal)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        Firebase.auth.signOut()
                        navController.navigate("admin_login") {
                            popUpTo(0)
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Service Type Selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modes.forEachIndexed { index, name ->
                    FilterChip(
                        selected = selectedMode == index,
                        onClick = { selectedMode = index },
                        label = { Text(name) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Summary Dashboard
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Total", modeFilteredOrders.size.toString(), Icons.Default.Receipt, Color(0xFF673AB7), Modifier.weight(1f))
                StatCard("New", modeFilteredOrders.count { it.status.contains("New", ignoreCase = true) || it.status.contains("pending", ignoreCase = true) }.toString(), Icons.Default.Pending, Color(0xFFFF9800), Modifier.weight(1f))
                StatCard("Revenue", "$${String.format(Locale.US, "%.0f", modeFilteredOrders.filter { it.status != "Cancelled" }.sumOf { it.total })}", Icons.Default.Payments, Color(0xFF4CAF50), Modifier.weight(1f))
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = PrimaryColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryColor
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No ${tabs[selectedTab].lowercase()} ${modes[selectedMode].lowercase()}", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders) { order ->
                        AdminOrderCard(order = order, onClick = { selectedOrder = order })
                    }
                }
            }
        }
    }

    if (selectedOrder != null) {
        AdminOrderDetailsDialog(
            order = selectedOrder!!,
            onDismiss = { selectedOrder = null },
            adminViewModel = adminViewModel
        )
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AdminOrderCard(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(
                    if (order.type == "Reservation") Color(0xFFE8F5E9) else PrimaryColor.copy(alpha = 0.1f), 
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (order.type == "Reservation") Icons.Default.EventSeat else Icons.Default.Fastfood,
                    contentDescription = null,
                    tint = if (order.type == "Reservation") Color(0xFF2E7D32) else PrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = order.customerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${order.type} • ${order.date} • ${order.time}", fontSize = 12.sp, color = Color.Gray)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "$${String.format(Locale.US, "%.2f", order.total)}", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                ReservationStatusBadge(order.status)
            }
        }
    }
}

@Composable
fun ReservationStatusBadge(status: String) {
    val (containerColor, contentColor) = when {
        status.contains("New", ignoreCase = true) || status.contains("pending", ignoreCase = true) -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        status == "Completed" || status == "Delivered" || status == "Picked up" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        status == "Cancelled" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        else -> Color(0xFFFFF3E0) to Color(0xFFE65100)
    }

    Surface(color = containerColor, shape = RoundedCornerShape(6.dp)) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AdminOrderDetailsDialog(order: Order, onDismiss: () -> Unit, adminViewModel: AdminViewModel) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(top = 40.dp),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                    Text("Details", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = { adminViewModel.deleteOrder(order.id); onDismiss() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                DetailSection("Order Info") {
                    DetailRow("Type", order.type)
                    DetailRow("Date", order.date)
                    DetailRow("Time", order.time)
                    if (order.type == "Reservation") {
                        DetailRow("Guests", order.numberOfPeople)
                    }
                    DetailRow("Status", order.status)
                }

                Spacer(modifier = Modifier.height(16.dp))

                DetailSection("Customer Info") {
                    DetailRow("Name", order.customerName)
                    DetailRow("Phone", order.customerPhone)
                    DetailRow("Email", order.customerEmail)
                    if (order.customerAddress.isNotEmpty()) DetailRow("Address", order.customerAddress)
                    if (order.specialInstructions.isNotEmpty()) DetailRow("Special Notes", order.specialInstructions)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Update Status", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val statuses = when(order.type) {
                    "Delivery" -> listOf("New Delivery", "Preparing", "Out for Delivery", "Delivered", "Cancelled")
                    "Pick up" -> listOf("New Pick up", "Preparing", "Ready for Pick up", "Picked up", "Cancelled")
                    else -> listOf("New Reservation", "Confirmed", "Completed", "Cancelled")
                }
                
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    statuses.forEach { status ->
                        FilterChip(
                            selected = order.status == status,
                            onClick = { adminViewModel.updateOrderStatus(order.id, status) },
                            label = { Text(status) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryColor, selectedLabelColor = Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (order.items.isNotEmpty()) {
                    Text("Items", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    order.items.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                                tint = PrimaryColor
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold)
                                Text("Qty: ${item.quantity}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text("$${String.format(Locale.US, "%.2f", item.price * item.quantity)}", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("$${String.format(Locale.US, "%.2f", order.total)}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryColor)
                    }
                } else if (order.type == "Reservation") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        Text("No items pre-ordered", color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                    Text("Close Panel")
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))) {
            Column(modifier = Modifier.padding(16.dp)) { content() }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
    }
}
