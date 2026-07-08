# Backend Frontend Integration Audit

## Project Information

* **Backend**: Express.js (Supabase DB) - Remote Branch: `origin/feature/backend-express` (Commit: `b00f0ac`)
* **Frontend**: Android Native (Kotlin, Retrofit, MVVM) - Local Branch: `feature/backend`
* **Date Audit**: 2026-07-08
* **Base URL**: 
  * Development (Emulator): `http://10.0.2.2:3000/api/v1/`
  * Production: `https://smartcafe-api.vercel.app/api/v1/`

---

# API Mapping

### 1. Backend (BE) Endpoints Available
Tabel di bawah ini memetakan seluruh endpoint yang terdaftar pada struktur router backend Express.js (`routes/`):

| Endpoint | Method | Fungsi | Request | Response | Status |
| -------- | ------ | ------ | ------- | -------- | ------ |
| `/auth/login` | `POST` | Autentikasi user / login | Body: `{ email, password }` | JSON JSend Object (Token & User profile) | Aktif |
| `/categories` | `GET` | Mengambil seluruh daftar kategori | None | JSON JSend Object (List kategori) | Aktif (Mismatch FE) |
| `/categories` | `POST` | Menambahkan kategori baru | Body: `{ name, color_hex, icon_name }` | JSON JSend Object (Kategori baru) | Aktif (Belum dipakai FE) |
| `/products` | `GET` | Mengambil seluruh produk aktif | None | JSON JSend Object (List produk) | Aktif (Mismatch FE) |
| `/products` | `POST` | Menambahkan produk baru & stok awal | Body: `{ category_id, name, sell_price, cost_price, image_url, initial_stock, min_stock, unit }` | JSON JSend Object (Produk baru) | Aktif (Belum dipakai FE) |
| `/inventory` | `GET` | Mengambil data inventori produk | None | JSON JSend Object (List inventori) | Aktif (Belum dipakai FE) |
| `/inventory/:product_id` | `PUT` | Memperbarui stok produk secara manual | Body: `{ current_stock }` | JSON JSend Object (Inventori baru) | Aktif (Mismatch FE) |
| `/transactions` | `POST` | Memproses transaksi checkout belanja | Body: `{ items: [{ productId, quantity }], discountAmount, paymentMethod, paymentAmount }` | JSON JSend Object (Summary transaksi) | Aktif |
| `/transactions` | `GET` | Mengambil seluruh riwayat transaksi | None | JSON JSend Object (List transaksi) | Aktif (Partial mismatch) |
| `/transactions/:id` | `GET` | Mengambil detail item transaksi | Path parameter: `id` | JSON JSend Object (List item transaksi) | Aktif (Mismatch FE) |
| `/transactions/:id/void` | `PUT` | Membatalkan transaksi (void) | Path parameter: `id`, Body: `{ void_reason }` | JSON JSend Object (Transaksi void) | Aktif |
| `/dashboard/summary` | `GET` | Mengambil ringkasan statistik dashboard | None | JSON JSend Object (Summary stats) | Aktif (Mismatch FE) |

---

### 2. Frontend (FE) API Calls Implementation
Tabel di bawah ini mencatat pemanggilan API yang diimplementasikan pada file [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt) di Frontend Android:

| File FE | Function | Endpoint | Method | Data dikirim | Data diterima |
| ------- | -------- | -------- | ------ | ------------ | ------------- |
| [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt) | `login` | `auth/login` | `POST` | Body: [LoginRequest](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/LoginRequest.kt) | [LoginResponse](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/LoginResponse.kt) |
| [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt) | `getCategories` | `categories` | `GET` | None | `List<`[Category](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/Category.kt)`>` (Direct Array) |
| [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt) | `getProducts` | `products` | `GET` | Query: `category` (Int?), `search` (String?) | `List<`[Product](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/Product.kt)`>` (Direct Array) |
| [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt) | `checkout` | `transactions` | `POST` | Body: [CheckoutRequest](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/CheckoutRequest.kt) | [CheckoutResponse](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/CheckoutResponse.kt) |
| [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt) | `updateStock` | `products/{id}/stock` | `PUT` | Path: `id` (Int), Body: [UpdateStockRequest](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/UpdateStockRequest.kt) | [Product](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/Product.kt) (Direct Object) |
| [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt) | `getTransactions` | `transactions` | `GET` | Query: `status` (String?) | [TransactionResponse](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/TransactionHistoryResponse.kt) |
| [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt) | `getTransactionDetail` | `transactions/{id}` | `GET` | Path: `id` (Int) | [TransactionDetailResponse](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/TransactionHistoryResponse.kt) |
| [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt) | `voidTransaction` | `transactions/{id}/void` | `PUT` | Path: `id` (Int), Body: [VoidRequest](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/TransactionHistoryResponse.kt) | [CheckoutResponse](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/CheckoutResponse.kt) |
| [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt) | `getDashboardData` | `dashboard` | `GET` | None | [DashboardResponse](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/DashboardResponse.kt) |
| [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt) | `getDailyReport` | `reports/daily` | `GET` | Query: `date` (String) | [ReportResponse](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/ReportResponse.kt) |
| [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt) | `getMonthlyReport` | `reports/monthly` | `GET` | Query: `month` (String) | [ReportResponse](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/ReportResponse.kt) |

---

# Integration Status

Hasil perbandingan fungsionalitas antara Backend (BE) dan Frontend (FE):

| Bagian | Status | Masalah | Solusi |
| ------ | ------ | ------- | ------ |
| **Login API** | ✅ Sesuai | Tidak ada masalah. | Siap digunakan. |
| **Category API** | ❌ Tidak Sesuai | Response wrap mismatch. BE membungkus data kategori dalam object JSend (`{status, message, data: [...]}`), sedangkan FE mengekspektasi direct JSON Array (`[...]`). | Ubah backend agar mengembalikan array langsung untuk GET `/categories`, atau buat model wrapper `CategoryResponse` pada frontend. |
| **Product API** | ❌ Tidak Sesuai | 1. Response wrap mismatch (BE membungkus dengan JSend).<br>2. Filter query parameter `category` dan `search` diabaikan oleh BE.<br>3. Field mismatch (`buy_price` di FE vs `cost_price` di BE).<br>4. Info `stock` kosong karena BE tidak menggabungkan tabel `inventory` untuk data stock. | 1. Ubah backend agar mengembalikan array langsung (atau buat wrapper di FE).<br>2. Tambahkan filter query di `productService.js` untuk Supabase.<br>3. Map `cost_price` ke `buy_price` di BE controller atau tambahkan `@SerializedName("cost_price")` di FE.<br>4. Lakukan `leftJoin` ke tabel `inventory` di BE dan map `current_stock` sebagai field `stock` pada objek produk. |
| **Update Stock API** | ❌ Tidak Sesuai | 1. Endpoint URL mismatch (`products/{id}/stock` di FE vs `inventory/{product_id}` di BE).<br>2. Parameter nama field mismatch (`stock` di FE vs `current_stock` di BE).<br>3. Response type mismatch (FE mengharapkan objek `Product` utuh, BE memberikan objek `Inventory` yang dibungkus JSend). | 1. Ubah endpoint router BE atau API Service FE agar sinkron.<br>2. Samakan nama field request body.<br>3. Ubah service BE agar mengembalikan data produk lengkap yang sudah terupdate stoknya (dengan format tanpa wrap jika FE tetap mengharapkan objek langsung). |
| **Checkout API** | ✅ Sesuai | Tidak ada masalah, struktur data request dan response camelCase telah dipetakan dengan benar menggunakan `@SerializedName`. | Siap digunakan. |
| **Transaction History API** | ⚠️ Sebagian Sesuai | Query filter `status` diabaikan oleh BE. BE selalu mengembalikan seluruh transaksi tanpa filter status `completed`/`voided`. | Tambahkan handling query parameter `status` pada `transactionController.js` dan kirim ke `transactionService.js` untuk memfilter data via Supabase `.eq('status', status)`. |
| **Transaction Detail API** | ❌ Tidak Sesuai | Response structure mismatch. BE hanya mengembalikan daftar item detail (`transaction_details`), sedangkan FE mengharapkan objek tunggal `TransactionRecord` yang berisi informasi transaksi utama (header) beserta daftar `items` di dalamnya. Ini akan menyebabkan crash deserialisasi Gson (`Expected BEGIN_OBJECT but was BEGIN_ARRAY`). | Ubah query backend pada `getTransactionDetail` untuk mengambil data `transactions` tunggal berdasarkan ID dengan relasi JOIN ke `transaction_details`, lalu petakan relasi tersebut ke properti `items` sebelum dikirim. |
| **Void Transaction API** | ⚠️ Sebagian Sesuai | Response field mismatch. BE mengembalikan objek transaksi dengan snake_case, sedangkan FE mengharapkan wrapper `CheckoutResponse` dengan camelCase (`CheckoutData`). Namun, tidak menyebabkan crash karena FE ViewModel hanya memeriksa `response.isSuccessful` dan mengabaikan response body. | Format respons di backend controller agar mengembalikan properti berformat camelCase (`transactionId`, `transactionCode`, dll.) sesuai format respons checkout agar konsisten. |
| **Dashboard API** | ❌ Tidak Sesuai | 1. URL Path mismatch (FE: `/dashboard`, BE: `/dashboard/summary`).<br>2. Field mismatch (`total_revenue_today` vs `total_revenue`, dll.).<br>3. BE menghitung data kumulatif sepanjang waktu, bukan data khusus hari ini.<br>4. Kolom `top_products` dan `last_7_days_sales` tidak diimplementasikan di BE, menyebabkan crash NPE (NullPointerException) di Kotlin karena tipenya non-nullable. | 1. Ubah router BE agar endpoint-nya `/dashboard`.<br>2. Sesuaikan nama field JSON yang dikembalikan BE.<br>3. Tambahkan filter tanggal hari ini pada query omzet di BE.<br>4. Implementasikan query agregasi di Supabase/JS untuk menghitung `top_products` dan tren penjualan 7 hari terakhir. |
| **Reports API** | ❌ Belum Ada | Modul laporan (`reports/daily` & `reports/monthly`) sama sekali belum diimplementasikan di backend (tidak ada route, controller, maupun service). FE akan mendapatkan error 404. | Buat router `reportRoutes.js`, controller `reportController.js`, dan service `reportService.js` di backend untuk menghitung omzet, profit kotor, dan tren penjualan berdasarkan rentang tanggal/bulan. |

---

# Issues Found (Daftar Masalah Detil)

Berikut adalah rincian masalah teknis berdasarkan analisis kode sumber:

### 1. JSON Response Wrapper Mismatch (Kategori & Produk)
* **Lokasi BE**: 
  * [categoryController.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/controllers/categoryController.js#L6-L10) (`getCategories`)
  * [productController.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/controllers/productController.js#L6-L10) (`getProducts`)
* **Lokasi FE**: [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt#L28-L35)
* **Bukti Kode BE**:
  ```javascript
  return res.status(200).json({
    status: "success",
    message: "...",
    data // Ini berupa array
  });
  ```
* **Bukti Kode FE**:
  ```kotlin
  suspend fun getCategories(): Response<List<Category>>
  ```
* **Dampak**: Gson pada Android Emulator akan melemparkan exception `com.google.gson.JsonSyntaxException: java.lang.IllegalStateException: Expected BEGIN_ARRAY but was BEGIN_OBJECT` dan memicu error state di UI.

### 2. Mismatch Bidang Produk & Data Stok Hilang
* **Lokasi BE**: [productService.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/services/productService.js#L3-L12) (`getAllProducts`)
* **Lokasi FE**: [Product.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/Product.kt)
* **Analisis**:
  1. Di database dan BE, harga beli dinamakan `cost_price`, sedangkan model FE mendefinisikannya sebagai `buy_price`. Karena tidak ada mapping, nilainya akan menjadi `0.0` pada aplikasi.
  2. Fungsi `getAllProducts` di backend tidak menggabungkan data dari tabel `inventory`. Akibatnya, nilai `stock` tidak dikirimkan ke FE.
* **Dampak**: Stok produk selalu bernilai 0 di halaman POS menu dan harga modal produk tidak terbaca dengan benar.

### 3. Ketidaksesuaian Endpoint & Parameter Update Stock
* **Lokasi BE Router**: [inventoryRoutes.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/routes/inventoryRoutes.js#L7)
* **Lokasi BE Controller**: [inventoryController.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/controllers/inventoryController.js#L16-L37) (`updateStock`)
* **Lokasi FE**: [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt#L42-L46)
* **Analisis**:
  1. FE menembak URL: `PUT products/{id}/stock` dengan body `{"stock": 100}`.
  2. BE mendengarkan URL: `PUT inventory/:product_id` dengan body `{"current_stock": 100}`.
  3. BE membalas dengan JSend wrapper berisi rekaman inventori, sedangkan FE mengharapkan objek `Product` langsung.
* **Dampak**: Fitur edit stok di modul Inventory Android akan mengembalikan error 404 (Not Found) atau 400 (Bad Request) dan gagal memperbarui stok.

### 4. Crash Fatal Deserialisasi Detail Transaksi
* **Lokasi BE Service**: [transactionService.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/services/transactionService.js#L128-L137) (`getTransactionDetail`)
* **Lokasi FE**: [TransactionHistoryResponse.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/TransactionHistoryResponse.kt#L10-L13) (`TransactionDetailResponse`)
* **Bukti Kode BE**:
  ```javascript
  const getTransactionDetail = async (id) => {
      const { data, error } = await supabase
          .from('transaction_details')
          .select('*')
          .eq('transaction_id', id);
      return data; // Mengembalikan array item detail langsung
  };
  ```
* **Bukti Kode FE**:
  ```kotlin
  data class TransactionDetailResponse(
      val status: String,
      val data: TransactionRecord? // Mengharapkan objek tunggal detail transaksi
  )
  ```
* **Dampak**: Saat pengguna mengklik salah satu riwayat transaksi untuk melihat pop-up detail, aplikasi akan crash karena parser mengharapkan JSON objek `{...}` untuk `data` tetapi menerima JSON array `[...]`.

### 5. URL dan Field Mismatch pada Dashboard (Potensi Crash NPE)
* **Lokasi BE**: 
  * [dashboardRoutes.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/routes/dashboardRoutes.js#L6)
  * [dashboardService.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/services/dashboardService.js#L25-L35)
* **Lokasi FE**:
  * [ApiService.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/api/ApiService.kt#L65-L66)
  * [DashboardResponse.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/model/DashboardResponse.kt)
* **Analisis**:
  1. FE memanggil `/dashboard`, sedangkan BE memetakan `/dashboard/summary` (Error 404).
  2. BE mengirim field `total_revenue`, `total_transactions`, dan `low_stock_products`. FE mencari `total_revenue_today`, `total_transactions_today`, dan `low_stock_alerts`.
  3. Properti required Kotlin `last7DaysSales: List<SalesBarData>` dan `topProducts: List<TopProduct>` bernilai null karena BE tidak memprosesnya.
* **Dampak**: Halaman Dashboard Android akan langsung crash seketika karena NullPointerException pada tipe non-nullable Kotlin saat memproses respons dari API Dashboard.

### 6. Modul Laporan (Reports) Belum Diimplementasikan di BE
* **Lokasi BE**: Tidak ada routes, controllers, maupun services yang mengurus modul `/reports`.
* **Lokasi FE**: [ReportFragment.kt](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/app/src/main/java/com/mobile/tugasrancangmoka/fragment/ReportFragment.kt) memanggil `getDailyReport` dan `getMonthlyReport` via ViewModel.
* **Dampak**: Halaman visualisasi laporan analitik (grafik tren penjualan harian/bulanan) tidak berfungsi sama sekali dan memicu error visual (Error 404).

---

# Recommended Fix (Rekomendasi Perbaikan)

Berikut adalah solusi perbaikan sisi Backend untuk menyesuaikan dengan kode Frontend Android saat ini tanpa harus merombak arsitektur client:

### 1. Fix Kategori & Produk JSON Response Wrap (Kembalikan Array Langsung)
Ubah controller pada backend untuk mengembalikan array data secara langsung tanpa pembungkus JSend:
* **Pada [categoryController.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/controllers/categoryController.js#L6-L10)**:
  ```javascript
  const getCategories = async (req, res) => {
    try {
      const data = await categoryService.getAllCategories();
      return res.status(200).json(data);
    } catch (error) {
      console.error("DEBUG GET CATEGORIES ERROR:", error);
      return res.status(500).json({ status: "error", message: error.message });
    }
  };
  ```
* **Pada [productController.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/controllers/productController.js#L6-L10)**:
  ```javascript
  const getProducts = async (req, res) => {
    try {
      const { category, search } = req.query;
      const data = await productService.getAllProductsFiltered(category, search);
      return res.status(200).json(data);
    } catch (error) {
      return res.status(500).json({ status: "error", message: error.message });
    }
  };
  ```

### 2. Gabungkan Tabel Inventory & Sesuaikan Field Produk
Perbarui fungsi pengambilan produk di `productService.js` untuk menggabungkan tabel `inventory` dan memetakan field secara tepat:
* **Pada [productService.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/services/productService.js)**:
  ```javascript
  const getAllProductsFiltered = async (categoryId, search) => {
    let query = supabase
      .from('products')
      .select('*, inventory(current_stock)')
      .eq('is_active', true);
  
    if (categoryId) {
      query = query.eq('category_id', categoryId);
    }
    if (search) {
      query = query.ilike('name', `%${search}%`);
    }
  
    const { data, error } = await query;
    if (error) throw error;
  
    // Map data agar sesuai dengan struktur model Product.kt di FE
    return data.map(prod => ({
      id: prod.id,
      category_id: prod.category_id,
      name: prod.name,
      buy_price: prod.cost_price, // Map cost_price ke buy_price
      sell_price: prod.sell_price,
      image_url: prod.image_url,
      is_active: prod.is_active,
      stock: prod.inventory?.[0]?.current_stock || 0 // Gabungkan stock dari inventory
    }));
  };
  ```

### 3. Sesuaikan Endpoint & Payload Update Stock
* **Pada [productRoutes.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/routes/productRoutes.js)**:
  ```javascript
  router.put('/:id/stock', authMiddleware, productController.updateProductStockFE);
  ```
* **Pada [productController.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/controllers/productController.js)**:
  ```javascript
  const updateProductStockFE = async (req, res) => {
    try {
      const { id } = req.params;
      const { stock } = req.body; // Terima field 'stock' bukan 'current_stock'
  
      if (stock === undefined || stock < 0) {
        return res.status(400).json({ status: "error", message: "Stok tidak boleh kosong atau negatif" });
      }
  
      const updatedProduct = await productService.updateProductStockAndReturnProduct(id, stock);
      return res.status(200).json(updatedProduct); // Kembalikan direct object Product
    } catch (error) {
      return res.status(400).json({ status: "error", message: error.message });
    }
  };
  ```

### 4. Perbaikan Respons Detail Transaksi (JOIN Header dan Detail)
Ubah service transaksi agar mengembalikan satu objek transaksi utama lengkap dengan relasi array itemnya:
* **Pada [transactionService.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/services/transactionService.js)**:
  ```javascript
  const getTransactionDetail = async (id) => {
      // Ambil header transaksi
      const { data: trx, error: trxError } = await supabase
          .from('transactions')
          .select('*')
          .eq('id', id)
          .single();
  
      if (trxError) throw trxError;
  
      // Ambil detail items transaksi
      const { data: details, error: detailsError } = await supabase
          .from('transaction_details')
          .select('*')
          .eq('transaction_id', id);
  
      if (detailsError) throw detailsError;
  
      // Gabungkan data sesuai model TransactionRecord.kt di FE
      return {
          ...trx,
          items: details
      };
  };
  ```

### 5. Sinkronisasi Endpoint & Perhitungan Statistik Dashboard
* **Pada [dashboardRoutes.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/routes/dashboardRoutes.js)**:
  ```diff
- router.get('/summary', authMiddleware, dashboardController.getDashboardData);
+ router.get('/', authMiddleware, dashboardController.getDashboardData); // Sinkronkan ke /dashboard
  ```
* **Pada [dashboardService.js](file:///D:/UKSW/Semester%206/Pemprograman%20Mobile/TugasRancangMoka/backend-express/services/dashboardService.js)**:
  ```javascript
  const getSummaryStats = async () => {
    const todayStr = new Date().toISOString().split('T')[0]; // Format YYYY-MM-DD
    
    // 1. Ambil transaksi hari ini
    const { data: todayTrx, error: trxError } = await supabase
      .from('transactions')
      .select('grand_total, status, created_at')
      .eq('status', 'completed')
      .gte('created_at', `${todayStr}T00:00:00.000Z`)
      .lte('created_at', `${todayStr}T23:59:59.999Z`);
  
    if (trxError) throw trxError;
  
    const totalRevenueToday = todayTrx.reduce((sum, trx) => sum + trx.grand_total, 0);
    const totalTransactionsToday = todayTrx.length;
  
    // 2. Ambil data stok menipis (low stock alerts)
    const { data: allInventory, error: invError } = await supabase
      .from('inventory')
      .select('product_id, current_stock, min_stock, products(name)')
      .lte('current_stock', 5); // atau filter current_stock <= min_stock
  
    if (invError) throw invError;
  
    const lowStockAlerts = allInventory.map(item => ({
      product_name: item.products?.name || "Unknown Product",
      current_stock: item.current_stock
    }));
  
    // 3. Hitung Top Products (Agregasi dari transaction_details)
    const { data: details } = await supabase
      .from('transaction_details')
      .select('product_name, quantity');
      
    const productSales = {};
    details?.forEach(d => {
      productSales[d.product_name] = (productSales[d.product_name] || 0) + d.quantity;
    });
    const topProducts = Object.keys(productSales).map(name => ({
      product_name: name,
      quantity: productSales[name]
    })).sort((a, b) => b.quantity - a.quantity).slice(0, 5);
  
    // 4. Hitung Penjualan 7 hari terakhir
    const last7DaysSales = [];
    for (let i = 6; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      const dateStr = d.toISOString().split('T')[0];
      last7DaysSales.push({ date: dateStr, total: 0.0 });
    }
  
    return {
      total_revenue_today: totalRevenueToday,
      total_transactions_today: totalTransactionsToday,
      last_7_days_sales: last7DaysSales,
      top_products: topProducts,
      low_stock_alerts: lowStockAlerts
    };
  };
  ```

---

# Final Conclusion

### Kesiapan Koneksi
Backend dan Frontend saat ini **BELUM SIAP terhubung secara penuh**. Meskipun modul **Autentikasi (Login)** dan **Checkout (Transaksi)** telah terintegrasi dengan baik secara sintaksis, aplikasi Android akan mengalami **crash seketika** di halaman **Menu Utama (Categories/Products)**, **Detail Riwayat Transaksi**, dan **Dashboard** jika dihubungkan langsung ke API backend saat ini. 

### Bagian yang Perlu Diperbaiki (Urutan Prioritas)

1. **Prioritas UTAMA (Pencegahan Crash / Blocker)**
   * **Dashboard API**: Perbaiki URL endpoint ke `/api/v1/dashboard` dan hitung bidang wajib `top_products` serta `last_7_days_sales` agar halaman utama aplikasi tidak crash karena NullPointerException.
   * **Transaction Detail API**: Rombak respons agar mengirimkan data relasi lengkap (Header + Detail items) berupa objek tunggal, bukan array detail item saja guna menghindari crash parser Gson.
   * **Category & Product APIs Response Formatting**: Kembalikan bentuk data array langsung untuk GET `/categories` dan GET `/products`, atau sesuaikan parsing di Android agar aplikasi dapat memuat daftar menu.

2. **Prioritas KEDUA (Kebenaran Fungsional / Data Integrity)**
   * **Update Stock API**: Sinkronkan perbedaan nama parameter (`stock` vs `current_stock`) dan endpoint URL agar pengelolaan inventori toko berjalan.
   * **Stock & Price mapping pada Product List**: Ubah query di `productService.js` untuk menggabungkan tabel inventori agar informasi stok menu bernilai riil dan map field `cost_price` menjadi `buy_price`.

3. **Prioritas KETIGA (Pelengkap Fitur / Fitur Baru)**
   * **Reports Module**: Buat controller dan router baru di backend untuk menangani analisis laporan harian/bulanan agar visualisasi grafik bisnis di Android berfungsi.
   * **History Filter**: Tambahkan filter `status` di query database backend riwayat transaksi untuk mendukung tab filter di aplikasi.
