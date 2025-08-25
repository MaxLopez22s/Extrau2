package com.example.heartratemonitor.presentation

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WearMessagingService(private val context: Context) {
    private val messageClient by lazy { Wearable.getMessageClient(context) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(context) }
    private val scope = CoroutineScope(Dispatchers.IO)

    // Nombre de la capacidad que ambos deben compartir
    companion object {
        const val HEART_RATE_CAPABILITY = "heart_rate_monitor_capability"
    }

    suspend fun sendHeartRateToPhone(heartRate: Float) {
        try {
            Log.d("WEAR_COMM", "Buscando teléfonos con capacidad: $HEART_RATE_CAPABILITY")

            val capabilities = capabilityClient
                .getCapability(HEART_RATE_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                .await()

            Log.d("WEAR_COMM", "Nodos encontrados: ${capabilities.nodes.size}")
            capabilities.nodes.forEach { node ->
                Log.d("WEAR_COMM", "Nodo: ${node.displayName}, id: ${node.id}")
            }

            if (capabilities.nodes.isNotEmpty()) {
                for (node in capabilities.nodes) {
                    val message = "HR:${heartRate}:${System.currentTimeMillis()}"
                    val result = messageClient.sendMessage(
                        node.id,
                        "/heart_rate",
                        message.toByteArray()
                    ).await()

                    Log.d("WEAR_COMM", "✅ Datos enviados a ${node.displayName}: $heartRate BPM")
                }
            } else {
                Log.d("WEAR_COMM", "📵 No hay teléfonos conectados con la capacidad")
            }
        } catch (e: Exception) {
            Log.e("WEAR_COMM", "❌ Error enviando datos: ${e.message}")
        }
    }

    fun sendHeartRate(heartRate: Float) {
        scope.launch {
            sendHeartRateToPhone(heartRate)
        }
    }

    // Función para verificar la conectividad
    suspend fun isPhoneConnected(): Boolean {
        return try {
            val capabilities = capabilityClient
                .getCapability(HEART_RATE_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                .await()
            capabilities.nodes.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}