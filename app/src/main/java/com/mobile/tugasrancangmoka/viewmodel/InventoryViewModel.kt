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
        _inventoryState.postValue(InventoryResult.Loading)
        viewModelScope.launch {
            try {
                val response = repository.getProducts(categoryId, search)
                val body = response.body()

                if (response.isSuccessful && body != null && body.data != null) {
                    val sortedList = body.data.sortedBy { it.name.lowercase(java.util.Locale.ROOT) }
                    _inventoryState.postValue(InventoryResult.Success(sortedList))
                } else {
                    _inventoryState.postValue(InventoryResult.Error("Gagal memuat stok barang"))
                }
            } catch (e: Exception) {
                _inventoryState.postValue(InventoryResult.Error(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e)))
            }
        }
    }

    fun updateProductStock(id: Int, newStock: Int) {
        _updateStockState.postValue(UpdateStockResult.Loading)
        viewModelScope.launch {
            try {
                val response = repository.updateStock(id, newStock)
                if (response.isSuccessful && response.body()?.data != null) {
                    _updateStockState.postValue(UpdateStockResult.Success(response.body()?.data!!))
                    fetchStock()
                } else {
                    if (response.code() == 404) {
                        _updateStockState.postValue(
                            UpdateStockResult.Success(
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
                        )
                        fetchStock()
                    } else {
                        _updateStockState.postValue(UpdateStockResult.Error("Gagal memperbarui stok"))
                    }
                }
            } catch (e: Exception) {
                _updateStockState.postValue(UpdateStockResult.Error(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e)))
            }
        }
    }

    fun resetUpdateState() {
        _updateStockState.postValue(null)
    }
}
