package com.smartbus360.app.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.smartbus360.app.R
import com.smartbus360.app.ui.component.RoleCircleWithLabel
import com.smartbus360.app.ui.theme.Poppins
import com.smartbus360.app.viewModels.AdvertisementBannerViewModel
import com.smartbus360.app.viewModels.LanguageViewModel
import com.smartbus360.app.viewModels.RoleSelectionViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel

@SuppressLint("CoroutineCreationDuringComposition", "StateFlowValueCalledInComposition")
@Composable
fun RoleSelectionScreen(navController: NavController, viewModel: RoleSelectionViewModel,
                        languageViewModel: LanguageViewModel,
                        advertisementBannerViewModel: AdvertisementBannerViewModel = getViewModel()
                        )
{
    BackHandler {
        navController.popBackStack()  // Navigate back to the previous screen
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // State to track the selected language
    var selectedLanguage by remember { mutableStateOf("en") }
    val advertisementBannerState = advertisementBannerViewModel.state.collectAsState()

    // Load saved language from SharedPreferences/DataStore
    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        selectedLanguage = sharedPreferences.getString("language_code", "en") ?: "en"
        setLocale(context, selectedLanguage)
    // Recreate activity to apply changes
    //(context as MainActivity).recreate()
        }

    Scaffold {
        Column(Modifier.padding(it)) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.doodle_bg),
                    contentDescription = "Doodle background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )



                // Content overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.8f)) // Semi-transparent overlay
                        .padding(bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {


                    Column(
                        modifier = Modifier
                            .width(300.dp)
                            .shadow(0.dp, shape = RoundedCornerShape(15.dp))
                            .border(
                                width = 0.5.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(15.dp)
                            )
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White, RoundedCornerShape(15.dp))
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                        ,
                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = stringResource(R.string.choose_your_role),
                            color = Color.Black,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold
                        )

                    }

//                    Spacer(modifier = Modifier.height(60.dp))
                    Column(
                        modifier = Modifier
                            .padding(top = 60.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Role options row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
//                verticalAlignment = Alignment.CenterVertically

                        ) {
//                        Row (Modifier.fillMaxWidth(0.5f)){
                            RoleCircleWithLabel(
                                iconRes = R.drawable.school_bus_,
                                label = stringResource(R.string.i_am_a_driver),
                                onClick = {
                                    viewModel.setRole("driver")
                                    navController.navigate("login")
                                }
                            )
//                        }
                            // Driver role option


                            Spacer(modifier = Modifier.height(60.dp))

//                        Row (Modifier.fillMaxWidth()){
                            // Student role option
                            RoleCircleWithLabel(
                                iconRes = R.drawable.student_colored,
                                label = stringResource(R.string.i_am_a_student),
                                onClick = {
                                    viewModel.setRole("student")
                                    navController.navigate("login")
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(40.dp))

                        // Admin button
                        RoleCircleWithLabel(
                            iconRes = R.drawable.admin_icon,   // <-- Add an admin icon in drawable
                            label = "Admin",
                            onClick = {
                                val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                                prefs.edit().remove("ACCESS_TOKEN").apply()

                                val repoPrefs = context.getSharedPreferences("com.smartbus360.app_preferences", Context.MODE_PRIVATE)
                                repoPrefs.edit().clear().apply()

                                viewModel.setRole("admin")
                                navController.navigate("login")
                            }
                        )


//

                    }

            Spacer(Modifier.height(100.dp))
                    if (advertisementBannerState.value.message == "Advertisement found" && advertisementBannerState.value.success == true) {
                        Card(
                            modifier = Modifier
                                .padding(top = 60.dp)
                                .fillMaxWidth(0.90f)
                                .clip(RoundedCornerShape(20.dp))
                                .shadow(8.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(advertisementBannerState.value.bannerUrl)
                                        .crossfade(true) // Smooth transition effect
                                        .build()
                                ),
                                contentDescription = "Advertisement Image",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else if (advertisementBannerState.value.message == "No active advertisement found" && advertisementBannerState.value.success == true) {
                        // No advertisement case - You can display a subtle placeholder
//                        Box(
//                            modifier = Modifier
//                                .padding(top = 60.dp)
//                                .fillMaxWidth(0.90f)
//                                .height(180.dp)
//                                .clip(RoundedCornerShape(20.dp))
//                                .background(MaterialTheme.colorScheme.surfaceVariant),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = "No Active Advertisement",
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
                    } else {
//                        // Sleek Loader UI
//                        Box(
//                            modifier = Modifier
//                                .padding(top = 60.dp)
//                                .fillMaxWidth(0.90f)
//                                .height(180.dp)
//                                .clip(RoundedCornerShape(20.dp))
//                                .background(
//                                    Brush.linearGradient(
//                                        colors = listOf(
//                                            MaterialTheme.colorScheme.surfaceVariant,
//                                            MaterialTheme.colorScheme.surface
//                                        )
//                                    )
//                                ),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.spacedBy(12.dp)
//                            ) {
//                                repeat(3) { index ->
//                                    DotLoader(index)
//                                }
//                            }
//                        }
                    }




                }
            }
        }
    }

}

@Composable
fun DotLoader(index: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing, delayMillis = index * 150),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .size(10.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    )
}


@Preview(showBackground = true)
@Composable
fun PreviewRoleSelectionScreen() {
    val roleSelectionViewModel: RoleSelectionViewModel = koinViewModel()

//    RoleSelectionScreen(viewModel = roleSelectionViewModel, navController = rememberNavController())
}

