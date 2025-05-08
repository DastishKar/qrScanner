package com.example.scanner.data.repository

import android.content.Context
import com.example.scanner.data.RetrofitClient
import com.example.scanner.model.AuthRequest
import com.example.scanner.model.ScanRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QRCodeRepository(context: Context) {

    private val apiService = RetrofitClient.getService()
    private var sessionCookie: String? = null

    suspend fun login(): Boolean {
        val response = apiService.login(AuthRequest("johndoe", "JohnDoe123!@#"))

        return if (response.isSuccessful) {
            val headers = response.headers()
            val rawCookie = headers["Set-Cookie"]
            sessionCookie = rawCookie // сохраняем куку
            true
        } else {
            false
        }
    }

    suspend fun scanQRCode(qrCode: String): Boolean {
        // Если кука отсутствует — пробуем логин
        if (sessionCookie == null) {
            val loginSuccess = login()
            if (!loginSuccess) throw Exception("Не удалось авторизоваться")
        }

        val response = apiService.scan(
            cookie = sessionCookie!!,
            scanRequest = ScanRequest(qrCode)
        )

        return response.isSuccessful
    }
}
