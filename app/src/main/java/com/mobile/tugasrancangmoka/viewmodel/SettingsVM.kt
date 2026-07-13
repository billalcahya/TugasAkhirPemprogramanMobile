package com.mobile.tugasrancangmoka.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mobile.tugasrancangmoka.model.Category
import com.mobile.tugasrancangmoka.model.Product
import com.mobile.tugasrancangmoka.model.User
import com.mobile.tugasrancangmoka.repository.CategoryRepo
import com.mobile.tugasrancangmoka.repository.ProductRepo
import com.mobile.tugasrancangmoka.repository.UserRepo
import com.mobile.tugasrancangmoka.utils.UriToFileUtil
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

data class UnitModel(
    val id: Int,
    val name: String
)

class SettingsVM(
    private val categoryRepo: CategoryRepo,
    private val productRepo: ProductRepo,
    private val userRepo: UserRepo
) : ViewModel() {

    // --- STATE & LIVE DATA CATEGORIES ---
    private val _categoryResult = MutableLiveData<String>()
    val categoryResult: LiveData<String> = _categoryResult

    private val _categoriesCount = MutableLiveData<Int>(0)
    val categoriesCount: LiveData<Int> = _categoriesCount

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    // --- STATE & LIVE DATA UNITS ---
    private val _unitResult = MutableLiveData<String>()
    val unitResult: LiveData<String> = _unitResult

    private val _unitsCount = MutableLiveData<Int>(0)
    val unitsCount: LiveData<Int> = _unitsCount

    private val _units = MutableLiveData<List<UnitModel>>()
    val units: LiveData<List<UnitModel>> = _units

    // --- STATE & LIVE DATA PRODUCTS ---
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _productResult = MutableLiveData<String>()
    val productResult: LiveData<String> = _productResult

    private val _userResult = MutableLiveData<String>()
    val userResult: LiveData<String> = _userResult

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    // In-memory list for Units, initialized with common defaults
    private val unitList = mutableListOf(
        UnitModel(1, "pcs"),
        UnitModel(2, "gelas"),
        UnitModel(3, "box"),
        UnitModel(4, "kg")
    )

    fun resetProductResult() {
        _productResult.value = ""
    }

    fun resetCategoryResult(){
        _categoryResult.value = ""
    }

    fun resetUserResult(){
        _userResult.value = ""
    }

    init {
        fetchUnitsCount()
        fetchCategoriesCount()
        fetchUsers()
    }

    // ==========================================
    // LOGIKA OPERASI CATEGORY
    // ==========================================
    fun fetchCategoriesCount() {
        viewModelScope.launch {
            try {
                val response = categoryRepo.getCategories()
                if (response.isSuccessful && response.body() != null) {
                    val listData = response.body()!!
                    _categoriesCount.postValue(listData.size)
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
                val response = categoryRepo.addCategory(name)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _categoryResult.postValue("Kategori '$name' berhasil ditambahkan!")
                    fetchCategoriesCount()
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
                val response = categoryRepo.updateCategory(id, newName)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _categoryResult.postValue("Kategori berhasil diperbarui!")
                    fetchCategoriesCount()
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
                val response = categoryRepo.deleteCategory(id)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _categoryResult.postValue("Kategori berhasil dihapus!")
                    fetchCategoriesCount()
                } else {
                    _categoryResult.postValue("Gagal: ${response.body()?.message ?: response.message()}")
                }
            } catch (e: Exception) {
                _categoryResult.postValue("Gagal menghapus: ${e.message}")
            }
        }
    }

    // ==========================================
    // LOGIKA OPERASI UNITS
    // ==========================================
    fun fetchUnitsCount() {
        _unitsCount.postValue(unitList.size)
        _units.postValue(unitList.toList())
    }

    fun addUnit(name: String) {
        val newId = (unitList.maxOfOrNull { it.id } ?: 0) + 1
        unitList.add(UnitModel(newId, name))
        _unitResult.postValue("Unit '$name' berhasil ditambahkan!")
        fetchUnitsCount()
    }

    fun updateUnit(id: Int, newName: String) {
        val index = unitList.indexOfFirst { it.id == id }
        if (index != -1) {
            unitList[index] = UnitModel(id, newName)
            _unitResult.postValue("Unit berhasil diperbarui!")
            fetchUnitsCount()
        } else {
            _unitResult.postValue("Unit tidak ditemukan!")
        }
    }

    fun deleteUnit(id: Int) {
        val index = unitList.indexOfFirst { it.id == id }
        if (index != -1) {
            val name = unitList[index].name
            unitList.removeAt(index)
            _unitResult.postValue("Unit '$name' berhasil dihapus!")
            fetchUnitsCount()
        } else {
            _unitResult.postValue("Unit tidak ditemukan!")
        }
    }

    // ==========================================
    // LOGIKA OPERASI PRODUCTS
    // ==========================================
    fun fetchProducts(categoryId: Int?, search: String?) {
        viewModelScope.launch {
            try {
                val response = productRepo.getProducts(categoryId, search)
                if (response.isSuccessful && response.body() != null) {
                    val productList = response.body().orEmpty()
                    _products.postValue(productList)

                    // Ekstrak unit unik dari produk dan masukkan ke unitList jika belum ada
                    var updated = false
                    productList.mapNotNull { it.unit }.distinct().forEach { prodUnit ->
                        val exists = unitList.any { it.name.equals(prodUnit, ignoreCase = true) }
                        if (!exists && prodUnit.trim().isNotEmpty()) {
                            val newId = (unitList.maxOfOrNull { it.id } ?: 0) + 1
                            unitList.add(UnitModel(newId, prodUnit))
                            updated = true
                        }
                    }
                    if (updated) {
                        fetchUnitsCount()
                    }
                } else {
                    _productResult.postValue("Gagal memuat produk: ${response.message()}")
                }
            } catch (e: Exception) {
                _productResult.postValue("Error Koneksi: ${e.message}")
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                val response = productRepo.addProduct(product)
                if (response.isSuccessful) {
                    _productResult.postValue("Produk '${product.name}' berhasil ditambahkan!")
                    fetchProducts(null, null)
                } else {
                    _productResult.postValue("Gagal menambah produk: ${response.message()}")
                }
            } catch (e: Exception) {
                _productResult.postValue("Error: ${e.message}")
            }
        }
    }

    fun updateProduct(id: Int, product: Product) {
        viewModelScope.launch {
            try {
                val response = productRepo.updateProduct(id, product)
                if (response.isSuccessful) {
                    _productResult.postValue("Produk berhasil diperbarui!")
                    fetchProducts(null, null)
                } else {
                    _productResult.postValue("Gagal memperbarui: ${response.message()}")
                }
            } catch (e: Exception) {
                _productResult.postValue("Error: ${e.message}")
            }
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            try {
                val response = productRepo.deleteProduct(id)
                if (response.isSuccessful) {
                    _productResult.postValue("Produk berhasil dihapus!")
                    fetchProducts(null, null)
                } else {
                    _productResult.postValue("Gagal menghapus: ${response.message()}")
                }
            } catch (e: Exception) {
                _productResult.postValue("Error: ${e.message}")
            }
        }
    }

    fun addProductWithImage(context: Context, product: Product, imageUri: Uri?) {
        viewModelScope.launch {
            try {
                // Menyiapkan bagian Multipart untuk Gambar
                val imagePart: MultipartBody.Part? = if (imageUri != null) {
                    val file = UriToFileUtil.getFileFromUri(context, imageUri)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("product_image", file.name, requestFile) // "product_image" adalah nama field di API
                } else {
                    null // Gambar opsional
                }

                // Menyiapkan bagian Multipart untuk Data Produk (Mengubah JSON menjadi String Multipart)
                val productJson = Gson().toJson(product)
                val productPart = MultipartBody.Part.createFormData("product_data", productJson) // "product_data" adalah nama field di API

                // Panggil API dengan tipe Multipart
                val response = productRepo.addProductMultipart(productPart, imagePart)

                if (response.isSuccessful) {
                    _productResult.postValue("Produk '${product.name}' berhasil ditambahkan dengan gambar!")
                    fetchProducts(null, null)
                } else {
                    _productResult.postValue("Gagal menambah produk: ${response.message()}")
                }
            } catch (e: Exception) {
                _productResult.postValue("Error: ${e.message}")
            }
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val response = userRepo.getUsers()
                if (response.isSuccessful && response.body() != null) {
                    // Ambil property '.data' yang bertipe List<User> dari objek pembungkusnya
                    val userListData = response.body()!!.data
                    _users.postValue(userListData)
                } else {
                    _userResult.postValue("Gagal memuat user: ${response.message()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("DEBUG_MOKA", "Error fetch users: ${e.message}")
                _userResult.postValue("Error Koneksi User: ${e.message}")
            }
        }
    }

    fun addUser(userMap: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = userRepo.addUser(userMap)
                if (response.isSuccessful) {
                    _userResult.postValue("User berhasil ditambahkan!")
                    fetchUsers()
                } else {
                    _userResult.postValue("Gagal menambah user: ${response.message()}")
                }
            } catch (e: Exception) {
                _userResult.postValue("Error: ${e.message}")
            }
        }
    }

    fun updateUser(id: Int, userMap: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = userRepo.updateUser(id, userMap)
                if (response.isSuccessful) {
                    _userResult.postValue("Data user berhasil diperbarui!")
                    fetchUsers() // Refresh list user
                } else {
                    _userResult.postValue("Gagal memperbarui user: ${response.message()}")
                }
            } catch (e: Exception) {
                _userResult.postValue("Error: ${e.message}")
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            try {
                val response = userRepo.deleteUser(id)
                if (response.isSuccessful) {
                    _userResult.postValue("User berhasil dihapus!")
                    fetchUsers()
                } else {
                    _userResult.postValue("Gagal menghapus user: ${response.message()}")
                }
            } catch (e: Exception) {
                _userResult.postValue("Error: ${e.message}")
            }
        }
    }
}