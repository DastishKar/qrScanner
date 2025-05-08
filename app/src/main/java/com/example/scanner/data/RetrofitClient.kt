package com.example.scanner.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.scanner.model.AuthRequest
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://sold-out.kz/api/auth/"
    private const val TAG = "RetrofitClient"
    private const val PREFS_NAME = "ScannerPrefs"
    private const val KEY_AUTH_COOKIE = "AUTH_COOKIE"

    private var sharedPreferences: SharedPreferences? = null
    private var instance: ApiService? = null

    // Инициализация для сохранения контекста приложения
    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            Log.d(TAG, "SharedPreferences инициализирован")

            // Логируем текущий статус куки при инициализации
            val cookie = getAuthCookie()
            Log.d(TAG, "Текущая кука при инициализации: ${if (cookie.isEmpty()) "отсутствует" else cookie}")
        }
    }

    // Сохранение куки после авторизации
    fun saveAuthCookie(cookie: String) {
        Log.d(TAG, "Сохранение куки: $cookie")
        sharedPreferences?.edit()?.apply {
            putString(KEY_AUTH_COOKIE, cookie)
            apply()
        }

        // Проверяем, сохранилась ли кука
        val savedCookie = getAuthCookie()
        Log.d(TAG, "Проверка сохраненной куки: ${if (savedCookie.isEmpty()) "НЕ СОХРАНЕНО" else "СОХРАНЕНО"}")
    }

    // Получение куки для запросов
    fun getAuthCookie(): String {
        val cookie = sharedPreferences?.getString(KEY_AUTH_COOKIE, "") ?: ""
        Log.d(TAG, "Получение куки: ${if (cookie.isEmpty()) "не найдено" else cookie}")
        return cookie
    }

    // Очистка куки (для выхода)
    fun clearAuthCookie() {
        Log.d(TAG, "Очистка куки")
        sharedPreferences?.edit()?.remove(KEY_AUTH_COOKIE)?.apply()
    }

    // Интерсептор для добавления куки в заголовок
    private class CookieInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            val cookie = getAuthCookie()
            Log.d(TAG, "CookieInterceptor: ${if (cookie.isEmpty()) "куки нет" else "добавляем куки в запрос"}")

            if (cookie.isNotEmpty()) {
                val modifiedRequest = originalRequest.newBuilder()
                    .header("Cookie", cookie)
                    .build()
                return chain.proceed(modifiedRequest)
            }

            return chain.proceed(originalRequest)
        }
    }

    // Создание OkHttpClient с интерсептором для куки и логирования
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)  // Добавляем логирование сначала
            .addInterceptor(CookieInterceptor())  // Затем добавляем куки
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Создание и получение Retrofit-сервиса
    fun getService(): ApiService {
        if (instance == null) {
            // Создаем более гибкий Gson, который будет терпимее к некорректному JSON
            val gson = GsonBuilder()
                .setLenient()  // Устанавливаем режим снисходительности
                .create()

            instance = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))  // Используем настроенный Gson
                .build()
                .create(ApiService::class.java)
        }
        return instance!!
    }

    // Метод для авторизации и сохранения куки
    suspend fun login(username: String, password: String): Boolean {
        try {
            Log.d(TAG, "Попытка логина: $username")
            val service = getService()
            val response = service.login(AuthRequest(username, password))

            Log.d(TAG, "Ответ сервера: ${response.code()}")
            Log.d(TAG, "Заголовки: ${response.headers()}")

            if (response.isSuccessful) {
                val cookieHeader = response.headers()["Set-Cookie"]
                Log.d(TAG, "Set-Cookie заголовок: $cookieHeader")

                if (!cookieHeader.isNullOrEmpty()) {
                    saveAuthCookie(cookieHeader)
                    return true
                } else {
                    Log.e(TAG, "Set-Cookie заголовок отсутствует в ответе")
                }
            } else {
                Log.e(TAG, "Ошибка авторизации: ${response.code()} - ${response.errorBody()?.string()}")
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при авторизации", e)
            return false
        }
    }
}