package com.tiesiogdvd.composetest.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils


object GetThemeColor {

    fun getBackground(isSystemInDarkMode:Boolean): Color {
        return if (isSystemInDarkMode) backgroundDark else background
    }

    fun getBackgroundSecondary(isSystemInDarkMode:Boolean): Color {
        return if (isSystemInDarkMode) backgroundSecondaryDark else backgroundSecondary
    }

    fun getBackgroundThird(isSystemInDarkMode:Boolean): Color {
        return if (isSystemInDarkMode) backgroundThirdDark else backgroundThird
    }

    fun getText(isSystemInDarkMode:Boolean): Color {
        return if (isSystemInDarkMode) textDark else text
    }

    fun getTextSecondary(isSystemInDarkMode:Boolean): Color {
        return if (isSystemInDarkMode) text_secondaryDark else text_secondary
    }
    fun getButton(isSystemInDarkMode:Boolean): Color {
        return if (isSystemInDarkMode) buttonDark else button
    }
    fun getButtonSecondary(isSystemInDarkMode:Boolean): Color {
        return if (isSystemInDarkMode) button_secondaryDark else button_secondary
    }
    fun getDrawableMenu(isSystemInDarkMode:Boolean): Color {
        return if (isSystemInDarkMode) drawable_menuDark else drawable_menu
    }
    fun getDrawableBar(isSystemInDarkMode:Boolean): Color {
        return if (isSystemInDarkMode) drawable_barDark else drawable_bar
    }

    fun getPurple(isSystemInDarkMode:Boolean): Color{
        return if (isSystemInDarkMode) purpleDark else purple
    }

    fun getGreen(isSystemInDarkMode: Boolean): Color{
        return if (isSystemInDarkMode) greenDark else green
    }

    fun getError(isSystemInDarkMode: Boolean): Color{
        return if (isSystemInDarkMode) errorDark else error
    }

    fun getLoading(isSystemInDarkMode: Boolean): Color{
        return if (isSystemInDarkMode) loadingDark else loading
    }

    fun getSettingsPrimary(isSystemInDarkMode: Boolean): Color{
        return if (isSystemInDarkMode) settingsPrimary else settingsPrimaryDark
    }
    fun waveProgress(isSystemInDarkMode: Boolean): Color{
        return if (isSystemInDarkMode) waveProgressDark else waveProgress
    }

    fun waveBackground(isSystemInDarkMode: Boolean): Color{
        return if (isSystemInDarkMode) waveBackground else waveBackgroundDark
    }


    fun isDark(color: Color): Boolean {
        return ColorUtils.calculateLuminance(color.toArgb()) < 0.3
    }

    fun isColorDark(color: Int): Boolean {
        val darkness = 1 - (0.299 * android.graphics.Color.red(color) + 0.587 * android.graphics.Color.green(color) + 0.114 * android.graphics.Color.blue(color)) / 255
        return darkness >= 0.5
    }

}