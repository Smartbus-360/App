package com.smartbus360.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QrBlockedScreen(message: String, until: String?, code: Int? = null) {
    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF3CD)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 🔎 Debug label at the top
                code?.let {
                    Text(
                        text = "Blocked (HTTP $it)",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Text(
                    text = message,
                    color = Color(0xFF856404),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                until?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(text = "Until: $it", color = Color(0xFF856404))
                }
            }
        }
    }
}
