# SmartCafe POS API Integration

Dokumen ini mendokumentasikan integrasi API antara aplikasi POS Android dan backend server Express.js.

## Informasi Dasar
*   **Base URL**: `https://smartcafe-api.vercel.app/api/v1/`
*   **Format Data**: JSON (`application/json`)
*   **Konfigurasi Timeout**: 30 detik untuk koneksi dan pembacaan.

---

## Autentikasi & Header
Seluruh endpoint (kecuali `/auth/login`) memerlukan token JWT sebagai otorisasi. Token ditambahkan secara otomatis ke header HTTP melalui `AuthInterceptor` setelah user berhasil login.

**Format Header**:
```http
Authorization: Bearer <jwt_token>
```

---

## Daftar Endpoint & Skema Data

### 1. Authentication
*   **Login User**
    *   **Endpoint**: `POST auth/login`
    *   **Request Body (`LoginRequest`)**:
        ```json
        {
          "email": "user@example.com",
          "password": "password123"
        }
        ```
    *   **Response (`LoginResponse`)**:
        ```json
        {
          "status": "success",
          "data": {
            "token": "eyJhbGciOi...",
            "user": {
              "id": 1,
              "name": "Billal",
              "role": "admin"
            }
          }
        }
        ```

### 2. Category Module
*   **Get All Categories**
    *   **Endpoint**: `GET categories`
    *   **Response (`List<Category>`)**:
        ```json
        [
          {
            "id": 1,
            "name": "Coffee",
            "color_hex": "#8B4513",
            "is_active": true
          }
        ]
        ```

### 3. Product Module
*   **Get Products**
    *   **Endpoint**: `GET products`
    *   **Query Parameters**:
        *   `category` (Int, optional): Filter berdasarkan ID kategori
        *   `search` (String, optional): Cari nama produk
    *   **Response (`List<Product>`)**:
        ```json
        [
          {
            "id": 1,
            "category_id": 1,
            "name": "Espresso",
            "buy_price": 10000.0,
            "sell_price": 15000.0,
            "image_url": "https://...",
            "is_active": true,
            "stock": 50
          }
        ]
        ```

### 4. Transactions & Checkout
*   **Process Checkout**
    *   **Endpoint**: `POST transactions`
    *   **Request Body (`CheckoutRequest`)**:
        ```json
        {
          "items": [
            {
              "product_id": 1,
              "quantity": 2
            }
          ],
          "discount_amount": 5000.0,
          "payment_method": "cash",
          "payment_amount": 30000.0
        }
        ```
    *   **Response (`CheckoutResponse`)**:
        ```json
        {
          "status": "success",
          "message": "Transaction created successfully",
          "data": {
            "id": 12,
            "transaction_code": "TRX-20260705-001"
          }
        }
        ```

*   **Get Transaction History**
    *   **Endpoint**: `GET transactions`
    *   **Query Parameter**:
        *   `status` (String, optional): Filter status (`completed` atau `voided`)
    *   **Response (`TransactionResponse`)**:
        ```json
        {
          "status": "success",
          "data": [
            {
              "id": 12,
              "transaction_code": "TRX-20260705-001",
              "user_id": 1,
              "total_amount": 30000.0,
              "discount_amount": 5000.0,
              "grand_total": 25000.0,
              "payment_method": "cash",
              "payment_amount": 30000.0,
              "change_amount": 5000.0,
              "status": "completed",
              "void_reason": null,
              "created_at": "2026-07-05T14:30:00.000Z"
            }
          ]
        }
        ```

*   **Get Transaction Detail**
    *   **Endpoint**: `GET transactions/{id}`
    *   **Response (`TransactionDetailResponse`)**:
        ```json
        {
          "status": "success",
          "data": {
            "id": 12,
            "transaction_code": "TRX-20260705-001",
            "total_amount": 30000.0,
            "discount_amount": 5000.0,
            "grand_total": 25000.0,
            "payment_method": "cash",
            "payment_amount": 30000.0,
            "change_amount": 5000.0,
            "status": "completed",
            "created_at": "2026-07-05T14:30:00.000Z",
            "items": [
              {
                "id": 1,
                "product_id": 1,
                "product_name": "Espresso",
                "sell_price": 15000.0,
                "quantity": 2,
                "subtotal": 30000.0
              }
            ]
          }
        }
        ```

*   **Void / Cancel Transaction**
    *   **Endpoint**: `PUT transactions/{id}/void`
    *   **Request Body (`VoidRequest`)**:
        ```json
        {
          "void_reason": "Salah input pesanan pelanggan"
        }
        ```
    *   **Response (`CheckoutResponse`)**:
        ```json
        {
          "status": "success",
          "message": "Transaction voided successfully"
        }
        ```

### 5. Inventory Module
*   **Update Product Stock**
    *   **Endpoint**: `PUT products/{id}/stock`
    *   **Request Body (`UpdateStockRequest`)**:
        ```json
        {
          "stock": 100
        }
        ```
    *   **Response (`Product`)**:
        ```json
        {
          "id": 1,
          "name": "Espresso",
          "stock": 100
        }
        ```

### 6. Dashboard Module
*   **Get Dashboard Data**
    *   **Endpoint**: `GET dashboard`
    *   **Response (`DashboardResponse`)**:
        ```json
        {
          "status": "success",
          "data": {
            "total_revenue_today": 1250000.0,
            "total_transactions_today": 42,
            "top_products": [
              {
                "product_id": 1,
                "product_name": "Espresso",
                "quantity": 18
              }
            ],
            "low_stock_alerts": [
              {
                "product_id": 3,
                "product_name": "Caramel Macchiato",
                "current_stock": 3
              }
            ]
          }
        }
        ```

### 7. Business Reports Module
*   **Get Daily Report**
    *   **Endpoint**: `GET reports/daily`
    *   **Query Parameter**:
        *   `date` (String, required): format `YYYY-MM-DD`
    *   **Response (`ReportResponse`)**:
        ```json
        {
          "status": "success",
          "data": {
            "total_revenue": 1500000.0,
            "gross_profit": 800000.0,
            "sales_trend": [
              {
                "label": "09:00",
                "revenue": 150000.0
              }
            ]
          }
        }
        ```

*   **Get Monthly Report**
    *   **Endpoint**: `GET reports/monthly`
    *   **Query Parameter**:
        *   `month` (String, required): format `YYYY-MM`
    *   **Response (`ReportResponse`)**:
        ```json
        {
          "status": "success",
          "data": {
            "total_revenue": 45000000.0,
            "gross_profit": 24000000.0,
            "sales_trend": [
              {
                "label": "2026-07-01",
                "revenue": 1500000.0
              }
            ]
          }
        }
        ```
