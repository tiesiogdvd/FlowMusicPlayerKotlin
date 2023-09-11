package com.tiesiogdvd.composetest.ui.musicPlayer

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.data.settings.SettingsManager
import com.tiesiogdvd.composetest.service.MusicSource
import com.tiesiogdvd.composetest.service.ServiceConnector
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import linc.com.amplituda.*
import javax.inject.Inject

@OptIn(ExperimentalFoundationApi::class, ExperimentalCoroutinesApi::class)
@UnstableApi @HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val serviceConnector: ServiceConnector,
    private val musicSource: MusicSource,
    private val settingsManager: SettingsManager,
    private val preferencesManager: PreferencesManager,
    private val musicDao: MusicDao

    ):ViewModel(){
    val currentPosition = MutableStateFlow(0)
    val isPlaying = serviceConnector.playbackState

    val currentSource = serviceConnector.currentSource

    val songsList = musicSource.sourcePlaylist

    val shuffleList = serviceConnector.curShuffleList

    val curSongIndex = serviceConnector.curSongIndex


    val playerScreenSettings = settingsManager.playerSettingsFlow

    val shuffleStatus = serviceConnector.shuffleStatus

    val repeatMode = serviceConnector.repeatMode

    val playlistMenuEnabled = mutableStateOf(false)

    val isSongInFavorites = mutableStateOf(false)


    val curSongLyrics = serviceConnector.currentSongLyrics
    val curLyricsIndex = serviceConnector.currentResponseIndex
    val totalLyricsCount = serviceConnector.currentHitsNumber


    @OptIn(ExperimentalFoundationApi::class)
    var pagerState = PagerState(initialPage = curSongIndex.value?: indexFromMusicSource()?:0, initialPageOffsetFraction = 0f)

    val sourceSettings = preferencesManager.currentPlayModeFlow

    var bitmap = serviceConnector.bitmap

    var songDelayJob: Job? = null
    private val songDelayScope = CoroutineScope(Dispatchers.IO)

    init {
        viewModelScope.launch {
            println("collector")
            if(!musicDao.playlistExists("Favorites")){
                musicDao.insertPlaylist(Playlist("Favorites", isHidden = true))
            }

            currentSource.collect {curSong ->
                if(curSong!=null){
                    isSongInFavorites.value = musicDao.songExistsInPlaylist(songId = curSong.id, playlistId = musicDao.getPlaylist("Favorites").id)
                }
            }
        }
    }


    fun toggleRepeatMode(){
        serviceConnector.toggleRepeatModes()
    }
    fun togglePlaylistMenu(){
        if(playlistMenuEnabled.value == true){
            playlistMenuEnabled.value = false
        }else{
            playlistMenuEnabled.value = true
        }
    }

    fun toggleFavorite(song: Song){
        viewModelScope.launch {
            val playlist = musicDao.getPlaylist("Favorites")
            if(musicDao.songExistsInPlaylist(songId = song.id, playlistId = playlist.id)){
                musicDao.removeSongFromPlaylist(playlistId = playlist.id, song = song)
            }else{
                musicDao.insertSongToPlaylist(song = song, playlistId = playlist.id)
                Log.d("colector", "inserting ${song.songName}")
            }
        }
    }

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

    fun loadLyricsIndex(index: Int){
        serviceConnector.loadLyricsIndex(index)
    }
}