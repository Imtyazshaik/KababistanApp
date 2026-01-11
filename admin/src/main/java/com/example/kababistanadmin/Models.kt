package com.example.kababistanadmin

data class CartItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageRes: Int = 0,
    var quantity: Int = 1,
    val category: String? = null,
    val description: String = ""
)

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val date: String = "",
    val time: String = "",
    val type: String = "Reservation",
    var status: String = "New",
    val paymentMethod: String = "",
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val customerAddress: String = "",
    val numberOfPeople: String = "",
    val specialInstructions: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
