package com.example.mobile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HeartRateViewModel(application: Application) : AndroidViewModel(application) {
    private val _heartRate = MutableStateFlow(0f)
    val heartRate: StateFlow<Float> = _heartRate

    fun updateHeartRate(newHeartRate: Float) {
        viewModelScope.launch {
            _heartRate.value = newHeartRate
        }
    }
}