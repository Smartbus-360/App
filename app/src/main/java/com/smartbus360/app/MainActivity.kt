package com.smartbus360.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.module.appModule
import com.smartbus360.app.module.databaseModule
import com.smartbus360.app.module.repositoryModule
import com.smartbus360.app.navigation.AppNavGraph
import com.smartbus360.app.ui.screens.setLocale
import com.smartbus360.app.ui.screens.startLocationService
import com.smartbus360.app.ui.screens.stopLocationService
import com.smartbus360.app.utility.LocationService
import com.smartbus360.app.utility.NetworkBroadcastReceiver
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.startKoin
import org.osmdroid.config.Configuration
import java.io.File
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.smartbus360.app.viewModels.LoginViewModel


class MainActivity : ComponentActivity() {

    private val UPDATE_REQ = 7001
    private lateinit var appUpdateManager: AppUpdateManager
    private var keepSplashOn by mutableStateOf(true)
    private val loginViewModel: LoginViewModel by viewModel()


    // Listener for FLEXIBLE update: when download finishes, install
    private val updateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // Triggers the system install sheet and restarts app after install
            appUpdateManager.completeUpdate()
        }
    }


    // For Android 14 and above: Request foreground location, background location, and notifications
    @RequiresApi(Build.VERSION_CODES.O)
    private val requestLocationAndNotificationPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val backgroundLocationGranted = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true
            val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] == true

            if (locationGranted && backgroundLocationGranted && notificationGranted && (GlobalContext.getOrNull() != null)) {
                startLocationService()
            } else {
//                Toast.makeText(this, "Required permissions are not granted.", Toast.LENGTH_SHORT).show()
            }
        }
    private lateinit var networkReceiver: NetworkBroadcastReceiver

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        // Install the system splash screen
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashOn }   // ⬅️ ADD THIS
        super.onCreate(savedInstanceState)
        // ✅ One-time clear of any old OSM tiles from disk cache
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (!prefs.getBoolean("osmCacheCleared", false)) {
            val cacheDir = org.osmdroid.config.Configuration.getInstance().osmdroidTileCache
            try {
                cacheDir.deleteRecursively()
                println("OSM tile cache cleared")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            prefs.edit().putBoolean("osmCacheCleared", true).apply()
        }

        // Initialize Koin
//        if (GlobalContext.getOrNull() == null) {
//            startKoin {
//                androidContext(this@MainActivity)
//                modules(appModule, databaseModule, repositoryModule,)
//            }
//        }


        // Initialize and register the receiver
        networkReceiver = NetworkBroadcastReceiver { isConnected ->
            if (isConnected) {
                // Handle online state
                println("Internet is connected")
            } else {
                // Handle offline state
                println("Internet is disconnected")
            }
        }
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)

//        checkAndRequestPermissions()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkAndRequestPermissions() // ✅ request FINE + BACKGROUND + NOTIFICATIONS
        }



        // Load the saved language from SharedPreferences or default to English
        val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("language_code", "en") ?: "en"
        setLocale(this, savedLanguage)

        // Create notification channel for the service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "location_channel",
                "Location Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
        val ctx = applicationContext
        val basePath = File(ctx.cacheDir, "osmdroid").apply { mkdirs() }
        val tileCache = File(basePath, "tiles").apply { mkdirs() }

        Configuration.getInstance().osmdroidBasePath = basePath
        Configuration.getInstance().osmdroidTileCache = tileCache

        setContent {
            val context = LocalContext.current
            Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", MODE_PRIVATE))
            org.osmdroid.config.Configuration.getInstance().userAgentValue = "SmartBus360App"

            // Wrap the entire app in a CompositionLocalProvider to lock font scaling
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = LocalDensity.current.density,
                    fontScale = 1.0f // Locks font scaling to 1x
                )


            ) {
                AppNavGraph() // Your navigation graph

            }

            LaunchedEffect(Unit) {
                keepSplashOn = false
            }



        }

        checkForUpdate()
        handleQrDeepLink(intent)


      // requestPermissions() // Request location permissions on startup
    }


    override fun onResume() {
        super.onResume()
        if (::appUpdateManager.isInitialized) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    appUpdateManager.completeUpdate()
                }
            }
        }
    }


    fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkAndRequestPermissions() { 
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU is API 33
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        requestLocationAndNotificationPermissionsLauncher.launch(permissions.toTypedArray())
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(networkReceiver) } catch (_: Exception) {}

        if (::appUpdateManager.isInitialized) {
            try { appUpdateManager.unregisterListener(updateListener) } catch (_: Exception) {}
        }

    }

    override fun onRestart() {
        super.onRestart()
        val repository = PreferencesRepository(this)
        val started = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            .getBoolean("LOCATION_SHARING_STARTED", false)
        if (repository.getUserRole() == "driver" && started) {
            startLocationService(this)
        }
    }
    private fun checkForUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val updateAvailable =
                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

            if (updateAvailable) {
                // Register listener so we can auto-install when download completes
                appUpdateManager.registerListener(updateListener)

                // Start FLEXIBLE update (downloads in background)
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    this, // activity
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                    UPDATE_REQ
                )
            }
        }
    }


//    override fun onResume(){
//        super.onResume()
//        val repository = PreferencesRepository(this)
////        repository.setStartedSwitch(false) // Update preference on activity destroy
////        stopLocationService(this)
////        repository.getUserRole()
//        when ( repository.getUserRole()) {
//            "driver" ->
//                if (repository.isLoggedIn())
//                startLocationService(this)
//        }
//
//    }




    @RequiresApi(Build.VERSION_CODES.O)
    private fun startLocationService() {
        if (GlobalContext.getOrNull() != null) {
        val intent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
            }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleQrDeepLink(intent)
    }


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        handleQrDeepLink(intent)
//    }

    private fun handleQrDeepLink(intent: Intent) {
        val data = intent.data ?: return
        if (data.scheme == "smartbus360" &&
            (data.host.equals("qr-login", true) || data.path.equals("/qr-login", true))
        ) {
            val token = data.getQueryParameter("token")
            if (!token.isNullOrBlank()) {
                loginViewModel.exchangeQrLogin(token)
            }
        }
    }


}


