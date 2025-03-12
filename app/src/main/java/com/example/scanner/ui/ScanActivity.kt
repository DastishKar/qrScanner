package com.example.scanner.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.scanner.R
import com.example.scanner.databinding.ActivityScanBinding
import com.example.scanner.viewmodel.QRScannerViewModel
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var viewModel: QRScannerViewModel
    private lateinit var barcodeView: DecoratedBarcodeView
    private var isScanning = true // Флаг для контроля сканирования

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Подключаем ViewModel
        viewModel = ViewModelProvider(this)[QRScannerViewModel::class.java]

        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация barcodeView
        barcodeView = findViewById(R.id.barcode_scanner)

        // Наблюдаем за состоянием QR-кода
        viewModel.qrCodeState.observe(this, Observer { message ->
            when (message) {
                "QR-код успешно отсканирован." -> {
                    // Передаем результат в ResultActivity
                    val intent = Intent(this, ResultActivity::class.java).apply {
                        putExtra("SCAN_RESULT", true) // Успех
                    }
                    startActivity(intent)
                    finish() // Завершаем ScanActivity
                }
                else -> {
                    // Передаем результат в ResultActivity
                    val intent = Intent(this, ResultActivity::class.java).apply {
                        putExtra("SCAN_RESULT", false) // Ошибка
                    }
                    startActivity(intent)
                    finish() // Завершаем ScanActivity
                }
            }
        })

        // Настройка декодирования QR-кодов
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                if (isScanning) { // Проверяем, активно ли сканирование
                    val qrCode = result.text.trim()  // Отсканированный QR-код
                    viewModel.onQRCodeScanned(qrCode)  // Вызываем обработку QR-кода через ViewModel
                    isScanning = false // Останавливаем сканирование
                }
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {
                // Логика для отображения точек результата (если нужно)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        isScanning = true // Включаем сканирование при возобновлении активности
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }
}