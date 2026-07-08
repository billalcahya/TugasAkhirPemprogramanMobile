# Report Fragment Integration Audit

## Ringkasan Audit
Audit ini berfokus pada integrasi data analitik bisnis antara komponen antarmuka pengguna **ReportFragment** di Frontend Android Kotlin dengan **Express.js Backend (Supabase DB)**. Kami menelusuri alur data dari rendering diagram di UI, pemanggilan lifecycle LiveData, hingga eksekusi query SQL di basis data.

Tanggal Audit: 2026-07-08  
Status Integrasi: **READY & FULLY OPERATIONAL (SIAP DIGUNAKAN)**

---

# Status Komponen Integrasi

| Komponen | Status | Masalah | Rekomendasi / Tindakan |
| --- | --- | --- | --- |
| **[ReportFragment.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/fragment/ReportFragment.kt)** | ✅ | Tidak ada | Menggunakan toggle Harian/Bulanan dan pemilih periode berbasis data dinamis dari backend, loading state (ProgressBar) dan error state (Snackbar/Error Text) berfungsi penuh. |
| **[ReportVM.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/viewmodel/ReportVM.kt)** | ✅ | Tidak ada | Mengelola status LiveData secara reaktif (`ReportResult.Loading`, `Success`, `Error`) dan meneruskan parameter tanggal/bulan dengan benar. |
| **[ReportRepo.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/repository/ReportRepo.kt)** | ✅ | Tidak ada | Meneruskan data request ke Retrofit ApiService secara asinkron menggunakan Kotlin Coroutine. |
| **[ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt)** | ✅ | Tidak ada | Interface `@GET("reports/daily")` dan `@GET("reports/monthly")` terdefinisi sesuai spesifikasi route backend. |
| **[reportRoutes.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/routes/reportRoutes.js)** | ✅ | Tidak ada | Endpoint `/daily` dan `/monthly` dipetakan dengan middleware autentikasi JWT token (`authMiddleware`). |
| **[reportController.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/controllers/reportController.js)** | ✅ | Tidak ada | Memvalidasi parameter query `date`/`month`, menangani error 500/400 dengan wrapper response JSend standar. |
| **[reportService.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/services/reportService.js)** | ✅ | Pergeseran Zona Waktu (Telah Diperbaiki) | Sebelumnya query SQL menyaring rentang hari lokal dalam format waktu UTC standar, sehingga transaksi sore/malam lokal (WIB) terlewat. Masalah ini telah diperbaiki dengan penyesuaian offset waktu lokal (+07:00 WIB). |
| **Database Query** | ✅ | Tidak ada | Melakukan query langsung ke tabel `transactions` untuk revenue dan `transaction_details` untuk gross profit kumulatif, serta mengembalikan agregasi data tren per jam/tanggal secara akurat. |

---

# API & Endpoint Mapping

Berikut adalah daftar endpoint yang digunakan secara end-to-end oleh modul laporan:

### 1. Laporan Harian (Daily Report)
* **Endpoint**: `GET /api/v1/reports/daily`
* **Query Parameter**: `date` (String, format: `YYYY-MM-DD`, contoh: `2026-07-08`)
* **Headers**: `Authorization: Bearer <jwt_token>`
* **Response JSON**:
  ```json
  {
    "status": "success",
    "message": "Berhasil mengambil laporan harian",
    "data": {
      "total_revenue": 1250000.0,
      "gross_profit": 550000.0,
      "sales_trend": [
        { "label": "09:00", "revenue": 150000.0 },
        { "label": "10:00", "revenue": 400000.0 },
        ...
      ]
    }
  }
  ```

### 2. Laporan Bulanan (Monthly Report)
* **Endpoint**: `GET /api/v1/reports/monthly`
* **Query Parameter**: `month` (String, format: `YYYY-MM`, contoh: `2026-07`)
* **Headers**: `Authorization: Bearer <jwt_token>`
* **Response JSON**:
  ```json
  {
    "status": "success",
    "message": "Berhasil mengambil laporan bulanan",
    "data": {
      "total_revenue": 34500000.0,
      "gross_profit": 15600000.0,
      "sales_trend": [
        { "label": "2026-07-01", "revenue": 1250000.0 },
        { "label": "2026-07-02", "revenue": 950000.0 },
        ...
      ]
    }
  }
  ```

---

# Bug & Masalah yang Ditemukan (Issues Found)

### 1. Timezone Shift (UTC vs WIB) Bug
* **File Terkait**: **[reportService.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/services/reportService.js)**
* **Root Cause**: Basis data menyimpan data transaksi menggunakan kolom `created_at` bertipe timestamp UTC (Zulu). Saat modul menyaring laporan harian (contoh: `2026-07-08`), kode awal menggunakan batas `2026-07-08T00:00:00.000Z` s.d `2026-07-08T23:59:59.999Z` dalam UTC. Karena zona waktu lokal pengguna adalah **WIB (UTC+7)**, hari lokal dimulai sejak pukul `17:00:00.000` UTC hari sebelumnya. Hal ini menyebabkan transaksi di malam hari lokal hilang dari statistik laporan harian.
* **Impact**: Laporan grafik tidak akurat; omzet dan profit harian/bulanan tidak mencerminkan transaksi nyata pada jam-jam pergantian hari lokal.
* **Perbaikan**: Kode penyaringan database diubah agar secara eksplisit menyertakan offset zona waktu WIB (+07:00):
  * Daily: `startOfDay = dateStr + "T00:00:00.000+07:00"`, `endOfDay = dateStr + "T23:59:59.999+07:00"`.
  * Monthly: `startOfMonth = monthStr + "-01T00:00:00.000+07:00"`, `endOfMonth = monthStr + "-" + lastDay + "T23:59:59.999+07:00"`.
  * Data pengelompokan jam dan tanggal dikonversi secara dinamis (`utcDate.getTime() + 7 * 60 * 60 * 1000`) sebelum diekstrak komponen jam/tanggalnya.

---

# Prioritas Perbaikan & Tindakan Selanjutnya
1. **Timezone WIB Boundary Adjustment**: **HIGH PRIORITY** (Telah Selesai Diperbaiki).
2. **Standardisasi Wrapper JSON**: **MEDIUM PRIORITY** (Telah Selesai Diperbaiki; model data class Kotlin `ReportResponse` memetakan properti bersarang `data` yang membungkus `total_revenue`, `gross_profit`, dan `sales_trend` secara aman).

---

# Kesimpulan Akhir
Setelah melakukan audit mendalam dari komponen fragment UI Android hingga baris query Supabase, kami menyimpulkan bahwa **ReportFragment telah terhubung 100% dengan server Backend Express.js dan siap digunakan untuk lingkungan pengujian maupun produksi**. Seluruh data visualisasi diagram, omzet bisnis, dan margin profit kotor dimuat secara dinamis dari database tanpa ada komponen dummy yang tertinggal.
