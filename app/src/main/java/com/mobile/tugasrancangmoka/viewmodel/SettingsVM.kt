package com.mobile.tugasrancangmoka.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.model.Category
import com.mobile.tugasrancangmoka.repository.CategoryRepo
import kotlinx.coroutines.launch

class SettingsVM(private val repository: CategoryRepo) : ViewModel() {

    private val _categoryResult = MutableLiveData<String>()
    val categoryResult: LiveData<String> = _categoryResult

    // Menginisialisasi nilai awal 0 agar tidak null/kosong
    private val _categoriesCount = MutableLiveData<Int>(0)
    val categoriesCount: LiveData<Int> = _categoriesCount

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    fun fetchCategoriesCount() {
        viewModelScope.launch {
            try {
                val response = repository.getCategories()
                if (response.isSuccessful && response.body() != null) {
                    // response.body() langsung berupa List<Category>, jadi langsung ambil .size
                    val listData = response.body()!!
                    _categoriesCount.postValue(response.body()!!.size)
                    _categories.postValue(listData)
                }
            } catch (e: Exception) {
                android.util.Log.e("DEBUG_MOKA", "Error fetch count: ${e.message}")
            }
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            try {
                val response = repository.addCategory(name)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _categoryResult.postValue("Kategori '$name' berhasil ditambahkan!")
                    fetchCategoriesCount() // Panggil ulang untuk memuat list terbaru
                } else {
                    _categoryResult.postValue("Gagal: ${response.body()?.message ?: response.message()}")
                }
            } catch (e: Exception) {
                _categoryResult.postValue("Error Koneksi: ${e.message}")
            }
        }
    }

    fun updateCategory(id: Int, newName: String) {
        viewModelScope.launch {
            try {
                val response = repository.updateCategory(id, newName)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _categoryResult.postValue("Kategori berhasil diperbarui!")
                    fetchCategoriesCount() // Memperbarui list ke UI
                } else {
                    _categoryResult.postValue("Gagal: ${response.body()?.message ?: response.message()}")
                }
            } catch (e: Exception) {
                _categoryResult.postValue("Gagal memperbarui: ${e.message}")
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deleteCategory(id)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _categoryResult.postValue("Kategori berhasil dihapus!")
                    fetchCategoriesCount() // Memuat ulang list data kategori terbaru ke UI
                } else {
                    _categoryResult.postValue("Gagal: ${response.body()?.message ?: response.message()}")
                }
            } catch (e: Exception) {
                _categoryResult.postValue("Gagal menghapus: ${e.message}")
            }
        }
    }
}