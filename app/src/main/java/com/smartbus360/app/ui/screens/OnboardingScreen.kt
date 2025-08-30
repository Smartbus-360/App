package com.smartbus360.app.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.smartbus360.app.R
import com.smartbus360.app.ui.theme.InterMedium
import com.smartbus360.app.ui.theme.SmartBusPrimaryBlue
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue
import com.smartbus360.app.ui.theme.SmartBusTertiaryBlue
import com.smartbus360.app.viewModels.LanguageViewModel
import com.smartbus360.app.viewModels.OnboardingViewModel
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel,
    languageViewModel: LanguageViewModel
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    // Track the checkbox state
    var isAgreeChecked by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            SmartBusPrimaryBlue,
                            SmartBusSecondaryBlue,
                            SmartBusTertiaryBlue
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                SmartBusPrimaryBlue,
                                SmartBusSecondaryBlue,
                                SmartBusTertiaryBlue
                            )
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Horizontal Pager for Onboarding Screens
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(.5f)
                ) { pageIndex ->
                    when (pageIndex) {
//                        0 -> OnboardingScreen0(navController, isChecked = isAgreeChecked, onCheckedChange = { isAgreeChecked = it })
                        0 -> OnboardingScreen1()
                        1 -> OnboardingScreen2()
                        2 -> OnboardingScreen3()
                    }
                }

                HorizontalPagerIndicator(
                    modifier = Modifier.background(Color.Transparent),
                    pageCount = pagerState.pageCount,
                    currentPage = pagerState.currentPage,
                    targetPage = pagerState.targetPage,
                    currentPageOffsetFraction = pagerState.currentPageOffsetFraction
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (pagerState.currentPage < pagerState.pageCount - 1) {
                                pagerState.scrollToPage(pagerState.currentPage + 1)
                            } else {
                                viewModel.completeOnboarding()
                                navController.navigate("home") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .height(50.dp)
                        .fillMaxWidth()
                        .padding(bottom = 10.dp, start = 10.dp, end = 10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SmartBusPrimaryBlue),
                    enabled = isAgreeChecked // Enable/Disable button based on checkbox state
                ) {
                    Text(
                        when (pagerState.currentPage) {
                            0 -> "Continue"
                            1 -> "Next"
                            2 -> "Next"
                            else -> "Finish"
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen0(navController: NavController, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        SmartBusPrimaryBlue,
                        SmartBusSecondaryBlue,
                        SmartBusTertiaryBlue
                    )
                )
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberImagePainter(R.drawable.smartbus_nobg_2),
            contentDescription = "Onboarding Image",
            modifier = Modifier
                .size(150.dp)
                .shadow(20.dp, RoundedCornerShape(360.dp))
        )

        Spacer(Modifier.size(40.dp))

        Text(
            text = "SmartBus360 – Track. Ride. Arrive Safe.",
            fontSize = 25.sp,
            fontFamily = InterMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!isChecked) }
                .padding(horizontal = 65.dp)
        ) {
            Column {
                Text(
                    text = "Before continuing, please review and agree to our Terms & Conditions and Privacy Policy.",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!isChecked) }
                .padding(horizontal = 10.dp)
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "I agree to the",
                    fontSize = 14.sp,
                    color = Color.White
                )
                Row {
                    ClickableText(
                        text = AnnotatedString("Terms & Conditions"),
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.Cyan,
                            textDecoration = TextDecoration.Underline
                        ),
                        onClick = { navController.navigate("terms&condition") }
                    )
                    Text(text = " and ", fontSize = 14.sp, color = Color.White)
                    ClickableText(
                        text = AnnotatedString("Privacy Policy"),
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.Cyan,
                            textDecoration = TextDecoration.Underline
                        ),
                        onClick = { navController.navigate("privacy_policy") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}





@Composable
fun OnboardingScreen1() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        SmartBusPrimaryBlue,
                        SmartBusSecondaryBlue,
                        SmartBusTertiaryBlue
                    )
                )
            )

//            .background(color = SmartBusSecondaryBlue)

            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Image(
            painter = rememberImagePainter(R.drawable.onboarding),
            contentDescription = "Onboarding Image",
            modifier = Modifier
                .size(150.dp)
                .shadow(20.dp, RoundedCornerShape(20.dp))
        )

        Spacer(Modifier.size(40.dp))

        Text(
            text = "Welcome to SmartBus360",
            fontSize = 18.sp,
            fontFamily = InterMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center


        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text ="SmartBus 360 is a cutting-edge real-time bus tracking " +
                    "app designed to offer users an efficient and reliable way" +
                    " to monitor the live location of buses.",
            fontSize = 14.sp,

            fontFamily = InterMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = Color.White
        )
    }
}

@Composable
fun OnboardingScreen2() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        SmartBusPrimaryBlue,
                        SmartBusSecondaryBlue,
                        SmartBusTertiaryBlue
                    )
                )
            )

//            .background(color = SmartBusSecondaryBlue)

            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = rememberImagePainter(data = R.drawable.on_boarding2),
            contentDescription = "Onboarding Image",
            modifier = Modifier
                .size(150.dp)
                .shadow(20.dp, RoundedCornerShape(20.dp))
        )

        Spacer(Modifier.size(40.dp))

        Text(
            text = "Live Location Tracking, ETA and Route Details",
            fontSize = 18.sp,
            fontFamily = InterMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center


        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text ="Get accurate estimated arrival times and route overviews for each bus stop," +
                    " updated continuously as the bus progresses.",
            fontSize = 14.sp,

            fontFamily = InterMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = Color.White
        )
    }
}@Composable
fun OnboardingScreen3() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        SmartBusPrimaryBlue,
                        SmartBusSecondaryBlue,
                        SmartBusTertiaryBlue
                    )
                )
            )

//            .background(color = SmartBusSecondaryBlue)

            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Image(
            painter =  rememberImagePainter(R.drawable.onboarding3),
            contentDescription = "Onboarding Image",
            modifier = Modifier
                .size(150.dp)
                .shadow(20.dp, RoundedCornerShape(20.dp))
        )

        Spacer(Modifier.size(40.dp))

        Text(
            text = "Driver and Passenger Interface",
            fontSize = 18.sp,
            fontFamily = InterMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center

        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text ="Role-based navigation ensures a smooth experience for both drivers and" +
                    " passengers, with customized views and features.",
            fontSize = 14.sp,

            fontFamily = InterMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = Color.White
        )
    }
}

@Composable
private fun HorizontalPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    targetPage: Int,
    currentPageOffsetFraction: Float,
    modifier: Modifier = Modifier,
    indicatorColor: Color = Color.DarkGray,
    unselectedIndicatorSize: Dp = 8.dp,
    selectedIndicatorSize: Dp = 10.dp,
    indicatorCornerRadius: Dp = 2.dp,
    indicatorPadding: Dp = 2.dp
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .wrapContentSize()
            .height(selectedIndicatorSize + indicatorPadding * 2)
    ) {

        // draw an indicator for each page
        repeat(pageCount) { page ->
            // calculate color and size of the indicator
            val (color, size) =
                if (currentPage == page || targetPage == page) {
                    // calculate page offset
                    val pageOffset =
                        ((currentPage - page) + currentPageOffsetFraction).absoluteValue
                    // calculate offset percentage between 0.0 and 1.0
                    val offsetPercentage = 1f - pageOffset.coerceIn(0f, 1f)

                    val size =
                        unselectedIndicatorSize + ((selectedIndicatorSize - unselectedIndicatorSize) * offsetPercentage)

                    indicatorColor.copy(
                        alpha = offsetPercentage
                    ) to size
                } else {
                    indicatorColor.copy(alpha = 0.1f) to unselectedIndicatorSize
                }

            // draw indicator
            Box(
                modifier = Modifier
                    .padding(
                        // apply horizontal padding, so that each indicator is same width
                        horizontal = ((selectedIndicatorSize + indicatorPadding * 2) - size) / 2,
                        vertical = size / 4
                    )
                    .clip(RoundedCornerShape(indicatorCornerRadius))
                    .background(color)
                    .width(size)
                    .height(size / 2)
            )
        }
    }
}