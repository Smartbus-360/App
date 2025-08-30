package com.smartbus360.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartbus360.app.ui.theme.Poppins
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue

@Composable
fun RoleCircleWithLabel(
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.padding(8.dp),
//        .border(width=.5.dp, color = Color.Black, RoundedCornerShape(30.dp))
//        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circle with Icon inside
        Box(
            modifier = Modifier.padding(8.dp).shadow(20.dp, CircleShape)
                .clickable(onClick = onClick)
                .size(110.dp)
                .background(color = SmartBusSecondaryBlue, shape = CircleShape)
                .border(width=.5.dp, color = Color.Black, shape = CircleShape)
                ,
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
//                tint = Color.White,
                modifier = Modifier.shadow(0.dp,).padding(8.dp)                   .size(75.dp) // Icon size inside circle

            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Label below the circle
//        ElevatedButton(onClick) {
            Text(
                text = label,
                color = Color.Black,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = Poppins,

                )
//        }

    }
}