package com.tiesiogdvd.composetest.ui.bottomNavBar

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class NavbarController @Inject constructor(){
    val navbarEnabled = MutableStateFlow(true)
    val songbarEnabled = MutableStateFlow(true)

    fun changeNavbarState(state:Boolean){
        navbarEnabled.update { state }
    }
    fun changeSongbarState(state:Boolean){
        songbarEnabled.update { state }
    }
}