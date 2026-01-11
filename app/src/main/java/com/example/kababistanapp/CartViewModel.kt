package com.example.kababistanapp

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CartViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val ordersCollection = db.collection("orders")
    private val usersCollection = db.collection("users")

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _previousOrders = MutableStateFlow<List<Order>>(emptyList())
    val previousOrders: StateFlow<List<Order>> = _previousOrders.asStateFlow()

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

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                fetchOrderHistory(user.uid)
                setupUserListener(user.uid)
            } else {
                _previousOrders.value = emptyList()
                userListener?.remove()
            }
        }
    }

    private fun setupUserListener(userId: String) {
        userListener?.remove()
        userListener = usersCollection.document(userId).addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null && snapshot.exists()) {
                customerName.value = snapshot.getString("name") ?: ""
                customerPhone.value = snapshot.getString("phone") ?: ""
                customerEmail.value = snapshot.getString("email") ?: ""
                customerAddress.value = snapshot.getString("address") ?: ""
            }
        }
    }

    private fun fetchOrderHistory(userId: String) {
        historyListener?.remove()
        
        historyListener = ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    ordersCollection
                        .whereEqualTo("userId", userId)
                        .addSnapshotListener { snapshot2, e2 ->
                            if (e2 == null && snapshot2 != null) {
                                val orders = snapshot2.toObjects(Order::class.java)
                                _previousOrders.value = orders.sortedByDescending { it.timestamp }
                            }
                        }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.toObjects(Order::class.java)
                    _previousOrders.value = orders
                }
            }
    }

    fun applyVoucher(code: String) {
        when (code.uppercase()) {
            "WELCOME30" -> {
                appliedVoucherCode.value = "WELCOME30"
                voucherDiscountPercentage.value = 0.30
            }
            "KABABISTAN10" -> {
                appliedVoucherCode.value = "KABABISTAN10"
                voucherDiscountPercentage.value = 0.10
            }
            "SAVE15" -> {
                appliedVoucherCode.value = "SAVE15"
                voucherDiscountPercentage.value = 0.15
            }
            else -> {
                appliedVoucherCode.value = null
                voucherDiscountPercentage.value = 0.0
            }
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
                currentItems.map {
                    if (it.name == name) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                currentItems + CartItem(
                    id = name,
                    name = name,
                    price = priceDouble,
                    imageRes = imageRes,
                    quantity = 1
                )
            }
        }
    }

    fun confirmOrder() {
        val currentUser = auth.currentUser
        val orderType = selectedOrderType.value
        val prefix = if (orderType == "Delivery") "DEL" else if (orderType == "Reservation") "RES" else "PICK"
        val orderId = "#$prefix${(1000..9999).random()}"
        currentOrderId.value = orderId
        
        val currentUserEmail = currentUser?.email ?: ""
        val finalEmail = if (customerEmail.value.isBlank()) currentUserEmail else customerEmail.value

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
            status = "New $orderType",
            paymentMethod = selectedPaymentMethod.value,
            cardNumber = if (selectedPaymentMethod.value == "Credit/Debit Card") cardNumber.value else "",
            cardExpiry = if (selectedPaymentMethod.value == "Credit/Debit Card") cardExpiry.value else "",
            customerName = customerName.value.ifBlank { currentUser?.displayName ?: "Guest" },
            customerPhone = customerPhone.value,
            customerEmail = finalEmail,
            customerAddress = customerAddress.value,
            numberOfPeople = numberOfPeople.value,
            specialInstructions = specialInstructions.value,
            timestamp = System.currentTimeMillis()
        )
        
        ordersCollection.document(orderId).set(newOrder)
            .addOnSuccessListener {
                isOrderConfirmed.value = true
                confirmedDate.value = selectedDate.value
                confirmedTime.value = selectedTime.value
                showReservationReminder.value = false 
                showTimeUpConfirmation.value = false
                lastReminderMinutes = -1
                
                if (currentUser != null) {
                    val userData = hashMapOf(
                        "name" to customerName.value,
                        "phone" to customerPhone.value,
                        "email" to finalEmail,
                        "address" to customerAddress.value
                    )
                    usersCollection.document(currentUser.uid).set(userData, com.google.firebase.firestore.SetOptions.merge())
                }
                
                removeVoucher()
                clearCart()
                
                if (currentUser != null) {
                    fetchOrderHistory(currentUser.uid)
                }
            }
    }

    fun checkReservationTime() {
        if (!isOrderConfirmed.value || confirmedTime.value.isEmpty()) return

        try {
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
                    showTimeUpConfirmation.value = true
                    timeUpShownForOrderId = currentOrderId.value
                }
                return
            }

            val type = selectedOrderType.value
            if (diff == 15 && lastReminderMinutes != 15) {
                reminderMessage.value = "Your order will be ready for $type in 15 minutes!"
                showReservationReminder.value = true
                lastReminderMinutes = 15
            } else if (diff == 5 && lastReminderMinutes != 5) {
                reminderMessage.value = "Your order is almost ready for $type!"
                showReservationReminder.value = true
                lastReminderMinutes = 5
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onTimeUpResponse(arrived: Boolean) {
        val status = if (arrived) "Completed" else "Pending"
        ordersCollection.document(currentOrderId.value).update("status", status)
        
        showTimeUpConfirmation.value = false
        isOrderConfirmed.value = false
        confirmedDate.value = ""
        confirmedTime.value = ""
    }

    fun dismissReminder() {
        showReservationReminder.value = false
    }

    fun cancelOrder(orderId: String? = null) {
        val idToCancel = orderId ?: currentOrderId.value
        if (idToCancel.isNotEmpty()) {
            ordersCollection.document(idToCancel).update("status", "Cancelled")
        }
        
        if (idToCancel == currentOrderId.value) {
            isOrderConfirmed.value = false
            confirmedDate.value = ""
            confirmedTime.value = ""
            showReservationReminder.value = false
            showTimeUpConfirmation.value = false
            lastReminderMinutes = -1
        }
    }

    fun markAsReceived(orderId: String) {
        ordersCollection.document(orderId).update("status", "Completed")
    }

    fun increaseQuantity(item: CartItem) {
        _cartItems.update { currentItems ->
            currentItems.map {
                if (it.id == item.id) it.copy(quantity = it.quantity + 1) else it
            }
        }
    }

    fun decreaseQuantity(item: CartItem) {
        _cartItems.update { currentItems ->
            currentItems.map {
                if (it.id == item.id && it.quantity > 1) {
                    it.copy(quantity = it.quantity - 1)
                } else it
            }
        }
    }

    fun removeFromCart(item: CartItem) {
        _cartItems.update { currentItems ->
            currentItems.filter { it.id != item.id }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    val subtotal: Double
        get() = _cartItems.value.sumOf { it.price * it.quantity }

    val taxRate: Double = 0.18 

    val autoDiscountRate: Double
        get() = if (_previousOrders.value.isEmpty()) 0.10 else 0.0 

    val discountAmount: Double
        get() {
            val totalDiscountRate = autoDiscountRate + voucherDiscountPercentage.value
            return subtotal * totalDiscountRate
        }

    val taxAmount: Double
        get() = (subtotal - discountAmount) * taxRate

    val total: Double
        get() = (subtotal - discountAmount) + taxAmount

    override fun onCleared() {
        super.onCleared()
        historyListener?.remove()
        userListener?.remove()
    }
}
