package com.tiesiogdvd.composetest.ui.selectionBar

import androidx.lifecycle.ViewModel
import com.tiesiogdvd.composetest.ui.bottomNavBar.NavbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SelectionBarViewModel @Inject constructor(
    navbarController: NavbarController
) : ViewModel() {
    val isNavbarVisible = navbarController.navbarEnabled
    val isSongbarVisible = navbarController.songbarEnabled
    val isSelectionBarVisible = MutableStateFlow(false)

    fun isSelectionBarSelected(boolean: Boolean){
        isNavbarVisible.update { !boolean }
        isSelectionBarVisible.update { boolean }
    }
}