package com.example.kababistanapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kababistanapp.ui.theme.PrimaryColor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavController, cartViewModel: CartViewModel = viewModel()) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val orderType by cartViewModel.selectedOrderType.collectAsState()
    val date by cartViewModel.selectedDate.collectAsState()
    val time by cartViewModel.selectedTime.collectAsState()
    
    val customerName by cartViewModel.customerName.collectAsState()
    val customerPhone by cartViewModel.customerPhone.collectAsState()
    val customerEmail by cartViewModel.customerEmail.collectAsState()
    val specialInstructions by cartViewModel.specialInstructions.collectAsState()
    val peopleCount by cartViewModel.numberOfPeople.collectAsState()
    val paymentMethod by cartViewModel.selectedPaymentMethod.collectAsState()

    val totalAmount = cartViewModel.total
    val subtotal = cartViewModel.subtotal
    val discount = cartViewModel.discountAmount
    val tax = cartViewModel.taxAmount

    val isReservation = orderType == "Reservation"

    Scaffold(
        topBar = {
            CheckoutTopBar(
                navController = navController, 
                title = if (isReservation) "Reservation Confirmed" else "Pickup Confirmed"
            )
        },
        containerColor = Color(0xFFF8F9FA),
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Header Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White, Color(0xFFF8F9FA))
                            )
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(100.dp).shadow(12.dp, CircleShape),
                            shape = CircleShape,
                            color = Color(0xFF4CAF50)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = if (isReservation) "Table Reserved!" else "Pickup Order Confirmed!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2E7D32)
                            ),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (isReservation) 
                                "Your table for $peopleCount guests is booked for $date at $time." 
                                else "Your order is scheduled for pickup on $date at $time.",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // Adaptable Details Card
                    ModernBookingDetailsCard(
                        date = date, 
                        time = time, 
                        name = customerName, 
                        phone = customerPhone, 
                        email = customerEmail,
                        instructions = specialInstructions,
                        people = peopleCount,
                        paymentMethod = if (isReservation) null else paymentMethod,
                        isReservation = isReservation
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (cartItems.isNotEmpty()) {
                        Text(
                            text = if (isReservation) "Pre-ordered Food" else "Ordered Items", 
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OrderItemsSection(cartItems)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CheckoutSummarySection(
                        subtotal = subtotal,
                        discount = discount,
                        tax = tax,
                        total = totalAmount
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // Action Buttons
                    PrimaryButton(
                        text = "Back to Home",
                        onClick = { 
                            cartViewModel.clearCart()
                            navController.navigate("home") { popUpTo(0) } 
                        },
                        containerColor = Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { 
                            cartViewModel.cancelOrder()
                            cartViewModel.clearCart()
                            navController.navigate("home") { popUpTo(0) }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isReservation) "Cancel Booking" else "Cancel Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, containerColor: Color = PrimaryColor) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp).shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutTopBar(navController: NavController, title: String) {
    CenterAlignedTopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun ModernBookingDetailsCard(
    date: String, 
    time: String, 
    name: String, 
    phone: String, 
    email: String,
    instructions: String,
    people: String, 
    paymentMethod: String?, 
    isReservation: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isReservation) "Reservation Summary" else "Pickup Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            HorizontalDivider(color = Color(0xFFF5F5F5))

            DetailRowItem(Icons.Default.Event, "Scheduled Date", date)
            DetailRowItem(Icons.Default.Schedule, "Scheduled Time", time)
            
            if (isReservation) DetailRowItem(Icons.Default.Person, "Guests", "$people People")
            
            DetailRowItem(Icons.Default.Person, "Customer Name", name)
            DetailRowItem(Icons.Default.Phone, "Contact Number", phone)
            DetailRowItem(Icons.Default.Email, "Email Address", email)
            
            if (instructions.isNotEmpty()) {
                DetailRowItem(Icons.Default.Info, "Special Notes", instructions)
            }
            
            if (paymentMethod != null) {
                DetailRowItem(Icons.Default.ShoppingCart, "Payment Method", paymentMethod)
            }
            DetailRowItem(Icons.Default.ShoppingCart, "Branch", "Downtown Kababistan")
        }
    }
}

@Composable
fun DetailRowItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(36.dp).background(PrimaryColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun OrderItemsSection(items: List<CartItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            items.forEachIndexed { index, item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.name} x${item.quantity}", color = Color.DarkGray, fontWeight = FontWeight.Medium)
                    // Use priceDouble from CartItem in Models.kt to resolve type ambiguity
                    Text(String.format(Locale.US, "$%.2f", item.priceDouble * item.quantity), fontWeight = FontWeight.Bold)
                }
                if (index < items.size - 1) HorizontalDivider(color = Color(0xFFF9F9F9))
            }
        }
    }
}

@Composable
fun CheckoutSummarySection(subtotal: Double, discount: Double, tax: Double, total: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SummaryRowLine("Subtotal", String.format(Locale.US, "$%.2f", subtotal))
            if (discount > 0) {
                SummaryRowLine("Discount", String.format(Locale.US, "-$%.2f", discount), valueColor = Color(0xFF2E7D32))
            }
            SummaryRowLine("Tax (18%)", String.format(Locale.US, "$%.2f", tax))
            
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color(0xFFF5F5F5))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Amount", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(String.format(Locale.US, "$%.2f", total), fontWeight = FontWeight.ExtraBold, color = PrimaryColor, fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun SummaryRowLine(label: String, value: String, valueColor: Color = Color.Black) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
