package com.example.mobile

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import android.content.Intent

class WearDataReceiver : WearableListenerService() {
    private val apiService by lazy { ApiService() }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        Log.d("PHONE_APP", "📩 Mensaje recibido del reloj")

        if (messageEvent.path == "/heart_rate") {
            try {
                val message = String(messageEvent.data)
                Log.d("PHONE_APP", "Raw message: $message")

                // Procesar el mensaje
                val parts = message.split(":")
                if (parts.size >= 3 && parts[0] == "HR") {
                    val heartRate = parts[1].toFloat()
                    val timestamp = parts[2].toLongOrNull() ?: System.currentTimeMillis()

                    Log.d("PHONE_APP", "❤️ Procesado: $heartRate BPM at $timestamp")

                    // Enviar a la API
                    apiService.sendHeartRateToApi(heartRate, timestamp)

                    // Enviar broadcast para actualizar la UI
                    sendBroadcastToActivity(heartRate)
                } else {
                    Log.e("PHONE_APP", "❌ Formato de mensaje inválido: $message")
                }
            } catch (e: Exception) {
                Log.e("PHONE_APP", "❌ Error procesando mensaje", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("PHONE_APP", "🟢 Servicio WearDataReceiver iniciado")

        // Probar endpoints al iniciar
        val apiService = ApiService()
        apiService.testAllEndpoints()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("PHONE_APP", "🔴 Servicio WearDataReceiver detenido")
    }

    private fun sendBroadcastToActivity(heartRate: Float) {
        try {
            val intent = Intent("HEART_RATE_UPDATE")
            intent.putExtra("heart_rate", heartRate)
            sendBroadcast(intent)
            Log.d("PHONE_APP", "📡 Broadcast enviado: $heartRate BPM")
        } catch (e: Exception) {
            Log.e("PHONE_APP", "❌ Error enviando broadcast", e)
        }
    }
}