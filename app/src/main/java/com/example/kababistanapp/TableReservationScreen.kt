package com.example.kababistanapp

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun TableReservationScreen(navController: NavController, cartViewModel: CartViewModel = viewModel()) {
    val context = LocalContext.current
    
    val resDateText by cartViewModel.selectedDate.collectAsState()
    val resTimeText by cartViewModel.selectedTime.collectAsState()
    val numberOfPeople by cartViewModel.numberOfPeople.collectAsState()
    
    val customerName by cartViewModel.customerName.collectAsState()
    val customerPhone by cartViewModel.customerPhone.collectAsState()
    val customerEmail by cartViewModel.customerEmail.collectAsState()
    val specialInstructions by cartViewModel.specialInstructions.collectAsState()

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
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Table Reservation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA),
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.shinwari_karahi),
                        contentDescription = "Restaurant Interior",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                    ) {
                        Text(
                            "Book Your Table",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            "Experience the best Kababs in town",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Schedule Card
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
                                Text("Reservation Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                DateTimeBox(
                                    label = "DATE",
                                    value = resDateText,
                                    icon = Icons.Default.CalendarToday,
                                    modifier = Modifier.weight(1f),
                                    onClick = { showDatePicker = true }
                                )
                                DateTimeBox(
                                    label = "TIME",
                                    value = resTimeText,
                                    icon = Icons.Default.AccessTime,
                                    modifier = Modifier.weight(1f),
                                    onClick = { showTimePicker = true }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = numberOfPeople,
                                onValueChange = { cartViewModel.numberOfPeople.value = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Number of Guests") },
                                placeholder = { Text("e.g. 4 People") },
                                leadingIcon = { Icon(Icons.Default.People, null, tint = PrimaryColor) },
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = Color(0xFFEEEEEE)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact Info
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
                            
                            ReservationTextField(customerName, { cartViewModel.customerName.value = it }, "Full Name", Icons.Default.Person)
                            Spacer(modifier = Modifier.height(12.dp))
                            ReservationTextField(customerEmail, { cartViewModel.customerEmail.value = it }, "Email Address", Icons.Default.Email)
                            Spacer(modifier = Modifier.height(12.dp))
                            ReservationTextField(customerPhone, { cartViewModel.customerPhone.value = it }, "Phone Number", Icons.Default.Phone)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            OutlinedTextField(
                                value = specialInstructions,
                                onValueChange = { cartViewModel.specialInstructions.value = it },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                label = { Text("Special Requests") },
                                placeholder = { Text("Any specific table or dietary notes...") },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = Color(0xFFEEEEEE)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.shadow(16.dp),
                color = Color.White
            ) {
                Button(
                    onClick = {
                        if (resDateText == "Select Date" || resTimeText == "Select Time" || customerName.isBlank() || customerPhone.isBlank() || numberOfPeople.isBlank()) {
                            Toast.makeText(context, "Please fill all details", Toast.LENGTH_SHORT).show()
                        } else {
                            cartViewModel.selectedOrderType.value = "Reservation"
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
                    Text("Confirm Reservation", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    )
}

@Composable
fun DateTimeBox(label: String, value: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFF4F1))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryColor)
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 10.sp, color = PrimaryColor, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black)
    }
}
