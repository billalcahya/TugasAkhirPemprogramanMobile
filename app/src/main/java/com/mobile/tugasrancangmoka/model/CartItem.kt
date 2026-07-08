package com.mobile.tugasrancangmoka.model

data class CartItem(
    val product: Product,
    var quantity: Int
) {
    val totalPrice: Double
        get() = product.price * quantity
}
