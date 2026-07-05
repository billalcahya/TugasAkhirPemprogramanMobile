package com.mobile.tugasrancangmoka.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smartcafe_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
    }

    // Simpan token setelah login
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    // Ambil token buat dikirim ke API
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    // Simpan info user
    fun saveUser(id: Int, name: String, email: String, role: String) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, id)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ROLE, role)
            apply()
        }
    }

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, null)

    // Cek udah login atau belum
    fun isLoggedIn(): Boolean = getToken() != null

    // Hapus semua data (logout)
    fun clear() {
        prefs.edit().clear().apply()
    }
}
