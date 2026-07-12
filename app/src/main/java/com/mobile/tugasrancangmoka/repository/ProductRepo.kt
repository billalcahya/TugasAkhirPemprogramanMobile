package com.mobile.tugasrancangmoka.repository

import com.mobile.tugasrancangmoka.api.ApiService
import com.mobile.tugasrancangmoka.model.Product
import com.mobile.tugasrancangmoka.model.ProductResponse
import com.mobile.tugasrancangmoka.model.SingleProductResponse
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Part
import okhttp3.ResponseBody
import retrofit2.Response

class ProductRepo(private val apiService: ApiService) {
    suspend fun getProducts(categoryId: Int?, search: String?): Response<List<Product>> {
        return apiService.getProducts(categoryId, search)
    }
    suspend fun addProduct(product: Product) = apiService.addProduct(product)
    suspend fun updateProduct(id: Int, product: Product) = apiService.updateProduct(id, product)
    suspend fun deleteProduct(id: Int) = apiService.deleteProduct(id)

    suspend fun addProductMultipart(
        productData: Part,
        productImage: Part?
    ): Response<SingleProductResponse> { // Sesuaikan tipe data response-nya dengan yang ada di ApiService
        return apiService.addProductMultipart(productData, productImage)
    }
}