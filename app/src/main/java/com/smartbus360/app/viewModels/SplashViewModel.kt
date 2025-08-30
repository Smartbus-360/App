package com.smartbus360.app.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbus360.app.data.repository.PreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(private val preferencesRepository: PreferencesRepository) : ViewModel() {

    fun checkOnboarding(onFinish: (Boolean) -> Unit) {
        viewModelScope.launch {
            delay(500) // Simulating splash delay
            val isOnboardingCompleted = preferencesRepository.isOnboardingCompleted()
            onFinish(isOnboardingCompleted)
        }
    }
}
