package com.example.kababistanapp

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ListAlt
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kababistanapp.ui.theme.PrimaryColor
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, cartViewModel: CartViewModel = viewModel()) {
    val user = Firebase.auth.currentUser
    val context = LocalContext.current
    val db = Firebase.firestore
    
    var isEditing by remember { mutableStateOf(false) }
    
    var name by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") } 
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load extra data from Firestore
    LaunchedEffect(user?.uid) {
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        phone = document.getString("phone") ?: ""
                        address = document.getString("address") ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Request persistable URI permission to fix SecurityException
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Some providers don't support persistable permissions
                e.printStackTrace()
            }
        }
        selectedImageUri = uri
    }

    val saveProfile = {
        isLoading = true
        val profileUpdates = userProfileChangeRequest {
            displayName = name
            selectedImageUri?.let { photoUri = it }
        }
        
        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Save phone and address to Firestore
                val userData = hashMapOf(
                    "phone" to phone,
                    "address" to address,
                    "name" to name,
                    "email" to (user.email ?: "")
                )
                
                db.collection("users").document(user.uid).set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                        isEditing = false
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Firestore Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        isLoading = false
                    }
            } else {
                Toast.makeText(context, "Update Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = "profile")
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        if (isLoading && !isEditing) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(PrimaryColor, PrimaryColor.copy(alpha = 0.8f))
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                        
                        Text(
                            "My Profile",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        IconButton(
                            onClick = { 
                                if (isEditing) saveProfile() else isEditing = true 
                            },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                "Edit",
                                tint = Color.White
                            )
                        }
                    }

                    // Profile Image overlapping the gradient
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 50.dp)
                            .size(120.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(4.dp, Color.White, CircleShape)
                            .clickable(enabled = isEditing) { photoPickerLauncher.launch(arrayOf("image/*")) },
                        contentAlignment = Alignment.Center
                    ) {
                        val displayImage = selectedImageUri ?: user?.photoUrl
                        if (displayImage != null) {
                            AsyncImage(
                                model = displayImage,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.LightGray)
                        }
                        
                        if (isEditing) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))

                // User Info Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryColor,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    } else {
                        Text(
                            text = name.ifBlank { "Add Name" },
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = user?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Details Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Personal Information", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)

                    if (isEditing) {
                        ProfileEditField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = "Phone Number",
                            placeholder = "Enter your phone number",
                            icon = Icons.Default.Phone
                        )
                        ProfileEditField(
                            value = address,
                            onValueChange = { address = it },
                            label = "Delivery Address",
                            placeholder = "Enter your delivery address",
                            icon = Icons.Default.LocationOn
                        )
                    } else {
                        ProfileInfoCardModern(
                            icon = Icons.Default.Phone,
                            label = "Phone",
                            value = phone.ifBlank { "Not Set" }
                        )
                        ProfileInfoCardModern(
                            icon = Icons.Default.LocationOn,
                            label = "Address",
                            value = address.ifBlank { "Not Set" }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Account Options", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                    
                    // Order History
                    ProfileOptionItem(
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        title = "Order History",
                        subtitle = "View your past orders",
                        onClick = { navController.navigate("orders") }
                    )
                    
                    // Table Reservation
                    ProfileOptionItem(
                        icon = Icons.Default.EventSeat,
                        title = "Table Reservation",
                        subtitle = "Book a table for your next visit",
                        onClick = { navController.navigate("table_reservation") }
                    )
                    
                    // Help & Support
                    ProfileOptionItem(
                        icon = Icons.Default.HelpOutline,
                        title = "Help & Support",
                        subtitle = "Contact us for any queries",
                        onClick = { navController.navigate("help") }
                    )

                    // Logout Button
                    Button(
                        onClick = {
                            Firebase.auth.signOut()
                            navController.navigate("login") { popUpTo(0) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ProfileEditField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, null, tint = PrimaryColor) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = Color(0xFFEEEEEE)
        )
    )
}

@Composable
fun ProfileInfoCardModern(icon: ImageVector, label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(PrimaryColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, fontSize = 12.sp, color = Color.Gray)
                Text(value, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ProfileOptionItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.DarkGray, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
        }
    }
}