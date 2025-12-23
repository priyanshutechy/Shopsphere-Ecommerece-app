package com.example.shopsphere.data.order

sealed class OrderStatus(val status: String) {

    data object Ordered : OrderStatus("Ordered")
    data object Cancelled : OrderStatus("Cancelled")
    data object Confirmed : OrderStatus("Confirmed")
    data object Shipped : OrderStatus("Shipped")
    data object Delivered : OrderStatus("Delivered")
    data object Returned : OrderStatus("Returned")
}

fun getOrderStatus(status: String): OrderStatus {
    return when (status) {
        "Ordered" -> OrderStatus.Ordered
        "Cancelled" -> OrderStatus.Cancelled
        "Confirmed" -> OrderStatus.Confirmed
        "Shipped" -> OrderStatus.Shipped
        "Delivered" -> OrderStatus.Delivered
        else -> OrderStatus.Returned

    }
}