package com.example.mobile

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val endpoints = listOf(
        Endpoint("https://extra-ru0x.onrender.com/api/heart-rate", "Render"),
        Endpoint("http://100.20.92.101:3000/api/heart-rate", "IP 1"),
        Endpoint("http://44.225.181.72:3000/api/heart-rate", "IP 2"),
        Endpoint("http://44.227.217.144:3000/api/heart-rate", "IP 3")
    )

    data class Endpoint(val url: String, val name: String)

    fun sendHeartRateToApi(heartRate: Float, timestamp: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            var success = false

            for (endpoint in endpoints) {
                if (trySendToEndpoint(endpoint, heartRate, timestamp)) {
                    success = true
                    break
                }
            }

            if (!success) {
                Log.e("API_SERVICE", "❌ Todos los endpoints fallaron")
            }
        }
    }

    private fun trySendToEndpoint(endpoint: Endpoint, heartRate: Float, timestamp: Long): Boolean {
        return try {
            val jsonObject = JSONObject().apply {
                put("bpm", heartRate)
                put("timestamp", timestamp)
                put("device", "wear-os")
                put("source", "android-app")
            }

            val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(endpoint.url)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Log.d("API_SERVICE", "✅ [${endpoint.name}] Datos enviados: $heartRate BPM")
                response.close()
                true
            } else {
                Log.w("API_SERVICE", "⚠️ [${endpoint.name}] Error ${response.code}: ${response.body?.string()}")
                response.close()
                false
            }

        } catch (e: Exception) {
            Log.e("API_SERVICE", "❌ [${endpoint.name}] Exception: ${e.message}")
            false
        }
    }

    // Función para probar todos los endpoints
    fun testAllEndpoints() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("API_SERVICE", "🧪 Probando todos los endpoints...")

            endpoints.forEach { endpoint ->
                val success = trySendToEndpoint(endpoint, 72.0f, System.currentTimeMillis())
                Log.d("API_SERVICE", "🧪 ${endpoint.name}: ${if (success) "✅" else "❌"}")
            }
        }
    }
}