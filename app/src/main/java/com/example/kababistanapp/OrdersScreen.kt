package com.example.kababistanapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kababistanapp.ui.theme.PrimaryColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(navController: NavController, cartViewModel: CartViewModel) {
    val previousOrders by cartViewModel.previousOrders.collectAsState()
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Orders & Reservations", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = "orders")
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        if (previousOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No history yet", color = Color.Gray, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate("home") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Explore Menu")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(previousOrders) { order ->
                    ReservationRecordCard(order = order, onClick = { selectedOrder = order })
                }
            }
        }
    }

    if (selectedOrder != null) {
        ReservationDetailsDialog(order = selectedOrder!!, onDismiss = { selectedOrder = null }, cartViewModel = cartViewModel)
    }
}

@Composable
fun ReservationRecordCard(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = order.id, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(
                            imageVector = if (order.type == "Reservation") Icons.Default.DateRange else Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(text = "${order.type}: ${order.date} • ${order.time}", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                ReservationStatusBadge(order.status)
            }
            
            if (order.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Items:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(order.items) { item ->
                        AsyncImage(
                            model = item.imageRes,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.img_burger_deal),
                            fallback = painterResource(R.drawable.img_burger_deal)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View All Details",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (order.total > 0) {
                    Text(
                        text = "Total: $${String.format(Locale.US, "%.2f", order.total)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ReservationStatusBadge(status: String) {
    val containerColor = when {
        status.contains("New") -> Color(0xFFE3F2FD)
        status == "Completed" || status == "Delivered" || status == "Picked up" -> Color(0xFFE8F5E9)
        status == "Cancelled" -> Color(0xFFFFEBEE)
        else -> Color(0xFFF5F5F5)
    }
    val contentColor = when {
        status.contains("New") -> Color(0xFF1976D2)
        status == "Completed" || status == "Delivered" || status == "Picked up" -> Color(0xFF2E7D32)
        status == "Cancelled" -> Color(0xFFD32F2F)
        else -> Color(0xFF757575)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ReservationDetailsDialog(order: Order, onDismiss: () -> Unit, cartViewModel: CartViewModel) {
    val orderDateFormat = remember { SimpleDateFormat("dd MMM, yyyy 'at' hh:mm a", Locale.US) }
    val orderDateFormatted = remember(order.timestamp) { 
        orderDateFormat.format(Date(order.timestamp)) 
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                    Text(
                        text = "Order Details",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val icon = when {
                        order.status == "Completed" || order.status == "Delivered" || order.status == "Picked up" -> Icons.Default.CheckCircle
                        order.status == "Cancelled" -> Icons.Default.Close
                        else -> Icons.Default.Info
                    }
                    val color = when {
                        order.status == "Completed" || order.status == "Delivered" || order.status == "Picked up" -> Color(0xFF4CAF50)
                        order.status == "Cancelled" -> Color.Red
                        else -> PrimaryColor
                    }
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = order.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = color
                    )
                    Text(text = "Order ID: ${order.id}", color = Color.Gray, fontSize = 14.sp)
                    Text(text = "Placed on: $orderDateFormatted", color = Color.Gray, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(text = "Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow("Order Type", order.type)
                        DetailRow("Scheduled Date", order.date)
                        DetailRow("Scheduled Time", order.time)
                        
                        if (order.type == "Reservation" && order.numberOfPeople.isNotEmpty()) {
                            DetailRow("Number of Guests", order.numberOfPeople)
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                        
                        DetailRow("Customer Name", order.customerName)
                        DetailRow("Contact Number", order.customerPhone)
                        DetailRow("Email", order.customerEmail)
                        
                        if (order.specialInstructions.isNotEmpty()) {
                            DetailRow("Special Notes", order.specialInstructions)
                        }
                        
                        if (order.paymentMethod.isNotEmpty()) {
                            DetailRow("Payment Method", order.paymentMethod)
                        }
                    }
                }

                if (order.items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "Ordered Items", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = item.imageRes,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.img_burger_deal),
                                fallback = painterResource(R.drawable.img_burger_deal)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.name, fontWeight = FontWeight.Bold)
                                Text(text = "Qty: ${item.quantity}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", item.price * item.quantity)}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total Amount", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", order.total)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = PrimaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                if (order.status.contains("New")) {
                    OutlinedButton(
                        onClick = { 
                            cartViewModel.cancelOrder(order.id)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red)
                    ) {
                        Text("Cancel Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(text = value, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.weight(1.5f))
    }
}
