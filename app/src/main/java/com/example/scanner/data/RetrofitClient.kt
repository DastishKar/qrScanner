package com.example.scanner.data

import android.content.Context
import android.content.SharedPreferences
import com.example.scanner.model.AuthRequest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://sold-out.kz/api/"
    private lateinit var sharedPreferences: SharedPreferences

    // Инициализация для сохранения контекста приложения
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("ScannerPrefs", Context.MODE_PRIVATE)
    }

    // Сохранение куки после авторизации
    fun saveAuthCookie(cookie: String) {
        sharedPreferences.edit().putString("AUTH_COOKIE", cookie).apply()
    }

    // Получение куки для запросов
    fun getAuthCookie(): String {
        return sharedPreferences.getString("AUTH_COOKIE", "") ?: ""
    }

    // Интерсептор для добавления куки в заголовок
    private class CookieInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            val cookie = getAuthCookie()
            if (cookie.isNotEmpty()) {
                val modifiedRequest = originalRequest.newBuilder()
                    .header("Cookie", cookie)
                    .build()
                return chain.proceed(modifiedRequest)
            }

            return chain.proceed(originalRequest)
        }
    }

    // Создание OkHttpClient с интерсептором для куки
    private val client = OkHttpClient.Builder()
        .addInterceptor(CookieInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Создание и получение Retrofit-сервиса
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    // Метод для авторизации и сохранения куки
    suspend fun login(username: String, password: String): Boolean {
        try {
            val service = getService()
            val response = service.login(AuthRequest(username, password))

            if (response.isSuccessful && response.headers().names().contains("Set-Cookie")) {
                val cookie = response.headers().get("Set-Cookie")
                if (cookie != null) {
                    saveAuthCookie(cookie)
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }
}