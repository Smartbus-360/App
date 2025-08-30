package com.smartbus360.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smartbus360.app.ui.theme.Poppins
import com.smartbus360.app.data.api.RetrofitInstance
import com.smartbus360.app.data.repository.PreferencesRepository
import kotlinx.coroutines.launch
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.activity.compose.BackHandler
import android.app.Activity
import io.socket.client.Manager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee


data class BusInfo(
    val busId: Int,
    val busNo: String,
    var driverName: String = "",
    var driverPhone: String = "",
    var status: Boolean,
    var latitude: Double,
    var longitude: Double,
    var speed: Double,
    var placeName: String = ""
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController
) {
    val context = navController.context
    BackHandler {
        // Move app to background instead of navigating or logging out
        (context as? Activity)?.moveTaskToBack(true)
    }

    var busList by remember { mutableStateOf<List<BusInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) } // 👈 for the floating pill

    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val token = "Bearer " + (sharedPreferences.getString("ACCESS_TOKEN", "") ?: "")

    // ✅ Socket.IO instance
    var mSocket: Socket? by remember { mutableStateOf(null) }

    // ✅ Fetch initial bus list (REST API) and subscribe for updates
    LaunchedEffect(true) {
        coroutineScope.launch {
            try {
                isLoading = true
                val response = RetrofitInstance.api.getBuses(token)
                val initialList = response.map {
                    BusInfo(
                        busId = it.driverId ?: 0,
                        busNo = it.busNumber,
                        driverName = "",
                        driverPhone = "",
                        status = (it.latitude ?: 0.0 != 0.0 && it.longitude ?: 0.0 != 0.0),
                        latitude = it.latitude ?: 0.0,
                        longitude = it.longitude ?: 0.0,
                        speed = it.speed ?: 0.0

                    )
                }
                busList = initialList

                // ✅ Connect to WebSocket after loading buses
                val options = IO.Options()
                options.transports = arrayOf(io.socket.engineio.client.transports.WebSocket.NAME)
                options.reconnection = true
                options.forceNew = true
                options.query = "token=${token.replace("Bearer ", "")}"  // ✅ Pass token
                mSocket = IO.socket("https://api.smartbus360.com/admin/notification", options)

                mSocket?.connect()
                mSocket?.on(Socket.EVENT_CONNECT) {
                    println("✅ Connected to Admin Notification Socket")
                    val current = busList
                    current.forEach { bus ->
                        val data = JSONObject().put("driverId", bus.busId) // send as NUMBER
                        mSocket?.emit("subscribeToDriver", data)
                    }
                }
// Also handle explicit reconnect events (older Socket.IO clients fire these)
                mSocket?.io()?.on(Manager.EVENT_RECONNECT) {
                    val current = busList
                    current.forEach { bus ->
                        val data = JSONObject().put("driverId", bus.busId)
                        mSocket?.emit("subscribeToDriver", data)
                    }
                }


                mSocket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                    println("❌ Socket connection error: ${args.joinToString()}")
                }


//                // ✅ Subscribe to each driver room
//                initialList.forEach { bus ->
//                    val data = JSONObject()
//                        .put("driverId", bus.busId)   // ✅ Ensure it's a string
//                    mSocket?.emit("subscribeToDriver", data)  // ✅ send object, not raw int
//                }


                // ✅ Listen for location updates
                mSocket?.on("locationUpdate") { args ->
                    if (args.isNotEmpty()) {
                        val data = args[0] as JSONObject
                        val driverInfo = data.getJSONObject("driverInfo")
                        val driverId = driverInfo.getInt("id")  // ✅ Match this to bus.busId
                        val driverName = driverInfo.optString("name", "")
                        val driverPhone = driverInfo.optString("phone", "")
                        val latitude = data.getDouble("latitude")
                        val longitude = data.getDouble("longitude")
                        val speed = data.optDouble("speed", 0.0)
                        val placeName = if (data.has("placeName") && !data.isNull("placeName")) {
                            data.getString("placeName")
                        } else {
                            "Fetching location..."
                        }
// ✅ turn off loader after setup completes
                        isLoading = false

                        busList = busList.map { bus ->
                            if (bus.busId == driverId) {
                                bus.copy(
                                    driverName = driverName,
                                    driverPhone = driverPhone,
                                    latitude = latitude,
                                    longitude = longitude,
                                    speed = if (speed >= 0) speed else bus.speed,
                                    placeName = placeName,
                                    status = (latitude != 0.0 && longitude != 0.0)
                                )
                            } else bus
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()

                isLoading = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mSocket?.disconnect()
            mSocket?.off("locationUpdate")
        }
    }
    suspend fun refresh() {
        try {
            isLoading = true
            val response = RetrofitInstance.api.getBuses(token)
            val newList = response.map {
                BusInfo(
                    busId = it.driverId ?: 0,
                    busNo = it.busNumber,
                    driverName = "",
                    driverPhone = "",
                    status = (it.latitude ?: 0.0 != 0.0 && it.longitude ?: 0.0 != 0.0),
                    latitude = it.latitude ?: 0.0,
                    longitude = it.longitude ?: 0.0,
                    speed = it.speed ?: 0.0
                )
            }
            busList = newList

            // Re-subscribe to rooms
            newList.forEach { bus ->
                val data = JSONObject().put("driverId", bus.busId)
                mSocket?.emit("subscribeToDriver", data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Institute Admin Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { coroutineScope.launch { refresh() } }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                    TextButton(onClick = {
                        sharedPreferences.edit().clear().apply()
                        val repo = PreferencesRepository(context)
                        repo.setLoggedIn(false)
                        repo.setUserName("")
                        repo.setUserPass("")
                        navController.navigate("role") {
                            popUpTo("adminDashboard") { inclusive = true }
                        }
                    }) {
                        Text("Logout", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        // Wrap content in a Box so the banner can align TopCenter (and stay fixed)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF6F6F6))
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F6F6))
                    .padding(horizontal = 10.dp, vertical = 10.dp)

            ) {
                Spacer(modifier = Modifier.height(20.dp))


                Text(
                    text = "Live Bus Status",
                    fontFamily = Poppins,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (busList.isEmpty()) {
                            item {
                                Text(
                                    text = "No buses available",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            items(busList) { bus ->
                                BusCard(bus)
                            }
                        }
                    }
                }
            }

            TopAlertMarquee(
                text = ("Refresh for better location updates • Live GPS active • Tap to refresh •  ").repeat(2),
                modifier = Modifier
                    .align(Alignment.TopCenter)   // inside the same Box
                    .fillMaxWidth(),
                onClick = {
                    // optional: trigger refresh from the banner
                    coroutineScope.launch {
                        isRefreshing = true
                        refresh()
                        isRefreshing = false
                    }
                }
            )


        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopAlertMarquee(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}   // ← make it non-null with a default
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFD32F2F),
        contentColor = Color.White,
        shadowElevation = 2.dp,
        onClick = onClick        // ← types now match
    ) {
        Text(
            text = text,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .basicMarquee(velocity = 60.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

    @Composable
    fun BusCard(bus: BusInfo) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bus No: ${bus.busNo}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    StatusChip(bus.status)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text("Driver: ${bus.driverName}", fontSize = 16.sp, color = Color.DarkGray)
                Text("Phone: ${bus.driverPhone}", fontSize = 16.sp, color = Color.DarkGray)
                Text(
                    "Speed: ${"%.2f".format(bus.speed)} km/h",
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
                Text(
                    "Location: ${bus.latitude}, ${bus.longitude}",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Place: ${if (bus.placeName.isNotBlank()) bus.placeName else "Fetching location..."}",
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )


            }
        }
    }

    @Composable
    fun StatusChip(isOnline: Boolean) {
        val bgColor = if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336)
        val text = if (isOnline) "Online" else "Offline"

        Box(
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }


