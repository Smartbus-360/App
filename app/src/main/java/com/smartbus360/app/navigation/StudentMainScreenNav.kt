package com.smartbus360.app.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import com.smartbus360.app.R
import com.smartbus360.app.data.database.AlertStatusDao
import com.smartbus360.app.data.model.response.GetUserDetailResponseX
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.ui.component.NotificationPopup
import com.smartbus360.app.ui.screens.BatteryOptimizationScreen
import com.smartbus360.app.ui.screens.BusLocationScreen
import com.smartbus360.app.ui.screens.CreditsScreen
import com.smartbus360.app.ui.screens.DateTimeLogScreen
import com.smartbus360.app.ui.screens.LatLngPlace
import com.smartbus360.app.ui.screens.NotificationList
import com.smartbus360.app.ui.screens.NotificationPermissionRequest
import com.smartbus360.app.ui.screens.ReachedStopagesScreen
import com.smartbus360.app.ui.screens.StudentAboutScreens
import com.smartbus360.app.ui.screens.StudentHomeScreenContent
import com.smartbus360.app.ui.screens.StudentScreenSettings
import com.smartbus360.app.ui.screens.StudentSettingsLanguageSelectionScreen
import com.smartbus360.app.ui.screens.createSocket
import com.smartbus360.app.ui.screens.getPlaceNameFromLatLng
import com.smartbus360.app.ui.screens.listenForLocationUpdates
import com.smartbus360.app.ui.screens.showNotification
import com.smartbus360.app.ui.screens.subscribeToDriver
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue
import com.smartbus360.app.ui.webView.WebViewScreen
import com.smartbus360.app.utility.AppPreferences
import com.smartbus360.app.viewModels.LanguageViewModel
import com.smartbus360.app.viewModels.NetworkViewModel
import com.smartbus360.app.viewModels.NotificationViewModel
import com.smartbus360.app.viewModels.StudentScreenViewModel
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.util.GeoPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.smartbus360.app.ui.screens.AdminDashboardScreen
import com.smartbus360.app.ui.screens.BusInfo

val dummyBuses = listOf(
    BusInfo(1, "CG 04 AB 1234", "Dummy Driver 1", "9999999999", true, 20.298, 81.667, 45.0, "Raipur"),
    BusInfo(2, "CG 04 XY 5678", "Dummy Driver 2", "8888888888", false, 20.312, 81.701, 0.0, "Bhilai")
)



@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "SuspiciousIndentation",
    "CoroutineCreationDuringComposition"
)
@Composable
fun StudentMainScreenNav(navController: NavHostController = rememberNavController()
//                         ,
//     refresh: String?
//                         studentScreenViewModel1: StudentScreenViewModel = getViewModel(),
//                         notificationViewModel: NotificationViewModel = getViewModel(),
                         ) {


    val studentScreenViewModel: StudentScreenViewModel = koinViewModel()
    val  notificationViewModel: NotificationViewModel = koinViewModel()
    // This gets triggered on re-navigation
    LaunchedEffect(Unit) {
        studentScreenViewModel.loadData()
        notificationViewModel.loadData()
    }

    val  context = LocalContext.current

    val activity = context as? Activity // Ensure the context is an Activity

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navControllerBottomBar = rememberNavController()
//    val  context = LocalContext.current
    val preferencesRepository = PreferencesRepository(context)
    val busLocationUpdates = remember { mutableStateOf(LatLngPlace(20.5937, 78.962, "New Delhi")) } // Initial coordinates for India

    var showExitDialog by remember { mutableStateOf(false) }

    var placeName by remember { mutableStateOf("Fetching location...") }
    val busLocation = remember { mutableStateOf(LatLngPlace(20.5937, 78.9629, placeName = placeName)) } // Initial coordinates for India
    val socket: Socket?
        socket = remember { createSocket("users",preferencesRepository.getAuthToken()?:"null") }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var driverName by remember { mutableStateOf("") }
    var driverPhone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Waiting for driver's location...") }

    val state = studentScreenViewModel.state.collectAsState()
    val stateBusReplaced = studentScreenViewModel.busReplacedStatus.collectAsState()
    val stateNotification = notificationViewModel.state.collectAsState()
    val stateBusNoti = notificationViewModel.stateBusNoti.collectAsState()
    val state1 = studentScreenViewModel.state.collectAsState()
    var stoppages = state.value.routeStoppages
    val stoppageX = studentScreenViewModel.stoppages.collectAsState()
    val stoppagesForPolyLines = studentScreenViewModel.stoppagesForPolyLines.collectAsState()

    val triggeredStoppages = remember { mutableStateOf(mutableSetOf<String>()) }
    val exitedStoppages = remember { mutableStateOf(mutableSetOf<String>()) }

    val counter = remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val mediaPlayer = MediaPlayer.create(context, R.raw.bus_reached_notify)
    // in - app update
    val appUpdateManager: AppUpdateManager = remember { AppUpdateManagerFactory.create(context) }

    // State to track if an update is available
    var updateAvailable by remember { mutableStateOf(false) }


    val appUpdateInfoTask = appUpdateManager.appUpdateInfo
    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
        ) {
            updateAvailable = true

            try {
                if (activity != null) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        activity,
                        100 // Request code for tracking
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast
                    .makeText(
                        context,
                        "Failed to start update",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }

        }
    }
    // in-app review
//    val reviewManager = ReviewManagerFactory.create(context)
//    val scope = rememberCoroutineScope()
//
//    scope.launch {
//        try {
//            // Request the review flow
//            val reviewInfo = reviewManager.requestReview()
//            // Launch the review flow
//            if (activity != null) {
//                reviewManager.launchReviewFlow(activity, reviewInfo)
//            }
////            Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            // Handle error
//            e.printStackTrace()
////            Toast.makeText(context, "Error launching review flow: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
//    }



    LaunchedEffect(Unit) {
        AppPreferences.incrementLaunchCount(context)

        val launchCount = AppPreferences.getLaunchCount(context)
        val alreadyShown = AppPreferences.hasReviewBeenShown(context)

        if (launchCount >= 5 && !alreadyShown) {
            try {
                val reviewManager = ReviewManagerFactory.create(context)
                val reviewRequest = reviewManager.requestReviewFlow()
                val reviewInfo = reviewRequest.await()

                activity?.let {
                    val flowResult = reviewManager.launchReviewFlow(it, reviewInfo)
                    AppPreferences.setReviewShown(context)
                }
            } catch (e: Exception) {
                Log.e("Review", "Error: ${e.message}")
            }
        }
    }


    if (state.value.success == true){
    NotificationPermissionRequest()
}






if (state.value.success == true && stateBusNoti.value.success == true && stateNotification.value.success == true) {



stoppages = state.value.routeStoppages

    val unseenNotifications = stateNotification.value.notifications.filter {
        it.id != null && it.id.toString() !in preferencesRepository.getSeenNotifications()
                && it.isMandatory == 1
    }

    val unseenBusNotifications = stateBusNoti.value.notifications.filter {
        it.id != null && it.id.toString() !in preferencesRepository.getSeenBusNotifications()
                && it.isMandatory == 1
    }




    // Combine both notifications lists
    val allUnseenNotifications = (stateNotification.value.notifications.filter {
        it.id != null && it.id.toString() !in preferencesRepository.getSeenNotifications()
                && it.isMandatory == 1
    } + stateBusNoti.value.notifications.filter {
        it.id != null && it.id.toString() !in preferencesRepository.getSeenNotifications()
                && it.isMandatory == 1
    }).distinctBy { it.id } // Ensure no duplicates

    var showPopup by remember { mutableStateOf(allUnseenNotifications.isNotEmpty()) }

    if (showPopup) {
        NotificationPopup(
            notifications = allUnseenNotifications,
            onDismiss = {
                showPopup = false
                allUnseenNotifications.forEach { it.id?.let { id -> preferencesRepository.markNotificationAsSeen(id) } }
            },
            onNotificationSeen = { notification ->
                notification.id?.let { id -> preferencesRepository.markNotificationAsSeen(id) }
            }
        )
    }


    // Initialize the alertStatus list with appropriate size
    val alertStatusDao: AlertStatusDao = get() // Koin example



    val totalItems =
        if (!state.value.routeStoppages.isNullOrEmpty()) {
            when (state.value.user?.routeCurrentJourneyPhase) {
                "afternoon" -> state.value.routeStoppages?.count {
                    it.stopType == "afternoon" &&
                            it.rounds?.afternoon?.any { round -> round.round == state.value.user?.routeCurrentRound } == true
                } ?: 0
                "evening" -> state.value.routeStoppages?.count {
                    it.stopType == "evening" &&
                            it.rounds?.evening?.any { round -> round.round == state.value.user?.routeCurrentRound } == true
                } ?: 0
                else -> state.value.routeStoppages?.count {
                    it.stopType == "morning" &&
                            it.rounds?.morning?.any { round -> round.round == state.value.user?.routeCurrentRound } == true
                } ?: 0
            }
        } else {
            0
        }

// Initialize alertStatus with proper size
    val alertStatus = remember {
        mutableStateListOf<Boolean>().apply {
            addAll(List(totalItems) { true }) // Default to 'false' for all stops
        }
    }



// Fetch and update alert status from the database
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            alertStatusDao.getAlertStatuses().collect { allStatuses ->
                val statusMap = allStatuses.associate { it.stopId to it.isEnabled }
                statusMap.forEach { (stopId, isEnabled) ->
                    if (stopId in alertStatus.indices) { // Ensure stopId is within range
                        alertStatus[stopId] = isEnabled
                    }
                }
            }
        }
    }

    if (state.value.user?.routeCurrentJourneyPhase == "afternoon") {
    stoppages?.filter { it.stopType == "afternoon"
            &&
            it.rounds?.afternoon?.any { round ->
                round.round == state.value.user?.routeCurrentRound
            } == true

    }?.forEachIndexed { index, stoppage ->
        // Check if within radius and if notification hasn't been triggered for this stoppage
        if (index in alertStatus.indices && isWithinRadius(
                currentLatitude = latitude,
                currentLongitude = longitude,
                targetLatitude = stoppage.latitude.toDouble(),
                targetLongitude = stoppage.longitude.toDouble()
            ) && (stoppage.stopOrder.toString() !in triggeredStoppages.value ||
                    stoppage.stopOrder.toString() in exitedStoppages.value)
            && alertStatus[index]
        ) {
            // Get the current date and time in 12-hour format
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")
            val formattedTime = currentDateTime.format(formatter)


            // Remove from exitedStoppages
            exitedStoppages.value =
                exitedStoppages.value.toMutableSet().apply { remove(stoppage.stopOrder.toString()) }

            showNotification(
                context,
                "Bus Reached",
                "Bus has reached a scheduled stop ${index + 1} at ${stoppage.stopName} "
                    +  "at $formattedTime"
            )
            // Launch coroutine for delayed navigation
            coroutineScope.launch {
                // Trigger notification and add stoppage ID to triggered set



//                }

                triggeredStoppages.value = triggeredStoppages.value.toMutableSet().apply {
                    add(stoppage.stopOrder.toString())
                }

                delay(5000) // 7-second delay
                counter.intValue += 1 // Increment the counter
//                navController.navigate("student") {
//                    // Uncomment the following line if you want to pop up to a specific route
//                    // popUpTo("login") { inclusive = true }
//                }
            }
        }
        // If the stoppage was triggered but now is outside the radius, mark it as exited
        else if ( !isWithinRadius(
                currentLatitude = latitude,
                currentLongitude = longitude,
                targetLatitude = stoppage.latitude.toDouble(),
                targetLongitude = stoppage.longitude.toDouble()
            ) && stoppage.stopOrder.toString() in triggeredStoppages.value) {
            exitedStoppages.value = exitedStoppages.value.toMutableSet().apply { add(stoppage.stopOrder.toString()) }
            triggeredStoppages.value = triggeredStoppages.value.toMutableSet().apply { remove(stoppage.stopOrder.toString()) }
        }

    }
}
    else if (state.value.user?.routeCurrentJourneyPhase == "evening")
    {
        stoppages?.filter {
            it.stopType == "evening"
                    &&
                    it.rounds?.evening?.any { round ->
                        round.round == state.value.user?.routeCurrentRound
                    } == true

        }?.forEachIndexed { index, stoppage ->
            // Check if within radius and if notification hasn't been triggered for this stoppage
            if (index in alertStatus.indices && isWithinRadius(
                    currentLatitude = latitude,
                    currentLongitude = longitude,
                    targetLatitude = stoppage.latitude.toDouble(),
                    targetLongitude = stoppage.longitude.toDouble()
                ) && (stoppage.stopOrder.toString() !in triggeredStoppages.value ||
                        stoppage.stopOrder.toString() in exitedStoppages.value)
                && alertStatus[index]
            ) {


                // Get the current date and time in 12-hour format
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")
                val formattedTime = currentDateTime.format(formatter)

                // Remove from exitedStoppages
                exitedStoppages.value =
                    exitedStoppages.value.toMutableSet().apply { remove(stoppage.stopOrder.toString()) }
                showNotification(
                    context,
                    "Bus Reached",
                    "Bus has reached a scheduled stop ${index + 1} at ${stoppage.stopName} "
                       +   "at $formattedTime"
                )

                // Launch coroutine for delayed navigation
                coroutineScope.launch {

                    // Trigger notification and add stoppage ID to triggered set
//                }

                    triggeredStoppages.value = triggeredStoppages.value.toMutableSet().apply {
                        add(stoppage.stopOrder.toString())
                    }

                    delay(5000) // 7-second delay
                    counter.intValue += 1 // Increment the counter
//                navController.navigate("student") {
//                    // Uncomment the following line if you want to pop up to a specific route
//                    // popUpTo("login") { inclusive = true }
//                }
                }
            }
            // If the stoppage was triggered but now is outside the radius, mark it as exited
            else if ( !isWithinRadius(
                    currentLatitude = latitude,
                    currentLongitude = longitude,
                    targetLatitude = stoppage.latitude.toDouble(),
                    targetLongitude = stoppage.longitude.toDouble()
                ) && stoppage.stopOrder.toString() in triggeredStoppages.value) {
                exitedStoppages.value = exitedStoppages.value.toMutableSet().apply { add(stoppage.stopOrder.toString()) }
                triggeredStoppages.value = triggeredStoppages.value.toMutableSet().apply { remove(stoppage.stopOrder.toString()) }
            }
        }
    }
    else if(state.value.user?.routeCurrentJourneyPhase == "morning"){

        stoppages?.filter { it.stopType == "morning"
                &&
                it.rounds?.morning?.any { round ->
                    round.round == state.value.user?.routeCurrentRound
                } == true
        }?.forEachIndexed { index, stoppage ->
            // Check if within radius and if notification hasn't been triggered for this stoppage
            if (index in alertStatus.indices && isWithinRadius(
                    currentLatitude = latitude,
                    currentLongitude = longitude,
                    targetLatitude = stoppage.latitude.toDouble(),
                    targetLongitude = stoppage.longitude.toDouble()
                ) && (stoppage.stopOrder.toString() !in triggeredStoppages.value ||
                        stoppage.stopOrder.toString() in exitedStoppages.value)
                && alertStatus[index]
            ) {


                // Get the current date and time in 12-hour format
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")
                val formattedTime = currentDateTime.format(formatter)

                // Remove from exitedStoppages
                exitedStoppages.value =
                    exitedStoppages.value.toMutableSet().apply { remove(stoppage.stopOrder.toString()) }
                showNotification(
                    context,
                    "Bus Reached",
                    "Bus has reached a scheduled stop ${index + 1} at ${stoppage.stopName} "
                          + "at $formattedTime"
                )
                // Launch coroutine for delayed navigation
                coroutineScope.launch {


//                }

                    triggeredStoppages.value = triggeredStoppages.value.toMutableSet().apply {
                        add(stoppage.stopOrder.toString())


                    }
                    delay(5000) // 7-second delay
                    counter.intValue += 1 // Increment the counter
//                navController.navigate("student") {
//                    // Uncomment the following line if you want to pop up to a specific route
//                     popUpTo("login") { inclusive = true }
//                }
                }
            }
            // If the stoppage was triggered but now is outside the radius, mark it as exited
            else if ( !isWithinRadius(
                    currentLatitude = latitude,
                    currentLongitude = longitude,
                    targetLatitude = stoppage.latitude.toDouble(),
                    targetLongitude = stoppage.longitude.toDouble()
                ) && stoppage.stopOrder.toString() in triggeredStoppages.value) {
                exitedStoppages.value = exitedStoppages.value.toMutableSet().apply { add(stoppage.stopOrder.toString()) }
                triggeredStoppages.value = triggeredStoppages.value.toMutableSet().apply { remove(stoppage.stopOrder.toString()) }
            }
        }

    }


    LaunchedEffect(socket, state.value.success, stateBusReplaced.value.status) {
        while (true) {
            val driverId = when {
                stateBusReplaced.value.message == "Bus has been replaced." && stateBusReplaced.value.status == true ->
                    stateBusReplaced.value.replacedBus.replacement_driver_id
                else ->
                    state.value.user?.driverId
            }

            if (driverId != null) {
                try {
                    subscribeToDriver(socket, driverId)
                } catch (e: Exception) {
                    e.printStackTrace() // Handle errors gracefully
                }
            } else {
                Toast.makeText(context, "Driver has not been assigned yet.", Toast.LENGTH_LONG).show()
            }

            try {
                listenForLocationUpdates(socket) { newLatitude, newLongitude, name, phone ->
                    latitude = newLatitude
                    longitude = newLongitude
                    driverName = name ?: ""
                    driverPhone = phone ?: ""
                    message = "Driver Location: Latitude = $latitude, Longitude = $longitude," +
                            " Name = $driverName, Phone = $driverPhone"

                    coroutineScope.launch(Dispatchers.IO) {
                        placeName = getPlaceNameFromLatLng(context, latitude, longitude)
                        preferencesRepository.setLastBusLocation("$latitude,$longitude")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace() // Handle errors gracefully
            }

            delay(15000) // Poll every 15 seconds
        }
    }

}

    busLocation.value = LatLngPlace(latitude, longitude,placeName)



    busLocationUpdates.value = LatLngPlace(latitude, longitude, placeName)





    Scaffold(
        //  drawerContent = { DrawerContent(navController) },  // Left-side Drawer
        //  topBar = { TopAppBar(title = { Text("Home Screen") }) },  // Optional Top Bar
        bottomBar = {
            if (shouldShowStudentBottomBar(navControllerBottomBar)) {
            StudentBottomNavigationBar(navControllerBottomBar)
            }
                    },  // Bottom Navigation
        //    drawerState = drawerState  // Use Material 3's DrawerState
    ) {
        paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Applies padding from Scaffold
        ) {

            StudentBottomNavGraph(navController,
                navControllerBottomBar,
                busLocationUpdates,
                state,
                stoppagesForPolyLines,
                counter,
                onForceLogout = {
                    // clear back stack & go to login (adjust if you prefer a different route)
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }

            )

        }
    }
}


@Composable
fun StudentBottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(stringResource(R.string.home), Icons.Default.Home, "home"),
        BottomNavItem(stringResource(R.string.bus_location), Icons.Default.LocationOn, "bus_location"),
        BottomNavItem(stringResource(R.string.about), Icons.Default.Info, "about")
    )

    val currentRoute = currentRoute(navController) // Current route tracking
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1f),
        color = Color.White,
//        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp), // Rounded corners
        tonalElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.Black,
            contentColor = Color.White
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            item.icon,
                            contentDescription = null
                        )
                    },
                    label = { Text(item.title) },
                    selected = currentRoute == item.route,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SmartBusSecondaryBlue,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = SmartBusSecondaryBlue,
                        indicatorColor = Color.Transparent
                    ),
                    onClick = {
//                        if (currentRoute != item.route) {
//                            navController.navigate(item.route) {
//                                popUpTo(navController.graph.startDestinationId) {
//                                    inclusive = true // Ensure the previous destination is removed
//                                }
//                                launchSingleTop = true
//                                restoreState = false // Ensure the state is reloaded
//                            }
//                        }

                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }

//                        if (currentRoute != item.route) {
//                            navController.navigate(item.route) {
//                                popUpTo("home") { // Replace "home" with your actual start route name if different
//                                    saveState = true
//                                }
//                                launchSingleTop = true
//                                restoreState = true
//                            }
//                        }
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StudentBottomNavGraph(navController: NavHostController,
    navControllerBottomBar: NavHostController,
    busLocationUpdates: MutableState<LatLngPlace>,
    state: State<GetUserDetailResponseX>,
    stoppageX: State<List<GeoPoint>>,
    counter: MutableIntState,
    networkViewModel: NetworkViewModel = getViewModel(),
                          onForceLogout: () -> Unit            // ← add this

) {
    AnimatedNavHost(
        navController = navControllerBottomBar,
        startDestination = "home", // change based on your initial route
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 1000 })  + fadeIn()
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut()
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 1000 })  + fadeOut()
        }
    ) {
        composable("home") {
            StudentHomeScreenContent(navController,navControllerBottomBar, busLocationUpdates,state,stoppageX, counter)
        }

        composable("bus_location") {
            val  context = LocalContext.current
          BusLocationScreen(busLocationUpdates,         onForceLogout = {}

          )
        }
        composable("adminDashboard") {
            AdminDashboardScreen(navController) // Replace with real data
        }


        composable("studentSettings") {
            StudentScreenSettings(navControllerBottomBar,state)
        }

        composable("about") {
            StudentAboutScreens(navControllerBottomBar)

        }

        composable("student_settings_language") {
            val languageViewModel: LanguageViewModel = koinViewModel()
            StudentSettingsLanguageSelectionScreen(navControllerBottomBar)
        }

//        composable("student")
//        {
//            val languageViewModel: LanguageViewModel = koinViewModel()
//            // StudentScreen()
//            StudentMainScreenNav(navController)
//
//        }

        composable("privacy_policy") {

            WebViewScreen("https://smartbus360.com/home/privacy-policy",navControllerBottomBar)
        }

        composable("terms&condition") {

            WebViewScreen("https://smartbus360.com/home/terms-and-conditions",navControllerBottomBar)
        }

        composable("creditsScreen") {

            CreditsScreen(navControllerBottomBar)
        }

        composable("notification") {

            NotificationList(navControllerBottomBar)
        }

        composable("reachDateTime") {

            ReachedStopagesScreen(navControllerBottomBar)
        }

        composable("date") {

                        DateTimeLogScreen(
//                apiData = dummyData,
//                selectedDate = selectedDate,
//                onDateChange = { selectedDate = it }
            )
        }

    }
}



@Composable
fun shouldShowStudentBottomBar(navController: NavHostController): Boolean {
    val currentRoute = currentRoute(navController)
    return currentRoute != "studentSettings" && currentRoute != "student_settings_language"
}