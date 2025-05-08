package com.example.scanner.data

import com.example.scanner.model.AuthRequest
import com.example.scanner.model.ScanRequest
import com.example.scanner.model.ScanResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: AuthRequest): Response<Any>

    @POST("auth")
    suspend fun scan(
        @Header("Cookie") cookie: String,
        @Body scanRequest: ScanRequest
    ): Response<ScanResponse>
}