package com.example.kababistanapp

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kababistanapp.ui.theme.PrimaryColor
import kotlinx.coroutines.delay
import java.util.Calendar

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
    
    var favoriteItemNames by remember { mutableStateOf(setOf<String>()) }
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartItemCount = cartItems.sumOf { it.quantity }

    val isConfirmed by cartViewModel.isOrderConfirmed.collectAsState()
    val confirmedDate by cartViewModel.confirmedDate.collectAsState()
    val confirmedTime by cartViewModel.confirmedTime.collectAsState()

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
        while(true) {
            cartViewModel.checkReservationTime()
            delay(15000)
        }
    }

    if (permissionGranted) {
        locationViewModel.startLocationUpdates(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.background(Color.White)) {
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
                    AnimatedVisibility(
                        visible = showSearchBar,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            SearchBarSection(searchText = searchText, onSearchChange = { searchText = it })
                        }
                    }

                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
                AppBottomBar(navController = navController, currentRoute = "home")
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
                        Spacer(modifier = Modifier.height(16.dp))
                        ActiveReservationCard(
                            date = confirmedDate,
                            time = confirmedTime,
                            onClick = { navController.navigate("orders") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (searchText.isBlank() && selectedCategory == "All") {
                        SpecialOfferSection()
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        val favoriteItems = allMenuItems.filter { it.name in favoriteItemNames }
                        if (favoriteItems.isNotEmpty()) {
                            FavoriteSection(
                                favoriteItems = favoriteItems,
                                onToggleFavorite = { itemName ->
                                    favoriteItemNames = if (itemName in favoriteItemNames) {
                                        favoriteItemNames - itemName
                                    } else {
                                        favoriteItemNames + itemName
                                    }
                                },
                                onAddToCart = { item ->
                                    cartViewModel.addToCart(item.name, item.price, item.imageRes)
                                },
                                onItemClick = { item ->
                                    val encodedName = Uri.encode(item.name)
                                    navController.navigate("food_detail/$encodedName/${item.price}/${item.imageRes}")
                                }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
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
                        Spacer(modifier = Modifier.height(16.dp))
                        
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
                                            FoodItemCard(
                                                item = item,
                                                isFavorite = item.name in favoriteItemNames,
                                                onToggleFavorite = {
                                                    favoriteItemNames = if (item.name in favoriteItemNames) {
                                                        favoriteItemNames - item.name
                                                    } else {
                                                        favoriteItemNames + item.name
                                                    }
                                                },
                                                onAddToCart = {
                                                    cartViewModel.addToCart(item.name, item.price, item.imageRes)
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
fun HomeTopBar(location: String, navController: NavController, hasNotifications: Boolean, cartItemCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
            ) {
                BadgedBox(
                    badge = {
                        if (hasNotifications) {
                            Badge(containerColor = Color.Red)
                        }
                    }
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = { navController.navigate("cart") },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
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
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
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
            .height(56.dp)
            .shadow(4.dp, RoundedCornerShape(28.dp)),
        placeholder = { Text("Search for your favorite food", fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        trailingIcon = { 
            if (searchText.isNotEmpty()) {
                IconButton(onClick = { onSearchChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(28.dp),
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
                label = { Text(category) },
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
fun SpecialOfferSection() {
    val offers = listOf(
        SpecialOffer("20% OFF", "On your first order", R.drawable.chicken_takka, Color(0xFFFFE8E8), Color(0xFFFF5252), "WELCOME20"),
        SpecialOffer("Buy 1 Get 1", "On all beverages", R.drawable.curd_img, Color(0xFFE3F2FD), Color(0xFF2196F3), "BOGOAFG"),
        SpecialOffer("Free Delivery", "On orders above $30", R.drawable.beef_takka, Color(0xFFE8F5E9), Color(0xFF4CAF50), "FREEDEL")
    )
    
    val pagerState = rememberPagerState(pageCount = { offers.size })
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Special Offers", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            TextButton(onClick = { /* See all */ }) {
                Text("See All", color = PrimaryColor)
            }
        }
        
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val offer = offers[page]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = offer.backgroundColor)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
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
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = offer.description,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.Black
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
                    Image(
                        painter = painterResource(offer.imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topStart = 40.dp, bottomStart = 40.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        
        Row(
            Modifier.height(24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(offers.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) PrimaryColor else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(if (pagerState.currentPage == iteration) 8.dp else 6.dp)
                )
            }
        }
    }
}

@Composable
fun FoodItemCard(item: MenuItem, isFavorite: Boolean, onToggleFavorite: () -> Unit, onAddToCart: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
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
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color.Red else Color.Gray,
                        modifier = Modifier.size(18.dp)
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
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(item.rating, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
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
                            color = PrimaryColor
                        )
                    )
                    
                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .size(32.dp)
                            .background(PrimaryColor, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteSection(favoriteItems: List<MenuItem>, onToggleFavorite: (String) -> Unit, onAddToCart: (MenuItem) -> Unit, onItemClick: (MenuItem) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Favorites", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            TextButton(onClick = { /* See all */ }) {
                Text("See All", color = PrimaryColor)
            }
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(favoriteItems) { item ->
                Box(modifier = Modifier.width(160.dp)) {
                    FoodItemCard(
                        item = item,
                        isFavorite = true,
                        onToggleFavorite = { onToggleFavorite(item.name) },
                        onAddToCart = { onAddToCart(item) },
                        onClick = { onItemClick(item) }
                    )
                }
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
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("Active Reservation", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Text("$date at $time", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun AppBottomBar(navController: NavController, currentRoute: String) {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.shadow(16.dp)
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "HOME") },
            label = { Text("HOME") },
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
            label = { Text("TABLE") },
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
            label = { Text("ORDERS") },
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
            label = { Text("PROFILE") },
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
