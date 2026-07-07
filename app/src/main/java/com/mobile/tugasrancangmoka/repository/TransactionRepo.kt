package com.mobile.tugasrancangmoka.repository

import com.mobile.tugasrancangmoka.api.ApiService
import com.mobile.tugasrancangmoka.model.CheckoutRequest
import com.mobile.tugasrancangmoka.model.CheckoutResponse
import com.mobile.tugasrancangmoka.model.TransactionDetailResponse
import com.mobile.tugasrancangmoka.model.TransactionResponse
import com.mobile.tugasrancangmoka.model.VoidRequest
import retrofit2.Response

class TransactionRepo(private val apiService: ApiService) {
    suspend fun checkout(request: CheckoutRequest): Response<CheckoutResponse> {
        return apiService.checkout(request)
    }
    suspend fun getTransactions(status: String?): Response<TransactionResponse> {
        return apiService.getTransactions(status)
    }

    suspend fun getTransactionDetail(id: Int): Response<TransactionDetailResponse> {
        return apiService.getTransactionDetail(id)
    }

    suspend fun voidTransaction(id: Int, reason: String): Response<CheckoutResponse> {
        return apiService.voidTransaction(id, VoidRequest(reason))
    }
}