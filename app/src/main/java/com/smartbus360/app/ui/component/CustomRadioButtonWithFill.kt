package com.smartbus360.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue

@Composable
fun CustomRadioButtonWithFill(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = SmartBusSecondaryBlue,
    unselectedColor: Color = Color.Gray,
    tickColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .size(24.dp) // Size of the custom radio button
            .clip(CircleShape)
            .padding(4.dp)
            .clickable { onClick()
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw outer circle (radio button border)
            drawCircle(
                color = Color.Black,
//                if (selected) Color.Black else unselectedColor,
                style = Stroke(width = 3.dp.toPx()) // Outer circle stroke
            )

            // Draw the inner filled circle when selected
            if (selected) {
                drawCircle(
                    color = selectedColor, // Use the selected color for the fill
                    radius = size.minDimension / 2  // Adjust size of the inner circle
                )

                // Draw the tick inside
                drawLine(
                    color = tickColor,
                    start = Offset(x = size.width * 0.3f, y = size.height * 0.5f),
                    end = Offset(x = size.width * 0.45f, y = size.height * 0.7f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tickColor,
                    start = Offset(x = size.width * 0.45f, y = size.height * 0.7f),
                    end = Offset(x = size.width * 0.75f, y = size.height * 0.3f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}