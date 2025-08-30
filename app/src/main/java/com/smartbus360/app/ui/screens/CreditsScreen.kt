package com.smartbus360.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.smartbus360.app.R
import com.smartbus360.app.ui.theme.InterMedium
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(navController: NavHostController) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credits", style = MaterialTheme.typography.titleLarge,
                    fontSize = 21.sp,
                    fontFamily = InterMedium,
                    fontWeight = FontWeight.Medium
                    )

                        },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_icon),
                            contentDescription = "Back",
                            tint = Color.Unspecified
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AttributionItem(
                    "Image by vectorjuice on Freepik",
                    "https://www.freepik.com/free-vector/school-bus-tracking-system-abstract-concept-illustration_12290906.htm#fromView=search&page=1&position=1&uuid=09f25576-eae2-44c8-9f5f-4688a9c7be4f"
                )
                AttributionItem(
                    "Image by storyset on Freepik",
                    "https://www.freepik.com/free-vector/directions-concept-illustration_12892951.htm#fromView=search&page=1&position=20&uuid=352814ea-3d0d-457d-b89b-997e2c510387"
                )
                AttributionItem(
                    "Image by vectorjuice on Freepik",
                    "https://www.freepik.com/free-vector/autonomous-public-transport-abstract-concept-illustration_12291364.htm#fromView=search&page=1&position=2&uuid=17c3cace-76f0-429d-bdf5-737e4e33b525"
                )
                AttributionItem(
                    "Icon by nawicon",
                    "https://www.freepik.com/icon/bus_5193843#fromView=keyword&page=1&position=73&uuid=68a2bc81-f651-4317-b3a3-192329f938f5"
                )
                AttributionItem(
                    "Sound Effect from Pixabay",
                    "https://pixabay.com/?utm_source=link-attribution&utm_medium=referral&utm_campaign=music&utm_content=74147"
                )
                AttributionItem(
                    "Sound Effect from Pixabay",
                    "https://pixabay.com/?utm_source=link-attribution&utm_medium=referral&utm_campaign=music&utm_content=72279"
                )
            }

            Text(
                text = "This app is developed with ❤️ by Triads",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun AttributionItem(title: String, url: String) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                append(title)
                append(" ")
                pushStringAnnotation(tag = "URL", annotation = url)
                withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                    append("[Link]")
                }
                pop()
            },
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { copyToClipboard(context, url)}) {
            Icon(
                painter = painterResource(id = R.drawable.copy_icon ),
                contentDescription = "Copy",
                tint = SmartBusSecondaryBlue
            )
        }
//        Button(onClick = { copyToClipboard(context, url) }) {
//            Text(text = "Copy")
//        }
    }
}

fun copyToClipboard(context: android.content.Context, content: String) {
    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager

    // Copy to clipboard
    val clip = android.content.ClipData.newPlainText("Copied Text", content)
    clipboardManager.setPrimaryClip(clip)

    // Display a toast message
    android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
}

