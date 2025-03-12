package com.example.scanner.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.scanner.R
import com.example.scanner.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private var successSound: MediaPlayer? = null
    private var errorSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем результат сканирования из Intent
        val isSuccess = intent.getBooleanExtra("SCAN_RESULT", false)

        // Инициализация MediaPlayer для звуков
        successSound = MediaPlayer.create(this, R.raw.success_sound)
        errorSound = MediaPlayer.create(this, R.raw.error_sound)

        // Отображаем результат
        if (isSuccess) {
            binding.statusImageView.setImageResource(R.drawable.ic_check)
            successSound?.start()
        } else {
            binding.statusImageView.setImageResource(R.drawable.ic_cross)
            errorSound?.start()
        }

        // Кнопка для возврата к сканированию
        binding.backToScanButton.setOnClickListener {
            finish() // Закрываем ResultActivity и возвращаемся к ScanActivity
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        successSound?.release()
        errorSound?.release()
    }
}