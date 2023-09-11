package com.tiesiogdvd.composetest.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tiesiogdvd.composetest.R

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)


val Jost = FontFamily(
    Font(R.font.jost_bolditalic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(R.font.jost_mediumitalic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(R.font.jost_regular, weight = FontWeight.Normal, style = FontStyle.Italic)

)