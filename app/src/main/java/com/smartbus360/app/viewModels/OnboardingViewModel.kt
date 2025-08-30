package com.smartbus360.app.viewModels

import androidx.lifecycle.ViewModel
import com.smartbus360.app.data.repository.PreferencesRepository


class OnboardingViewModel(private val preferencesRepository: PreferencesRepository) : ViewModel() {

    fun completeOnboarding() {
        preferencesRepository.setOnboardingCompleted()
    }
}