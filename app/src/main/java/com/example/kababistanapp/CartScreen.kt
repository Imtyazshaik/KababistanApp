package com.example.kababistanapp

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kababistanapp.ui.theme.PrimaryColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController, cartViewModel: CartViewModel = viewModel()) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val cartItems by cartViewModel.cartItems.collectAsState()
    val selectedOrderType by cartViewModel.selectedOrderType.collectAsState()
    
    val resDateText by cartViewModel.selectedDate.collectAsState()
    val resTimeText by cartViewModel.selectedTime.collectAsState()
    
    val customerName by cartViewModel.customerName.collectAsState()
    val customerPhone by cartViewModel.customerPhone.collectAsState()
    val customerEmail by cartViewModel.customerEmail.collectAsState()
    val specialInstructions by cartViewModel.specialInstructions.collectAsState()
    
    val selectedPaymentMethod by cartViewModel.selectedPaymentMethod.collectAsState()
    val cardNumber by cartViewModel.cardNumber.collectAsState()
    val cardExpiry by cartViewModel.cardExpiry.collectAsState()
    val cardCvv by cartViewModel.cardCvv.collectAsState()

    val appliedVoucherCode by cartViewModel.appliedVoucherCode.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(is24Hour = false)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Date(it)
                        val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.US)
                        cartViewModel.selectedDate.value = formatter.format(date)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Pick a Time", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(24.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            val hour = if (timePickerState.hour % 12 == 0) 12 else timePickerState.hour % 12
                            val amPm = if (timePickerState.hour < 12) "AM" else "PM"
                            cartViewModel.selectedTime.value = String.format(Locale.US, "%02d:%02d %s", hour, timePickerState.minute, amPm)
                            showTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = { CartTopBar(navController = navController, title = "My Cart") },
        containerColor = Color(0xFFF8F9FA),
        content = { paddingValues ->
            if (cartItems.isEmpty()) {
                EmptyCartView(paddingValues, navController)
            } else {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(PrimaryColor, PrimaryColor.copy(alpha = 0.8f))
                                    )
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text(
                                    "Review Your Order",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Text(
                                    "${cartItems.size} items selected",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            // Cart Items Section
                            Text(
                                text = "Selected Items",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            CartItemsSection(cartItems, cartViewModel)

                            Spacer(modifier = Modifier.height(24.dp))

                            // Order Type Selection
                            Text(
                                text = "Choose Service",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            ModernOrderTypeSelector(
                                selectedType = selectedOrderType,
                                onTypeSelected = { cartViewModel.selectedOrderType.value = it }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (selectedOrderType == "Pick up") {
                                UniqueDateTimePicker(
                                    date = resDateText,
                                    time = resTimeText,
                                    onDateClick = { showDatePicker = true },
                                    onTimeClick = { showTimePicker = true }
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                CustomerInfoCard(
                                    customerName = customerName,
                                    customerPhone = customerPhone,
                                    customerEmail = customerEmail,
                                    specialInstructions = specialInstructions,
                                    onNameChange = { cartViewModel.customerName.value = it },
                                    onPhoneChange = { cartViewModel.customerPhone.value = it },
                                    onEmailChange = { cartViewModel.customerEmail.value = it },
                                    onInstructionsChange = { cartViewModel.specialInstructions.value = it }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                PromoCodeSection(
                                    appliedCode = appliedVoucherCode,
                                    onApply = { cartViewModel.applyVoucher(it) },
                                    onRemove = { cartViewModel.removeVoucher() }
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                PaymentMethodCard(
                                    selectedMethod = selectedPaymentMethod,
                                    onMethodSelected = { cartViewModel.selectedPaymentMethod.value = it },
                                    cardNumber = cardNumber,
                                    onCardNumberChange = { cartViewModel.cardNumber.value = it },
                                    cardExpiry = cardExpiry,
                                    onCardExpiryChange = { cartViewModel.cardExpiry.value = it },
                                    cardCvv = cardCvv,
                                    onCardCvvChange = { cartViewModel.cardCvv.value = it }
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                OrderSummarySection(
                                    subtotal = cartViewModel.subtotal,
                                    discountAmount = cartViewModel.discountAmount,
                                    taxAmount = cartViewModel.taxAmount,
                                    total = cartViewModel.total
                                )
                            } else {
                                DoorDashDeliveryCard {
                                    uriHandler.openUri("https://www.doordash.com/store/kababistan-plano-36320377/80912392/?srsltid=AfmBOoqDmnOm5yvpdzSGPCg1nWUKlaEiX6Z9bb2HsgtppFZY8Tu3Xx8t")
                                }
                            }

                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (cartItems.isNotEmpty() && selectedOrderType == "Pick up") {
                Surface(
                    modifier = Modifier.shadow(16.dp),
                    color = Color.White
                ) {
                    Button(
                        onClick = {
                            if (resDateText == "Select Date" || resTimeText == "Select Time" || customerName.isBlank() || customerPhone.isBlank()) {
                                Toast.makeText(context, "Please fill required details", Toast.LENGTH_SHORT).show()
                            } else if (selectedPaymentMethod == "Credit/Debit Card" && (cardNumber.length < 16 || cardExpiry.isBlank() || cardCvv.isBlank())) {
                                Toast.makeText(context, "Please enter valid card details", Toast.LENGTH_SHORT).show()
                            } else {
                                cartViewModel.confirmOrder()
                                navController.navigate("order_confirmation")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text("Place Pickup Order", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    )
}

@Composable
fun PromoCodeSection(appliedCode: String?, onApply: (String) -> Unit, onRemove: () -> Unit) {
    var codeText by remember { mutableStateOf("") }
    
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CardGiftcard, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Promo Code", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (appliedCode == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = codeText,
                        onValueChange = { codeText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter Code (e.g. WELCOME30)") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = Color(0xFFEEEEEE)
                        )
                    )
                    Button(
                        onClick = { 
                            if (codeText.isNotBlank()) {
                                onApply(codeText)
                                codeText = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        Text("Apply", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Code Applied: $appliedCode", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartView(paddingValues: PaddingValues, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.kababistan_logo),
            contentDescription = "Empty Cart",
            modifier = Modifier.size(200.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Your Cart is Empty",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Looks like you haven't added anything to your cart yet. Browse our menu and select your favorite items!",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { navController.navigate("home") },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Browse Menu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun ModernOrderTypeSelector(selectedType: String, onTypeSelected: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(6.dp)) {
            listOf("Pick up", "Delivery").forEach { type ->
                val isSelected = selectedType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) PrimaryColor else Color.Transparent)
                        .clickable { onTypeSelected(type) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (type == "Pick up") Icons.Default.Storefront else Icons.Default.DirectionsCar,
                            null,
                            tint = if (isSelected) Color.White else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = type,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UniqueDateTimePicker(
    date: String,
    time: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Schedule Pickup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF4F1))
                        .clickable { onDateClick() }
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("DATE", fontSize = 10.sp, color = PrimaryColor, fontWeight = FontWeight.Bold)
                    Text(date, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF4F1))
                        .clickable { onTimeClick() }
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = PrimaryColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("TIME", fontSize = 10.sp, color = PrimaryColor, fontWeight = FontWeight.Bold)
                    Text(time, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun DoorDashDeliveryCard(onDoorDashClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_delivery_time),
                contentDescription = "Delivery",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Home Delivery via DoorDash",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "For the fastest delivery to your doorstep, please use our official DoorDash store.",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDoorDashClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3008)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Order on DoorDash", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CustomerInfoCard(
    customerName: String,
    customerPhone: String,
    customerEmail: String,
    specialInstructions: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onInstructionsChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contact Information", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ReservationTextField(customerName, onNameChange, "Full Name", Icons.Default.Person)
            Spacer(modifier = Modifier.height(12.dp))
            ReservationTextField(customerEmail, onEmailChange, "Email Address", Icons.Default.Email)
            Spacer(modifier = Modifier.height(12.dp))
            ReservationTextField(customerPhone, onPhoneChange, "Phone Number", Icons.Default.Phone)
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = specialInstructions,
                onValueChange = onInstructionsChange,
                modifier = Modifier.fillMaxWidth().height(100.dp),
                label = { Text("Special Instructions") },
                placeholder = { Text("Any special requests...") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = Color(0xFFEEEEEE)
                )
            )
        }
    }
}

@Composable
fun PaymentMethodCard(
    selectedMethod: String, 
    onMethodSelected: (String) -> Unit,
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    cardExpiry: String,
    onCardExpiryChange: (String) -> Unit,
    cardCvv: String,
    onCardCvvChange: (String) -> Unit
) {
    val methods = listOf(
        Triple("Credit/Debit Card", R.drawable.ic_visa, "Visa/Mastercard"), 
        Triple("Pay at Counter (Cash)", R.drawable.ic_visa, "Cash/Card at Counter")
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payment, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            methods.forEach { (method, iconRes, subtitle) ->
                val isSelected = selectedMethod == method
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMethodSelected(method) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (method == "Pay at Counter (Cash)") {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Payments, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                                }
                            } else {
                                Image(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(method, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { onMethodSelected(method) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                        )
                    }

                    if (isSelected && method == "Credit/Debit Card") {
                        Column(modifier = Modifier.padding(bottom = 16.dp)) {
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { if (it.length <= 16) onCardNumberChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Card Number") },
                                placeholder = { Text("XXXX XXXX XXXX XXXX") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = Color(0xFFEEEEEE)
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = { if (it.length <= 5) onCardExpiryChange(it) },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Expiry (MM/YY)") },
                                    placeholder = { Text("MM/YY") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryColor,
                                        unfocusedBorderColor = Color(0xFFEEEEEE)
                                    )
                                )
                                OutlinedTextField(
                                    value = cardCvv,
                                    onValueChange = { if (it.length <= 3) onCardCvvChange(it) },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("CVV") },
                                    placeholder = { Text("XXX") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryColor,
                                        unfocusedBorderColor = Color(0xFFEEEEEE)
                                    )
                                )
                            }
                        }
                    }

                    if (method != methods.last().first) {
                        HorizontalDivider(color = Color(0xFFF5F5F5))
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSummarySection(subtotal: Double, discountAmount: Double, taxAmount: Double, total: Double) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            CartSummaryRow("Subtotal", String.format(Locale.US, "$%.2f", subtotal))
            if (discountAmount > 0) {
                CartSummaryRow("Discount", String.format(Locale.US, "-$%.2f", discountAmount), color = Color(0xFF2E7D32))
            }
            CartSummaryRow("Tax (18%)", String.format(Locale.US, "$%.2f", taxAmount))
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF5F5F5))
            CartSummaryRow("Total", String.format(Locale.US, "$%.2f", total), isTotal = true)
        }
    }
}

@Composable
fun CartSummaryRow(label: String, value: String, isTotal: Boolean = false, color: Color = Color.Black) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = if (isTotal) Color.Black else if (color != Color.Black) color else Color.Gray, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal)
        Text(value, color = if (isTotal) PrimaryColor else color, fontWeight = if (isTotal) FontWeight.ExtraBold else FontWeight.Bold, fontSize = if (isTotal) 18.sp else 14.sp)
    }
}

@Composable
fun ReservationTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        shape = RoundedCornerShape(16.dp),
        leadingIcon = { Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(20.dp)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = Color(0xFFEEEEEE)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartTopBar(navController: NavController, title: String) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        CenterAlignedTopAppBar(
            title = { Text(title, fontWeight = FontWeight.Bold) },
            navigationIcon = { 
                IconButton(onClick = { 
                    navController.popBackStack()
                }) { 
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null) 
                } 
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
        )
    }
}

@Composable
fun CartItemsSection(items: List<CartItem>, viewModel: CartViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { item ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(
                        painter = painterResource(item.imageRes), 
                        null, 
                        modifier = Modifier.size(70.dp).clip(RoundedCornerShape(16.dp)), 
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = { viewModel.removeFromCart(item) }, modifier = Modifier.size(20.dp)) { 
                                Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp)) 
                            }
                        }
                        Text("$${String.format(Locale.US, "%.2f", item.price)}", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        QuantityControls(item.quantity, { viewModel.increaseQuantity(item) }, { viewModel.decreaseQuantity(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun QuantityControls(quantity: Int, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconButton(
            onClick = onDecrease, 
            modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFF5F5F5))
        ) { 
            Box(modifier = Modifier.size(8.dp, 1.5.dp).background(Color.Black)) 
        }
        Text(quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        IconButton(
            onClick = onIncrease, 
            modifier = Modifier.size(28.dp).clip(CircleShape).background(PrimaryColor)
        ) { 
            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(10.dp)) 
        }
    }
}
