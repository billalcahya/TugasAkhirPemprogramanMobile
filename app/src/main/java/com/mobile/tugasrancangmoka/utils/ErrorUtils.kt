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
}
