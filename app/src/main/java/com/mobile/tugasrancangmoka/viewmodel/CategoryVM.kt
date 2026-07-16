package com.mobile.tugasrancangmoka.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.model.Category
import com.mobile.tugasrancangmoka.repository.CategoryRepo
import kotlinx.coroutines.launch

sealed class CategoryResult {
    object Loading : CategoryResult()
    data class Success(val categories: List<Category>) : CategoryResult()
    data class Error(val message: String) : CategoryResult()
}

class CategoryVM(private val repository: CategoryRepo) : ViewModel() {

    private val _categoryState = MutableLiveData<CategoryResult>()
    val categoryState: LiveData<CategoryResult> = _categoryState

    fun fetchCategories() {
        _categoryState.postValue(CategoryResult.Loading)
        viewModelScope.launch {
            try {
                val response = repository.getCategories()
                if (response.isSuccessful && response.body() != null) {
                    _categoryState.postValue(CategoryResult.Success(response.body()!!))
                } else {
                    _categoryState.postValue(CategoryResult.Error("Gagal mengambil data kategori"))
                }
            } catch (e: Exception) {
                _categoryState.postValue(CategoryResult.Error(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e)))
            }
        }
    }
}