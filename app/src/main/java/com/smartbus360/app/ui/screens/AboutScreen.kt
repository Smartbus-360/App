package com.smartbus360.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.smartbus360.app.R
import com.smartbus360.app.ui.theme.InterMedium
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue

//import com.skydoves.landscapist.*
//import com.skydoves.landscapist.coil3.*

@Composable
fun AboutScreen(navController:NavHostController) {
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
                .padding(top = 30.dp, start = 18.dp, end = 18.dp,)
                .verticalScroll(rememberScrollState())
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 32.dp,)
                    .fillMaxWidth()
            ){
                Icon(
                    painter = painterResource(id = R.drawable.back_icon), // replace with your back icon
                    contentDescription = "Back",
                    tint= Color.Unspecified,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable {
                            //                            navController.popBackStack()
                            navController.popBackStack(
                                route = "home",
                                inclusive = false
                            )

                        }
                )

                Text(
                    stringResource(R.string.about),
                    color = Color(0xFF000000),
                    fontSize = 21.sp,
                    fontFamily = InterMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 20.dp,)
                    .fillMaxWidth()
            ){
                Icon(
                    painter = painterResource(id = R.drawable.terms___conditions), // replace with your back icon
                    contentDescription = "Back",
                    tint= SmartBusSecondaryBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            navController.navigate("terms&condition") {
                                popUpTo("home") { inclusive = false }
                            }
//                            navController.popBackStack()
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(0.12f)
                ){
                }
                Text(
                    stringResource(R.string.terms_conditions),
                    color = Color(0xFF000000),
                    fontSize = 18.sp,
                    modifier =  Modifier.clickable {
                        navController.navigate("terms&condition") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable{
                            navController.navigate("terms&condition") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                ){
                }
                Icon(
                    painter = painterResource(id = R.drawable.chevron_right), // replace with your back icon
                    contentDescription = "right_icon",
                    tint= Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
//                            navController.popBackStack()
                            navController.navigate("terms&condition") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 20.dp)

                    .fillMaxWidth()
            ){
                Icon(
                    painter = painterResource(id = R.drawable.privacy_policy), // replace with your back icon
                    contentDescription = "privacy policy",
                    tint= SmartBusSecondaryBlue,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            navController.navigate("privacy_policy") {
                                popUpTo("home") { inclusive = false }
                            }
//                            navController.popBackStack()
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(0.12f)
                ){
                }
                Text(
                    stringResource(R.string.privacy_policy),
                    color = Color(0xFF000000),
                    fontSize = 18.sp,
                    modifier = Modifier.clickable {
                        navController.navigate("privacy_policy") {
                            popUpTo("home") { inclusive = false }
                        }
                    }

                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            navController.navigate("privacy_policy") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                ){

                }
                Icon(
                    painter = painterResource(id = R.drawable.chevron_right), // replace with your back icon
                    contentDescription = "right_icon",
                    tint= Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
//                            navController.popBackStack()
//                            navController.popBackStack(
//                                route = "privacy_policy",
//                                inclusive = false
//                            )
                            navController.navigate("privacy_policy") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                )
            }
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .padding(bottom = 20.dp,)
//                    .fillMaxWidth()
//            ){
//                Icon(
//                    painter = painterResource(id = R.drawable.credits_icon), // replace with your back icon
//                    contentDescription = "credits",
//                    tint= SmartBusSecondaryBlue,
//                    modifier = Modifier
//                        .size(20.dp)
//                        .clickable {
////                            navController.popBackStack()
//                            navController.navigate("creditsScreen") {
//                                popUpTo("home") { inclusive = false }
//                            }
//                        }
//                )
//                Column(
//                    modifier = Modifier
//                        .weight(0.12f)
//                ){
//                }
//                Text(
//                    stringResource(R.string.credits),
//                    color = Color(0xFF000000),
//                    fontSize = 18.sp,
//                    modifier = Modifier.
//                    clickable {
//                        navController.navigate("creditsScreen") {
//                            popUpTo("home") { inclusive = false }
//                        }
//                    }
//
//
//                )
//                Column(
//                    modifier = Modifier
//                        .weight(1f)
//                ){
//                }
//                Icon(
//                    painter = painterResource(id = R.drawable.chevron_right), // replace with your back icon
//                    contentDescription = "right_icon",
//                    tint= Color.Unspecified,
//                    modifier = Modifier
//                        .size(24.dp)
//                        .clickable {
//                            navController.navigate("creditsScreen") {
//                                popUpTo("home") { inclusive = false }
//                            }
//                        }
//                )
//            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 20.dp,)
                    .fillMaxWidth()
            ){
                Icon(
                    painter = painterResource(id = R.drawable.app_version), // replace with your back icon
                    contentDescription = "app version",
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

                Text(
                    stringResource(R.string.app_version),
                    color = Color(0xFF000000),
                    fontSize = 18.sp,

                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                ){
                }
                Text(
                    stringResource(R.string.v1_1),
                    color = Color(0xFF000000),
                    fontSize = 18.sp,
                )
//                Column(
//                    modifier = Modifier
//                        .weight(1f)
//                ){
//                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun CustomAboutScreenPreview() {
    val navController = rememberNavController()
    AboutScreen(navController)
}