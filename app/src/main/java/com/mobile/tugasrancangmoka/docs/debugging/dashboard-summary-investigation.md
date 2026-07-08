# Laporan Investigasi Bug Dashboard Summary

## Ringkasan Investigasi
Ditemukan bahwa nilai **Today's Revenue** dan **Transactions** bernilai `0` serta **Top Selling Products** menampilkan *"No sales data for today"* disebabkan oleh dua faktor utama:
1. **Ketidaksesuaian Kontrak API (Key Mismatch)** antara JSON yang dikembalikan oleh backend dan properti `@SerializedName` pada model Kotlin.
2. **Penyaringan Tanggal (Date Filtering)** di backend yang menggunakan format UTC, di mana transaksi yang ada di database berlokasi di tanggal 4 Juli 2026, sedangkan hari pencarian data adalah 8 Juli 2026.

---

## Detail Tracing Alur Data

### 1. Endpoint yang Dipanggil
* **Endpoint**: `GET dashboard`
* **URL Lengkap**: `http://10.0.2.2:3000/api/v1/dashboard`
* **Definisi di Android**: Mapped melalui `getDashboardData()` pada [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt#L65-L66)
* **Auth**: Memerlukan JWT Bearer Token.

### 2. Response yang Diterima
Backend mengembalikan status **HTTP 200 OK** dengan payload:
```json
{
  "status": "success",
  "message": "Berhasil mengambil data statistik dashboard",
  "data": {
    "total_products": 1,
    "total_categories": 2,
    "total_transactions_today": 0,
    "revenue_today": 0,
    "revenue_this_month": 34000,
    "best_selling_products": [
      {
        "product_name": "Es Kopi Susu",
        "total_quantity_sold": 2
      }
    ],
    "low_stock_alerts_count": 0,
    "low_stock_products": []
  }
}
```

### 3. Parsing Status (Kotlin Models)
Beberapa field gagal dipetakan (deserialized) karena ketidakcocokan kunci JSON:
* [x] **`total_transactions_today`**: Berhasil dipetakan.
* [ ] **`total_revenue_today`**: **Gagal**. Backend mengembalikan `"revenue_today"`, sehingga bernilai default `0.0`.
* [ ] **`top_products`**: **Gagal**. Backend mengembalikan `"best_selling_products"`. Selain itu, field inner `"quantity"` dikirim sebagai `"total_quantity_sold"`.
* [ ] **`low_stock_alerts`**: **Gagal**. Backend mengembalikan `"low_stock_products"`.
* [ ] **`last_7_days_sales`**: **Gagal**. Tidak ada dalam respons backend.

### 4. Data Masuk ke ViewModel
Pada [DashboardVM.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/viewmodel/DashboardVM.kt), data dibungkus dalam state `DashboardResult.Success` dengan nilai:
* `totalRevenueToday` = `0.0`
* `totalTransactionsToday` = `0`
* `topProducts` = `null`
* `lowStockAlerts` = `null`

### 5. Data Masuk ke State
State `dashboardState` (LiveData) memancarkan pembaruan sukses tersebut ke Fragment UI.

### 6. Data Tampil di UI
Pada [DashboardFragment.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/fragment/DashboardFragment.kt):
* `textRevenueToday` -> `Rp0` (karena parsing gagal)
* `textTransactionsToday` -> `0` (karena hasil query 0)
* `rvTopProducts` tersembunyi, `textEmptyTopProducts` tampil -> *"No sales data for today"*
* `rvLowStock` tersembunyi (berfungsi normal tanpa crash, menampilkan status kosong).

---

## Analisis Penyebab Bug
1. **Penyebab Utama 1 (Key Mismatch)**:
   Backend tidak mematuhi kontrak spesifikasi API yang disepakati (sebagaimana tercantum dalam `docs/api-integration.md`).
2. **Penyebab Utama 2 (Logika Filter Tanggal)**:
   Database hanya berisi transaksi bertanggal `2026-07-04`, sedangkan hari pengujian adalah `2026-07-08`. Oleh karena itu, backend mengembalikan nilai hari ini (`revenue_today` dan `total_transactions_today`) bernilai `0`.

---

## File yang Bermasalah
1. **Android Model**:
   * [DashboardResponse.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/DashboardResponse.kt)
2. **Backend Service**:
   * [dashboardService.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMokaBE/services/dashboardService.js)

---

## Solusi yang Disarankan
Ubah return object dari fungsi `getSummaryStats()` di [dashboardService.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMokaBE/services/dashboardService.js) agar sesuai dengan kontrak API Android:

```javascript
return {
  total_products: totalProducts || 0,
  total_categories: totalCategories || 0,
  total_transactions_today: totalTransactionsToday,
  total_revenue_today: revenueToday,
  revenue_this_month: revenueThisMonth,
  last_7_days_sales: [], // Stub atau isi dengan data real
  top_products: bestSellingProducts.map(p => ({
    product_name: p.product_name,
    quantity: p.total_quantity_sold
  })),
  low_stock_alerts: alertItems.map(item => ({
    product_name: item.products?.name,
    current_stock: item.current_stock
  }))
};
```
