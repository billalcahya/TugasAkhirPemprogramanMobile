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

    private val _categoryResult = MutableLiveData<String>()
    val categoryResult: LiveData<String> = _categoryResult

    private val _categoriesCount = MutableLiveData<Int>(0)
    val categoriesCount: LiveData<Int> = _categoriesCount

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _unitResult = MutableLiveData<String>()
    val unitResult: LiveData<String> = _unitResult

    private val _unitsCount = MutableLiveData<Int>(0)

    private val _units = MutableLiveData<List<UnitModel>>()
    val units: LiveData<List<UnitModel>> = _units

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _productResult = MutableLiveData<String>()
    val productResult: LiveData<String> = _productResult

    private val _userResult = MutableLiveData<String>()
    val userResult: LiveData<String> = _userResult

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val unitList = mutableListOf(
        UnitModel(1, "pcs"),
        UnitModel(2, "gelas"),
        UnitModel(3, "box"),
        UnitModel(4, "kg")
    )

    fun resetProductResult() {
        _productResult.postValue("")
    }

    fun resetCategoryResult(){
        _categoryResult.postValue("")
    }

    fun resetUserResult(){
        _userResult.postValue("")
    }

    init {
        fetchUnitsCount()
        fetchCategoriesCount()
        fetchUsers()
    }

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
                _categoryResult.postValue(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e))
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
                _categoryResult.postValue(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e))
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
                _categoryResult.postValue(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e))
            }
        }
    }

    fun fetchUnitsCount() {
        _unitsCount.postValue(unitList.size)
        _units.postValue(unitList.toList())
    }

    fun fetchProducts(categoryId: Int?, search: String?) {
        viewModelScope.launch {
            try {
                val response = productRepo.getProducts(categoryId, search)
                if (response.isSuccessful && response.body() != null) {
                    val productList = response.body().orEmpty()
                    _products.postValue(productList)

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
                _productResult.postValue(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e))
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
                _productResult.postValue(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e))
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
                _productResult.postValue(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e))
            }
        }
    }

    fun addProductWithImage(context: Context, product: Product, imageUri: Uri?) {
        viewModelScope.launch {
            try {
                val imagePart: MultipartBody.Part? = if (imageUri != null) {
                    val file = UriToFileUtil.getFileFromUri(context, imageUri)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("product_image", file.name, requestFile)
                } else {
                    null
                }

                val productJson = Gson().toJson(product)
                val productPart = MultipartBody.Part.createFormData("product_data", productJson)

                val response = productRepo.addProductMultipart(productPart, imagePart)

                if (response.isSuccessful) {
                    _productResult.postValue("Produk '${product.name}' berhasil ditambahkan dengan gambar!")
                    fetchProducts(null, null)
                } else {
                    _productResult.postValue("Gagal menambah produk: ${response.message()}")
                }
            } catch (e: Exception) {
                _productResult.postValue(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e))
            }
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val response = userRepo.getUsers()
                if (response.isSuccessful && response.body() != null) {
                    val userListData = response.body()!!.data
                    _users.postValue(userListData)
                } else {
                    _userResult.postValue("Gagal memuat user: ${response.message()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("DEBUG_MOKA", "Error fetch users: ${e.message}")
                _userResult.postValue(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e))
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
                _userResult.postValue(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e))
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
                _userResult.postValue(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e))
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
                _userResult.postValue(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e))
            }
        }
    }
}