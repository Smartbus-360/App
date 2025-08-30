package com.smartbus360.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

class PreferencesRepository(context: Context) {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    companion object {
        private const val KEY_IS_QR = "IS_QR_SESSION"
    }

    fun isOnboardingCompleted(): Boolean = sharedPrefs.getBoolean("onboarding_completed", false)
    fun setIsQrSession(v: Boolean) = sharedPrefs.edit().putBoolean(KEY_IS_QR, v).apply()
    fun isQrSession(): Boolean = sharedPrefs.getBoolean(KEY_IS_QR, false)

    fun setOnboardingCompleted() {
        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
    }

    fun isLangSelectCompleted(): Boolean = sharedPrefs.getBoolean("lang_completed", false)

    fun setLangSelectCompleted() {
        sharedPrefs.edit().putBoolean("lang_completed", true).apply()
    }

    fun isLoggedIn(): Boolean = sharedPrefs.getBoolean("is_logged_in", false)

    fun setLoggedIn(loggedIn: Boolean) {
        sharedPrefs.edit().putBoolean("is_logged_in", loggedIn).apply()
    }

    fun isMapAlwaysCenter(): Boolean = sharedPrefs.getBoolean("map_center", false)

    fun setMapAlwaysCenter(loggedIn: Boolean) {
        sharedPrefs.edit().putBoolean("map_center", loggedIn).apply()
    }

    fun getUserRole(): String? = sharedPrefs.getString("user_role", null)

    fun setUserRole(role: String) {
        sharedPrefs.edit().putString("user_role", role).apply()
    }

    fun getUserName(): String = sharedPrefs.getString("user_name", "").toString()

    fun setUserName(role: String) {
        sharedPrefs.edit().putString("user_name", role).apply()
    }
    fun getUserPass(): String = sharedPrefs.getString("user_pass", "").toString()

    fun setUserPass(role: String) {
        sharedPrefs.edit().putString("user_pass", role).apply()
    }


    fun getLastBusLocation(): String? = sharedPrefs.getString("bus_location", " 20.5937,78.9629")

    fun setLastBusLocation(latLong: String) {
        sharedPrefs.edit().putString("bus_location", latLong).apply()
    }


    fun getSelectedLanguage(): String? = sharedPrefs.getString("language", "en")

    fun setSelectedLanguage(language: String) {
        sharedPrefs.edit().putString("language", language).apply()
    }
    fun setAuthToken(token: String) {
        sharedPrefs.edit().putString("token", token).apply()
    }
    fun getAuthToken(): String? = sharedPrefs.getString("token", "default_token")
    fun setDriverId(driverId: Int) {
        sharedPrefs.edit().putInt("driverId", driverId).apply()
    }
    fun getDriverId(): Int? = sharedPrefs.getInt("driverId", 0)

    fun setUserId(userId: Int) {
        sharedPrefs.edit().putInt("userId", userId).apply()
    }
    fun getUserId(): Int = sharedPrefs.getInt("userId", 0)


    fun soundSwitchState(): Boolean = sharedPrefs.getBoolean("sound_state", true)

    fun setSoundSwitch(loggedIn: Boolean) {
        sharedPrefs.edit().putBoolean("sound_state", loggedIn).apply()
    }


    fun startedSwitchState(): Boolean = sharedPrefs.getBoolean("started_state", false)

    fun setStartedSwitch(started: Boolean) {
        sharedPrefs.edit().putBoolean("started_state", started).apply()
    }

    fun journeyFinishedState(): String? = sharedPrefs.getString("journey_state", "morning")

    fun setJourneyFinished(loggedIn: String?) {
        sharedPrefs.edit().putString("journey_state", loggedIn).apply()
    }

    // notification

    fun markNotificationAsSeen(notificationId: Int) {
        val seenNotifications = getSeenNotifications().toMutableSet()
        seenNotifications.add(notificationId.toString())
        sharedPrefs.edit().putStringSet("seen_notifications", seenNotifications).apply()
    }

    fun getSeenNotifications(): Set<String> {
        return sharedPrefs.getStringSet("seen_notifications", emptySet()) ?: emptySet()
    }

    fun markBusNotificationAsSeen(notificationId: Int) {
        val seenNotifications = getSeenBusNotifications().toMutableSet()
        seenNotifications.add(notificationId.toString())
        sharedPrefs.edit().putStringSet("seen_Busnotifications", seenNotifications).apply()
    }
    fun getSeenBusNotifications(): Set<String> {
        return sharedPrefs.getStringSet("seen_Busnotifications", emptySet()) ?: emptySet()
    }

    // OLD


//
//    fun setAppLocale(language: String, context: Context) {
//        val locale = Locale(language)
//        Locale.setDefault(locale)
//        val resources = context.resources
//        val config = resources.configuration
//        config.setLocale(locale)
//        resources.updateConfiguration(config, resources.displayMetrics)
//
//        // Store the selected language in SharedPreferences
//        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
//        sharedPreferences.edit().putString("language", language).apply()
//    }

//    fun setAppLocale( languageCode: String, context: Context) {
//        val locale = Locale(languageCode)
//        Locale.setDefault(locale)
//        val configuration = Configuration(context.resources.configuration)
//        configuration.setLocale(locale)
//
//        // If API >= 24 (Android 7.0 Nougat), setLocales instead of setLocale
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//            configuration.setLocales(android.os.LocaleList(locale))
//        }
//
//        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
//        // Store the selected language in SharedPreferences
//        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
//        sharedPreferences.edit().putString("language", languageCode).apply()
//    }

    fun setLocale( languageCode: String , context: Context) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        // If API >= 24 (Android 7.0 Nougat), setLocales instead of setLocale
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            configuration.setLocales(android.os.LocaleList(locale))
        }

        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

//    fun clearSession() {
//        sharedPrefs.edit().clear().apply()
//    }
fun clearSession() {
    sharedPrefs.edit()
        .remove("token")
        .remove("driverId")
        .remove(KEY_IS_QR)
        .remove("is_logged_in")
        .apply()
}


}
