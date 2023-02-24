package com.tiesiogdvd.composetest.ui.bottomNavBar

import androidx.lifecycle.ViewModel
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.composetest.service.ServiceConnector
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class NavbarViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val musicDao: MusicDao,
    private val serviceConnector: ServiceConnector,
    private val navbarController: NavbarController,
) : ViewModel() {
    val currentSong = serviceConnector.currentSong
    val isPlaying = serviceConnector.playbackState
    val isNavbarVisible = navbarController.navbarEnabled
    val isSongbarVisible = navbarController.songbarEnabled


    val sourcePreferencesFlow = preferencesManager.currentSongFlow
    val currentSource = sourcePreferencesFlow.flatMapLatest {
        musicDao.getSongFromId(it.currentSongID)!!
    }


    fun changePlaybackState(){
        when(serviceConnector.controller?.isPlaying){
            true -> {serviceConnector.controller?.pause()}
            false -> {serviceConnector.controller?.play()}
            else -> {serviceConnector.controller?.prepare()}
        }
    }

}