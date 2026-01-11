package com.example.kababistanadmin

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    val allOrders: StateFlow<List<Order>> = _allOrders.asStateFlow()

    private var ordersListener: ListenerRegistration? = null

    init {
        fetchAllOrders()
    }

    private fun fetchAllOrders() {
        ordersListener?.remove()
        ordersListener = ordersCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val orders = snapshot.toObjects(Order::class.java)
                    _allOrders.value = orders
                }
            }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        ordersCollection.document(orderId).update("status", newStatus)
    }

    fun deleteOrder(orderId: String) {
        ordersCollection.document(orderId).delete()
    }

    override fun onCleared() {
        super.onCleared()
        ordersListener?.remove()
    }
}
