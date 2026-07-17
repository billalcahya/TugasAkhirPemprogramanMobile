package com.mobile.tugasrancangmoka.utils

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

object ErrorUtils {
    /**
     * Mengubah exception/throwable menjadi pesan error bahasa Indonesia yang sederhana
     * dan mudah dipahami oleh pengguna.
     */
    fun getFriendlyMessage(e: Throwable): String {
        return when (e) {
            is UnknownHostException, is ConnectException -> {
                "Tidak dapat terhubung ke internet. Periksa koneksi Anda."
            }
            is SocketTimeoutException -> {
                "Koneksi lambat/habis waktu. Silakan coba lagi."
            }
            is HttpException -> {
                when (e.code()) {
                    400 -> "Permintaan tidak valid."
                    401 -> "Sesi telah berakhir. Silakan masuk kembali."
                    403 -> "Akses ditolak."
                    404 -> "Data tidak ditemukan."
                    in 500..599 -> "Terjadi gangguan pada server. Silakan hubungi admin."
                    else -> "Terjadi kesalahan server (Kode: ${e.code()})."
                }
            }
            is IOException -> {
                "Gagal memproses data. Silakan coba lagi."
            }
            else -> {
                val msg = e.message
                if (msg.isNullOrBlank()) {
                    "Terjadi kesalahan yang tidak diketahui."
                } else {
                    // Jika berisi kata kunci teknis, bersihkan jadi bahasa sederhana
                    when {
                        msg.contains("Connection refused", ignoreCase = true) ||
                        msg.contains("Failed to connect", ignoreCase = true) -> {
                            "Koneksi gagal. Pastikan server aktif."
                        }
                        msg.contains("timeout", ignoreCase = true) -> {
                            "Waktu koneksi habis. Silakan coba lagi."
                        }
                        msg.contains("syntax", ignoreCase = true) || 
                        msg.contains("parse", ignoreCase = true) -> {
                            "Gagal membaca format data dari server."
                        }
                        else -> msg
                    }
                }
            }
        }
    }

    /**
     * Mengubah response error JSON dari API menjadi pesan bahasa Indonesia yang ramah pengguna.
     */
    fun parseApiError(errorBodyString: String?): String {
        if (errorBodyString.isNullOrBlank()) {
            return "Transaksi gagal diproses"
        }
        
        var parsedMsg = try {
            val jsonObject = org.json.JSONObject(errorBodyString)
            jsonObject.optString("message", "")
                .ifBlank { jsonObject.optString("error", "") }
                .ifBlank { "Transaksi gagal diproses" }
        } catch (e: Exception) {
            errorBodyString
        }

        // Deteksi pesan tentang stok barang tidak cukup
        val lowerMsg = parsedMsg.lowercase(java.util.Locale.ROOT)
        if (lowerMsg.contains("stock") || lowerMsg.contains("stok") || lowerMsg.contains("insufficient") || 
            lowerMsg.contains("kurang") || lowerMsg.contains("cukup")) {
            
            // Coba terjemahkan secara cerdas dengan mempertahankan nama produk jika ada
            return when {
                parsedMsg.contains("insufficient stock for", ignoreCase = true) -> {
                    parsedMsg.replace("Insufficient stock for", "Stok tidak mencukupi untuk", ignoreCase = true)
                }
                parsedMsg.contains("not enough stock for", ignoreCase = true) -> {
                    parsedMsg.replace("Not enough stock for", "Stok tidak mencukupi untuk", ignoreCase = true)
                }
                else -> "Stok produk tidak mencukupi untuk memproses transaksi."
            }
        }
        
        return parsedMsg
    }
}

