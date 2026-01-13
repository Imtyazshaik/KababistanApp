package com.example.kababistanapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kababistanapp.model.CartItem as OrderCartItem
import com.example.kababistanapp.model.Order as OrderModel
import com.example.kababistanapp.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CartViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val ordersCollection = db.collection("orders")
    private val usersCollection = db.collection("users")
    
    private val repository = OrderRepository()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _previousOrders = MutableStateFlow<List<Order>>(emptyList())
    val previousOrders: StateFlow<List<Order>> = _previousOrders.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val dateFormatter = SimpleDateFormat("dd MMM, yyyy", Locale.US)
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.US)

    var currentOrderId = MutableStateFlow("")
    var selectedOrderType = MutableStateFlow("Pick up")
    
    var selectedDate = MutableStateFlow(dateFormatter.format(Date()))
    var selectedTime = MutableStateFlow(timeFormatter.format(Date()))
    
    var isOrderConfirmed = MutableStateFlow(false)
    var confirmedDate = MutableStateFlow("")
    var confirmedTime = MutableStateFlow("")
    
    var showReservationReminder = MutableStateFlow(false)
    var reminderMessage = MutableStateFlow("")
    private var lastReminderMinutes = -1 
    private var reminderTimerJob: Job? = null

    var showTimeUpConfirmation = MutableStateFlow(false)
    private var timeUpShownForOrderId = ""

    var numberOfPeople = MutableStateFlow("")
    var customerName = MutableStateFlow("")
    var customerPhone = MutableStateFlow("")
    var customerEmail = MutableStateFlow("")
    var customerAddress = MutableStateFlow("") 
    var specialInstructions = MutableStateFlow("")
    
    var selectedPaymentMethod = MutableStateFlow("Credit/Debit Card")
    var cardNumber = MutableStateFlow("")
    var cardExpiry = MutableStateFlow("")
    var cardCvv = MutableStateFlow("")

    var appliedVoucherCode = MutableStateFlow<String?>(null)
    var voucherDiscountPercentage = MutableStateFlow(0.0)

    private var historyListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null
    private var orderStatusListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                fetchOrderHistory(user.uid)
                setupUserListener(user.uid)
            } else {
                _previousOrders.value = emptyList()
                _favorites.value = emptySet()
                userListener?.remove()
                orderStatusListener?.remove()
            }
        }
    }

    private fun setupUserListener(userId: String) {
        userListener?.remove()
        userListener = usersCollection.document(userId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("USER_LISTENER", "Error: ${e.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                customerName.value = snapshot.getString("name") ?: ""
                customerPhone.value = snapshot.getString("phone") ?: ""
                customerEmail.value = snapshot.getString("email") ?: ""
                customerAddress.value = snapshot.getString("address") ?: ""
                
                @Suppress("UNCHECKED_CAST")
                val favList = snapshot.get("favorites") as? List<String>
                _favorites.value = favList?.toSet() ?: emptySet()
            }
        }
    }

    fun toggleFavorite(itemName: String) {
        val user = auth.currentUser ?: return
        val currentFavs = _favorites.value.toMutableSet()
        if (currentFavs.contains(itemName)) {
            currentFavs.remove(itemName)
        } else {
            currentFavs.add(itemName)
        }
        _favorites.value = currentFavs.toSet()
        usersCollection.document(user.uid).set(
            mapOf("favorites" to currentFavs.toList()), 
            com.google.firebase.firestore.SetOptions.merge()
        )
    }

    fun fetchOrderHistory(userId: String) {
        historyListener?.remove()
        historyListener = ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val orders = snapshot.toObjects(Order::class.java)
                    _previousOrders.value = orders
                    
                    // Try to auto-detect active order if none is tracking
                    if (!isOrderConfirmed.value) {
                        val active = orders.firstOrNull { it.status.contains("New", ignoreCase = true) || it.status == "accepted" }
                        if (active != null) {
                            currentOrderId.value = active.id
                            confirmedDate.value = active.date
                            confirmedTime.value = active.time
                            selectedOrderType.value = active.type
                            isOrderConfirmed.value = true
                        }
                    }
                }
            }
    }

    fun applyVoucher(code: String) {
        when (code.uppercase()) {
            "WELCOME30" -> { appliedVoucherCode.value = "WELCOME30"; voucherDiscountPercentage.value = 0.30 }
            "KABABISTAN10" -> { appliedVoucherCode.value = "KABABISTAN10"; voucherDiscountPercentage.value = 0.10 }
            "SAVE15" -> { appliedVoucherCode.value = "SAVE15"; voucherDiscountPercentage.value = 0.15 }
            else -> { appliedVoucherCode.value = null; voucherDiscountPercentage.value = 0.0 }
        }
    }

    fun removeVoucher() {
        appliedVoucherCode.value = null
        voucherDiscountPercentage.value = 0.0
    }

    fun addToCart(name: String, price: String, imageRes: Int) {
        val priceDouble = price.toDoubleOrNull() ?: 0.0
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.name == name }
            if (existingItem != null) {
                currentItems.map { if (it.name == name) it.copy(quantity = it.quantity + 1) else it }
            } else {
                currentItems + CartItem(id = name, name = name, price = priceDouble, imageRes = imageRes, quantity = 1)
            }
        }
    }

    fun listenToOrderStatus(orderId: String) {
        orderStatusListener?.remove()
        orderStatusListener = ordersCollection.document(orderId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status") ?: ""
                    Log.d("ORDER_STATUS", "Current status: $status")
                }
            }
    }

    fun confirmOrder(userId: String, userName: String, phone: String, cartItems: List<OrderCartItem>, total: Double) {
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val order = OrderModel(userId = userId, userName = userName, phone = phone, items = cartItems, totalPrice = total, orderDate = date, orderTime = time)
        repository.placeOrder(order = order, onSuccess = {}, onFailure = {})
    }

    fun confirmOrder() {
        val currentUser = auth.currentUser
        val orderType = selectedOrderType.value
        val prefix = if (orderType == "Delivery") "DEL" else if (orderType == "Reservation") "RES" else "PICK"
        val orderId = "#$prefix${(1000..9999).random()}"
        currentOrderId.value = orderId
        
        val initialStatus = when (orderType) {
            "Reservation" -> "New Reservation"
            "Delivery" -> "New Delivery"
            else -> "New Pick up"
        }

        val newOrder = Order(
            id = orderId,
            userId = currentUser?.uid ?: "",
            items = _cartItems.value.toList(),
            total = total,
            subtotal = subtotal,
            discount = discountAmount,
            taxAmount = taxAmount,
            date = selectedDate.value,
            time = selectedTime.value,
            type = selectedOrderType.value,
            status = initialStatus,
            paymentMethod = selectedPaymentMethod.value,
            cardNumber = if (selectedPaymentMethod.value == "Credit/Debit Card") cardNumber.value else "",
            cardExpiry = if (selectedPaymentMethod.value == "Credit/Debit Card") cardExpiry.value else "",
            customerName = customerName.value.ifBlank { currentUser?.displayName ?: "Guest" },
            customerPhone = customerPhone.value,
            customerEmail = if (customerEmail.value.isBlank()) currentUser?.email ?: "" else customerEmail.value,
            customerAddress = customerAddress.value,
            numberOfPeople = numberOfPeople.value,
            specialInstructions = specialInstructions.value,
            timestamp = System.currentTimeMillis()
        )
        
        ordersCollection.document(orderId).set(newOrder)
            .addOnSuccessListener {
                isOrderConfirmed.value = true
                confirmedDate.value = selectedDate.value
                confirmedTime.value = confirmedTime.value.ifEmpty { selectedTime.value }
                showReservationReminder.value = false 
                showTimeUpConfirmation.value = false
                lastReminderMinutes = -1
                listenToOrderStatus(orderId)
                if (currentUser != null) {
                    val userData = hashMapOf("name" to customerName.value, "phone" to customerPhone.value, "email" to (if (customerEmail.value.isBlank()) currentUser.email ?: "" else customerEmail.value), "address" to customerAddress.value)
                    usersCollection.document(currentUser.uid).set(userData, com.google.firebase.firestore.SetOptions.merge())
                }
                removeVoucher(); clearCart()
                if (currentUser != null) fetchOrderHistory(currentUser.uid)
            }
    }

    fun checkReservationTime() {
        if (!isOrderConfirmed.value || confirmedTime.value.isEmpty()) return

        try {
            // Only remind for today's orders
            val todayStr = dateFormatter.format(Date())
            if (confirmedDate.value != todayStr) return

            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            val orderTimeDate = sdf.parse(confirmedTime.value) ?: return
            
            val calendar = Calendar.getInstance()
            val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
            val nowMin = calendar.get(Calendar.MINUTE)
            
            val orderCalendar = Calendar.getInstance()
            orderCalendar.time = orderTimeDate
            val orderHour = orderCalendar.get(Calendar.HOUR_OF_DAY)
            val orderMin = orderCalendar.get(Calendar.MINUTE)
            
            val nowTotalMins = nowHour * 60 + nowMin
            val orderTotalMins = orderHour * 60 + orderMin
            val diff = orderTotalMins - nowTotalMins
            
            if (diff <= 0) {
                if (timeUpShownForOrderId != currentOrderId.value) {
                    showReservationReminder.value = false // Hide reminder if time is up
                    showTimeUpConfirmation.value = true
                    timeUpShownForOrderId = currentOrderId.value
                }
                return
            }

            val type = selectedOrderType.value
            // Robust check: if between 0 and 10 minutes, show reminder
            if (diff <= 10 && lastReminderMinutes != 10) {
                reminderMessage.value = "Reminder: Your $type is scheduled in $diff minutes!"
                showReservationReminder.value = true
                lastReminderMinutes = 10
                
                // Auto dismiss after 5000 seconds
                reminderTimerJob?.cancel()
                reminderTimerJob = viewModelScope.launch {
                    delay(5000 * 1000L)
                    showReservationReminder.value = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onTimeUpResponse(confirmed: Boolean) {
        val status = if (confirmed) "completed" else "pending"
        if (currentOrderId.value.isNotEmpty()) {
            ordersCollection.document(currentOrderId.value).update("status", status)
        }
        
        showTimeUpConfirmation.value = false
        isOrderConfirmed.value = false
        confirmedDate.value = ""
        confirmedTime.value = ""
        timeUpShownForOrderId = ""
        lastReminderMinutes = -1
    }

    fun dismissReminder() {
        showReservationReminder.value = false
        reminderTimerJob?.cancel()
    }

    fun cancelOrder(orderId: String? = null) {
        val idToCancel = orderId ?: currentOrderId.value
        if (idToCancel.isNotEmpty()) ordersCollection.document(idToCancel).update("status", "cancelled")
        if (idToCancel == currentOrderId.value) {
            isOrderConfirmed.value = false; confirmedDate.value = ""; confirmedTime.value = ""
            showReservationReminder.value = false; showTimeUpConfirmation.value = false
            lastReminderMinutes = -1; orderStatusListener?.remove()
        }
    }

    fun markAsReceived(orderId: String) { ordersCollection.document(orderId).update("status", "completed") }

    fun increaseQuantity(item: CartItem) { _cartItems.update { currentItems -> currentItems.map { if (it.id == item.id) it.copy(quantity = it.quantity + 1) else it } } }

    fun decreaseQuantity(item: CartItem) { _cartItems.update { currentItems -> currentItems.map { if (it.id == item.id && it.quantity > 1) it.copy(quantity = it.quantity - 1) else it } } }

    fun removeFromCart(item: CartItem) { _cartItems.update { currentItems -> currentItems.filter { it.id != item.id } } }

    fun clearCart() { _cartItems.value = emptyList() }

    val subtotal: Double get() = _cartItems.value.sumOf { it.priceDouble * it.quantity.toDouble() }
    val taxRate: Double = 0.18 
    val autoDiscountRate: Double get() = if (_previousOrders.value.isEmpty()) 0.10 else 0.0 
    val discountAmount: Double get() = subtotal * (autoDiscountRate + voucherDiscountPercentage.value)
    val taxAmount: Double get() = (subtotal - discountAmount) * taxRate
    val total: Double get() = (subtotal - discountAmount) + taxAmount

    override fun onCleared() {
        super.onCleared()
        historyListener?.remove()
        userListener?.remove()
        orderStatusListener?.remove()
        reminderTimerJob?.cancel()
    }
}
