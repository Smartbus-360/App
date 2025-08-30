package com.smartbus360.app.ui.screens

import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Calendar


//@Composable
//fun LoginScreen() {
//    var userId by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var passwordVisible by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(16.dp)
//    ) {
//        // Top left back icon
//        Icon(
//            painter = painterResource(id = R.drawable.back_icon), // replace with your back icon
//            contentDescription = "Back",
//            tint= Color.Unspecified,
//            modifier = Modifier
//                .size(42.dp)
//                .clickable { /* Handle back action */ }
//        )
//
//        Spacer(modifier = Modifier.height(40.dp))
//
//        // "Sign In for Student" centered in the screen
//        Text(
//            text = "Sign In for Student",
//            style = MaterialTheme.typography.titleLarge,
//            fontSize = 32.sp,
//            fontWeight = FontWeight.Bold,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(modifier = Modifier.height(40.dp))
//
//        // User ID field with mail icon
//        Text(
//            text = "User ID",
//            fontSize = 16.sp,
//            modifier = Modifier.padding(start = 16.dp)
//        )
//        TextField(
//            value = userId,
//            onValueChange = { userId = it },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp),
//            placeholder = { Text("Enter User ID") },
//            singleLine = true,
//            leadingIcon = {
//                Icon(
//                    painter = painterResource(id = R.drawable.mail_icon), // Custom mail icon
//                    contentDescription = "Email Icon",
//                    tint = Color.Gray
//                )
//            }
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Password field with password icon and show/hide toggle
//        Text(
//            text = "Password",
//            fontSize = 16.sp,
//            modifier = Modifier.padding(start = 16.dp)
//        )
//        TextField(
//            value = password,
//            onValueChange = { password = it },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp),
//            placeholder = { Text("Enter Password") },
//            singleLine = true,
//            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//            leadingIcon = {
//                Icon(
//                    painter = painterResource(id = R.drawable.lock__password), // Custom password icon
//                    contentDescription = "Password Icon",
//                    tint = Color.Gray
//                )
//            },
//            trailingIcon = {
//                val visibilityIcon = if (passwordVisible)
//                    R.drawable.visibility_icon // Custom visibility icon (shown)
//                else
//                    R.drawable.visiblity_off_icon // Custom visibility off icon (hidden)
//
//                IconButton(onClick = {
//                    passwordVisible = !passwordVisible
//                }) {
//                    Icon(
//                        painter = painterResource(id = visibilityIcon),
//                        contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
//                    )
//                }
//            }
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Forget Password text
//        Text(
//            text = "Forget user Id or password?",
//            fontSize = 14.sp,
//            color = GarudaCrayolaBlue,
//            textAlign = TextAlign.Start,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp)
//                .clickable { /* Handle forget password */ }
//        )
//
//        Spacer(modifier = Modifier.height(40.dp))
//
//        // Sign In button
//        Button(
//            onClick = { /* Handle Sign In action */ },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp)
//                .height(50.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = GarudaYellow)
//        ) {
//            Text(
//                text = "Sign In",
//                color = Color.White,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold
//            )
//        }
//
//        Spacer(modifier = Modifier.height(30.dp))
//
//        // "Not a member? Contact administration" link at the bottom
//        ClickableText(
//            text = androidx.compose.ui.text.AnnotatedString("Not a member? Contact administration"),
//            onClick = { /* Handle contact administration */ },
//            style = TextStyle(
//                color = Color.Blue,
//                textAlign = TextAlign.Center,
//                textDecoration = TextDecoration.Underline,
//                fontSize = 14.sp
//            ),
//            modifier = Modifier.fillMaxWidth()
//        )
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewLoginScreen() {
//    LoginScreen()
//}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionDropdown() {
    var expanded by remember { mutableStateOf(false) } // State for dropdown visibility
    var selectedDate by remember { mutableStateOf("Select a date") } // State for selected date

    val context = LocalContext.current

    // Custom Date Picker logic
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            selectedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
        }, year, month, day
    )

    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row {
            // Display selected date
            Text(
                text = selectedDate,
                modifier = Modifier.weight(1f).padding(8.dp),
                textAlign = TextAlign.Start
            )

            // IconButton to trigger dropdown
            IconButton(onClick = { expanded = true }) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_more), // Replace with a custom icon
                    contentDescription = "Dropdown icon",
                    tint = Color.Black
                )
            }

            // Dropdown menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false } // Close dropdown when clicked outside
            ) {
                // Option 1: Select Today
                DropdownMenuItem(
                    onClick = {
                        val today = Calendar.getInstance()
                        selectedDate = "${today.get(Calendar.DAY_OF_MONTH)}/${today.get(Calendar.MONTH) + 1}/${today.get(Calendar.YEAR)}"
                        expanded = false // Close dropdown
                    },
                    text = {
                        Text(text = "Today")
                    }
                )

                // Option 2: Select Tomorrow
                DropdownMenuItem(
                    onClick = {
                        val tomorrow = Calendar.getInstance()
                        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
                        selectedDate = "${tomorrow.get(Calendar.DAY_OF_MONTH)}/${tomorrow.get(Calendar.MONTH) + 1}/${tomorrow.get(Calendar.YEAR)}"
                        expanded = false // Close dropdown
                    },
                    text = {
                        Text(text = "Tomorrow")
                    }
                )

                // Option 3: Select Custom Date
                DropdownMenuItem(
                    onClick = {
                        datePickerDialog.show()
                        expanded = false // Close dropdown
                    },
                    text = {
                        Text(text = "Custom Date")
                    }
                )
            }
        }
    }
}