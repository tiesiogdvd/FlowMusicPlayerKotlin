package com.tiesiogdvd.composetest.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.palette.graphics.Palette
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ImagePalette(
    val textColor: Color,
    val textColorByBrightness: Color,
    val gradientColor: Color,
    val gradientSecondary: Color,
)


class BitmapPalette {
    fun generatePalette(bitmap:ImageBitmap?,isDarkTheme:Boolean):ImagePalette{
        if(bitmap!=null){
            val palette = Palette.from(bitmap.asAndroidBitmap()).generate()
            val dominantSwatch = palette.dominantSwatch
            var gradientSecondary: Color? = null

            val gradientColor = dominantSwatch?.rgb?.let { Color(it) } ?: GetThemeColor.getBackground(isDarkTheme)

            if(GetThemeColor.isDark(gradientColor)){
                gradientSecondary = palette.darkMutedSwatch?.rgb?.let { Color(it) } ?: GetThemeColor.getBackground(isDarkTheme)
            }else{
                //val newGradientSecondary = newPalette.darkMutedSwatch?.rgb?.let { Color(it) } ?: GetThemeColor.getBackground(isDarkTheme)
                gradientSecondary = gradientColor
            }
            val textColor = dominantSwatch?.rgb?.let { Color(it) } ?: GetThemeColor.getText(isDarkTheme)
            val textColorBrightness = if(GetThemeColor.isDark(gradientColor)){ GetThemeColor.getText(true)}else{ GetThemeColor.getText(false)}

            return ImagePalette(textColor = textColor, textColorByBrightness = textColorBrightness, gradientColor = gradientColor, gradientSecondary = gradientSecondary)

        }else{
            val gradientColor = GetThemeColor.getBackground(isDarkTheme)
            val gradientSecondary = GetThemeColor.getBackground(isDarkTheme)
            val textColor = GetThemeColor.getText(isDarkTheme)
            val textColorByBrightness = GetThemeColor.getText(isDarkTheme)

            return ImagePalette(textColor = textColor, textColorByBrightness = textColorByBrightness, gradientColor = gradientColor, gradientSecondary = gradientSecondary)
        }
    }

    companion object {
        fun defaultPalette(): ImagePalette {
            return ImagePalette(textColor = GetThemeColor.getText(true), textColorByBrightness = GetThemeColor.getText(true), gradientColor = GetThemeColor.getBackground(true), gradientSecondary = GetThemeColor.getBackground(true))
        }
    }
}