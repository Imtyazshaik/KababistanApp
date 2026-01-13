package com.example.kababistanapp

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kababistanapp.ui.theme.PrimaryColor
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlinx.coroutines.yield
import kotlin.math.absoluteValue

val LightBlueUI = Color(0xFFE3F2FD)

data class MenuItem(
    val name: String,
    val price: String,
    val imageRes: Int,
    val category: String,
    val rating: String = "4.8"
)

data class SpecialOffer(
    val discount: String,
    val description: String,
    val imageRes: Int,
    val backgroundColor: Color,
    val secondaryColor: Color,
    val voucherCode: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, cartViewModel: CartViewModel = viewModel()) {
    var searchText by remember { mutableStateOf("") }
    val categories = listOf("All", "Appetizer", "Main Dishes", "Combo Platter", "Kids Meal", "Dessert", "Drinks", "Sides")
    var selectedCategory by remember { mutableStateOf("All") }
    val locationViewModel: LocationViewModel = viewModel()
    var permissionGranted by remember { mutableStateOf(false) }
    val locationData by locationViewModel.location.collectAsState()
    val context = LocalContext.current
    
    val favorites by cartViewModel.favorites.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartItemCount = cartItems.sumOf { it.quantity }

    val isConfirmed by cartViewModel.isOrderConfirmed.collectAsState()
    val confirmedDate by cartViewModel.confirmedDate.collectAsState()
    val confirmedTime by cartViewModel.confirmedTime.collectAsState()
    val selectedOrderType by cartViewModel.selectedOrderType.collectAsState()
    
    val previousOrders by cartViewModel.previousOrders.collectAsState()
    val latestOrder = previousOrders.firstOrNull()

    var showClosedDialog by remember { mutableStateOf(false) }
    val showReminder by cartViewModel.showReservationReminder.collectAsState()
    val reminderMsg by cartViewModel.reminderMessage.collectAsState()
    val showTimeUp by cartViewModel.showTimeUpConfirmation.collectAsState()

    val allMenuItems = remember {
        listOf(
            MenuItem("Bolani", "7.99", R.drawable.boloni, "Appetizer"),
            MenuItem("Mantu(Beef/Chicken)", "13.99", R.drawable.mantu, "Appetizer"),
            MenuItem("Borani Banjan", "8.99", R.drawable.borani_banjan1, "Appetizer"),
            MenuItem("Beef Tikka kabab", "19.99", R.drawable.beef_takka, "Main Dishes"),
            MenuItem("Chicken Tikka kabab with Rice", "17.99", R.drawable.chicken_takka, "Main Dishes"),
            MenuItem("Lamb Tikka kabab", "18.99", R.drawable.beef_takka, "Main Dishes"),
            MenuItem("Qabuli Palaw With Baby Goat", "18.99", R.drawable.kabulipilau, "Main Dishes"),
            MenuItem("Shinnwari Karahi(Baby Goat)", "35.00", R.drawable.shinwari_karahi, "Main Dishes"),
            MenuItem("Shinnwari Karahi(Beef Tikka)", "38.99", R.drawable.shinwari_karahi, "Main Dishes"),
            MenuItem("Chapli kabab", "25.00", R.drawable.chapli_kabab, "Main Dishes"),
            MenuItem("Afghan Shami Chicken Kabab", "17.99", R.drawable.afghan_shami, "Main Dishes"),
            MenuItem("Afghan Burger", "13.99", R.drawable.afghan_burger, "Main Dishes"),
            MenuItem("Double Combo(Chicken/Beef)", "26.99", R.drawable.double_combo, "Combo Platter"),
            MenuItem("Triple Combo(Chicken/beef/Chapli)", "31.99", R.drawable.triple_combo, "Combo Platter"),
            MenuItem("Chicken Nuggets with Hand Cut Fries", "7.99", R.drawable.chicken_nuggetes, "Kids Meal"),
            MenuItem("Chocolate Ice Cream Cone", "3.49", R.drawable.chocolate_icecreem, "Dessert"),
            MenuItem("Vanilla Ice Cream", "3.49", R.drawable.vanilla_icecream, "Dessert"),
            MenuItem("Twist Ice Cream", "3.49", R.drawable.twist_icecream, "Dessert"),
            MenuItem("Shirpera", "4.99", R.drawable.sheerpera_img, "Dessert"),
            MenuItem("Afghan Cream Roll (3 pieces)", "6.99", R.drawable.afghan_cream, "Dessert"),
            MenuItem("Water", "1.99", R.drawable.water_bottle, "Drinks"),
            MenuItem("Sparkling Water", "2.99", R.drawable.sparkling_water, "Drinks"),
            MenuItem("Green/Black Tea (Cup)", "1.99", R.drawable.black_tea, "Drinks"),
            MenuItem("Green/Black Tea (Pot)", "3.99", R.drawable.green_tea, "Drinks"),
            MenuItem("Doogh(Yogurt Drink)", "3.49", R.drawable.curd_img, "Drinks")
        )
    }

    val filteredItems = if (searchText.isBlank()) {
        if (selectedCategory == "All") allMenuItems else allMenuItems.filter { it.category == selectedCategory }
    } else {
        allMenuItems.filter { 
            it.name.contains(searchText, ignoreCase = true) || 
            it.category.contains(searchText, ignoreCase = true)
        }
    }

    val scrollState = rememberScrollState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean -> permissionGranted = isGranted }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        
        // Check if restaurant is closed (after 10 PM or before 10 AM)
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        if (hour >= 22 || hour < 10) {
            showClosedDialog = true
        }

        while(true) {
            cartViewModel.checkReservationTime()
            delay(15000)
        }
    }

    if (permissionGranted) {
        locationViewModel.startLocationUpdates(context)
    }

    if (showClosedDialog) {
        AlertDialog(
            onDismissRequest = { showClosedDialog = false },
            title = { 
                Text(
                    "Restaurant Closed", 
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = { 
                Text(
                    "Kababistan is currently closed. We can't fulfill your orders after closing hours (10:00 PM). Please place your order tomorrow!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            confirmButton = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = { showClosedDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("OK")
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    if (showReminder) {
        AlertDialog(
            onDismissRequest = { cartViewModel.dismissReminder() },
            icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(40.dp)) },
            title = { Text("Order Reminder", fontWeight = FontWeight.Bold) },
            text = { Text(reminderMsg, textAlign = TextAlign.Center) },
            confirmButton = {
                Button(onClick = { cartViewModel.dismissReminder() }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showTimeUp) {
        val question = if (selectedOrderType == "Reservation") "Have you got the table? Please confirm." else "Have you picked up your order yet?"
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(40.dp)) },
            title = { Text("Confirmation", fontWeight = FontWeight.Bold) },
            text = { Text(question, textAlign = TextAlign.Center) },
            confirmButton = {
                Button(onClick = { cartViewModel.onTimeUpResponse(true) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) {
                    Text("Yes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { cartViewModel.onTimeUpResponse(false) }) {
                    Text("No")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier
                    .background(LightBlueUI)
                    .statusBarsPadding()
                ) {
                    val locationString = locationData?.let { 
                        listOfNotNull(it.city, it.state).joinToString(", ") 
                    } ?: "Detecting location..."
                    
                    HomeTopBar(
                        location = locationString,
                        navController = navController,
                        hasNotifications = isConfirmed,
                        cartItemCount = cartItemCount
                    )
                    
                    val showSearchBar by remember { derivedStateOf { scrollState.value < 100 } }
                    this.AnimatedVisibility(
                        visible = showSearchBar,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { 
                            SearchBarSection(searchText = searchText, onSearchChange = { searchText = it })
                        }
                    }

                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { 
                        CategorySection(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategorySelect = { 
                                selectedCategory = it
                                searchText = ""
                            }
                        )
                    }
                }
            },
            bottomBar = {
                Column(modifier = Modifier.background(LightBlueUI)) {
                    if (latestOrder != null && (System.currentTimeMillis() - latestOrder.timestamp < 12 * 60 * 60 * 1000)) {
                        OrderStatusBanner(order = latestOrder) {
                            navController.navigate("orders")
                        }
                    }
                    AppBottomBar(navController = navController, currentRoute = "home", containerColor = LightBlueUI)
                }
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFAFAFA))
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                ) {
                    if (isConfirmed && searchText.isBlank() && selectedCategory == "All") {
                        Spacer(modifier = Modifier.height(12.dp))
                        ActiveReservationCard(
                            date = confirmedDate,
                            time = confirmedTime,
                            onClick = { navController.navigate("orders") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (searchText.isBlank() && selectedCategory == "All") {
                        SpecialOfferSection(navController)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val favoriteItemsList = allMenuItems.filter { it.name in favorites }
                        if (favoriteItemsList.isNotEmpty()) {
                            FavoriteSection(
                                favoriteItems = favoriteItemsList,
                                onToggleFavorite = { itemName ->
                                    cartViewModel.toggleFavorite(itemName)
                                },
                                onAddToCart = { item ->
                                    cartViewModel.addToCart(item.name, item.price, item.imageRes)
                                },
                                onItemClick = { item ->
                                    val encodedName = Uri.encode(item.name)
                                    navController.navigate("food_detail/$encodedName/${item.price}/${item.imageRes}")
                                },
                                cartItems = cartItems,
                                cartViewModel = cartViewModel,
                                navController = navController
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = if (searchText.isBlank()) {
                                if (selectedCategory == "All") "Menu Items" else selectedCategory
                        } else "Search Results",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (filteredItems.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "No items found", color = Color.Gray)
                            }
                        } else {
                            filteredItems.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            val currentItem = cartItems.find { it.name == item.name }
                                            val quantity = currentItem?.quantity ?: 0
                                            FoodItemCard(
                                                item = item,
                                                isFavorite = item.name in favorites,
                                                quantity = quantity,
                                                onToggleFavorite = {
                                                    cartViewModel.toggleFavorite(item.name)
                                                },
                                                onAddToCart = {
                                                    cartViewModel.addToCart(item.name, item.price, item.imageRes)
                                                },
                                                onRemoveFromCart = {
                                                    currentItem?.let { cartViewModel.decreaseQuantity(it) }
                                                },
                                                onClick = {
                                                    val encodedName = Uri.encode(item.name)
                                                    navController.navigate("food_detail/$encodedName/${item.price}/${item.imageRes}")
                                                }
                                            )
                                        }
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        )
    }
}

@Composable
fun OrderStatusBanner(order: Order, onClick: () -> Unit) {
    val statusText = when (order.status.lowercase()) {
        "pending" -> "✔ Order Pending"
        "accepted" -> "✔ Confirmed / Preparing"
        "preparing" -> "✔ Preparing"
        "ready" -> "✔ Ready to Pick Up"
        "completed" -> "✔ Thank you for ordering from Kababistan ❤️"
        "picked up" -> "✔ Picked Up"
        "rejected" -> "✔ Order Rejected ❌"
        "cancelled" -> "✔ Order Cancelled"
        else -> "✔ Status: ${order.status}"
    }

    val backgroundColor = when (order.status.lowercase()) {
        "rejected", "cancelled" -> Color(0xFFFFEBEE)
        "completed" -> Color(0xFFE8F5E9)
        "ready" -> Color(0xFFE3F2FD)
        else -> PrimaryColor
    }

    val textColor = when (order.status.lowercase()) {
        "rejected", "cancelled" -> Color.Red
        "completed" -> Color(0xFF2E7D32)
        "ready" -> Color(0xFF1976D2)
        else -> Color.White
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(backgroundColor),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = statusText,
                color = textColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun HomeTopBar(location: String, navController: NavController, hasNotifications: Boolean, cartItemCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Location",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = location,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 200.dp)
                )
            }
        }
        
        Row {
            IconButton(
                onClick = { navController.navigate("notifications") },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            ) {
                BadgedBox(
                    badge = {
                        if (hasNotifications) {
                            Badge(containerColor = Color.Red)
                        }
                    }
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = { navController.navigate("cart") },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            ) {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(containerColor = PrimaryColor) {
                                Text(cartItemCount.toString(), color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun SearchBarSection(searchText: String, onSearchChange: (String) -> Unit) {
    TextField(
        value = searchText,
        onValueChange = onSearchChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(4.dp, RoundedCornerShape(25.dp)),
        placeholder = { Text("Search for your favorite food", fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
        trailingIcon = { 
            if (searchText.isNotEmpty()) {
                IconButton(onClick = { onSearchChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(20.dp))
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(25.dp),
        singleLine = true
    )
}

@Composable
fun CategorySection(categories: List<String>, selectedCategory: String, onCategorySelect: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelect(category) },
                label = { Text(category, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryColor,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) Color.Transparent else Color.LightGray
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun SpecialOfferSection(navController: NavController) {
    val offers = listOf(
        SpecialOffer("20% OFF", "On your first order", R.drawable.chapli_kabab, Color(0xFFFFE8E8), Color(0xFFFF5252), "WELCOME20"),
        SpecialOffer("Buy 1 Get 1", "On all beverages", R.drawable.sparkling_water, Color(0xFFE3F2FD), Color(0xFF2196F3), "BUY1"),
        SpecialOffer("10% OFF on orders above $60", "Any Dish", R.drawable.chicken_takka, Color(0xFFE8F5E9), Color(0xFF4CAF50), "ANYDISH")
    )
    
    val pagerState = rememberPagerState(pageCount = { offers.size })
    
    LaunchedEffect(Unit) {
        while (true) {
            yield()
            delay(4000)
            val nextPage = (pagerState.currentPage + 1) % offers.size
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(durationMillis = 1500)
            )
        }
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Special Offers", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp))
            TextButton(onClick = { navController.navigate("special_offers") }) {
                Text("See All", color = PrimaryColor, fontSize = 14.sp)
            }
        }
        
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val offer = offers[page]
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(offer.imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = pageOffset * size.width * 0.2f
                            },
                        contentScale = ContentScale.Crop
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.95f),
                                        Color.White.copy(alpha = 0.8f),
                                        Color.Transparent
                                    ),
                                    startX = 0f,
                                    endX = 500f 
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.65f)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = offer.secondaryColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = offer.discount,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = offer.description,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold, 
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { /* Claim */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Claim Now", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        
        Row(
            Modifier.height(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(offers.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) PrimaryColor else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(if (pagerState.currentPage == iteration) 6.dp else 4.dp)
                )
            }
        }
    }
}

@Composable
fun FoodItemCard(
    item: MenuItem, 
    isFavorite: Boolean, 
    quantity: Int,
    onToggleFavorite: () -> Unit, 
    onAddToCart: () -> Unit, 
    onRemoveFromCart: () -> Unit,
    onClick: () -> Unit
) {
    var showAddedToCart by remember { mutableStateOf(false) }

    LaunchedEffect(showAddedToCart) {
        if (showAddedToCart) {
            delay(2000)
            showAddedToCart = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(15.dp)),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.height(130.dp).fillMaxWidth()) {
                Image(
                    painter = painterResource(item.imageRes),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = showAddedToCart,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Item added to cart",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(30.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color.Red else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(item.rating, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color.Gray
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${item.price}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryColor,
                            fontSize = 15.sp
                        )
                    )
                    
                    if (quantity == 0) {
                        IconButton(
                            onClick = {
                                onAddToCart()
                                showAddedToCart = true
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryColor, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        QuantityControls(
                            quantity = quantity,
                            onIncrease = { 
                                onAddToCart()
                                showAddedToCart = true
                            },
                            onDecrease = onRemoveFromCart,
                            size = 30.dp,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteFoodItemCard(
    item: MenuItem, 
    quantity: Int,
    onToggleFavorite: () -> Unit, 
    onAddToCart: () -> Unit, 
    onRemoveFromCart: () -> Unit,
    onClick: () -> Unit
) {
    var showAddedToCart by remember { mutableStateOf(false) }

    LaunchedEffect(showAddedToCart) {
        if (showAddedToCart) {
            delay(2000)
            showAddedToCart = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$${item.price}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryColor,
                        fontSize = 14.sp
                    )
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (quantity == 0) {
                    // This is the "+" Add to Cart button for Favorites
                    Box(
                        modifier = Modifier
                            .size(24.dp) // Total button size
                            .clip(CircleShape)
                            .background(PrimaryColor)
                            .clickable {
                                onAddToCart()
                                showAddedToCart = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp) // Actual icon size reduced
                        )
                    }
                } else {
                    QuantityControls(
                        quantity = quantity,
                        onIncrease = { 
                            onAddToCart()
                            showAddedToCart = true
                        },
                        onDecrease = onRemoveFromCart,
                        size = 24.dp,
                        fontSize = 12.sp
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(item.imageRes),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(14.dp)
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showAddedToCart,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Added",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteSection(
    favoriteItems: List<MenuItem>, 
    onToggleFavorite: (String) -> Unit, 
    onAddToCart: (MenuItem) -> Unit, 
    onItemClick: (MenuItem) -> Unit,
    cartItems: List<CartItem>,
    cartViewModel: CartViewModel,
    navController: NavController
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Favorites", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp))
            TextButton(onClick = { navController.navigate("favorites") }) {
                Text("See All", color = PrimaryColor, fontSize = 14.sp)
            }
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(favoriteItems) { item ->
                Box(modifier = Modifier.width(220.dp)) {
                    val currentItem = cartItems.find { it.name == item.name }
                    val quantity = currentItem?.quantity ?: 0
                    FavoriteFoodItemCard(
                        item = item,
                        quantity = quantity,
                        onToggleFavorite = { onToggleFavorite(item.name) },
                        onAddToCart = { onAddToCart(item) },
                        onRemoveFromCart = { currentItem?.let { cartViewModel.decreaseQuantity(it) } },
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuantityControls(
    quantity: Int, 
    onIncrease: () -> Unit, 
    onDecrease: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        Surface(
            onClick = onDecrease,
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            modifier = Modifier.size(size),
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.size((size.value * 0.5).dp),
                    tint = PrimaryColor
                )
            }
        }
        
        Text(
            text = quantity.toString(), 
            fontWeight = FontWeight.ExtraBold, 
            fontSize = fontSize,
            modifier = Modifier.padding(horizontal = 4.dp),
            textAlign = TextAlign.Center
        )
        
        Surface(
            onClick = onIncrease,
            shape = RoundedCornerShape(8.dp),
            color = PrimaryColor,
            modifier = Modifier.size(size),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size((size.value * 0.5).dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ActiveReservationCard(date: String, time: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = PrimaryColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("Active Reservation", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                Text("$date at $time", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun AppBottomBar(navController: NavController, currentRoute: String, containerColor: Color = Color.White) {
    NavigationBar(
        containerColor = containerColor,
        modifier = Modifier.shadow(16.dp),
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "HOME") },
            label = { Text("HOME", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryColor,
                selectedTextColor = PrimaryColor,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = PrimaryColor.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "table_reservation",
            onClick = { navController.navigate("table_reservation") },
            icon = { Icon(Icons.Default.EventSeat, contentDescription = "TABLE") },
            label = { Text("TABLE", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryColor,
                selectedTextColor = PrimaryColor,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = PrimaryColor.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "orders",
            onClick = { navController.navigate("orders") },
            icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "ORDERS") },
            label = { Text("ORDERS", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryColor,
                selectedTextColor = PrimaryColor,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = PrimaryColor.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "PROFILE") },
            label = { Text("PROFILE", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryColor,
                selectedTextColor = PrimaryColor,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = PrimaryColor.copy(alpha = 0.1f)
            )
        )
    }
}
