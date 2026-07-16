package com.mobile.tugasrancangmoka.api.interceptor

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.mobile.tugasrancangmoka.activity.LoginActivity
import com.mobile.tugasrancangmoka.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val sharedPreferences = context.getSharedPreferences("smartcafe_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null)

        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            val requestUrl = chain.request().url.toString()
            if (!requestUrl.contains("auth/login")) {
                val sessionManager = SessionManager(context)
                if (sessionManager.getToken() != null) {
                    sessionManager.clearSession()

                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            "Sesi Anda telah berakhir, silakan login kembali.",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(context, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }

        return response
    }
}