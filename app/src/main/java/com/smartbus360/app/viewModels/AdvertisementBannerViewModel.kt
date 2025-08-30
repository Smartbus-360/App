package com.smartbus360.app.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbus360.app.data.api.ApiHelper
import com.smartbus360.app.data.model.response.AdvertisementBannerResponse
import com.smartbus360.app.data.network.RetrofitBuilder.apiService
import com.smartbus360.app.data.repository.MainRepository
import com.smartbus360.app.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AdvertisementBannerViewModel(
    private val repository: PreferencesRepository,
    private val mainRepository: MainRepository = MainRepository(ApiHelper(apiService))
) : ViewModel() {


        private val _state = MutableStateFlow(AdvertisementBannerResponse())
        val state: StateFlow<AdvertisementBannerResponse>
            get() = _state



        private val _stateException = MutableStateFlow<Exception?>(null)
        val stateException: StateFlow<Exception?>
            get() = _stateException

        private val _stateExceptionStatus = MutableStateFlow(false)
        val stateExceptionStatus: StateFlow<Boolean>
            get() = _stateExceptionStatus



        init {
            viewModelScope.launch {
          try {
              val response = mainRepository.advertisementBanner()

              _state.value = response

          }
          catch (e: HttpException) {
              // Handle HTTP exceptions with specific status codes
              when (e.code()) {
                  401-> Log.e("Unauthorized", "Authentication failed. Redirect to login.")

                  403 -> Log.e("AddError", "Access denied (403): ${e.message}")
                  else -> Log.e("AddError", "HTTP error: ${e.code()} - ${e.message()}")
              }
              _stateException.value = e
              _stateExceptionStatus.value = true
          } catch (e: IOException) {
              // Handle network-related exceptions
              Log.e("AddError", "Network error: ${e.message}")
              _stateException.value = e
              _stateExceptionStatus.value = true
          } catch (e: Exception) {
              // Handle any other exceptions
              Log.e("AddError", "Unexpected error: ${e.message}")
              _stateException.value = e
              _stateExceptionStatus.value = true
          }


            }
        }




}