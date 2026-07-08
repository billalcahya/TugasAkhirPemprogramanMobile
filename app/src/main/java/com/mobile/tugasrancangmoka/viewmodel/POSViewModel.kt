package com.mobile.tugasrancangmoka.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.model.CartItem
import com.mobile.tugasrancangmoka.model.Product
import com.mobile.tugasrancangmoka.repository.ProductRepo
import kotlinx.coroutines.launch

sealed class ProductResult {
    object Loading : ProductResult()
    data class Success(val products: List<Product>) : ProductResult()
    data class Error(val message: String) : ProductResult()
}

class POSViewModel(private val productRepo: ProductRepo) : ViewModel() {

    private val _productState = MutableLiveData<ProductResult>()
    val productState: LiveData<ProductResult> = _productState

    // State untuk keranjang belanja belanja lokal
    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    fun fetchProducts(categoryId: Int? = null, search: String? = null) {
        _productState.value = ProductResult.Loading
        viewModelScope.launch {
            try {
                val response = productRepo.getProducts(categoryId, search)
                if (response.isSuccessful && response.body() != null) {
                    _productState.value = ProductResult.Success(response.body()?.data.orEmpty())
                } else {
                    _productState.value = ProductResult.Error("Gagal memuat produk")
                }
            } catch (e: Exception) {
                _productState.value = ProductResult.Error("Koneksi internet bermasalah")
            }
        }
    }

    fun addToCart(product: Product) {
        val currentList = _cartItems.value.orEmpty().toMutableList()
        val existingItem = currentList.find { it.product.id == product.id }

        if (existingItem != null) {
            existingItem.quantity += 1
        } else {
            currentList.add(CartItem(product, 1))
        }
        _cartItems.value = currentList
    }

    fun decreaseQuantity(product: Product) {
        val currentList = _cartItems.value.orEmpty().toMutableList()
        val existingItem = currentList.find { it.product.id == product.id }

        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                existingItem.quantity -= 1
            } else {
                currentList.remove(existingItem)
            }
            _cartItems.value = currentList
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun getTotalPayment(): Double {
        return _cartItems.value.orEmpty().sumOf { it.subtotal }
    }
}