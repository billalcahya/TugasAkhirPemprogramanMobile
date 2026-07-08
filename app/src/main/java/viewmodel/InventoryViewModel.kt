package com.mobile.tugasrancangmoka.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.api.InventoryItem
import com.mobile.tugasrancangmoka.repository.InventoryRepository
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository(application)

    private val _inventory = MutableLiveData<List<InventoryItem>>()
    val inventory: LiveData<List<InventoryItem>> = _inventory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _updateResult = MutableLiveData<String>()
    val updateResult: LiveData<String> = _updateResult

    private var allInventory: List<InventoryItem> = emptyList()

    fun loadInventory() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getInventory()
            _isLoading.value = false
            result.onSuccess { data ->
                allInventory = data
                _inventory.value = data
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun updateStock(productId: Int, newStock: Int, notes: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.updateStock(productId, newStock, notes)
            _isLoading.value = false
            result.onSuccess {
                _updateResult.value = "Stok berhasil diperbarui"
                loadInventory()
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun filterAll() {
        _inventory.value = allInventory
    }

    fun filterLowStock() {
        _inventory.value = allInventory.filter {
            it.current_stock <= it.min_stock && it.current_stock > 0
        }
    }

    fun filterOutOfStock() {
        _inventory.value = allInventory.filter {
            it.current_stock == 0
        }
    }
}