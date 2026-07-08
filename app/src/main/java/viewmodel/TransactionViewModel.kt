package com.mobile.tugasrancangmoka.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.api.Transaction
import com.mobile.tugasrancangmoka.repository.TransactionRepository
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(application)

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _selectedTransaction = MutableLiveData<Transaction>()
    val selectedTransaction: LiveData<Transaction> = _selectedTransaction

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadTransactions() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getTransactions()
            _isLoading.value = false
            result.onSuccess { data ->
                _transactions.value = data
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun loadTransaction(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getTransaction(id)
            _isLoading.value = false
            result.onSuccess { data ->
                _selectedTransaction.value = data
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun filterByDate(date: String) {
        val all = _transactions.value ?: return
        _transactions.value = all.filter {
            it.created_at?.startsWith(date) == true
        }
    }

    fun filterByStatus(status: String) {
        val all = _transactions.value ?: return
        _transactions.value = all.filter {
            it.status == status
        }
    }
}