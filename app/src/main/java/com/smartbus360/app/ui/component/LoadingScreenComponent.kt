package com.smartbus360.app.ui.component

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingAnimationScreen() {
    // Infinite transition to animate the scale for pulsating effect
    val infiniteTransition = rememberInfiniteTransition(label = "LoadingTransition")

    // Scale animation with labels for animation preview/debugging
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulsatingScaleAnimation"
    )

    // Alpha (fade) animation for a subtle effect
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FadeAlphaAnimation"
    )

    // Box to center the animation on the screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)), // Light background
        contentAlignment = Alignment.Center
    ) {
        // Circular shape with pulsating effect
        Box(
            modifier = Modifier
                .size(100.dp) // Size of the outer circle
                .scale(scale) // Apply pulsating scale
                .alpha(alpha) // Apply fading effect
                .background(Color.Blue, shape = CircleShape) // Outer blue circle
        )

        // Inner static white circle
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color.White, shape = CircleShape) // Inner white circle
        )
    }
}
