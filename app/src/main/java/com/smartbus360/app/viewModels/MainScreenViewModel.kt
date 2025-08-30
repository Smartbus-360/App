package com.smartbus360.app.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbus360.app.data.api.ApiHelper
import com.smartbus360.app.data.model.request.BusReachedStoppageRequest
import com.smartbus360.app.data.model.request.MarkFinalStopRequest
import com.smartbus360.app.data.model.response.GetDriverDetailResponseNewXX
import com.smartbus360.app.data.model.response.GetUserDetailResponseX
import com.smartbus360.app.data.network.RetrofitBuilder.apiService
import com.smartbus360.app.data.repository.MainRepository
import com.smartbus360.app.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainScreenViewModel (private val repository: PreferencesRepository, private val mainRepository: MainRepository = MainRepository(
    ApiHelper(apiService)
)
) : ViewModel() {
    private val _state = MutableStateFlow(GetDriverDetailResponseNewXX())
    val   state: StateFlow<GetDriverDetailResponseNewXX>
        get() = _state


    private val _stateException = MutableStateFlow<Exception?>(null)
    val stateException: StateFlow<Exception?>
        get() = _stateException

    private val _stateExceptionStatus = MutableStateFlow(false)
    val stateExceptionStatus: StateFlow<Boolean>
        get() = _stateExceptionStatus

    private val authToken = repository.getAuthToken() ?: "null"

    init {
        viewModelScope.launch {
            try {
                val userId = repository.getDriverId() ?: null

                val res = mainRepository.getDriverDetailNew(userId ?: 0, "Bearer $authToken")
                _state.value = res
            }   catch (e: HttpException) {
                // Handle HTTP exceptions with specific status codes
                when (e.code()) {
                    401->   repository.setLoggedIn(false)
                    404 ->
                        e.message
//                                Log.e("Unauthorized", "Authentication failed. Redirect to login.")

                    403 -> Log.e("LOGIN_ERROR", "Access denied (403): ${e.message}")
                    else -> Log.e("LOGIN_ERROR", "HTTP error: ${e.code()} - ${e.message()}")
                }
                _stateException.value = e
                _stateExceptionStatus.value = true
            } catch (e: IOException) {
                // Handle network-related exceptions
                Log.e("LOGIN_ERROR", "Network error: ${e.message}")
                _stateException.value = e
                _stateExceptionStatus.value = true
            } catch (e: Exception) {
                // Handle any other exceptions
                Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
                _stateException.value = e
                _stateExceptionStatus.value = true
            }

            catch (e: IOException) {
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

    fun markFinalStop(routeId: Int){

        viewModelScope.launch {
            try{
                val response = mainRepository.markFinalStop("Bearer $authToken", data= MarkFinalStopRequest(routeId))
                val res = response
        } catch (e: IOException) {
            // Handle network or HTTP-related exceptions here
            Log.e("LOGIN_ERROR", "Network error: ${e.message}")
            // Optionally, update the UI to show an error message (e.g., via a LiveData or StateFlow)
        } catch (e: Exception) {
            // Catch any other unexpected exceptions
            Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
//                _state.value = GetDriverDetailResponseNewXX(message = e.message)
            // Handle the error as needed (e.g., display a message or perform a fallback)
        }

        }

    }

    fun busReachedStoppage(data: BusReachedStoppageRequest)
    {
        viewModelScope.launch {

            try {
                val response = mainRepository.busReachedStoppage( "Bearer $authToken", data)
                val res = response
//              _state.value = res

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