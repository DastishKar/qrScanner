package com.example.scanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.scanner.repository.QRCodeRepository

class QRScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val _qrCodeState = MutableLiveData<String>()
    val qrCodeState: MutableLiveData<String> get() = _qrCodeState

    private val qrCodeRepository = QRCodeRepository()

    fun onQRCodeScanned(qrCode: String) {
        // Предполагаем, что qrCode это уникальный идентификатор
        val qrCodeRef = qrCodeRepository.getQRCodeReference().child(qrCode)

        qrCodeRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                _qrCodeState.value = "QR-код не найден в базе данных."
                return@addOnSuccessListener
            }

            val scannedStatus = snapshot.child("scanned").getValue(Boolean::class.java) ?: false

            if (scannedStatus) {
                _qrCodeState.value = "QR-код уже был отсканирован."
            } else {
                qrCodeRef.child("scanned").setValue(true)
                    .addOnSuccessListener {
                        _qrCodeState.value = "QR-код успешно отсканирован."
                    }
                    .addOnFailureListener {
                        _qrCodeState.value = "Ошибка при обновлении статуса QR-кода."
                    }
            }
        }.addOnFailureListener {
            _qrCodeState.value = "Ошибка при подключении к базе данных."
        }
    }
}