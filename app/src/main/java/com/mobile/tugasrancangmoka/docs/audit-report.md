# SmartCafe POS Android Audit Report

Dokumen ini mencatat audit kode, bug yang ditemukan, perbaikan yang dilakukan, dan rekomendasi arsitektur untuk menjaga kualitas kode.

---

## Audit Ke-1: 5 Juli 2026

### 1. Masalah Kompilasi & Bug yang Ditemukan (Sudah Diperbaiki)

*   **Masalah**: Error unresolved reference `textStyle` pada `HistoryFragment.kt` (baris 91 dan 94).
    *   *Analisis*: Kelas `MaterialButton` tidak memiliki properti langsung bernama `textStyle`.
    *   *Perbaikan*: Menggunakan method `setTypeface(null, Typeface.BOLD)` dan `setTypeface(null, Typeface.NORMAL)` untuk mengubah font-weight tombol filter secara dinamis.
*   **Masalah**: Prohibited `return` statement pada `InventoryFragment.kt` (baris 106).
    *   *Analisis*: Penggunaan raw `return` dilarang di dalam lambda block (seperti observer LiveData).
    *   *Perbaikan*: Mengubahnya menjadi label-specific return: `return@observe`.
*   **Masalah**: Error tipe argumen pada inisialisasi list kategori default "All Menu" di `MenuFragment.kt` (baris 98).
    *   *Analisis*: Constructor `Category` membutuhkan `id: Int`, `name: String`, `colorHex: String`, dan `isActive: Boolean`. Pemanggilan kode sebelumnya melewatkan argumen `colorHex`.
    *   *Perbaikan*: Mengubah instansiasi menjadi `Category(0, "All Menu", "#FFFFFF", true)`.
*   **Masalah**: Error nested class 'Class' is prohibited here pada `MenuFragment.kt` (baris 147).
    *   *Analisis*: Kelas `CategoryAdapter` dideklarasikan sebagai `private inner class`, sehingga kelas nested `ViewHolder` di dalamnya juga harus bertipe `inner class` untuk menghindari batasan static nested class.
    *   *Perbaikan*: Mengubah deklarasi `ViewHolder` menjadi `inner class ViewHolder`.

### 2. Fitur yang Belum Lengkap / Hilang (Sudah Diimplementasikan)

*   **Fitur**: Kelas `ReportFragment.kt` tidak ditemukan sama sekali di dalam package `com.mobile.tugasrancangmoka.fragment` meskipun didefinisikan di `nav_graph.xml` dan memiliki template layout XML.
    *   *Perbaikan*: Membuat file `ReportFragment.kt` dari nol yang mengimplementasikan visualisasi data grafik tren penjualan harian dan bulanan dengan library `MPAndroidChart`, fungsionalitas tombol Toggle (Harian/Bulanan), pemilih tanggal (`MaterialDatePicker`), serta state Loading & Error.
*   **Fitur**: Auto Login belum terintegrasi di `SplashActivity.kt`.
    *   *Perbaikan*: Menambahkan pengecekan token JWT via `SessionManager` di dalam `SplashActivity.kt`. Jika token valid (tidak kosong), langsung mengarahkan pengguna ke `MainActivity`, jika kosong diarahkan ke `LoginActivity`.
*   **Fitur**: Tombol Logout dan logikanya belum ada di Dashboard.
    *   *Perbaikan*: Menambahkan tombol `btn_logout` bertipe Material TextButton di kanan atas `fragment_dashboard.xml`. Di dalam `DashboardFragment.kt`, menambahkan listener untuk menghapus session melalui `SessionManager.clearSession()`, mengarahkan ke `LoginActivity`, dan membersihkan tumpukan activity (backstack).

### 3. Kepatuhan Terhadap MVVM & Best Practices

*   **ViewBinding**: Seluruh fragment dan activity telah diverifikasi menggunakan ViewBinding secara aman dengan melakukan nullifikasi reference binding (`_binding = null`) di dalam `onDestroyView()` untuk mencegah memory leaks.
*   **Repository & API**: Semua transaksi jaringan dilakukan melalui class Repository dan diekspos melalui LiveData di ViewModel, mematuhi standar arsitektur POS.
*   **Resource Strings**: Rekomendasi di masa mendatang untuk memindahkan string teks statis (seperti toast dan dialog title) ke `strings.xml` guna mendukung lokalisasi bahasa.

## Audit Ke-2: 12 Juli 2026

### 1. Masalah Kompilasi & Bug yang Ditemukan (Sudah Diperbaiki)

*   **Masalah**: Error unresolved reference `ActivityResultContracts` di `SettingsFragment.kt` (baris 34).
    *   *Analisis*: Package `androidx.activity.result.contract.ActivityResultContracts` tidak diimpor.
    *   *Perbaikan*: Menambahkan import `androidx.activity.result.contract.ActivityResultContracts`.
*   **Masalah**: Error unresolved reference `UriToFileUtil` dan `addProductMultipart` di `SettingsVM.kt`.
    *   *Analisis*: Fungsionalitas upload multipart merupakan draf yang tidak didukung oleh endpoint API backend (yang hanya menerima input JSON standar).
    *   *Perbaikan*: Menghapus fungsi `addProductWithImage` dari `SettingsVM.kt` beserta import yang tidak digunakan, serta mengarahkan aksi CREATE produk di `SettingsFragment.kt` untuk menyimpan file gambar secara persisten di penyimpanan internal aplikasi dan mengirim path URI lokalnya sebagai field `image_url` melalui JSON API standar (`addProduct`).
