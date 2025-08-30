package com.smartbus360.app.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.smartbus360.app.R
import com.smartbus360.app.data.model.request.AdminLoginRequest
import com.smartbus360.app.data.model.request.LoginRequest
import com.smartbus360.app.data.model.request.LoginStudentRequest
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.ui.component.NetworkSnackbar
import com.smartbus360.app.ui.component.ShowProgressDialog
import com.smartbus360.app.ui.theme.GarudaCrayolaBlue
import com.smartbus360.app.ui.theme.GarudaRed
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue
import com.smartbus360.app.utility.isInternetAvailable
import com.smartbus360.app.viewModels.LanguageViewModel
import com.smartbus360.app.viewModels.LoginViewModel
import com.smartbus360.app.viewModels.NetworkViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import kotlinx.coroutines.delay
import android.content.Context
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import android.util.Log
import org.koin.androidx.compose.koinViewModel
import org.json.JSONObject




@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel,
    languageViewModel: LanguageViewModel,
    preferencesRepository: PreferencesRepository = get(),
    networkViewModel: NetworkViewModel = getViewModel()
) {
    val userName = preferencesRepository.getUserName()
    val userPass = preferencesRepository.getUserPass()
    var userId by remember { mutableStateOf(userName) }
    var password by remember { mutableStateOf(userPass) }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordForget by remember { mutableStateOf(false) }
    var clicked by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents ?: return@rememberLauncherForActivityResult
        val uri: Uri? = runCatching { Uri.parse(contents) }.getOrNull()

// smartbus360://qr-login?token=...
        val tokenFromDeepLink =
            if (
                "smartbus360".equals(uri?.scheme, ignoreCase = true) &&
                ("qr-login".equals(uri?.host, ignoreCase = true) ||
                        "/qr-login".equals(uri?.path, ignoreCase = true))
            ) uri?.getQueryParameter("token") else null

// JSON payload: {"token":"...","type":"driver-login"}
        val tokenFromJson = runCatching {
            if (contents.trim().startsWith("{")) {
                JSONObject(contents).optString("token", null)
            } else null
        }.getOrNull()

// Raw token
        val tokenFromRaw = contents.takeIf { it.length in 28..128 }

        val token = tokenFromDeepLink ?: tokenFromJson ?: tokenFromRaw
        if (!token.isNullOrBlank()) {
            viewModel.resetExceptionState()
            viewModel.exchangeQrLogin(token)
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val isConnected by networkViewModel.isConnected.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    // Only recompute state when necessary
    val loginState = viewModel.state.collectAsState().value
    val stateStudent = viewModel.stateStudent.collectAsState().value
    val stateException = viewModel.stateException.collectAsState().value
    val stateExceptionStatus = viewModel.stateExceptionStatus.collectAsState().value
    var isRedirecting by remember { mutableStateOf(false) }

    // Fetch role once
    val role = remember { preferencesRepository.getUserRole() }

    // Move context retrieval inside remember to avoid recomposition cost
    val context = LocalContext.current

    var navigationHandled by remember { mutableStateOf(false) }

    // Observe exception status and exception details
    val exceptionStatus by viewModel.stateExceptionStatus.collectAsState()
    val exception by viewModel.stateException.collectAsState()
    val adminLoginSuccess by viewModel.adminLoginSuccess.collectAsState()
//    val qrLoginSuccess by viewModel.qrLoginSuccess.collectAsState()
    val qrOk by viewModel.qrLoginSuccess.collectAsState()

//    LaunchedEffect(loginState.success, stateStudent.success, adminLoginSuccess) {
//        when {
//            adminLoginSuccess -> {
//                navController.navigate("adminDashboard") {
//                    popUpTo("login") { inclusive = true }
//                }
//            }
//            stateStudent.success -> {
//                navController.navigate("student") {
//                    popUpTo("login") { inclusive = true }
//                }
//            }
//            loginState.success -> {
//                navController.navigate("driver") {
//                    popUpTo("login") { inclusive = true }
//                }
//            }
//        }
//    }

    var navigated by remember { mutableStateOf(false) }

    LaunchedEffect(loginState.success, stateStudent.success, adminLoginSuccess) {
        if (navigated) return@LaunchedEffect

        when {
            adminLoginSuccess -> {
                navigated = true
                navController.navigate("adminDashboard") {
                    popUpTo("login") { inclusive = true }
                }
            }
            stateStudent.success -> {
                navigated = true
                navController.navigate("student") {
                    popUpTo("login") { inclusive = true }
                }
            }
            loginState.success -> {
                navigated = true
                navController.navigate("driver") {
                    popUpTo("login") { inclusive = true }
                }
            }


        }
    }
//    LaunchedEffect(qrLoginSuccess) {
//        if (qrLoginSuccess) {
//            navController.navigate("driver") {
//                popUpTo("login") { inclusive = true }
//            }
//        }
//    }
    LaunchedEffect(qrOk) {
        if (qrOk) {
            Log.d("NAV", "QR login success — navigating to driver")
            navController.navigate("driver") {
                popUpTo("login") { inclusive = true }   // ✅ clear login only
                launchSingleTop = true
            }
        }
    }



//    LaunchedEffect(exceptionStatus) {
//        if (exceptionStatus) {
//            exception?.let {
//                val message = when (it) {
//                    is HttpException -> "HTTP Error: ${it.code()} - ${it.message}"
//                    is IOException -> "Network Error: ${it.message}"
//                    else -> "Unexpected Error: ${it.message}"
//                }
//                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
//            }
//            // Reset exception status
//            viewModel.resetExceptionState()
//        }
//    }

    // ✅ FIX: Auto-redirect admin if already logged in
    LaunchedEffect(Unit) {
        if (preferencesRepository.isLoggedIn()&& !navigationHandled) {
            isRedirecting = true  // 🟡 Start showing loader
            navigationHandled = true  // ✅ Prevent double navigation

            val savedRole = preferencesRepository.getUserRole()
            when (savedRole) {
                "admin" -> navController.navigate("adminDashboard") {
                    popUpTo("login") { inclusive = true }
                }
                "student" -> navController.navigate("student") {
                    popUpTo("login") { inclusive = true }
                }
                "driver" -> navController.navigate("driver") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }


    // Launch effect for navigation based on role and state
//    LaunchedEffect(stateStudent.success) {
//        if (stateStudent.success && role == "student") {
//            preferencesRepository.setLoggedIn(true)
//            preferencesRepository.setUserRole("student")
//            preferencesRepository.setUserName(userId.trim())
//            preferencesRepository.setUserPass(password.trim())
//            delay(500)
//            navController.navigate("student") {
//                popUpTo("login") { inclusive = true }
//            }
//        }
//    }
//// --- Driver Login Success ---
//    LaunchedEffect(loginState.success) {
//        if (loginState.success && role == "driver") {
//            preferencesRepository.setLoggedIn(true)
//            preferencesRepository.setUserRole("driver")
//            preferencesRepository.setUserName(userId.trim())
//            preferencesRepository.setUserPass(password.trim())
//            delay(500)
//            navController.navigate("driver") {
//                popUpTo("login") { inclusive = true }
//            }
//        }
//    }
//

    // ✅ Loader only for fresh login clicks
    if (clicked && !stateExceptionStatus) {
        ShowProgressDialog(isLoading = true, onDismiss = { clicked = false })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {


//            if (preferencesRepository.isLoggedIn() && isInternetAvailable(context)) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(Color.White),
//                    contentAlignment = Alignment.Center // Centers the content within the Box
//                ) {
//                    CircularProgressIndicator()
//                }
//            } else {

                if (clicked && !stateExceptionStatus) {
//        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
//            TransparentLoadingModal(true)
                    ShowProgressDialog(isLoading = true, onDismiss = { clicked = false })

                }


                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) } // Attach SnackbarHost
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(16.dp)
                    ) {


                        // Back button
                        Icon(
                            painter = painterResource(id = R.drawable.back_icon),
                            contentDescription = "Back",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(42.dp)
                                .clickable {
                                    navController.popBackStack()
                                }
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Sign-in text based on user role
                        Text(
                            text = when (role) {
                                "student" -> stringResource(R.string.sign_in_for_student)
                                "admin" -> "Sign in for Admin"
                                else -> stringResource(R.string.sign_in_for_driver)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                viewModel.resetExceptionState()

                                val options = ScanOptions().apply {
                                    setDesiredBarcodeFormats(ScanOptions.QR_CODE) // or your existing config
                                    setBeepEnabled(true)
                                    setOrientationLocked(true)
                                    setPrompt("Scan driver QR")
                                }
                                launcher.launch(options)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .height(50.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = GarudaCrayolaBlue), // pick your theme color
                            shape = RectangleShape,
                            enabled = isConnected
                        ) {
                            Text(
                                text = "Scan QR to Login",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }



                        Spacer(modifier = Modifier.height(40.dp))

                        // User ID input field
                        Text(
                            text = stringResource(R.string.user_id),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        TextField(
                            value = userId,
                            onValueChange = { userId = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            placeholder = { Text(stringResource(R.string.enter_user_id)) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.mail_icon),
                                    contentDescription = "Email Icon",
                                    tint = Color.Gray
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            isError = stateExceptionStatus
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password input field
                        Text(
                            text = stringResource(R.string.enter_password),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            placeholder = { Text(stringResource(R.string.enter_password)) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.lock__password),
                                    contentDescription = "Password Icon",
                                    tint = Color.Gray
                                )
                            },
                            trailingIcon = {
                                val visibilityIcon =
                                    if (passwordVisible) R.drawable.visibility_icon else R.drawable.visiblity_off_icon
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        painter = painterResource(id = visibilityIcon),
                                        contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                                    )
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            isError = stateExceptionStatus,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Forget Password text
                        val annotatedText = buildAnnotatedString {
                            append(stringResource(R.string.forget_user_id_or_password))
                            if (passwordForget) {
                                append("\n")
                                pushStringAnnotation(
                                    tag = "ADMIN_LINK",
                                    annotation = "https://www.smartbus360.com/home/join-us"
                                )
                                withStyle(style = SpanStyle(color = Color.Red)) { // Apply red color
                                    append(stringResource(R.string.contact_smartbus360_administration))
                                }
                                pop()
                            }
                        }

                        ClickableText(
                            text = annotatedText,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                color = GarudaCrayolaBlue
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            onClick = { offset ->
                                annotatedText.getStringAnnotations(
                                    tag = "ADMIN_LINK",
                                    start = offset,
                                    end = offset
                                )
                                    .firstOrNull()?.let {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.item))
                                        context.startActivity(intent)
                                    }
                                passwordForget = true // Ensure passwordForget is updated on click
                            }
                        )

                        if (stateExceptionStatus) {
                            Text(
                                text = "wrong user id or password!",
                                fontSize = 14.sp,
                                color = GarudaRed,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .clickable { /* Handle forget password */ }
                            )
                        }
//        if (clicked && !stateExceptionStatus) {
//            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
////            TransparentLoadingModal(true)
//            ShowProgressDialog(isLoading = clicked, onDismiss = { clicked = false })
//
//        }


//        Spacer(modifier = Modifier.height(40.dp))

                        // Sign In button
                        Button(
                            onClick = {
                                val trimmedPassword = password.trim()
                                clicked = true
                                viewModel.resetExceptionState() // ✅ Clears any old error

                                when (role) {
                                    "student" -> coroutineScope.launch(Dispatchers.IO) {
                                        viewModel.loginStudent(
                                            LoginStudentRequest(
                                                email = userId.trim(),
                                                password = trimmedPassword
                                            )
                                        )
                                    }

                                    "admin" -> coroutineScope.launch(Dispatchers.IO) {
                                        val requestBody = AdminLoginRequest(
                                            email = userId.trim(),
                                            password = trimmedPassword,
                                            client_id = "90ddb74bd2b84b7870f5b693e8553ad3",
                                            client_secret = "c566e750e7456ef3bf73b2446c3b84dd45f28ec8c64b25eb603d16c1d46a71fa"
                                        )
                                        viewModel.loginAdmin(requestBody)
                                    }

                                    else -> coroutineScope.launch(Dispatchers.IO) {
                                        viewModel.login(
                                            LoginRequest(
                                                email = userId.trim(),
                                                password = trimmedPassword
                                            )
                                        )
                                    }
                                }
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 40.dp)
                                .height(50.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = SmartBusSecondaryBlue),
                            shape = RectangleShape,
                            enabled = isConnected
                        ) {
                            Text(
                                text = stringResource(R.string.sign_in),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        // Not a member link
                        ClickableText(
                            text = AnnotatedString(stringResource(R.string.not_a_member_contact_administration)),
                            onClick = { /* Handle contact administration */
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.smartbus360.com/home/join-us")
                                )
                                ContextCompat.startActivity(context, intent, null)
                            },
                            style = TextStyle(
                                color = Color.Blue,
                                textAlign = TextAlign.Center,
                                textDecoration = TextDecoration.Underline,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        NetworkSnackbar(
                            isConnected = isConnected,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }

        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewLoginScreen1() {
//    LoginScreen()

    }


