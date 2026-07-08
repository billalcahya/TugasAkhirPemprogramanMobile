package com.mobile.tugasrancangmoka.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.api.CartItemRequest
import com.mobile.tugasrancangmoka.api.CheckoutResponse
import com.mobile.tugasrancangmoka.api.CreateTransactionRequest
import com.mobile.tugasrancangmoka.api.Product
import com.mobile.tugasrancangmoka.repository.ProductRepository
import com.mobile.tugasrancangmoka.repository.TransactionRepository
import kotlinx.coroutines.launch

data class CartItem(
    val product: Product,
    var quantity: Int = 1
) {
    val subtotal: Double get() = product.sell_price * quantity
}

class POSViewModel(application: Application) : AndroidViewModel(application) {

    private val productRepository = ProductRepository(application)
    private val transactionRepository = TransactionRepository(application)

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _cart = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cart: LiveData<MutableList<CartItem>> = _cart

    private val _totalPrice = MutableLiveData<Double>(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    private val _cartItemCount = MutableLiveData<Int>(0)
    val cartItemCount: LiveData<Int> = _cartItemCount

    private val _checkoutResult = MutableLiveData<CheckoutResponse>()
    val checkoutResult: LiveData<CheckoutResponse> = _checkoutResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = productRepository.getProducts()
            _isLoading.value = false
            result.onSuccess { data ->
                _products.value = data.filter { it.is_active != false }
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun addToCart(product: Product) {
        val currentCart = _cart.value ?: mutableListOf()
        val existing = currentCart.find { it.product.id == product.id }
        if (existing != null) {
            existing.quantity++
        } else {
            currentCart.add(CartItem(product))
        }
        _cart.value = currentCart
        updateTotals()
    }

    fun removeFromCart(productId: Int) {
        val currentCart = _cart.value ?: mutableListOf()
        currentCart.removeAll { it.product.id == productId }
        _cart.value = currentCart
        updateTotals()
    }

    fun updateQuantity(productId: Int, quantity: Int) {
        val currentCart = _cart.value ?: mutableListOf()
        val item = currentCart.find { it.product.id == productId }
        if (item != null) {
            if (quantity <= 0) {
                currentCart.remove(item)
            } else {
                item.quantity = quantity
            }
        }
        _cart.value = currentCart
        updateTotals()
    }

    fun increaseQuantity(productId: Int) {
        val currentCart = _cart.value ?: return
        val item = currentCart.find { it.product.id == productId } ?: return
        item.quantity++
        _cart.value = currentCart
        updateTotals()
    }

    fun decreaseQuantity(productId: Int) {
        val currentCart = _cart.value ?: return
        val item = currentCart.find { it.product.id == productId } ?: return
        if (item.quantity > 1) {
            item.quantity--
        } else {
            currentCart.remove(item)
        }
        _cart.value = currentCart
        updateTotals()
    }

    fun clearCart() {
        _cart.value = mutableListOf()
        updateTotals()
    }

    fun checkout(
        discountAmount: Double = 0.0,
        paymentMethod: String,
        paymentAmount: Double
    ) {
        val currentCart = _cart.value
        if (currentCart.isNullOrEmpty()) {
            _error.value = "Keranjang kosong"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val request = CreateTransactionRequest(
                items = currentCart.map { CartItemRequest(it.product.id, it.quantity) },
                discountAmount = discountAmount,
                paymentMethod = paymentMethod,
                paymentAmount = paymentAmount
            )
            val result = transactionRepository.checkout(request)
            _isLoading.value = false
            result.onSuccess { data ->
                _checkoutResult.value = data
                clearCart()
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    private fun updateTotals() {
        val currentCart = _cart.value ?: mutableListOf()
        _totalPrice.value = currentCart.sumOf { it.subtotal }
        _cartItemCount.value = currentCart.sumOf { it.quantity }
    }
}