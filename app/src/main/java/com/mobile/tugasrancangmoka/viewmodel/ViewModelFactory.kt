package com.mobile.tugasrancangmoka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobile.tugasrancangmoka.repository.*

class ViewModelFactory(private val repository: Any) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthVM::class.java) -> {
                AuthVM(repository as AuthRepo) as T
            }
            modelClass.isAssignableFrom(CategoryVM::class.java) -> {
                CategoryVM(repository as CategoryRepo) as T
            }
            modelClass.isAssignableFrom(POSViewModel::class.java) -> {
                POSViewModel(repository as ProductRepo) as T
            }
            modelClass.isAssignableFrom(DashboardVM::class.java) -> {
                DashboardVM(repository as DashboardRepo) as T
            }
            modelClass.isAssignableFrom(CheckoutViewModel::class.java) -> {
                CheckoutViewModel(repository as TransactionRepo) as T
            }
            modelClass.isAssignableFrom(ReportVM::class.java) -> {
                ReportVM(repository as ReportRepo) as T
            }
            modelClass.isAssignableFrom(TransactionVM::class.java) -> {
                TransactionVM(repository as TransactionRepo) as T
            }
            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> {
                InventoryViewModel(repository as InventoryRepo) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
