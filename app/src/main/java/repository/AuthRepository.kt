package com.mobile.tugasrancangmoka.repository

import android.content.Context
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.api.LoginRequest
import com.mobile.tugasrancangmoka.utils.SessionManager

class AuthRepository(private val context: Context) {

    private val api = ApiClient.getService(context)
    private val session = SessionManager(context)

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val response = api.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body()?.status == "success") {
                val data = response.body()?.data
                val token = data?.token ?: return Result.failure(Exception("Token tidak ditemukan"))

                // Simpan token & user info
                session.saveToken(token)
                val user = data.user
                if (user != null) {
                    session.saveUser(
                        id = user.id,
                        name = user.full_name ?: user.name ?: "",
                        email = user.email,
                        role = user.role ?: "cashier"
                    )
                }

                Result.success("Login berhasil")
            } else {
                val msg = response.body()?.message ?: "Email atau password salah"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.message}"))
        }
    }

    suspend fun logout(): Result<String> {
        return try {
            api.logout()
            session.clear()
            Result.success("Logout berhasil")
        } catch (e: Exception) {
            session.clear()
            Result.success("Logout berhasil")
        }
    }
}
