package com.smartbus360.app.viewModels

import androidx.lifecycle.ViewModel
import com.smartbus360.app.data.database.AlertStatusRepository

class AlertStatusViewModel(private val repository: AlertStatusRepository) : ViewModel() {
//    val alertStatuses = repository.getAlertStatuses().stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(5000),
//        initialValue = emptyList()
//    )
//
//    fun updateAlertStatus(stopId: Int, isEnabled: Boolean) {
//        viewModelScope.launch {
//            repository.updateAlertStatus(stopId, isEnabled)
//        }
//    }
}
