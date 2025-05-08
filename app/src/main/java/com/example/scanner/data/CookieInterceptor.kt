package com.example.scanner.data

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

class CookieInterceptor(private val prefs: SharedPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val cookie = prefs.getString("cookie", null)

        val requestBuilder = originalRequest.newBuilder()
        if (!cookie.isNullOrEmpty()) {
            requestBuilder.addHeader("Cookie", cookie)
        }

        return chain.proceed(requestBuilder.build())
    }
}

