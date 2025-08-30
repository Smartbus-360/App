package com.smartbus360.app.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbus360.app.data.api.ApiHelper
import com.smartbus360.app.data.model.response.DateLogResponse
import com.smartbus360.app.data.model.response.GetUserDetailResponseX
import com.smartbus360.app.data.model.response.NotificationResponse
import com.smartbus360.app.data.model.response.ReachTimeResponse
import com.smartbus360.app.data.network.RetrofitBuilder.apiService
import com.smartbus360.app.data.repository.MainRepository
import com.smartbus360.app.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ReachDateTimeViewModel (private val repository: PreferencesRepository, private val mainRepository:
MainRepository = MainRepository(
        ApiHelper(apiService)
    )
) : ViewModel() {
    private val _state = MutableStateFlow(DateLogResponse())
    val state: StateFlow<DateLogResponse>
        get() = _state

    private val _stateUser = MutableStateFlow(GetUserDetailResponseX())
    val   stateUser: StateFlow<GetUserDetailResponseX>
        get() = _stateUser


    private val authToken = repository.getAuthToken() ?: "null"
    val userId = repository.getUserId()

    init {
        viewModelScope.launch {
            try {

                // Step 1: Get user detail
                val response = mainRepository.getUserDetail2(userId ?: 0, "Bearer $authToken")
                _stateUser.value = response

                // Step 2: Get reach times using routeId from user detail
                val routeId = response.user?.routeId ?: 0
                val reachTimes = mainRepository.getReachTimes(routeId, "Bearer $authToken")
                _state.value = reachTimes

            } catch (e: HttpException) {
                if (e.code() == 401) {
                    Log.e("Unauthorized", "Authentication failed. Redirect to login.")
                } else {
                    Log.e("HttpError", "HTTP ${e.code()}: ${e.message}")
                }
            } catch (e: IOException) {
                Log.e("NetworkError", "Network issue: ${e.message}")
            } catch (e: Exception) {
                Log.e("UnexpectedError", "Something went wrong: ${e.message}")
            }
        }
    }

}