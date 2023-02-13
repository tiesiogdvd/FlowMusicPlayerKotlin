package com.tiesiogdvd.composetest.ui

import android.media.session.PlaybackState
import android.os.Bundle
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaBrowser
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Song
import com.tiesiogdvd.service.ServiceConnector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val musicDao: MusicDao,
    private val serviceConnector: ServiceConnector,
) : ViewModel() {
    val currentSong = serviceConnector.currentSong


    fun playSource(){
       serviceConnector.controller?.seekToNext()
    }

    val sourcePreferencesFlow = preferencesManager.preferencesFlow
    val sourceFlow = sourcePreferencesFlow.flatMapLatest { musicDao.getSongFromId(it.currentSongID)!! }

    val currentSource = sourceFlow


    fun changePlaybackState(){
        when(serviceConnector.controller?.isPlaying){
            true -> {serviceConnector.controller?.pause()}
            false -> {serviceConnector.controller?.play()}
            else -> {serviceConnector.controller?.prepare()}
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

}