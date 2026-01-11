package com.example.kababistanapp

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kababistanapp.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavController, cartViewModel: CartViewModel) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Contact & Help", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = "help")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.kababistan_logo),
                contentDescription = "Kababistan Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 16.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Kababistan",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = Color.Black
            )
            
            Text(
                text = "Authentic Afghan Cuisine",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Contact Cards
            ContactInfoCard(
                icon = Icons.Default.LocationOn,
                title = "Our Address",
                detail = "3825 W Spring Creek Pkwy suite 209, Plano, TX 75023",
                onClick = {
                    val gmmIntentUri = Uri.parse("geo:0,0?q=3825 W Spring Creek Pkwy suite 209, Plano, TX 75023")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ContactInfoCard(
                icon = Icons.Default.Call,
                title = "Phone Number",
                detail = "(972) 774-8815",
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:9727748815")
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(40.dp))
            
            // Opening Hours Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "Opening Hours", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val hours = listOf(
                        "Monday" to "Closed",
                        "Tuesday" to "12:00 PM – 10:00 PM",
                        "Wednesday" to "12:00 PM – 10:00 PM",
                        "Thursday" to "12:00 PM – 10:00 PM",
                        "Friday" to "12:00 PM – 10:00 PM",
                        "Saturday" to "12:00 PM – 10:00 PM",
                        "Sunday" to "12:00 PM – 10:00 PM"
                    )
                    
                    hours.forEach { (day, time) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = day, fontWeight = FontWeight.SemiBold, color = if (day == "Monday") Color.Gray else Color.Black)
                            Text(text = time, color = if (time == "Closed") PrimaryColor else Color.Gray)
                        }
                        if (day != "Sunday") {
                            HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "© 2024 Kababistan App. All rights reserved.",
                fontSize = 12.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ContactInfoCard(icon: ImageVector, title: String, detail: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PrimaryColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = PrimaryColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                Text(text = detail, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black)
            }
        }
    }
}
