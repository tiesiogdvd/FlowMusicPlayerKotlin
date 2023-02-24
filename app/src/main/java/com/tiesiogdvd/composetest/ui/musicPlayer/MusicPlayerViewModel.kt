package com.tiesiogdvd.composetest.ui.musicPlayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.service.ServiceConnector
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val serviceConnector: ServiceConnector,
    preferencesManager: PreferencesManager,
    private val musicDao: MusicDao

    ):ViewModel(){
    val currentSong = serviceConnector.currentSong
    val currentSongFlow = currentSong.asFlow()
    val isPlaying = serviceConnector.playbackState
    val controller = serviceConnector.controller

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

    fun playNext(){
        serviceConnector.controller?.seekToNext()
    }

    fun playPrev(){
        serviceConnector.controller?.seekToPrevious()
    }

}