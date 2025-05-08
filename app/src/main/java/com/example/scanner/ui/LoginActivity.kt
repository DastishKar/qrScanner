package com.example.scanner.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.scanner.databinding.ActivityLoginBinding
import com.example.scanner.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                viewModel.loginUser(username, password)
            } else {
                Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loginState.observe(this) { success ->
            if (success) {
                startActivity(Intent(this, ScanActivity::class.java))
                finish()
            } else {
                // Обработка ошибки отдельно
                Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loginError.observe(this) { error ->
            error?.let {
                Log.e("LoginActivity123", "Ошибка авторизации: $it")
            }
        }

    }
}