package com.smartbus360.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.smartbus360.app.R

val Roboto = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal),

    Font(R.font.roboto_medium),
)
val Poppins = FontFamily(
    Font(R.font.poppins_regular)
)
val PoppinsMedium = FontFamily(
    Font(R.font.poppins_medium)
)
val Inter = FontFamily(
    Font(R.font.inter_24pt_bold)
)
val InterMedium = FontFamily(Font(R.font.inter_18pt_medium))
val Montserrat = FontFamily(Font(R.font.montserrat_semibold))
val InterRegular = FontFamily(Font(R.font.inter_18pt_regular))


// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    displayLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 21.sp,
        lineHeight = 31.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 21.sp,
        lineHeight = 31.sp,
        letterSpacing = 0.5.sp,

    ),

    headlineMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 31.sp,
        letterSpacing = 0.5.sp,

    ),
    headlineSmall = TextStyle(
        fontFamily = PoppinsMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 31.sp)

    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)



