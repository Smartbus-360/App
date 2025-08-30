package com.smartbus360.app.viewModels

import androidx.lifecycle.ViewModel
import com.smartbus360.app.data.repository.PreferencesRepository

class RoleSelectionViewModel(private val repository: PreferencesRepository) : ViewModel() {
    fun setRole(role: String) {
        repository.setUserRole(role)
    }
}