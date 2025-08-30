package com.smartbus360.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.smartbus360.app.R
import com.smartbus360.app.data.model.response.GetUserDetailResponseX
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.ui.theme.SmartBusTertiaryBlue
import com.smartbus360.app.viewModels.StudentScreenViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun StudentScreenComponentMid(
    state: State<GetUserDetailResponseX>,
    navController: NavHostController,
    studentScreenViewModel: StudentScreenViewModel = getViewModel()

) {
    val context = LocalContext.current
    val stateBusReplaced = studentScreenViewModel.busReplacedStatus.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SmartBusTertiaryBlue)
            .padding(vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            modifier = Modifier
                .padding(start = 25.dp, end = 10.dp)
                .fillMaxWidth()
        ) {
//            DateDropdownMenuWithCustomColor(navController)


            Spacer(modifier = Modifier.width(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        // Handle your date picker logic or navigation
                        navController.navigate("date")
                    }
                    .padding(end = 8.dp) // Optional padding
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.reached_icon), // Make sure icon exists
                    contentDescription = "Select Date",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.date_log), // \n creates a line break
                    color = Color.White,
                    fontSize = 12.sp
                )

            }
//


            // Column for Bus No and Driver Contact No
            Column(
                modifier = Modifier.weight(1f)
            ) {




                // Bus Number Section
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 15.dp)

                ) {
                    Text( stringResource(id = R.string.bus_no),
//                        "Bus No :",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(end = 18.dp)
                            .width(60.dp)
                            .background(Color.White)
                            .padding(vertical = 4.dp)
                    ) {

                        Text(
                            text =if (stateBusReplaced.value.status == true &&
                                (stateBusReplaced.value.message ?: "") == stringResource(R.string.bus_has_been_replaced)
                            )

                                (stateBusReplaced.value.replacedBus.new_bus_number?:"") + "*"
                            else
                                state.value.user?.busNumber?: stringResource(R.string.n_a),
                            color = if (stateBusReplaced.value.status == true &&
                                stateBusReplaced.value.message == stringResource(R.string.bus_has_been_replaced))

                                Color.Red
                            else
                                Color.Black,
                            fontSize = 14.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Driver Contact Section
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 15.dp)
                ) {
                    Text(
                        stringResource(R.string.driver_contact_no),
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(99.dp)
                            .background(Color.White)
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = state.value.user?.driverPhone?:stringResource(R.string.n_a),
                            color = Color.Black,
                            fontSize = 14.sp,
                        )
                    }
                }
            }

            // Share Icon
            Icon(
                painter = painterResource(id = R.drawable.sharing_icon),
                contentDescription = "Share Icon",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        val imageFile = R.drawable.smartbus360_logo
                        imageFile?.let { file ->
                            shareImageFromDrawable(context, file, state.value.user)
                        }
                    }
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewStudentScreenComponentMid() {
    val  context = LocalContext.current
    val preferencesRepository = PreferencesRepository(context)
    val studentScreenViewModel = StudentScreenViewModel(preferencesRepository)
    val state = studentScreenViewModel.state.collectAsState()
    StudentScreenComponentMid(state = state, navController = rememberNavController())
}