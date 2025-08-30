package com.smartbus360.app.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smartbus360.app.MainActivity
import com.smartbus360.app.R
import com.smartbus360.app.ui.component.CustomRadioButtonWithFill
import com.smartbus360.app.ui.component.NetworkSnackbar
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue
import com.smartbus360.app.viewModels.LanguageViewModel
import com.smartbus360.app.viewModels.NetworkViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LanguageSelectionScreen(navController: NavController, languageViewModel: LanguageViewModel,
                            networkViewModel: NetworkViewModel = getViewModel()
                            ) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val isConnected by networkViewModel.isConnected.collectAsState()
    // Handle the back button press
    BackHandler {
        navController.popBackStack()  // Navigate to the previous screen
    }

    val coroutineScope = rememberCoroutineScope()

    // List of languages
    val languages = listOf(
        "en" to "English",
        "hi" to "Hindi \n(हिन्दी)",
        "bn" to "Bengali \n(বাংলা)",
        "ta" to "Tamil \n(தமிழ்)" ,
        "te" to "Telugu \n(తెలుగు)",
        "mr" to "Marathi \n(मराठी)")


    // State to track the selected language
    var selectedLanguage by remember { mutableStateOf("en") }

    // Load saved language from SharedPreferences/DataStore
    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        selectedLanguage = sharedPreferences.getString("language_code", "en") ?: "en"
    }

    //  var selectedLanguageCode by remember { mutableStateOf<String?>("hi") }
    Scaffold(floatingActionButton = {
//        // Proceed Button
//        Button(
//            onClick = {
//
//                navController.navigate("role")
//            },
//            enabled =  selectedLanguage != null ,  // Button is enabled only if a language is selected
//            modifier = Modifier
//
//                .width(150.dp)
//                .clip(
//                    shape = RoundedCornerShape(15.dp),
//                )
//            ,
//            colors = buttonColors(contentColor = Color.White, containerColor = SmartBusSecondaryBlue),
//            shape = RoundedCornerShape(10.dp)
//
//        ) {
//            Text(text = stringResource(R.string.proceed))
//        }
    }, floatingActionButtonPosition = FabPosition.Center,snackbarHost = { SnackbarHost(hostState = snackbarHostState) } ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .shadow(20.dp, RoundedCornerShape(15.dp))
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(15.dp)
                )
        ) {

            NetworkSnackbar(
                isConnected = isConnected,
                snackbarHostState = snackbarHostState
            )
            // Background image
            Image(
                painter = painterResource(id = R.drawable.doodle_bg),
                contentDescription = "Doodle background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                Modifier
                    .background(Color.White.copy(alpha = 0.8f))
                    .fillMaxSize()

            ){

//                Column(
//                    Modifier
//                        .fillMaxWidth()
////                    .weight(.5f)
//                        .background(Color.White.copy(alpha = 0.8f))
//                        .padding(top = 40.dp)
//                        .verticalScroll(rememberScrollState()),
//                ){
//
//                }

// hide belo column magic happens. Please help me to make it visible

                    // Grid of Radio Buttons with Tick and Fill when selected
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),  // 2 columns per row
                        modifier = Modifier
                            .weight(1f)
                            .padding(15.dp),
                        horizontalArrangement = Arrangement.spacedBy(17.dp),
                        verticalArrangement = Arrangement.spacedBy(17.dp)
                    ) {
                        item(span = { GridItemSpan(2) }) {

                            Column {
                                Icon(
                                    painter = painterResource(id = R.drawable.language_change_icon),
                                    contentDescription = "icon",
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    Color.Black

                                )
                                Text(
                                    text = stringResource(R.string.hi_select_language),
                                    modifier = Modifier
                                        .padding(horizontal = 15.dp, vertical = 4.dp),
//                    textAlign = TextAlign.Center,
//                    color = Color.White,
                                    lineHeight = 31.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 26.sp,

                                    )

                            }
                        }

                        items(languages) { (code, name) ->

                            Surface(
                                shadowElevation =10.dp ,  // Apply elevation here
                                shape = MaterialTheme.shapes.medium, // Optional: Add shape
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Row(
                                    modifier = Modifier
                                        .background(Color.White)


                                        .width(166.dp)
                                        .height(100.dp)
                                        .clip(shape = RoundedCornerShape(15.dp))
                                        .padding(start = 15.dp, end = 15.dp)


//                            .shape = RoundedCornerShape(15.dp))
                                        .fillMaxWidth()
                                        .clickable {
//                            selectedLanguageCode = language
                                            selectedLanguage = code
                                            coroutineScope.launch {
                                                // languageViewModel.changeLanguage(code)
                                                // Save in SharedPreferences or DataStore
                                                val sharedPreferences = context.getSharedPreferences(
                                                    "app_preferences",
                                                    Context.MODE_PRIVATE
                                                )
                                                val editor = sharedPreferences.edit()
                                                editor.putString("language_code", code)
                                                editor.apply()

                                                // Change locale immediately
                                                setLocale(context, code)

                                                // Recreate activity to apply changes
                                                (context as MainActivity).recreate()
                                            }

                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,

                                    ) {
                                    Text(
                                        name,
                                        color = Color.Black,
                                        fontSize = 18.sp
                                    )
                                    CustomRadioButtonWithFill(
                                        selected = code == selectedLanguage,
                                        onClick = {
                                            selectedLanguage = code
                                            coroutineScope.launch {
                                                // languageViewModel.changeLanguage(code)
                                                // Save in SharedPreferences or DataStore
                                                val sharedPreferences = context.getSharedPreferences(
                                                    "app_preferences",
                                                    Context.MODE_PRIVATE
                                                )
                                                val editor = sharedPreferences.edit()
                                                editor.putString("language_code", code)
                                                editor.apply()

                                                // Change locale immediately
                                                setLocale(context, code)

                                                // Recreate activity to apply changes
                                                (context as MainActivity).recreate()
                                            }
                                        },

                                        )

                                }
                            }
                        }
                        item(span = { GridItemSpan(2) }) {
                            // Proceed Button
                            Button(
                                onClick = {
                                    languageViewModel.completeLanguageSelection()
                                    navController.navigate("role")
                                },
                                enabled =  selectedLanguage != null ,  // Button is enabled only if a language is selected
                                modifier = Modifier

                                    .fillMaxWidth(0.8f)
                                    .clip(
                                        shape = RoundedCornerShape(15.dp),
                                    )
                                ,
                                colors = buttonColors(contentColor = Color.White, containerColor = SmartBusSecondaryBlue),
                                shape = RoundedCornerShape(10.dp)

                            ) {
                                Text(text = stringResource(R.string.proceed))
                            }
                        }



                    }




            }

        }
    }


}


//fun setLocale(context: Context, languageCode: String) {
//    val locale = Locale(languageCode)
//    Locale.setDefault(locale)
//    val configuration = Configuration(context.resources.configuration)
//    configuration.setLocale(locale)
//
//    // If API >= 24 (Android 7.0 Nougat), setLocales instead of setLocale
//    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//        configuration.setLocales(LocaleList(locale))
//    }
//
//    context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
//}


fun setLocale(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = Configuration(context.resources.configuration)

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        config.setLocales(LocaleList(locale))
    } else {
        config.setLocale(locale)
    }

    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}