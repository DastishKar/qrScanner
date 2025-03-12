package com.example.scanner.model

data class QRCode(
    val created: Long = 0L,  // Время создания QR-кода
    val scanned: Boolean = false, // Статус, был ли QR код сканирован
    val uuid: String = ""
)
