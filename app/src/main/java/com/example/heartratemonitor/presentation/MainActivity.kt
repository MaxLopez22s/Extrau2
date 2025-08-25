/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.heartratemonitor.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WearApp() {
    // Estado para el permiso del sensor corporal
    val bodySensorsPermissionState = rememberPermissionState(
        android.Manifest.permission.BODY_SENSORS
    )

    // Lógica para manejar la respuesta del permiso
    when (bodySensorsPermissionState.status) {
        is PermissionStatus.Granted -> {
            // Si el permiso está concedido, mostrar la pantalla principal del sensor
            SensorMainScreen()
        }
        else -> {
            // Si no tiene permiso, pedirlo
            PermissionScreen(
                permissionState = bodySensorsPermissionState
            )
        }
    }
}

@Composable
fun SensorMainScreen() {
    // Variables de estado
    var heartRate by remember { mutableStateOf(0f) }
    var sensorStatus by remember { mutableStateOf("Iniciando...") }
    var hasHeartRateSensor by remember { mutableStateOf(true) }
    var isPhoneConnected by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf("Verificando...") }

    val context = LocalContext.current
    val messagingService = remember { WearMessagingService(context) }

    // Efecto para verificar conectividad cada 5 segundos
    LaunchedEffect(Unit) {
        while (true) {
            isPhoneConnected = messagingService.isPhoneConnected()
            connectionStatus = if (isPhoneConnected) "Conectado" else "Desconectado"
            delay(5000)
        }
    }

    // Efecto para manejar el sensor
    DisposableEffect(Unit) {
        val sensorManager = ContextCompat.getSystemService(context, SensorManager::class.java)!!
        val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val newHeartRate = event.values[0]
                heartRate = newHeartRate
                sensorStatus = "Latido detectado"

                // Enviar datos al teléfono si está conectado
                if (isPhoneConnected) {
                    messagingService.sendHeartRate(newHeartRate)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                // Manejar cambios de precisión si es necesario
            }
        }

        if (heartRateSensor == null) {
            hasHeartRateSensor = false
            sensorStatus = "Sensor no disponible"
        } else {
            sensorManager.registerListener(
                listener,
                heartRateSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            sensorStatus = "Coloca el dedo en el sensor"
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // UI fuera del DisposableEffect
    HeartRateScreen(
        heartRate = heartRate,
        status = "$sensorStatus | $connectionStatus",
        hasSensor = hasHeartRateSensor,
        isPhoneConnected = isPhoneConnected
    )
}

@Composable
fun HeartRateScreen(
    heartRate: Float,
    status: String,
    hasSensor: Boolean,
    isPhoneConnected: Boolean
) {
    // Colores para mejor contraste
    val primaryColor = Color(0xFF00C853)
    val secondaryColor = Color(0xFF03A9F4)
    val backgroundColor = Color(0xFF121212)
    val textColor = Color.White
    val errorColor = Color(0xFFFF5252)
    val warningColor = Color(0xFFFFC107)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (!hasSensor) {
            // Pantalla de error
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("❌", color = errorColor, style = MaterialTheme.typography.title2)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sensor no disponible", color = errorColor, fontWeight = FontWeight.Bold)
            }
        } else {
            // UI principal
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicador de conexión
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (isPhoneConnected) "📱✅" else "📱❌",
                        style = MaterialTheme.typography.caption2,
                        color = if (isPhoneConnected) primaryColor else warningColor
                    )
                }

                // Contenido principal
                Text("FRECUENCIA CARDÍACA",
                    color = secondaryColor,
                    style = MaterialTheme.typography.caption1)

                Spacer(modifier = Modifier.height(16.dp))

                Text("${heartRate.toInt()}",
                    color = if (heartRate > 0) primaryColor else textColor.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.display2,
                    fontWeight = FontWeight.Bold)

                Text("LPM", color = secondaryColor, style = MaterialTheme.typography.title3)

                Spacer(modifier = Modifier.height(16.dp))

                // Estado de conexión
                if (!isPhoneConnected) {
                    Text("Teléfono no conectado",
                        color = warningColor,
                        style = MaterialTheme.typography.caption2)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(status.uppercase(),
                    color = textColor.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.caption1)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(permissionState: PermissionState) {
    val backgroundColor = Color(0xFF121212)
    val primaryColor = Color(0xFF00C853)
    val textColor = Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔒",
                style = MaterialTheme.typography.display1,
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PERMISO REQUERIDO",
                style = MaterialTheme.typography.title2,
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Para medir tu frecuencia cardíaca, necesitamos acceso al sensor de salud",
                style = MaterialTheme.typography.body2,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            CircularProgressIndicator(
                progress = Float.NaN, // Indeterminado
                indicatorColor = primaryColor
            )
        }

        // Solicitar permiso automáticamente
        LaunchedEffect(Unit) {
            permissionState.launchPermissionRequest()
        }
    }
}

// Vistas previas (sin cambios)
@Preview(device = Devices.WEAR_OS_SQUARE, showSystemUi = true)
@Composable
fun HeartRatePreview() {
    MaterialTheme {
        HeartRateScreen(
            heartRate = 72f,
            status = "Latido detectado",
            hasSensor = true,
            isPhoneConnected = true
        )
    }
}

@Preview(device = Devices.WEAR_OS_SQUARE, showSystemUi = true)
@Composable
fun NoSensorPreview() {
    MaterialTheme {
        HeartRateScreen(
            heartRate = 0f,
            status = "Sensor no disponible",
            hasSensor = false,
            isPhoneConnected = false
        )
    }
}

@Preview(device = Devices.WEAR_OS_SQUARE, showSystemUi = true)
@Composable
fun WaitingPreview() {
    MaterialTheme {
        HeartRateScreen(
            heartRate = 0f,
            status = "Coloca el dedo en el sensor",
            hasSensor = true,
            isPhoneConnected = false
        )
    }
}

@Preview(device = Devices.WEAR_OS_SQUARE, showSystemUi = true)
@Composable
fun ConnectedPreview() {
    MaterialTheme {
        HeartRateScreen(
            heartRate = 65f,
            status = "Conectado y midiendo",
            hasSensor = true,
            isPhoneConnected = true
        )
    }
}

@Preview(device = Devices.WEAR_OS_SQUARE, showSystemUi = true)
@Composable
fun DisconnectedPreview() {
    MaterialTheme {
        HeartRateScreen(
            heartRate = 0f,
            status = "Teléfono no conectado",
            hasSensor = true,
            isPhoneConnected = false
        )
    }
}

@Preview(device = Devices.WEAR_OS_SQUARE, showSystemUi = true)
@Composable
fun PermissionPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            Text(
                text = "Pantalla de Permisos",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}