package com.smartbus360.app.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbus360.app.data.api.ApiHelper
import com.smartbus360.app.data.database.AlertStatusDao
import com.smartbus360.app.data.database.AlertStatusEntity
import com.smartbus360.app.data.model.response.BusReplacedResponse
import com.smartbus360.app.data.model.response.GetUserDetailResponseX
import com.smartbus360.app.data.network.RetrofitBuilder.apiService
import com.smartbus360.app.data.repository.MainRepository
import com.smartbus360.app.data.repository.PreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.osmdroid.util.GeoPoint
import retrofit2.HttpException
import java.io.IOException

class StudentScreenViewModel(private val repository: PreferencesRepository, private val mainRepository: MainRepository = MainRepository(
    ApiHelper(apiService)
)
) : ViewModel(), KoinComponent {
    private val _state = MutableStateFlow(GetUserDetailResponseX())
    val   state: StateFlow<GetUserDetailResponseX>
        get() = _state

    private val _stoppages = MutableStateFlow<List<GeoPoint>>(emptyList())
    val stoppages: StateFlow<List<GeoPoint>>
        get() = _stoppages

    private val _stoppagesForPolyLines = MutableStateFlow<List<GeoPoint>>(emptyList())
    val stoppagesForPolyLines: StateFlow<List<GeoPoint>>
        get() = _stoppagesForPolyLines

    private val _busReplacedStatus = MutableStateFlow(BusReplacedResponse())
    val   busReplacedStatus: StateFlow<BusReplacedResponse>
        get() = _busReplacedStatus


    val userId = repository.getUserId()
    private val authToken = repository.getAuthToken()?:"null"
    private val alertStatusDao: AlertStatusDao by inject()
    init {
        viewModelScope.launch {

            // Start polling every 30 seconds
            launch {
                while (isActive) {
                    delay(3_000) // 30 seconds
                    loadData()
//                    loadStoppages()
                }
            }
            try {
                val response = mainRepository.getUserDetail2(userId ?: 0, "Bearer $authToken")
                _state.value = response
            } catch (e: IOException) {
                // Handle network or HTTP-related exceptions here
                Log.e("LOGIN_ERROR", "Network error: ${e.message}")
                // Optionally, update the UI to show an error message (e.g., via a LiveData or StateFlow)
            } catch (e: Exception) {
                // Catch any other unexpected exceptions
                Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
//                _state.value = GetUserDetailResponseX(message = e.message)
                // Handle the error as needed (e.g., display a message or perform a fallback)
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    // Handle unauthorized error
                    Log.e("Unauthorized", "Authentication failed. Redirect to login.")
                } else {
                    // Handle other HTTP errors
                    Log.e("HttpError", "Error code: ${e.code()}, message: ${e.message}")
                }
            }

            try{
                val response = mainRepository.getBusReplacedStatus(state.value.user?.busId?: 0,"Bearer $authToken")
                _busReplacedStatus.value = response
            } catch (e: IOException) {
                // Handle network or HTTP-related exceptions here
                Log.e("LOGIN_ERROR", "Network error: ${e.message}")
                // Optionally, update the UI to show an error message (e.g., via a LiveData or StateFlow)
            } catch (e: Exception) {
                // Catch any other unexpected exceptions
                Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
                // Handle the error as needed (e.g., display a message or perform a fallback)
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    // Handle unauthorized error
                    Log.e("Unauthorized", "Authentication failed. Redirect to login.")
                } else
                {
                    // Handle other HTTP errors
                    Log.e("HttpError", "Error code: ${e.code()}, message: ${e.message}")
                }
            }

            try {
                // Map the response's stoppages to a list of GeoPoint objects

                val stoppagesList = state.value.routeStoppages?.map { stoppage ->
                    GeoPoint(stoppage.latitude.toDouble(), stoppage.longitude.toDouble())
                }
                val stoppagesForPolyLines = state.value.routeStoppages?.map { stoppage ->
                    if (stoppage.reached == 1) {
                        GeoPoint(0.0, 0.0) // Set to 0.0, 0.0 if reached
                    } else {
                        GeoPoint(stoppage.latitude.toDouble(), stoppage.longitude.toDouble()) // Use actual values otherwise
                    }
                }
                // Update the stoppages StateFlow with the new list
                if (stoppagesList != null) {
                    _stoppages.value = stoppagesList
                }
                if (stoppagesForPolyLines != null) {
                    _stoppagesForPolyLines.value = stoppagesForPolyLines
                }
            } catch (e: IOException) {
            // Handle network or HTTP-related exceptions here
            Log.e("LOGIN_ERROR", "Network error: ${e.message}")
            // Optionally, update the UI to show an error message (e.g., via a LiveData or StateFlow)
        } catch (e: Exception) {
            // Catch any other unexpected exceptions
            Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
            // Handle the error as needed (e.g., display a message or perform a fallback)
        }
        catch (e: HttpException) {
            if (e.code() == 401) {
                // Handle unauthorized error
                Log.e("Unauthorized", "Authentication failed. Redirect to login.")
            } else {
                // Handle other HTTP errors
                Log.e("HttpError", "Error code: ${e.code()}, message: ${e.message}")
            }
        }


            try {
                // Check if the alertStatus table is empty
                val existingStatus = alertStatusDao.getAlertStatuses() // Assuming this is a function to fetch all records


                if (existingStatus.count() == 0) {
                    // If the table is empty, insert alert status based on stoppage list with default value (false)
                    stoppages.value.forEachIndexed { stoppage ,_->
                        alertStatusDao.insertAlertStatus(
                            AlertStatusEntity(
                                stopId = stoppage , // Assuming you can use a unique identifier for each stoppage
                                isEnabled = false
                            )
                        )
                    }
                } else {
                    // If not empty, update or insert based on existing alert status
                    Log.e("ERROR", "Database error")
                }

            } catch (e: IOException) {
                // Handle network or HTTP-related exceptions here
                Log.e("LOGIN_ERROR", "Network error: ${e.message}")
                // Optionally, update the UI to show an error message (e.g., via a LiveData or StateFlow)
            } catch (e: Exception) {
                // Catch any other unexpected exceptions
                Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
                // Handle the error as needed (e.g., display a message or perform a fallback)
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    // Handle unauthorized error
                    Log.e("Unauthorized", "Authentication failed. Redirect to login.")
                } else {
                    // Handle other HTTP errors
                    Log.e("HttpError", "Error code: ${e.code()}, message: ${e.message}")
                }
            }



    }

    }

    private suspend fun loadStoppages() {
        try {
            // Check if the alertStatus table is empty
            val existingStatus = alertStatusDao.getAlertStatuses() // Assuming this is a function to fetch all records


            if (existingStatus.count() == 0) {
                // If the table is empty, insert alert status based on stoppage list with default value (false)
                stoppages.value.forEachIndexed { stoppage ,_->
                    alertStatusDao.insertAlertStatus(
                        AlertStatusEntity(
                            stopId = stoppage , // Assuming you can use a unique identifier for each stoppage
                            isEnabled = false
                        )
                    )
                }
            } else {
                // If not empty, update or insert based on existing alert status
                Log.e("ERROR", "Database error")
            }

        } catch (e: IOException) {
            // Handle network or HTTP-related exceptions here
            Log.e("LOGIN_ERROR", "Network error: ${e.message}")
            // Optionally, update the UI to show an error message (e.g., via a LiveData or StateFlow)
        } catch (e: Exception) {
            // Catch any other unexpected exceptions
            Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
            // Handle the error as needed (e.g., display a message or perform a fallback)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                // Handle unauthorized error
                Log.e("Unauthorized", "Authentication failed. Redirect to login.")
            } else {
                // Handle other HTTP errors
                Log.e("HttpError", "Error code: ${e.code()}, message: ${e.message}")
            }
        }

    }


    fun loadData() {
        viewModelScope.launch {
            try {
                val response = mainRepository.getUserDetail2(userId ?: 0, "Bearer $authToken")
                _state.value = response
            } catch (e: IOException) {
                // Handle network or HTTP-related exceptions here
                Log.e("LOGIN_ERROR", "Network error: ${e.message}")
                // Optionally, update the UI to show an error message (e.g., via a LiveData or StateFlow)
            } catch (e: Exception) {
                // Catch any other unexpected exceptions
                Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
//                _state.value = GetUserDetailResponseX(message = e.message)
                // Handle the error as needed (e.g., display a message or perform a fallback)
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    // Handle unauthorized error
                    Log.e("Unauthorized", "Authentication failed. Redirect to login.")
                } else {
                    // Handle other HTTP errors
                    Log.e("HttpError", "Error code: ${e.code()}, message: ${e.message}")
                }
            }

            try{
                val response = mainRepository.getBusReplacedStatus(state.value.user?.busId?: 0,"Bearer $authToken")
                _busReplacedStatus.value = response
            } catch (e: IOException) {
                // Handle network or HTTP-related exceptions here
                Log.e("LOGIN_ERROR", "Network error: ${e.message}")
                // Optionally, update the UI to show an error message (e.g., via a LiveData or StateFlow)
            } catch (e: Exception) {
                // Catch any other unexpected exceptions
                Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
                // Handle the error as needed (e.g., display a message or perform a fallback)
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    // Handle unauthorized error
                    Log.e("Unauthorized", "Authentication failed. Redirect to login.")
                } else
                {
                    // Handle other HTTP errors
                    Log.e("HttpError", "Error code: ${e.code()}, message: ${e.message}")
                }
            }

            try {
                // Map the response's stoppages to a list of GeoPoint objects

                val stoppagesList = state.value.routeStoppages?.map { stoppage ->
                    GeoPoint(stoppage.latitude.toDouble(), stoppage.longitude.toDouble())
                }
                val stoppagesForPolyLines = state.value.routeStoppages?.map { stoppage ->
                    if (stoppage.reached == 1) {
                        GeoPoint(0.0, 0.0) // Set to 0.0, 0.0 if reached
                    } else {
                        GeoPoint(stoppage.latitude.toDouble(), stoppage.longitude.toDouble()) // Use actual values otherwise
                    }
                }
                // Update the stoppages StateFlow with the new list
                if (stoppagesList != null) {
                    _stoppages.value = stoppagesList
                }
                if (stoppagesForPolyLines != null) {
                    _stoppagesForPolyLines.value = stoppagesForPolyLines
                }
            } catch (e: IOException) {
                // Handle network or HTTP-related exceptions here
                Log.e("LOGIN_ERROR", "Network error: ${e.message}")
                // Optionally, update the UI to show an error message (e.g., via a LiveData or StateFlow)
            } catch (e: Exception) {
                // Catch any other unexpected exceptions
                Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
                // Handle the error as needed (e.g., display a message or perform a fallback)
            }
            catch (e: HttpException) {
                if (e.code() == 401) {
                    // Handle unauthorized error
                    Log.e("Unauthorized", "Authentication failed. Redirect to login.")
                } else {
                    // Handle other HTTP errors
                    Log.e("HttpError", "Error code: ${e.code()}, message: ${e.message}")
                }
            }


            try {
                // Check if the alertStatus table is empty
                val existingStatus = alertStatusDao.getAlertStatuses() // Assuming this is a function to fetch all records


                if (existingStatus.count() == 0) {
                    // If the table is empty, insert alert status based on stoppage list with default value (false)
                    stoppages.value.forEachIndexed { stoppage ,_->
                        alertStatusDao.insertAlertStatus(
                            AlertStatusEntity(
                                stopId = stoppage , // Assuming you can use a unique identifier for each stoppage
                                isEnabled = false
                            )
                        )
                    }
                } else {
                    // If not empty, update or insert based on existing alert status
                    Log.e("ERROR", "Database error")
                }

            } catch (e: IOException) {
                // Handle network or HTTP-related exceptions here
                Log.e("LOGIN_ERROR", "Network error: ${e.message}")
                // Optionally, update the UI to show an error message (e.g., via a LiveData or StateFlow)
            } catch (e: Exception) {
                // Catch any other unexpected exceptions
                Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
                // Handle the error as needed (e.g., display a message or perform a fallback)
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    // Handle unauthorized error
                    Log.e("Unauthorized", "Authentication failed. Redirect to login.")
                } else {
                    // Handle other HTTP errors
                    Log.e("HttpError", "Error code: ${e.code()}, message: ${e.message}")
                }
            }



        }
    }


}