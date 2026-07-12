# SmartCafe POS Android Changelog

Semua perubahan kode pada project SmartCafe POS Android dicatat dalam dokumen ini berdasarkan urutan kronologis.

---

## [1.0.1] - 2026-07-12

### Fixed
*   **SettingsFragment.kt**: Memperbaiki error kompilasi dengan mengimpor `ActivityResultContracts` dan mengarahkan penyimpanan gambar produk yang dipilih ke direktori penyimpanan internal aplikasi sebelum melakukan POST/PUT via JSON API.
*   **SettingsVM.kt**: Memperbaiki error kompilasi dengan menghapus fungsi `addProductWithImage` multipart beserta dependensi unresolved reference (`UriToFileUtil` & `addProductMultipart`) yang tidak didukung backend API.

## [1.0.0] - 2026-07-05

### Added
*   **ReportFragment.kt**: Mengimplementasikan kelas fragment analitik bisnis yang menampilkan grafik tren penjualan menggunakan `MPAndroidChart` dan toggle laporan Harian/Bulanan dengan DatePicker.
*   **Auto Login**: Menambahkan verifikasi status login di `SplashActivity.kt` menggunakan `SessionManager`. Pengguna yang sudah login langsung diarahkan ke `MainActivity`.
*   **Logout Button**: Menambahkan elemen UI tombol Logout di `fragment_dashboard.xml` dan click listener di `DashboardFragment.kt` yang memanggil `SessionManager.clearSession()`.

### Fixed
*   **HistoryFragment.kt**: Memperbaiki error kompilasi unresolved reference `textStyle` pada MaterialButton dengan mengubahnya menjadi `setTypeface(null, Typeface.BOLD/NORMAL)`.
*   **InventoryFragment.kt**: Memperbaiki runtime compile error *return is prohibited* pada observer LiveData `updateStockState` dengan mengubahnya menjadi `return@observe`.
*   **MenuFragment.kt**: Memperbaiki pemanggilan constructor `Category` yang kekurangan argumen `colorHex` dengan menambahkan parameter warna default `#FFFFFF`.
*   **MenuFragment.kt**: Memperbaiki error deklarasi static nested class `ViewHolder` di dalam `CategoryAdapter` dengan menambahkan kata kunci `inner class`.
