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
        const val HEART_RATE_CAPABILITY = "heart_rate_monitor_capability"
    }

    fun setupCapability() {
        scope.launch {
            try {
                // Forzar el descubrimiento llamando a la capacidad
                val capabilityInfo = capabilityClient
                    .getCapability(HEART_RATE_CAPABILITY, CapabilityClient.FILTER_ALL)
                    .await()

                Log.d("PHONE_APP", "📱 Capabilidad '$HEART_RATE_CAPABILITY' configurada")
                Log.d("PHONE_APP", "📱 Nodos encontrados: ${capabilityInfo.nodes.size}")

                capabilityInfo.nodes.forEach { node ->
                    Log.d("PHONE_APP", "📱 Nodo: ${node.displayName}, id: ${node.id}, nearby: ${node.isNearby}")
                }

            } catch (e: Exception) {
                Log.e("PHONE_APP", "❌ Error configurando capacidad: ${e.message}")
            }
        }
    }

    // Función adicional para verificar conectividad
    suspend fun checkConnection(): Boolean {
        return try {
            val capabilities = capabilityClient
                .getCapability(HEART_RATE_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                .await()
            val isConnected = capabilities.nodes.isNotEmpty()
            Log.d("PHONE_APP", "📱 Conectado: $isConnected, nodos: ${capabilities.nodes.size}")
            isConnected
        } catch (e: Exception) {
            Log.e("PHONE_APP", "❌ Error verificando conexión: ${e.message}")
            false
        }
    }
}