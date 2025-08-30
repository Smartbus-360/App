package com.smartbus360.app.viewModels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbus360.app.data.api.ApiHelper
import com.smartbus360.app.data.model.request.BusReachedStoppageRequest
import com.smartbus360.app.data.model.response.BusReplacedResponse
import com.smartbus360.app.data.model.response.GetDriverDetailResponseNewXX
import com.smartbus360.app.data.model.response.GetUserDetailResponseX
import com.smartbus360.app.data.network.RetrofitBuilder.apiService
import com.smartbus360.app.data.repository.MainRepository
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.navigation.isWithinRadius
import com.smartbus360.app.navigation.showLocationReachedNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import retrofit2.HttpException
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class BusLocationScreenViewModel (private val preference: PreferencesRepository, private val mainRepository: MainRepository = MainRepository(
    ApiHelper(apiService)
)
) : ViewModel() {
    private val _state = MutableStateFlow(GetDriverDetailResponseNewXX())
    val   state: StateFlow<GetDriverDetailResponseNewXX>
        get() = _state

    private val _stateStudent = MutableStateFlow(GetUserDetailResponseX())
    val   stateStudent: StateFlow<GetUserDetailResponseX>
        get() = _stateStudent


    private val _stoppages = MutableStateFlow<List<GeoPoint>>(emptyList())
    val stoppages: StateFlow<List<GeoPoint>>
        get() = _stoppages

    private val _stoppagesForPolyLines = MutableStateFlow<List<GeoPoint>>(emptyList())
    val stoppagesForPolyLines: StateFlow<List<GeoPoint>>
        get() = _stoppagesForPolyLines

    private val _busReplacedStatus = MutableStateFlow(BusReplacedResponse())
    val   busReplacedStatus: StateFlow<BusReplacedResponse>
        get() = _busReplacedStatus

    private val _triggeredStoppages = mutableStateOf(mutableSetOf<String>())
    private val triggeredStoppages: State<Set<String>> = _triggeredStoppages

    private val _exitedStoppages = mutableStateOf(mutableSetOf<String>())
    private val exitedStoppages: State<Set<String>> = _exitedStoppages
    private val authToken = preference.getAuthToken()?:"null"

    init {
        viewModelScope.launch {
            try {
                val driverId = preference.getDriverId()?: null
                val userId =  preference.getUserId()?: null
//                val authToken = preference.getAuthToken()?:"null"
                val role = preference.getUserRole()

                if(preference.getUserRole() == "driver"){   // change this to driver

                    try{
                        val response = mainRepository.getBusReplacedStatus(state.value.driver?.busId?: 0,"Bearer $authToken")
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
                        val response =
                            mainRepository.getDriverDetailNew(driverId ?: 0, "Bearer $authToken")
                        _state.value = response

                        // Map the response's stoppages to a list of GeoPoint objects
                        val stoppagesList = response.routes.map { stoppage ->
                            GeoPoint(
                                stoppage.stoppageLatitude.toDouble(),
                                stoppage.stoppageLongitude.toDouble()
                            )
                        }
                        val stoppagesForPolyLines = response.routes.map { stoppage ->
                            if (stoppage.stoppageReached == 1) {
                                GeoPoint(0.0, 0.0) // Set to 0.0, 0.0 if reached
                            } else {
                                GeoPoint(
                                    stoppage.stoppageLatitude.toDouble(),
                                    stoppage.stoppageLongitude.toDouble()
                                ) // Use actual values otherwise
                            }
                        }


                        // Update the stoppages StateFlow with the new list
                        _stoppages.value = stoppagesList
                        _stoppagesForPolyLines.value = stoppagesForPolyLines

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


                }else{
                    try {
                        val response =
                            mainRepository.getUserDetail2(userId ?: 0, "Bearer $authToken")
                        _stateStudent.value = response

                        // Map the response's stoppages to a list of GeoPoint objects
                        val stoppagesList = response.routeStoppages?.map { stoppage ->
                            GeoPoint(stoppage.latitude.toDouble(), stoppage.longitude.toDouble())
                        }
                        val stoppagesForPolyLines = response.routeStoppages?.map { stoppage ->
                            if (stoppage.reached == 1) {
                                GeoPoint(0.0, 0.0) // Set to 0.0, 0.0 if reached
                            } else {
                                GeoPoint(
                                    stoppage.latitude.toDouble(),
                                    stoppage.longitude.toDouble()
                                ) // Use actual values otherwise
                            }
                        }
                        // Update the stoppages StateFlow with the new list
                        if (stoppagesList != null) {
                            _stoppages.value = stoppagesList
                        }
                        if (stoppagesForPolyLines != null) {
                            _stoppagesForPolyLines.value = stoppagesForPolyLines
                        }
                    }catch (e: IOException) {
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
          catch (e: HttpException) {
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

    fun checkAndNotifyStoppage(
        latitude: Double,
        longitude: Double,
        state: GetDriverDetailResponseNewXX,
        context: Context
    ) {
        var repository = PreferencesRepository(context)
        // Mutable set to store IDs of triggered stoppages with explicit type
//        val triggeredStoppages = remember { mutableStateOf(mutableSetOf<String>()) }
//        val exitedStoppages = remember { mutableStateOf(mutableSetOf<String>()) }

        val journeyState = repository.journeyFinishedState()
        val relevantRoutes = state.routes.filter {
            it.stopType == journeyState &&
                    when (journeyState) {
                        "morning" -> it.rounds?.morning?.any { round -> round.round == it.routeCurrentRound } == true
                        "afternoon" -> it.rounds?.afternoon?.any { round -> round.round == it.routeCurrentRound } == true
                        "evening" -> it.rounds?.evening?.any { round -> round.round == it.routeCurrentRound } == true
                        else -> false
                    }
        }

        relevantRoutes.forEach { stoppage ->
            val stoppageId = stoppage.stoppageId.toString()
            val isInsideRadius = isWithinRadius(
                latitude, longitude,
                stoppage.stoppageLatitude.toDouble(),
                stoppage.stoppageLongitude.toDouble()
            )

            if (isInsideRadius && (stoppageId !in triggeredStoppages.value || stoppageId in exitedStoppages.value)) {
                _exitedStoppages.value = exitedStoppages.value.toMutableSet().apply { remove(stoppageId) }

                val formattedTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))

                viewModelScope.launch {
                    try {
                        busReachedStoppage(
                            BusReachedStoppageRequest(
                                formattedTime,
                                "1",
                                stoppageId,
                                stoppage.routeId.toString(),
                                stoppage.stopType.toString(),
                                stoppage.routeCurrentRound
                            )
                        )
                    } catch (e: Exception) {
                        // Log or handle API failure
                    }
                }

                showLocationReachedNotification(
                    context,
                    title = "Stoppage Reached",
                    message = "You have reached a scheduled stop at ${stoppage.stoppageName} at $formattedTime"
                )

                _triggeredStoppages.value = triggeredStoppages.value.toMutableSet().apply { add(stoppageId) }

                viewModelScope.launch {
                    delay(3000)
//                    counter.value += 1
                }
            } else if (!isInsideRadius && stoppageId in triggeredStoppages.value) {
                _exitedStoppages.value = exitedStoppages.value.toMutableSet().apply { add(stoppageId) }
                _triggeredStoppages.value = triggeredStoppages.value.toMutableSet().apply { remove(stoppageId) }
            }
        }
    }

}


