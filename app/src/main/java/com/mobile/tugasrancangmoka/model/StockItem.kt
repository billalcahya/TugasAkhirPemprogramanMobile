package com.mobile.tugasrancangmoka.model

data class StockItem(
    val id: Int,
    val name: String,
    val subtitle: String,
    val stockPercentage: Int,
    val status: StockStatus
)