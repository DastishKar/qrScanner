package com.example.scanner.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.scanner.data.RetrofitClient
import com.example.scanner.model.AuthRequest

class AuthRepository(private val context: Context) {

    private val api = RetrofitClient.getService()

    suspend fun login(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val response = api.login(AuthRequest(username, password))
        if (response.isSuccessful) {
            val cookie = response.headers()["Set-Cookie"]
            context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("cookie", cookie)
                .apply()
            return@withContext true
        }
        return@withContext false
    }
}


