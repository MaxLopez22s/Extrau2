package com.example.mobile

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.app.Application

class WearDataReceiver : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        Log.d("PHONE_APP", "Mensaje recibido: path=${messageEvent.path}")

        if (messageEvent.path == "/heart_rate") {
            try {
                val message = String(messageEvent.data)
                Log.d("PHONE_APP", "📩 Mensaje: $message")

                // Procesar el mensaje
                val parts = message.split(":")
                if (parts.size >= 2 && parts[0] == "HR") {
                    val heartRate = parts[1].toFloat()
                    Log.d("PHONE_APP", "❤️ Frecuencia cardíaca: $heartRate BPM")

                    // Enviar broadcast para actualizar la UI
                    sendBroadcastToActivity(heartRate)
                }
            } catch (e: Exception) {
                Log.e("PHONE_APP", "Error procesando mensaje", e)
            }
        }
    }

    private fun sendBroadcastToActivity(heartRate: Float) {
        val intent = Intent("HEART_RATE_UPDATE")
        intent.putExtra("heart_rate", heartRate)
        sendBroadcast(intent)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("PHONE_APP", "Servicio WearDataReceiver creado")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("PHONE_APP", "Servicio WearDataReceiver destruido")
    }
}