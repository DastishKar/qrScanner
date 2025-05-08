package com.example.scanner.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.scanner.R
import com.example.scanner.data.RetrofitClient
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
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация RetrofitClient с контекстом
        RetrofitClient.init(applicationContext)

        // Подключаем ViewModel
        viewModel = ViewModelProvider(this)[QRScannerViewModel::class.java]
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация прогресс-диалога
        progressDialog = ProgressDialog(this).apply {
            setMessage("Пожалуйста, подождите...")
            setCancelable(false)
        }

        // Инициализация barcodeView
        barcodeView = findViewById(R.id.barcode_scanner)

        // Наблюдаем за состоянием загрузки
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                progressDialog.show()
            } else {
                progressDialog.dismiss()
            }
        }

        // Наблюдаем за состоянием результата от сервера
        viewModel.qrCodeState.observe(this) { message ->
            // Показываем тост с сообщением
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            // Если это не сообщение о готовности к сканированию, открываем ResultActivity
            if (message != "Авторизация успешна. Готов к сканированию.") {
                val intent = Intent(this, ResultActivity::class.java).apply {
                    putExtra("SCAN_RESULT", message)
                }
                startActivity(intent)

                // Перезапускаем сканирование, но не закрываем активность
                isScanning = true
            }
        }

        // Настройка декодирования QR-кодов
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                if (isScanning) { // Проверяем, активно ли сканирование
                    val qrCode = result.text.trim()  // Отсканированный QR-код

                    // Временно останавливаем сканирование
                    isScanning = false

                    // Отправляем QR-код на сервер
                    viewModel.onQRCodeScanned(qrCode)
                }
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {
                // Логика для отображения точек результата (если нужно)
            }
        })

    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()

        // Проверяем наличие куки при возобновлении активности
        if (RetrofitClient.getAuthCookie().isEmpty()) {
            // Если куки нет, показываем сообщение
            Toast.makeText(this, "Требуется авторизация. Нажмите кнопку Войти.", Toast.LENGTH_LONG).show()
        } else {
            isScanning = true // Включаем сканирование
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
        progressDialog.dismiss() // Закрываем диалог при остановке активности
    }
}