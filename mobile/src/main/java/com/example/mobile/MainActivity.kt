package com.example.mobile

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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("PHONE_APP", "📱 Iniciando MainActivity")

        // Inicializar la capacidad
        val capabilitySetup = CapabilitySetup(this)
        capabilitySetup.setupCapability()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: HeartRateViewModel = viewModel()
                    HeartRateApp(capabilitySetup = capabilitySetup, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun HeartRateApp(capabilitySetup: CapabilitySetup, viewModel: HeartRateViewModel) {
    val heartRate by viewModel.heartRate
    var connectionStatus by remember { mutableStateOf("Verificando conexión...") }

    // Verificar conexión periódicamente
    LaunchedEffect(Unit) {
        while (true) {
            val isConnected = capabilitySetup.checkConnection()
            connectionStatus = if (isConnected) "✅ Conectado al reloj" else "❌ Desconectado"
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
            color = if (connectionStatus.contains("✅")) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (heartRate > 0) {
            Text(
                text = "${heartRate.toInt()} BPM",
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary,
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