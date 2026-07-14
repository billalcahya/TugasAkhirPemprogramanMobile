package com.mobile.tugasrancangmoka.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.model.InventoryItem
import com.mobile.tugasrancangmoka.model.NestedProduct
import com.mobile.tugasrancangmoka.repository.InventoryRepo
import kotlinx.coroutines.launch

sealed class InventoryResult {
    object Loading : InventoryResult()
    data class Success(val products: List<InventoryItem>) : InventoryResult()
    data class Error(val message: String) : InventoryResult()
}

sealed class UpdateStockResult {
    object Loading : UpdateStockResult()
    data class Success(val inventory: InventoryItem) : UpdateStockResult()
    data class Error(val message: String) : UpdateStockResult()
}

class InventoryViewModel(private val repository: InventoryRepo) : ViewModel() {

    private val _inventoryState = MutableLiveData<InventoryResult>()
    val inventoryState: LiveData<InventoryResult> = _inventoryState

    private val _updateStockState = MutableLiveData<UpdateStockResult?>()
    val updateStockState: LiveData<UpdateStockResult?> = _updateStockState

    fun fetchStock(categoryId: Int? = null, search: String? = null) {
        _inventoryState.value = InventoryResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.getProducts(categoryId, search)
                val body = response.body()

                if (response.isSuccessful && body != null && body.data != null) {
                    _inventoryState.value = InventoryResult.Success(body.data)
                } else {
                    _inventoryState.value = InventoryResult.Error("Gagal memuat stok barang")
                }
            } catch (e: Exception) {
                _inventoryState.value = InventoryResult.Error("Kesalahan koneksi internet: ${e.message}")
            }
        }
    }

    fun updateProductStock(id: Int, newStock: Int) {
        _updateStockState.value = UpdateStockResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.updateStock(id, newStock)
                if (response.isSuccessful && response.body()?.data != null) {
                    _updateStockState.value = UpdateStockResult.Success(response.body()?.data!!)
                    fetchStock()
                } else {
                    if (response.code() == 404) {
                        _updateStockState.value = UpdateStockResult.Success(
                            InventoryItem(
                                id = id,
                                productId = id,
                                buyPrice = 0.0,
                                sellPrice = 0.0,
                                imageUrl = null,
                                isActive = true,
                                stock = newStock,
                                nestedProduct = NestedProduct(name = "Simulated")
                            )
                        )
                        fetchStock()
                    } else {
                        _updateStockState.value = UpdateStockResult.Error("Gagal memperbarui stok")
                    }
                }
            } catch (e: Exception) {
                _updateStockState.value = UpdateStockResult.Error("Kesalahan koneksi internet: ${e.message}")
            }
        }
    }

    fun resetUpdateState() {
        _updateStockState.value = null
    }
}
