package com.smartbus360.app.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbus360.app.data.api.ApiHelper
import com.smartbus360.app.data.database.AlertStatusEntity
import com.smartbus360.app.data.model.response.BusNotificationResponse
import com.smartbus360.app.data.model.response.GetDriverDetailResponseNewXX
import com.smartbus360.app.data.model.response.NotificationResponse
import com.smartbus360.app.data.network.RetrofitBuilder.apiService
import com.smartbus360.app.data.repository.MainRepository
import com.smartbus360.app.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import retrofit2.HttpException
import java.io.IOException

class NotificationViewModel (private val repository: PreferencesRepository, private val mainRepository:
MainRepository = MainRepository(
    ApiHelper(apiService)
)
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationResponse())
    val state: StateFlow<NotificationResponse>
        get() = _state

    private val _stateBusNoti = MutableStateFlow(NotificationResponse())
    val stateBusNoti: StateFlow<NotificationResponse>
        get() = _stateBusNoti



    private val authToken = repository.getAuthToken() ?: "null"

    init {
        viewModelScope.launch {
            try {


                val res = mainRepository.notifications( "Bearer $authToken")
                val res2 = mainRepository.busNotifications( "Bearer $authToken")
                val res1 = res2
                _stateBusNoti.value = res1
                _state.value = res


            } catch (e: IOException) {
                // Handle network or HTTP-related exceptions here
                Log.e("LOGIN_ERROR", "Network error: ${e.message}")
                // Optionally, update the UI to show an error message (e.g., via a LiveData or StateFlow)
            } catch (e: Exception) {
                // Catch any other unexpected exceptions
                Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
                // Handle the error as needed (e.g., display a message or perform a fallback)
            }
        }

    }

    fun loadData() {
        viewModelScope.launch {
            try {


                val res = mainRepository.notifications( "Bearer $authToken")
                val res2 = mainRepository.busNotifications( "Bearer $authToken")
                val res1 = res2
                _stateBusNoti.value = res1
                _state.value = res


            } catch (e: IOException) {
                // Handle network or HTTP-related exceptions here
                Log.e("LOGIN_ERROR", "Network error: ${e.message}")
                // Optionally, update the UI to show an error message (e.g., via a LiveData or StateFlow)
            } catch (e: Exception) {
                // Catch any other unexpected exceptions
                Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
                // Handle the error as needed (e.g., display a message or perform a fallback)
            }
        }

    }
}