package com.smartbus360.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.smartbus360.app.R
import com.smartbus360.app.ui.theme.SmartBusPrimaryBlue
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue
import com.smartbus360.app.ui.theme.SmartBusTertiaryBlue
import com.smartbus360.app.viewModels.LanguageViewModel
import com.smartbus360.app.viewModels.SplashViewModel

@Composable
fun SplashScreen(navController: NavController, viewModel: SplashViewModel,  languageViewModel: LanguageViewModel) {
    LaunchedEffect(Unit) {
        viewModel.checkOnboarding { isOnboardingCompleted ->
            if (isOnboardingCompleted) {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                navController.navigate("onboarding") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    // UI for splash screen
    // Splash screen UI content

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SmartBusPrimaryBlue, SmartBusSecondaryBlue, SmartBusTertiaryBlue)
                )
            ),
//            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
//                . background(
//                    brush = Brush.linearGradient(
//                        colors = listOf(SmartBusPrimaryBlue, SmartBusSecondaryBlue, SmartBusTertiaryBlue)
//                    )
//                    )
        ) {
            // Logo/Image of the splash screen
            Image(
                painter =  rememberImagePainter(R.drawable.smartbus_nobg_2),
                contentDescription = "SmartBus360 Logo",
                modifier = Modifier
                    .size(192.dp)
                    .align(Alignment.CenterHorizontally)
                    .shadow(70.dp, CircleShape),
                contentScale = ContentScale.Crop,

            )

            Spacer(modifier = Modifier.height(16.dp))

            // Optional Text for branding or app name
            Text(
                text = "SmartBus 360",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}