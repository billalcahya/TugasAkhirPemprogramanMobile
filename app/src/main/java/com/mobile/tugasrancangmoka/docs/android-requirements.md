# SmartCafe POS Android Requirements

Project menggunakan:

* Kotlin
* Android Native
* MVVM Architecture
* Retrofit
* ViewModel
* LiveData / StateFlow
* Repository Pattern
* Navigation Component
* Material Design 3
* MPAndroidChart

---

## Cara Audit

Untuk setiap requirement, tentukan status berikut:

* ✅ Sudah selesai dan berfungsi
* ⚠️ Sudah dibuat tetapi belum lengkap / perlu revisi
* ❌ Belum dibuat

Untuk setiap poin jelaskan:

* File yang mengimplementasikan fitur
* Apa yang sudah benar
* Apa yang masih kurang
* Bug atau potensi masalah
* Rekomendasi perbaikan sesuai best practice Android (MVVM)

---

# Checklist Requirement

## 1. Network Layer

Pastikan project memiliki:

* ApiClient.kt
* ApiService.kt
* AuthInterceptor
* Retrofit Configuration
* Gson/Moshi Converter
* Logging Interceptor
* Timeout Configuration
* Error Handling

---

## 2. Authentication

Pastikan tersedia:

* AuthRepository
* AuthViewModel
* LoginActivity
* Login UI
* JWT Token Storage
* Auto Login
* Logout
* Session Management

Periksa alur login dari UI hingga backend.

---

## 3. Product Module

Pastikan tersedia:

* ProductRepository
* ProductViewModel
* MenuFragment
* Load Product List
* Product Detail
* Search Product (jika ada)
* Error Handling

---

## 4. Category Module

Pastikan tersedia:

* CategoryRepository
* Category ViewModel (jika digunakan)
* Wiring ke MenuFragment
* Filter berdasarkan kategori

---

## 5. POS Module

Periksa:

* POSViewModel
* POSFragment
* Cart Logic
* Add Item
* Remove Item
* Update Quantity
* Total Price
* Empty Cart
* State Management

---

## 6. Checkout Module

Periksa:

* CheckoutViewModel
* CheckoutBottomSheet
* Perhitungan subtotal
* Pajak (jika ada)
* Diskon (jika ada)
* Total pembayaran
* Validasi checkout
* Integrasi POST /transactions

---

## 7. Inventory Module

Periksa:

* InventoryRepository
* InventoryViewModel
* Inventory Fragment
* Load Stock
* Update Stock
* Error Handling

---

## 8. Transaction History

Periksa:

* TransactionRepository
* TransactionViewModel
* History Screen
* Detail Transaction
* Void Transaction (jika tersedia)

---

## 9. Reports

Periksa:

* ReportRepository
* ReportViewModel
* MPAndroidChart Integration
* Daily Report
* Monthly Report
* Loading State
* Empty State

---

## 10. Dashboard

Periksa:

* DashboardViewModel
* DashboardFragment
* Dashboard Summary
* Revenue
* Total Transaction
* Best Selling Product
* Statistics Card

---

## 11. End-to-End Integration Testing

Pastikan seluruh flow berjalan:

* Login
* Load Categories
* Load Products
* Add to Cart
* Checkout
* Update Stock
* Transaction History
* Dashboard
* Report

Tidak boleh ada crash maupun error API.

---

## 12. Bug Fix & Cleanup

Audit:

* Crash
* Memory Leak
* Duplicate Code
* Dead Code
* Hardcoded String
* Hardcoded Color
* Hardcoded Dimension
* Unused Import
* Unused Resource
* Error Handling
* Loading State
* Empty State
* Offline Handling (jika ada)

---

# Code Quality Review

Selain checklist di atas, audit juga:

* MVVM Architecture
* Repository Pattern
* Separation of Concern
* Clean Code
* Naming Convention
* Dependency Injection (jika digunakan)
* Retrofit Best Practice
* ViewBinding
* State Management
* Lifecycle Awareness
* Coroutines
* Error Handling
* Loading Indicator
* RecyclerView Performance
* Adapter Optimization
* Navigation
* UI Consistency
* Material Design Guideline

---

# Format Jawaban

Gunakan tabel berikut:

| Requirement | Status | Keterangan              | File             |
| ----------- | ------ | ----------------------- | ---------------- |
| ApiClient   | ✅      | Lengkap                 | ApiClient.kt     |
| Login       | ⚠️     | Session belum tersimpan | LoginActivity.kt |
| Dashboard   | ❌      | Belum dibuat            | -                |

---

# Ringkasan

Di akhir audit tampilkan:

* Total requirement
* Jumlah yang sudah selesai
* Jumlah yang perlu revisi
* Jumlah yang belum dibuat
* Persentase progres project

Kemudian buat daftar prioritas implementasi dari yang paling penting hingga yang paling akhir.

Jika menemukan requirement yang belum selesai, implementasikan satu per satu sesuai urutan prioritas.

Setiap selesai mengimplementasikan fitur:

* Update `docs/android-implementation-status.md`
* Update `docs/android-audit-report.md`
* Update `docs/android-changelog.md`
* Update `docs/android-api-integration.md` jika ada perubahan integrasi API
* Jangan mengubah `docs/android-requirements.md` kecuali saya memintanya secara eksplisit.
