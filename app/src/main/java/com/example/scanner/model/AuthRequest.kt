package com.example.scanner.model

// AuthRequest.kt
data class AuthRequest(val username: String, val password: String)

// ScanRequest.kt
data class ScanRequest(val qrCode: String)
data class ScanResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)
