package com.example.mobile

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CapabilitySetup(private val context: Context) {
    private val capabilityClient by lazy { Wearable.getCapabilityClient(context) }
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val HEART_RATE_CAPABILITY = "heart_rate_capability" // ← CAMBIADO para coincidir con wear
    }

    fun setupCapability() {
        scope.launch {
            try {
                // Verificar la capacidad
                val capabilityInfo = capabilityClient
                    .getCapability(HEART_RATE_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                    .await()

                Log.d("PHONE_APP", "📱 Capacidad '$HEART_RATE_CAPABILITY' - Nodos: ${capabilityInfo.nodes.size}")

                capabilityInfo.nodes.forEach { node ->
                    Log.d("PHONE_APP", "📱 Nodo encontrado: ${node.displayName}, id: ${node.id}, nearby: ${node.isNearby}")
                }

            } catch (e: Exception) {
                Log.e("PHONE_APP", "❌ Error en capacidad: ${e.message}")
            }
        }
    }

    suspend fun checkConnection(): Boolean {
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