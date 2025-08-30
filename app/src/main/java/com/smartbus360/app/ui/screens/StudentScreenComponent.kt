package com.smartbus360.app.ui.screens

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.smartbus360.app.R
import com.smartbus360.app.data.model.response.GetUserDetailResponseX
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue
import com.smartbus360.app.viewModels.NotificationViewModel
import com.smartbus360.app.viewModels.StudentScreenViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun StudentMainScreenComponent(
    state1: State<GetUserDetailResponseX>,
    navController: NavHostController,
    studentScreenViewModel: StudentScreenViewModel = getViewModel(),
            notificationViewModel: NotificationViewModel = getViewModel()
) {
    var threeLineBarExpanded by remember { mutableStateOf(false) }
    val state = studentScreenViewModel.busReplacedStatus.collectAsState()
    val instituteType = studentScreenViewModel.state.collectAsState().value.user?.institute_type
    val busId = studentScreenViewModel.state.collectAsState().value.user?.busId

    val notificationState = notificationViewModel.state.collectAsState()

    val busNotiState = notificationViewModel.stateBusNoti.collectAsState()


    val scope = rememberCoroutineScope()
    val animatedColor = remember { Animatable(Color.Gray) }

    var messageCount by remember { mutableIntStateOf(0) }
    val notifications = remember { mutableStateListOf<com.smartbus360.app.data.model.response.Notification>() }

    LaunchedEffect(notificationState.value) {
        val filteredNotifications = notificationState.value.notifications.filter { notification ->
            notification.instituteType == instituteType
        }
        val filteredBusNotifications = busNotiState.value.notifications.filter { notification ->
            notification.busId == busId
        }
        notifications.clear()
        notifications.addAll(filteredNotifications)
        notifications.addAll(filteredBusNotifications)
        messageCount = notifications.size // Update message count accordingly
    }




//    val notifications = notificationState.value.notifications.map { noti ->
//        if (noti.instituteType != instituteType) {
//         notificationState.value.notifications
//        } else {
//            // Use actual values otherwise
//        }
//    }

    messageCount = if (notifications.isEmpty()){
        0
    }
    else{
        notifications.size
    }

    // Example: Replace with actual message count from ViewModel

    // Trigger animation when the replacement occurs
    LaunchedEffect(state.value.status, state.value.message) {
        if (state.value.status == true && state.value.message == "Bus has been replaced.") {
            animatedColor.animateTo(
                targetValue = Color.Black,
                animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
            )
            animatedColor.animateTo(
                targetValue = Color.Red,
                animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SmartBusSecondaryBlue)
            .padding(vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
        ) {
            // Three-line menu icon
            Icon(
                painter = painterResource(id = R.drawable.three_line_horizontal_icon),
                contentDescription = "Menu Icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .clickable {
                        threeLineBarExpanded = !threeLineBarExpanded
                        navController.navigate("studentSettings") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                    .size(24.dp) // Adjust size accordingly
            )

            // Bus Status Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .width(166.dp)
                    .background(Color.White)
                    .padding(vertical = 5.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        if (state.value.status == true && state.value.message == "Bus has been replaced.") {
                            append(stringResource(R.string.bus_no_))
                            withStyle(
                                style = SpanStyle(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = Color.Gray
                                )
                            ) {
                                append(state.value.replacedBus.old_bus_number)
                            }
                            append(stringResource(R.string.replaced_with))
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = animatedColor.value
                                )
                            ) {
                                append(state.value.replacedBus.new_bus_number)
                                append("*")
                            }
                        } else {
                            append(stringResource(R.string.bus_not_replaced))
                        }
                    },
                    color = if (state.value.status == true && state.value.message == "Bus has been replaced.") {
                        Color.Red
                    } else {
                        Color.Gray
                    },
                    fontSize = 14.sp,
                )
            }

            // Message Icon with Badge
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.mail_icon),
                    contentDescription = "Message Icon",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp) // Adaptive size for icon
                        .clickable {
                            navController.navigate("notification")
//                            navController.navigate("reachDateTime")
                        }
                )

                if (messageCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (8).dp, y = (-2).dp) // Adjust position
                    ) {
                        Text(
                            text = messageCount.toString(),
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewStudentMainScreenComponent() {
   val navController = rememberNavController()
    val  context = LocalContext.current
    val preferencesRepository = PreferencesRepository(context)
    val studentScreenViewModel = StudentScreenViewModel(preferencesRepository)
    val state = studentScreenViewModel.state.collectAsState()
    StudentMainScreenComponent(state, navController)
}
