package com.tiesiogdvd.composetest.ui.musicPlayer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.service.ServiceConnector
import com.tiesiogdvd.composetest.util.convertListToArray
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import linc.com.amplituda.*
import javax.inject.Inject

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val serviceConnector: ServiceConnector,
    preferencesManager: PreferencesManager,
    private val musicDao: MusicDao,
    val context:Application

    ):ViewModel(){
    val currentSong = serviceConnector.currentSong
    val currentPosition = MutableStateFlow(0)
    val isPlaying = serviceConnector.playbackState
    val controller = serviceConnector.controller

    var amplituda = MutableStateFlow<IntArray?>(null)
    var songDuration = MutableStateFlow<Long>(0)

    val sourcePreferencesFlow = preferencesManager.currentSongFlow
    val currentSource = sourcePreferencesFlow.flatMapLatest {
        musicDao.getSongFromId(it.currentSongID)!!
    }

    val amplitudaProcess = Amplituda(context)

    val amplitudaProgressListener = object: AmplitudaProgressListener() {
        override fun onProgress(operation: ProgressOperation?, progress: Int) {}
        override fun onStartProgress() { super.onStartProgress() }
        override fun onStopProgress() { super.onStopProgress()
        }
    }

    init {
        currentSource.asLiveData().observeForever{
            viewModelScope.launch{
                amplituda.value=null
                if(it!=null && it.songName!=null){
                    getAmplituda(it)
                }
            }
        }
    }

    suspend fun getAmplituda(song: Song) = withContext(Dispatchers.Default){
        val amplituda = amplitudaProcess.processAudio(song.songPath, Compress.withParams(Compress.AVERAGE,1), Cache.withParams(Cache.REUSE), amplitudaProgressListener).get().amplitudesAsList()
        if(currentSource.first().songName==song.songName){
            this@MusicPlayerViewModel.amplituda.value = null
            this@MusicPlayerViewModel.amplituda.value = convertListToArray(amplituda)
            this@MusicPlayerViewModel.songDuration.value = song.length
        }
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