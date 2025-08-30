package com.smartbus360.app.data.olaMaps

import androidx.lifecycle.ViewModel


import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapViewModel : ViewModel() {

    var route: Route? = null

    fun fetchRoute(
        origin: String,
        destination: String,
        waypoints: String,
        apiKey: String
    ) {
        viewModelScope.launch {
            val call = RetrofitClient.olaMapsService.getRoute(
                origin = origin,
                destination = destination,
                waypoints = waypoints,
                apiKey = apiKey
            )

            call.enqueue(object : Callback<OlaRouteResponse> {
                override fun onResponse(
                    call: Call<OlaRouteResponse>,
                    response: Response<OlaRouteResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            route = it.routes.firstOrNull()
                            // Notify the UI that the route is ready
                        }
                    }
                }

                override fun onFailure(call: Call<OlaRouteResponse>, t: Throwable) {
                    // Handle failure
                    t.printStackTrace()
                }
            })
        }
    }
}
