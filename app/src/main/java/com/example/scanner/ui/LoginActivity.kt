package com.example.scanner.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.scanner.data.RetrofitClient
import com.example.scanner.databinding.ActivityLoginBinding
import com.example.scanner.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация RetrofitClient
        RetrofitClient.init(applicationContext)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                Log.d(TAG, "Нажата кнопка входа, попытка авторизации: $username")
                viewModel.loginUser(username, password)
            } else {
                Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loginState.observe(this) { success ->
            if (success) {
                Log.d(TAG, "Авторизация успешна, проверяем куки...")
                val cookie = RetrofitClient.getAuthCookie()
                Log.d(TAG, "Текущая кука после авторизации: ${if (cookie.isEmpty()) "ПУСТАЯ" else cookie}")

                if (cookie.isNotEmpty()) {
                    Toast.makeText(this, "Авторизация успешна", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ScanActivity::class.java))
                    finish()
                } else {
                    Log.e(TAG, "Авторизация успешна, но кука отсутствует!")
                    Toast.makeText(this, "Ошибка авторизации: куки не получены", Toast.LENGTH_LONG).show()
                }
            } else {
                // Обработка ошибки отдельно
                Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loginError.observe(this) { error ->
            error?.let {
                Log.e(TAG, "Ошибка авторизации: $it")
                Toast.makeText(this, "Ошибка: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }
}