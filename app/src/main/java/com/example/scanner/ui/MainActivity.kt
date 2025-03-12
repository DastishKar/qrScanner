package com.example.scanner.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.scanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initView()
    }

    private fun initBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initView() {
        // Устанавливаем слушатель на FloatingActionButton
        binding.fab.setOnClickListener {
            startQRScanner()
        }
    }

    private fun startQRScanner() {
        // Переход на экран сканера QR-кодов
        val intent = Intent(this,ScanActivity::class.java)
        startActivity(intent)
    }
}
