package com.mobile.tugasrancangmoka.api

import android.content.Context
import com.mobile.tugasrancangmoka.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://smartcafe-api.vercel.app/api/v1/"

    private var retrofit: Retrofit? = null

    fun getService(context: Context): ApiService {
        if (retrofit == null) {
            // Logging - buat debug di Logcat
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // Auth Interceptor - otomatis tambahin token ke setiap request
            val authInterceptor = Interceptor { chain ->
                val token = SessionManager(context).getToken()
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                if (token != null) {
                    request.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(request.build())
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }
}
