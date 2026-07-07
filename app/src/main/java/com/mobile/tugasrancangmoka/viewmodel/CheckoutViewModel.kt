package com.mobile.tugasrancangmoka.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.model.CheckoutItem
import com.mobile.tugasrancangmoka.model.CheckoutRequest
import com.mobile.tugasrancangmoka.model.CheckoutResponse
import com.mobile.tugasrancangmoka.repository.TransactionRepo
import kotlinx.coroutines.launch
import retrofit2.Response

sealed class CheckoutResult {
    object Loading : CheckoutResult()
    data class Success(val response: CheckoutResponse) : CheckoutResult()
    data class Error(val message: String) : CheckoutResult()
}

class CheckoutViewModel(private val repository: TransactionRepo) : ViewModel() {

    private val _checkoutState = MutableLiveData<CheckoutResult>()
    val checkoutState: LiveData<CheckoutResult> = _checkoutState

    // Fungsi utama mengirim transaksi ke server backend
    fun processCheckout(
        cartItems: List<com.mobile.tugasrancangmoka.model.CartItem>,
        discount: Double,
        paymentMethod: String,
        paymentAmount: Double
    ) {
        _checkoutState.value = CheckoutResult.Loading

        // Konversi item keranjang lokal menjadi objek CheckoutItem API
        val checkoutItems = cartItems.map {
            CheckoutItem(productId = it.product.id, quantity = it.quantity)
        }

        val request = CheckoutRequest(
            items = checkoutItems,
            discountAmount = discount,
            paymentMethod = paymentMethod,
            paymentAmount = paymentAmount
        )

        viewModelScope.launch {
            try {
                val response = repository.checkout(request)
                if (response.isSuccessful && response.body() != null) {
                    _checkoutState.value = CheckoutResult.Success(response.body()!!)
                } else {
                    // Menangkap pesan error spesifik dari backend (misal: "Stok tidak mencukupi")
                    val errorMsg = response.errorBody()?.string() ?: "Transaksi gagal diproses"
                    _checkoutState.value = CheckoutResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                _checkoutState.value = CheckoutResult.Error("Masalah jaringan: ${e.message}")
            }
        }
    }
}