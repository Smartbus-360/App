package com.smartbus360.app.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeedViewModel(private val dep: Any? = null) : ViewModel() {
    constructor() : this(null)

    private val _currentSpeed = MutableStateFlow(0f) // Speed in km/h
    val currentSpeed = _currentSpeed.asStateFlow()

    // Speed readings to calculate average speed
    private val speedReadings = mutableListOf<Float>()

    // Update speed and calculate average speed
    fun updateSpeed(newSpeed: Float) {
        if (newSpeed >= 1) {
            speedReadings.add(newSpeed)
            if (speedReadings.size > 5) {
                speedReadings.removeAt(0)
            }
            _currentSpeed.value = speedReadings.average().toFloat()
        } else {
            _currentSpeed.value = 0f // Set to 0 if below threshold
        }
    }
}