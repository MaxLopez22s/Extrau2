package com.example.mobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModelProvider

class HeartRateBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "HEART_RATE_UPDATE") {
            val heartRate = intent.getFloatExtra("heart_rate", 0f)
            Log.d("PHONE_APP", "Broadcast recibido: $heartRate BPM")

            // Actualizar el ViewModel
            val viewModel = ViewModelProvider.AndroidViewModelFactory
                .getInstance(context.applicationContext as Application)
                .create(HeartRateViewModel::class.java)
            viewModel.updateHeartRate(heartRate)
        }
    }
}