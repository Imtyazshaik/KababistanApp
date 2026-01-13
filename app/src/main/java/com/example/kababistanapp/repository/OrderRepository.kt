package com.example.kababistanapp.repository

import com.example.kababistanapp.model.Order
import com.google.firebase.firestore.FirebaseFirestore

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ordersRef = db.collection("orders")

    fun placeOrder(order: Order, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = ordersRef.document()
        val newOrder = order.copy(orderId = docRef.id)

        docRef.set(newOrder)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
