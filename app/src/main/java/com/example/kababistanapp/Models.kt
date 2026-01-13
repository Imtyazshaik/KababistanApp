package com.example.kababistanapp

data class CartItem(
    val id: String = "",
    val name: String = "",
    val price: Any? = 0.0, // Changed to Any? to handle both String and Double from Firestore
    val imageRes: Int = 0,
    var quantity: Int = 1,
    val category: String? = null,
    val description: String = ""
) {
    // Helper to safely get price as Double
    val priceDouble: Double get() = when(price) {
        is Number -> price.toDouble()
        is String -> price.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}

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
