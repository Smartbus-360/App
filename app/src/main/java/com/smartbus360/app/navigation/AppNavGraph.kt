package com.smartbus360.app.navigation

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.ui.screens.HomeScreen
import com.smartbus360.app.ui.screens.OnboardingScreen
import com.smartbus360.app.ui.screens.SplashScreen
import com.smartbus360.app.ui.screens.TermsAndPrivacyScreen
import com.smartbus360.app.ui.webView.WebViewScreen
import com.smartbus360.app.viewModels.LanguageViewModel
import com.smartbus360.app.viewModels.OnboardingViewModel
import com.smartbus360.app.viewModels.SplashViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavGraph() {
    // Create a NavController using rememberNavController
    val navController = rememberNavController()
    val preferencesRepository = PreferencesRepository(navController.context)
    val isOnboardingCompleted = preferencesRepository.isOnboardingCompleted()
    // Koin injects LanguageViewModel
    val languageViewModel: LanguageViewModel = koinViewModel()
    // Define your NavHost with startDestination and composable routes
    NavHost(
        navController = navController,        // The NavController for navigating
        startDestination = "splash"           // Starting screen
    ) {
        // Define destinations here using `composable`
        composable("splash") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isOnboardingCompleted) {
//                    HomeScreen(languageViewModel)
                    NavGraphAfterOnboard()
                } else {

//                    TermsAndPrivacyScreen(navController,
//                        onContinue = { navController.navigate("homeScreen") },
//                        onOpenTerms = { navController.navigate("terms&condition") },
//                        onOpenPrivacy = { navController.navigate("privacy_policy") }
//                    )


                    val onboardingViewModel: OnboardingViewModel = koinViewModel()
                    OnboardingScreen(navController, onboardingViewModel, languageViewModel)
                }
            }
            else{
                val splashViewModel: SplashViewModel = koinViewModel()
                SplashScreen(navController, splashViewModel,languageViewModel)
            }
        }
        composable("onboarding") {
            val onboardingViewModel: OnboardingViewModel = koinViewModel()
            OnboardingScreen(navController, onboardingViewModel, languageViewModel)
        }
        composable("home") {
            HomeScreen(languageViewModel)
        }
        composable("privacy_policy") {

            WebViewScreen("https://smartbus360.com/home/privacy-policy",navController)
        }

        composable("terms&condition") {

            WebViewScreen("https://smartbus360.com/home/terms-and-conditions",navController)
        }

    }
}
