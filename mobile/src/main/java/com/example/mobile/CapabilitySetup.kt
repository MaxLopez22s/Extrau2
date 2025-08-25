package com.example.mobile

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable

class CapabilitySetup(private val context: Context) {
    private val capabilityClient by lazy { Wearable.getCapabilityClient(context) }

    companion object {
        const val HEART_RATE_CAPABILITY = "heart_rate_capability"
    }

    fun setupCapability() {
        capabilityClient.getCapability(HEART_RATE_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilityInfo ->
                Log.d("PHONE_APP", "📱 Capacidad '$HEART_RATE_CAPABILITY' - Nodos: ${capabilityInfo.nodes.size}")

                capabilityInfo.nodes.forEach { node ->
                    Log.d("PHONE_APP", "📱 Nodo encontrado: ${node.displayName}, id: ${node.id}, nearby: ${node.isNearby}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("PHONE_APP", "❌ Error en capacidad: ${e.message}")
            }
    }

    fun checkConnection(callback: (Boolean) -> Unit) {
        capabilityClient.getCapability(HEART_RATE_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilities ->
                callback(capabilities.nodes.isNotEmpty())
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}