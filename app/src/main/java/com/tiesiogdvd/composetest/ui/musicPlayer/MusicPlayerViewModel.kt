package com.tiesiogdvd.composetest.ui.musicPlayer

import android.app.Application
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.data.settings.SettingsManager
import com.tiesiogdvd.composetest.service.MusicSource
import com.tiesiogdvd.composetest.service.ServiceConnector
import com.tiesiogdvd.composetest.ui.libraryPlaylist.BitmapLoader
import com.tiesiogdvd.composetest.util.AudioAmplitudes
import com.tiesiogdvd.composetest.util.convertListToArray
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.Provides
import dagger.hilt.android.lifecycle.HiltViewModel

import fetchLyrics4
import fetchLyricsWithKtor
import fetchLyricsWithKtor2
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import linc.com.amplituda.*
import linc.com.amplituda.exceptions.AmplitudaException
import okhttp3.internal.wait
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalFoundationApi::class)
@UnstableApi @HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val serviceConnector: ServiceConnector,
    private val musicSource: MusicSource,
    private val settingsManager: SettingsManager,
    private val preferencesManager: PreferencesManager,

    ):ViewModel(){
    val currentPosition = MutableStateFlow(0)
    val isPlaying = serviceConnector.playbackState

    val currentSource = serviceConnector.currentSource

    val songsList = musicSource.sourcePlaylist

    val songsList2 = serviceConnector.curSongList

    val shuffleList = serviceConnector.curShuffleList

    val curSongIndex = serviceConnector.curSongIndex

    val curSongLyrics = serviceConnector.currentSongLyrics

    val playerScreenSettings = settingsManager.playerSettingsFlow

    val shuffleStatus = serviceConnector.shuffleStatus

    @OptIn(ExperimentalFoundationApi::class)
    var pagerState = PagerState(initialPage = curSongIndex.value?: indexFromMusicSource()?:0, initialPageOffsetFraction = 0f)

    val sourceSettings = preferencesManager.currentPlayModeFlow

    var bitmap = serviceConnector.bitmap

    var songDelayJob: Job? = null
    private val songDelayScope = CoroutineScope(Dispatchers.IO)
    fun indexFromMusicSource():Int? = musicSource.currentSongIndex

    fun seekToPosition(position: Long){
        serviceConnector.seekTo(position)
    }
    fun getSongIndex(song:Song?):Int? = musicSource.itemIndexById(song?.id)


    fun changePlaybackState(){
        serviceConnector.changePlaybackState()
    }

    fun playNext(){
        serviceConnector.playNext()
    }

    fun playPrev(){
        serviceConnector.playPrev()
    }

    fun playNextDelay(millis: Long){
        songDelayJob?.cancel()
        songDelayJob = songDelayScope.launch {
            delay(millis)
            withContext(Dispatchers.Main){
                serviceConnector.playNext()
            }
        }
    }
    fun playPrevDelay(millis: Long){
        songDelayJob?.cancel()
        songDelayJob = songDelayScope.launch {
            delay(millis)
            withContext(Dispatchers.Main){
                serviceConnector.playPrev()
                //serviceConnector.controller.getshu
            }

        }
    }

    fun playIndexDelay(millis: Long){
        songDelayJob?.cancel()
        songDelayJob = songDelayScope.launch {
            delay(millis)
            withContext(Dispatchers.Main){
                if(curSongIndex.value!=null){
                 //   serviceConnector.controller?.seekTo(curSongIndex.value!!, 0)
                }
            }
        }
    }

    fun playPage(index:Int, song: Song){
        if(shuffleStatus.value == false){
            if (pagerState.currentPage != curSongIndex.value || getSongIndex(song) != curSongIndex.value) {
                if(index!=curSongIndex.value){
                    serviceConnector.controller?.seekTo(index, 0)
                }
            }
        }else{
            if (pagerState.currentPage != shuffleList.value.indexOf(curSongIndex.value)  || shuffleList.value.indexOf(getSongIndex(song)) != shuffleList.value.indexOf(curSongIndex.value) ) {
                serviceConnector.controller?.seekTo(shuffleList.value.get(index), 0)
            }
        }
    }
    fun getPlaybackState():Int = serviceConnector.controller?.playbackState?: ExoPlayer.STATE_IDLE

    fun getCurrentPosition():Long = serviceConnector.getPosition()


}