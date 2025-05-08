package com.example.scanner.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AuthViewModel"
    val loginState = MutableLiveData<Boolean>()
    val loginError = MutableLiveData<String?>()

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Попытка авторизации: $username")

                val success = RetrofitClient.login(username, password)

                if (success) {
                    Log.d(TAG, "Логин успешен через RetrofitClient.login")

                    // Проверяем наличие куки
                    val cookie = RetrofitClient.getAuthCookie()
                    if (cookie.isNotEmpty()) {
                        Log.d(TAG, "Кука получена и сохранена: $cookie")
                        loginState.value = true
                        loginError.value = null
                    } else {
                        Log.e(TAG, "Авторизация успешна, но кука отсутствует!")
                        loginState.value = false
                        loginError.value = "Ошибка: куки не получены"
                    }
                } else {
                    Log.e(TAG, "RetrofitClient.login вернул false")
                    loginState.value = false
                    loginError.value = "Ошибка авторизации"
                }
            } catch (e: HttpException) {
                val errorMsg = "Ошибка HTTP: ${e.code()} - ${e.message()}"
                Log.e(TAG, errorMsg, e)
                loginState.value = false
                loginError.value = errorMsg
            } catch (e: IOException) {
                val errorMsg = "Ошибка сети: ${e.localizedMessage}"
                Log.e(TAG, errorMsg, e)
                loginState.value = false
                loginError.value = errorMsg
            } catch (e: Exception) {
                val errorMsg = "Непредвиденная ошибка: ${e.localizedMessage}"
                Log.e(TAG, errorMsg, e)
                loginState.value = false
                loginError.value = errorMsg
            }
        }
    }
}