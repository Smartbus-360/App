package com.smartbus360.app.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.smartbus360.app.R
import com.ola.mapsdk.camera.MapControlSettings
import com.ola.mapsdk.interfaces.OlaMapCallback
import com.ola.mapsdk.model.OlaLatLng
import com.ola.mapsdk.model.OlaMarkerOptions
import com.ola.mapsdk.utils.OlaMapsConfig
import com.ola.mapsdk.view.OlaMap
import com.ola.mapsdk.view.OlaMapView

@Composable
fun MapScreen(busLocationUpdates: State<LatLngPlace>) {
    val apiKey =  "CDjyXDYiY6u9SlDQpf7brSijvyuQNgL4ZotfvRdy"
//    var user by remember { mutableStateOf<User?>(null) }




    val context = LocalContext.current
    var olaMap by remember { mutableStateOf<OlaMap?>(null) }
    val mapView = remember { OlaMapView(context) }

    var currentLocation : OlaLatLng

    // Create MapControlSettings instance
    val mapControlSettings = remember {
        MapControlSettings.Builder()
            .setRotateGesturesEnabled(true)
            .setScrollGesturesEnabled(true)
            .setZoomGesturesEnabled(false)
            .setCompassEnabled(true)
            .setTiltGesturesEnabled(true)
            .setDoubleTapGesturesEnabled(true)
            .build()
    }




    AndroidView(
        factory = { mapView.apply {
            getMap(
                apiKey = apiKey,
                olaMapCallback = object : OlaMapCallback {
                    override fun onMapReady(map: OlaMap) {
                        olaMap = map
                        olaMap!!.showCurrentLocation()
                        currentLocation = olaMap!!.getCurrentLocation()?:  OlaLatLng(latitude = 23.3728637,longitude = 85.3446311,
                            altitude = 0.0)
                        olaMap!!.zoomToLocation(currentLocation,8.0)
                        olaMap!!.moveCameraToLatLong(currentLocation,15.5, 1000)

                        olaMap!!.updateMapUiSettings(mapControlSettings)
                        val markerOptions1 = OlaMarkerOptions.Builder()
                            .setMarkerId("marker1")
                            .setPosition(currentLocation)
                            .setIconSize(1F)
                            .setPosition(OlaLatLng(currentLocation.latitude, currentLocation.longitude))
                            .setIsIconClickable(true)
                            .setIconRotation(0f)
                            .setIsAnimationEnable(true)
                            .setIsInfoWindowDismissOnClick(true)
                            .setIconBitmap(drawableToBitmap(context,R.drawable.bus_location))
                            .build()

                        val config = OlaMapsConfig.Builder()
                            .setCurrentLocationIcon(drawableToBitmap(context,R.drawable.bus_location))
                            .build()

//                        olaMap!!.addMarker(markerOptions1)
                    }

                    override fun onMapError(error: String) {
                        // Handle map error
                    }
                }
//                        mapControlSettings // Pass the MapControlSettings

            )
        }},
        modifier = Modifier.fillMaxSize(),
        update = { mapView ->
            // Update the view if necessary
        }
    )

    // Handle lifecycle events
    DisposableEffect(Unit) {
        onDispose {
            // mapView.onDestroy()
        }
    }
}

fun drawableToBitmap(context: Context, drawableId: Int): Bitmap {

    val drawable: Drawable = context.resources.getDrawable(drawableId, context.theme)
    return if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        // Convert Drawable to Bitmap
        val bitmap = Bitmap.createBitmap(
            12,
            22,
            Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
}