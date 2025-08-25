package com.example.mobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var capabilitySetup: CapabilitySetup
    private var isReceiverRegistered = false
    private val heartRateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "HEART_RATE_UPDATE") {
                val heartRate = intent.getFloatExtra("heart_rate", 0f)
                Log.d("PHONE_APP", "📡 Broadcast recibido: $heartRate BPM")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("PHONE_APP", "📱 Iniciando MainActivity")

        try {
            // Registrar receiver para broadcasts
            val filter = IntentFilter("HEART_RATE_UPDATE")

            // Registrar el receiver con el flag correcto para diferentes versiones de Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ (API 33+) requiere RECEIVER_EXPORTED o RECEIVER_NOT_EXPORTED
                registerReceiver(heartRateReceiver, filter, Context.RECEIVER_EXPORTED)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ (API 26+) - método normal
                registerReceiver(heartRateReceiver, filter)
            } else {
                // Versiones anteriores
                registerReceiver(heartRateReceiver, filter)
            }

            isReceiverRegistered = true
            Log.d("PHONE_APP", "✅ Receiver registrado correctamente")

            capabilitySetup = CapabilitySetup(this)
            capabilitySetup.setupCapability()

            // Debug: probar todos los endpoints
            val apiService = ApiService()
            apiService.testAllEndpoints()

        } catch (e: Exception) {
            Log.e("PHONE_APP", "❌ Error en onCreate: ${e.message}")
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HeartRateApp(capabilitySetup = capabilitySetup)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (isReceiverRegistered) {
                unregisterReceiver(heartRateReceiver)
                Log.d("PHONE_APP", "✅ Receiver desregistrado")
            }
        } catch (e: IllegalArgumentException) {
            Log.w("PHONE_APP", "Receiver no estaba registrado")
        }
    }
}

@Composable
fun HeartRateApp(capabilitySetup: CapabilitySetup) {
    var heartRate by remember { mutableStateOf(0f) }
    var connectionStatus by remember { mutableStateOf("Verificando conexión...") }

    // Verificar conexión periódicamente
    LaunchedEffect(Unit) {
        while (true) {
            try {
                capabilitySetup.checkConnection { isConnected ->
                    connectionStatus = if (isConnected) "✅ Conectado al reloj" else "❌ Desconectado"
                    Log.d("PHONE_APP", "📡 Estado conexión: $isConnected")
                }
            } catch (e: Exception) {
                Log.e("PHONE_APP", "❌ Error verificando conexión: ${e.message}")
                connectionStatus = "❌ Error de conexión"
            }
            delay(3000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "❤️", fontSize = 48.sp)
        Text(
            text = "Heart Rate Monitor",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Estado de conexión
        Text(
            text = connectionStatus,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (heartRate > 0) {
            Text(
                text = "${heartRate.toInt()} BPM",
                fontSize = 32.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Text(
                text = "Datos recibidos del reloj",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Esperando datos del reloj...",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "Abre la app en tu Wear OS",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}