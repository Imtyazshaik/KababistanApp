package com.example.kababistanapp.model

data class CartItem(
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1
)

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val userName: String = "",
    val phone: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val status: String = "pending",
    val orderDate: String = "",
    val orderTime: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
