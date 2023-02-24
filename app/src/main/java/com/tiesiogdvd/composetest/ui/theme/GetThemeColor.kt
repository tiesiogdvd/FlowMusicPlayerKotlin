package com.tiesiogdvd.composetest.ui.theme

import androidx.compose.ui.graphics.Color

object GetThemeColor {

    fun getBackground(isSystemInDarkMode:Boolean): Color {
        if (isSystemInDarkMode){
            return backgroundDark
        }else{
            return background
        }
    }

    fun getBackgroundSecondary(isSystemInDarkMode:Boolean): Color {
        if (isSystemInDarkMode){
            return backgroundSecondaryDark
        }else{
            return backgroundSecondary
        }
    }

    fun getBackgroundThird(isSystemInDarkMode:Boolean): Color {
        if (isSystemInDarkMode){
            return backgroundThirdDark
        }else{
            return backgroundThird
        }
    }

    fun getText(isSystemInDarkMode:Boolean): Color {
        if (isSystemInDarkMode){
            return textDark
        }else{
            return text
        }
    }

    fun getTextSecondary(isSystemInDarkMode:Boolean): Color {
        if (isSystemInDarkMode){
            return text_secondaryDark
        }else{
            return text_secondary
        }
    }
    fun getButton(isSystemInDarkMode:Boolean): Color {
        if (isSystemInDarkMode){
            return buttonDark
        }else{
            return button
        }
    }
    fun getButtonSecondary(isSystemInDarkMode:Boolean): Color {
        if (isSystemInDarkMode){
            return button_secondaryDark
        }else{
            return button_secondary
        }
    }
    fun getDrawableMenu(isSystemInDarkMode:Boolean): Color {
        if (isSystemInDarkMode){
            return drawable_menuDark
        }else{
            return drawable_menu
        }
    }
    fun getDrawableBar(isSystemInDarkMode:Boolean): Color {
        if (isSystemInDarkMode){
            return drawable_barDark
        }else{
            return drawable_bar
        }
    }

    fun getPurple(isSystemInDarkMode:Boolean): Color {
        if (isSystemInDarkMode){
            return purple
        }else{
            return purpleDark
        }
    }

}