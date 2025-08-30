package com.smartbus360.app.viewModels

import androidx.lifecycle.ViewModel
import com.smartbus360.app.data.api.ApiHelper
import com.smartbus360.app.data.network.RetrofitBuilder.apiService
import com.smartbus360.app.data.repository.MainRepository
import com.smartbus360.app.data.repository.PreferencesRepository

class BusReplacedStatusViewModel(private val preference: PreferencesRepository, private val mainRepository: MainRepository = MainRepository(
    ApiHelper(apiService)
)
) : ViewModel() {
//    private val _state = MutableStateFlow(BusReplacedStatus(""))
//    val   state: StateFlow<BusReplacedStatus>
//        get() = _state
//    private val authToken = preference.getAuthToken()?:"null"
////    init {
////        viewModelScope.launch {
////            val userId =  preference.getUserId()?: null
////            val driverId =  preference.getDriverId()?: null
////            val response = mainRepository.getBusReplacedStatus(driverId ?: 0, "Bearer $authToken")
////            val res = response
////            _state.value = res
////
////        }
////    }
//    fun busStatus(driverId: Int?) {
//        viewModelScope.launch {
//            val response = mainRepository.getBusReplacedStatus(driverId ?: 0)
//            val res = response
//            _state.value = res
//        }
//
//    }
}