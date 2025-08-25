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

    companion object {
        const val HEART_RATE_CAPABILITY = "heart_rate_capability" // ← Nombre simplificado
    }

    suspend fun sendHeartRateToPhone(heartRate: Float) {
        try {
            Log.d("WEAR_COMM", "Buscando teléfonos con capacidad: $HEART_RATE_CAPABILITY")

            val capabilities = capabilityClient
                .getCapability(HEART_RATE_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                .await()

            Log.d("WEAR_COMM", "Nodos encontrados: ${capabilities.nodes.size}")

            if (capabilities.nodes.isNotEmpty()) {
                for (node in capabilities.nodes) {
                    val message = "HR:${heartRate}:${System.currentTimeMillis()}"
                    try {
                        val result = messageClient.sendMessage(
                            node.id,
                            "/heart_rate",
                            message.toByteArray()
                        ).await()
                        Log.d("WEAR_COMM", "✅ Datos enviados a ${node.displayName}: $heartRate BPM")
                    } catch (e: Exception) {
                        Log.e("WEAR_COMM", "❌ Error enviando a ${node.displayName}: ${e.message}")
                    }
                }
            } else {
                Log.d("WEAR_COMM", "📵 No hay teléfonos conectados")
            }
        } catch (e: Exception) {
            Log.e("WEAR_COMM", "❌ Error buscando nodos: ${e.message}")
        }
    }

    // ... resto del código igual ...
}