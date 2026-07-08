package com.mobile.tugasrancangmoka.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.api.Category
import com.mobile.tugasrancangmoka.api.Product
import com.mobile.tugasrancangmoka.repository.ProductRepository
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductRepository(application)

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _selectedProduct = MutableLiveData<Product>()
    val selectedProduct: LiveData<Product> = _selectedProduct

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getProducts()
            _isLoading.value = false
            result.onSuccess { data ->
                _products.value = data
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            val result = repository.getCategories()
            result.onSuccess { data ->
                _categories.value = data
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun loadProduct(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getProduct(id)
            _isLoading.value = false
            result.onSuccess { data ->
                _selectedProduct.value = data
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun filterByCategory(categoryId: Int?) {
        val allProducts = _products.value ?: return
        if (categoryId == null) {
            _products.value = allProducts
        } else {
            _products.value = allProducts.filter { it.category_id == categoryId }
        }
    }
}