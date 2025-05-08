package com.example.scanner.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.RetrofitClient
import com.example.scanner.model.AuthRequest
import com.example.scanner.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)
    val loginState = MutableLiveData<Boolean>()
    val loginError = MutableLiveData<String?>()

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.getService().login(AuthRequest(username, password))

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("Login123", "Успешно: $body")
                    loginState.value = true
                } else {
                    Log.e("Login123", "Ошибка авторизации: Код: ${response.code()}, сообщение: ${response.errorBody()?.string() ?: "Нет тела ошибки"}")
                    loginState.value = false
                }
            } catch (e: Exception) {
                Log.e("Login123", "Ошибка запроса: ${e.localizedMessage}")
                loginState.value = false
            }
        }
    }


}

