package com.example.scanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.RetrofitClient
import com.example.scanner.model.ScanRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class QRScannerViewModel : ViewModel() {

    private val _qrCodeState = MutableLiveData<String>()
    val qrCodeState: LiveData<String> get() = _qrCodeState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading


    // Отправка QR-кода на сервер
    fun onQRCodeScanned(qrCode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val apiService = RetrofitClient.getService()

                // Получаем куки из RetrofitClient
                val cookie = RetrofitClient.getAuthCookie()

                // Проверяем, есть ли куки
                if (cookie.isEmpty()) {
                    _qrCodeState.value = "Ошибка: нет авторизации. Пожалуйста, войдите в систему."
                    _isLoading.value = false
                    return@launch
                }

                // Создаем объект запроса
                val scanRequest = ScanRequest(qrCode)

                // Выполняем запрос
                val response = apiService.scan(cookie, scanRequest)

                // Обрабатываем ответ
                if (response.isSuccessful) {
                    val scanResponse = response.body()
                    if (scanResponse?.success == true) {
                        _qrCodeState.value = scanResponse.message
                    } else {
                        _qrCodeState.value = "Ошибка от сервера: ${scanResponse?.message ?: "Неизвестная ошибка"}"
                    }
                } else {
                    // Если у нас код 401 - значит токен истек, нужна повторная авторизация
                    if (response.code() == 401) {
                        _qrCodeState.value = "Сессия истекла. Требуется повторная авторизация."
                    } else {
                        _qrCodeState.value = "Ошибка при сканировании QR-кода: ${response.code()} - ${response.message()}"
                    }
                }
            } catch (e: HttpException) {
                _qrCodeState.value = "Ошибка при запросе: ${e.message()}"
            } catch (e: Throwable) {
                _qrCodeState.value = "Неизвестная ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Метод для установки состояния ошибки
    fun setErrorState(message: String) {
        _qrCodeState.value = message
    }
}