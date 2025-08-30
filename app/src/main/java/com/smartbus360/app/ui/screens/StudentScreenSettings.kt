package com.smartbus360.app.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import com.smartbus360.app.MainActivity
import com.smartbus360.app.R
import com.smartbus360.app.data.database.AlertStatusDao
import com.smartbus360.app.data.model.response.GetUserDetailResponseX
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.ui.theme.Montserrat
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue
import com.smartbus360.app.utility.bugReportWhatsApp
import com.smartbus360.app.utility.supportEmail
import com.smartbus360.app.utility.supportWhatsApp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@Composable
fun StudentScreenSettings(navController: NavHostController, state: State<GetUserDetailResponseX>,)
{
    val  context = LocalContext.current
    val activity = context as? Activity // Ensure the context is an Activity


    val preferencesRepository = PreferencesRepository(context)
    val coroutineScope = rememberCoroutineScope()
    val alertStatusDao: AlertStatusDao = get() // Koin example

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

        }
    }

    // in-app review
    val reviewManager = ReviewManagerFactory.create(context)
    val scope = rememberCoroutineScope()



    BackHandler{
        navController.popBackStack(
            route = "home",
            inclusive = false
        )

    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                color = Color(0xFFFFFFFF),
            )
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = Color(0xFFFFFFFF),
                )
                .padding(top = 20.dp,)
                .verticalScroll(rememberScrollState())
        ){
            Row(
                modifier = Modifier
                    .padding(bottom = 10.dp, start = 18.dp, end = 18.dp,)
                    .fillMaxWidth()
            ){

                Icon(
                    painter = painterResource(id = R.drawable.back_icon), // replace with your back icon
                    contentDescription = "Back",
                    tint= Color.Unspecified,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable {
                            navController.popBackStack(
                                route = "home",
                                inclusive = false
                            )

                        }
                )

                Column(
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .fillMaxWidth()
                ) {
                    if (state.value.success == true) {
                        Text(
                            text = stringResource(
                                R.string.hi_,
                                state.value.user?.userName ?: "Name not available"
                            ),
                            color = Color(0xFF000000),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
//                            Image(painter = painterResource(R.drawable.amity_logo),
//                                contentDescription = "Change icon",
//                                contentScale = ContentScale.Crop,
//                                modifier = Modifier
//                                    .padding(4.dp)
//                                    .clip(CircleShape)
////                            .border(width = 0.3.dp, color = Color.Black, shape = CircleShape)
//                                    .size(60.dp)
//                                    .clickable {
//
////                                        navController.navigate("settings") {
////                                            popUpTo("home") { inclusive = false }
////                                        }
//                                    }
//                            )
                            Image(
                                painter = rememberImagePainter(
                                    data = state.value.user?.instituteLogo,
                                    builder = {
                                        placeholder(R.drawable.university_icon) // Show this image while loading
                                        error(R.drawable.university_icon)       // Show this image if there's an error
                                    }
                                ),
                                contentDescription = "Change icon",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .size(60.dp)
//											.border(width = 0.3.dp, color = Color.Black, shape = CircleShape)
                                    .clickable {
                                        // Uncomment and modify as needed for navigation
                                        // navController.navigate("settings") {
                                        //     popUpTo("home") { inclusive = false }
                                        // }
                                    }
                            )

                        Text(
                            state.value.user?.instituteName ?: stringResource(R.string.n_a)
//                            text = stringResource(
//                                R.string.university,
//                                state.value.user.instituteName ?: "N/A"
//                            )
                            ,
                            color = Color(0xFF000000),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        }

                        Text(
                            text = stringResource(
                                R.string.fee,
                                state.value.user?.userPaymentStatus ?: stringResource(R.string.n_a)
                            ),
                            color = Color(0xFF000000),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = "Hi, \n",
                            color = Color(0xFF000000),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        )
                    }
                }




            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 12.dp, start = 14.dp, end = 14.dp,)
                    .clip(shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF000000),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ){


                Icon(
                    painter = painterResource(id = R.drawable.settings_icon), // replace with your back icon
                    contentDescription = "Back",
                    tint= SmartBusSecondaryBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
//                            navController.popBackStack()
                        }
                )


                Text(stringResource(R.string.settings),
                    color = Color(0xFFFFFFFF),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(start = 20.dp))

            }
//        			Row(
//                				verticalAlignment = Alignment.CenterVertically,
//        				modifier = Modifier
//                					.padding(bottom = 13.dp,start = 49.dp,end = 49.dp,)
//        					.fillMaxWidth()
//        			){



            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 10.dp, start = 52.dp, end = 52.dp,)
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("student_settings_language") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
            ){


                Icon(
                    painter = painterResource(id = R.drawable.language_change_icon), // replace with your back icon
                    contentDescription = "Language icon",
                    tint= SmartBusSecondaryBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
//                            navController.popBackStack()
                            navController.navigate("student_settings_language") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(0.12f)
                ){
                }

               Text(
                    stringResource(R.string.language),
                    color = Color(0xFF000000),
                    fontSize = 18.sp, modifier = Modifier.clickable {
                        navController.navigate("student_settings_language") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                ){
                }


                Icon(
                    painter = painterResource(id = R.drawable.chevron_right), // replace with your back icon
                    contentDescription = "right",
                    tint= Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
//                            navController.popBackStack()

                            navController.navigate("student_settings_language") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                )

            }




//                    }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 10.dp, start = 52.dp, end = 52.dp,)
                    .fillMaxWidth()
            ){

                Icon(
                    painter = painterResource(id = R.drawable.sound_icon), // replace with your back icon
                    contentDescription = "sound icon",
                    tint= SmartBusSecondaryBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
//                            navController.popBackStack()
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(0.12f)
                ){
                }

                Text(stringResource(R.string.sounds),
                    color = Color(0xFF000000),
                    fontSize = 18.sp,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                ){
                }
                SwitchWithCustomColors(preferencesRepository)

            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 20.dp, start = 52.dp, end = 52.dp,)
                    .fillMaxWidth()
            ){

                Icon(
                    painter = painterResource(id = R.drawable.support_icon), // replace with your back icon
                    contentDescription = "Back",
                    tint= SmartBusSecondaryBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            supportWhatsApp(
                                context = context,
                                name = state.value.user?.userName ?: "",
                                email = state.value.user?.userEmail ?: "",
                                phoneNumber = state.value.user?.userPhoneNumber ?: ""
                            )
//                            navController.popBackStack()
                        }
                )

                Column(
                    modifier = Modifier
                        .weight(0.12f)
                ){
                }
                Text(stringResource(R.string.support),
                    color = Color(0xFF000000),
                    fontSize = 18.sp,
                    modifier = Modifier.clickable {
                        supportWhatsApp(context = context, name = state.value.user?.userName?:"",
                            email = state.value.user?.userEmail?:"",
                            phoneNumber =state.value.user?.userPhoneNumber?:"" )
                    }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                ){
                }

                Icon(
                    painter = painterResource(id = R.drawable.chevron_right), // replace with your back icon
                    contentDescription = "right icon",
                    tint= Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            supportWhatsApp(
                                context = context,
                                name = state.value.user?.userName ?: "",
                                email = state.value.user?.userEmail ?: "",
                                phoneNumber = state.value.user?.userPhoneNumber ?: ""
                            )

//                            navController.popBackStack()
                        }
                )



            }


            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 19.dp, start = 54.dp, end = 54.dp,)
                    .fillMaxWidth()
            ){

                Icon(
                    painter = painterResource(id = R.drawable.bug_icon), // replace with your back icon
                    contentDescription = "bug icon",
                    tint= SmartBusSecondaryBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
//                            navController.popBackStack()
                            bugReportWhatsApp(
                                context = context,
                                name = state.value.user?.userName ?: "n/a",
                                email = state.value.user?.userEmail ?: "n/a",
                                phoneNumber = state.value.user?.userPhoneNumber ?: "n/a"
                            )
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(0.12f)
                ){
                }

                Text(stringResource(R.string.report_a_bug),
                    color = Color(0xFF000000),
                    fontSize = 18.sp,
                    modifier = Modifier.clickable {
                        bugReportWhatsApp(
                            context = context,
                            name = state.value.user?.userName ?: "n/a",
                            email = state.value.user?.userEmail ?: "n/a",
                            phoneNumber = state.value.user?.userPhoneNumber ?: "n/a"
                        )

                    }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                ){
                }

                Icon(
                    painter = painterResource(id = R.drawable.chevron_right), // replace with your back icon
                    contentDescription = "right icon",
                    tint= Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
//                            navController.popBackStack()
                            bugReportWhatsApp(
                                context = context,
                                name = state.value.user?.userName ?: "n/a",
                                email = state.value.user?.userEmail ?: "n/a",
                                phoneNumber = state.value.user?.userPhoneNumber ?: "n/a"
                            )

                        }
                )

            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 18.dp, start = 54.dp, end = 54.dp,)
                    .fillMaxWidth()
            ){


                Icon(
                    painter = painterResource(id = R.drawable.rate_icon), // replace with your back icon
                    contentDescription = "rate icon",
                    tint= SmartBusSecondaryBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
//									launchInAppReview(activity, context)
//                            navController.popBackStack()

                            scope.launch {
                                try {
                                    // Request the review flow
                                    val reviewInfo = reviewManager.requestReview()
                                    // Launch the review flow
                                    if (activity != null) {
                                        reviewManager.launchReviewFlow(activity, reviewInfo)
                                    }
//                                    Toast
//                                        .makeText(
//                                            context,
//                                            "Thank you for your feedback!",
//                                            Toast.LENGTH_SHORT
//                                        )
//                                        .show()
                                } catch (e: Exception) {
                                    // Handle error
                                    e.printStackTrace()
                                    Toast
                                        .makeText(
                                            context,
                                            "Error launching review flow: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            }

                        }
                )

                Column(
                    modifier = Modifier
                        .weight(0.12f)
                ){
                }
                Text(
                    stringResource(R.string.rate_this_app),
                    color = Color(0xFF000000),
                    fontSize = 18.sp,
                    modifier = Modifier.clickable {
                        scope.launch {
                            try {
                                // Request the review flow
                                val reviewInfo = reviewManager.requestReview()
                                // Launch the review flow
                                if (activity != null) {
                                    reviewManager.launchReviewFlow(activity, reviewInfo)
                                }
//                                Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                // Handle error
                                e.printStackTrace()
                                Toast.makeText(context, "Error launching review flow: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                ){
                }

                Icon(
                    painter = painterResource(id = R.drawable.chevron_right), // replace with your back icon
                    contentDescription = "right icon",
                    tint= Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
//									launchInAppReview(activity, context)

                            scope.launch {
                                try {
                                    // Request the review flow
                                    val reviewInfo = reviewManager.requestReview()
                                    // Launch the review flow
                                    if (activity != null) {
                                        reviewManager.launchReviewFlow(activity, reviewInfo)
                                    }
//                                    Toast
//                                        .makeText(
//                                            context,
//                                            "Thank you for your feedback!",
//                                            Toast.LENGTH_SHORT
//                                        )
//                                        .show()
                                } catch (e: Exception) {
                                    // Handle error
                                    e.printStackTrace()
                                    Toast
                                        .makeText(
                                            context,
                                            "Error launching review flow: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            }

//                            navController.popBackStack()
                        }
                )


            }



            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 74.dp, start = 54.dp, end = 54.dp)
                    .fillMaxWidth()
            ) {
                Box {
                    Icon(
                        painter = painterResource(id = R.drawable.update_icon),
                        contentDescription = "update icon",
                        tint = SmartBusSecondaryBlue,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
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
                                    } else {
                                        Toast
                                            .makeText(
                                                context,
                                                "No updates available",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                }
                            }
                    )

                    // Display badge if update is available
                    if (updateAvailable) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color.Red, shape = CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                    else{
                       // Toast.makeText(context, "No updates available", Toast.LENGTH_SHORT).show()
                    }
                }

                Column(
                    modifier = Modifier.weight(0.12f)
                ) {}

                Text(
                    stringResource(R.string.check_for_update),
                    color = Color(0xFF000000),
                    fontSize = 18.sp,
                    modifier = Modifier.clickable {
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
                                    Toast.makeText(context, "Failed to start update", Toast.LENGTH_SHORT).show()
                                }
                            } else {
//                                Toast.makeText(context, "No updates available", Toast.LENGTH_SHORT).show()
                            }
                        }

                        if (!updateAvailable) {
                            Toast.makeText(context, "No updates available", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {}

                Icon(
                    painter = painterResource(id = R.drawable.chevron_right),
                    contentDescription = "right icon",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            // Handle right icon click
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
                                } else {
//                                    Toast.makeText(context, "No updates available", Toast.LENGTH_SHORT).show()
                                }
                            }

                            if (!updateAvailable) {
                                Toast
                                    .makeText(context, "No updates available", Toast.LENGTH_SHORT)
                                    .show()
                            }

                        }
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 115.dp,)
                    .clip(shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF000000),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 12.dp,)
            ){
                Box(
                    modifier = Modifier
                        .padding(end = 20.dp,)
                ){
                    Column(
                        modifier = Modifier
                    ){


                        Icon(
                            painter = painterResource(id = R.drawable.logout_icon), // replace with your back icon
                            contentDescription = "update icon",
                            tint= SmartBusSecondaryBlue,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    coroutineScope.launch {
                                        preferencesRepository.setLoggedIn(false)
                                        alertStatusDao.clearAllAlertStatuses()
                                        (context as? MainActivity)?.restartApp()

                                    }
                                }
                        )

                    }

                }
                Text(stringResource(R.string.log_out),
                    color = Color(0xFFFFFFFF),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        coroutineScope.launch {
                            preferencesRepository.setLoggedIn(false)
                            preferencesRepository.setUserName("")
                            preferencesRepository.setUserId(0)
                            preferencesRepository.setUserPass("")

                            (context as? MainActivity)?.restartApp()

                        }
                    }
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun CustomStudentScreenSettings() {
    val navController = rememberNavController()
//    StudentScreenSettings(navController, state)
}