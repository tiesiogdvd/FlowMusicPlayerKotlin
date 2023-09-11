package com.tiesiogdvd.composetest.ui.library

import android.app.Application
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.data.SongSortOrder
import com.tiesiogdvd.composetest.data.SortOrder
import com.tiesiogdvd.composetest.service.ServiceConnector
import com.tiesiogdvd.composetest.util.BitmapLoader
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.composetest.util.SongDataGetMusicInfo
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.PlaylistWithSongs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    val musicDao: MusicDao,
    val application: Application,
    val songDataGetMusicInfo: SongDataGetMusicInfo,
    val preferencesManager: PreferencesManager,
    val connector: ServiceConnector
): ViewModel(){
    val playlistsWithSongs = musicDao.getPlaylistsWithSongs(showHidden = false)
    val backgroundBitmap = MutableStateFlow<ImageBitmap?>(null)

    val currentSource = preferencesManager.currentSourceFlow.asLiveData()
    val currentPlayingPlaylist = MutableStateFlow(-1)

    val isPlaying = connector.playbackState

    val currentScroll = MutableStateFlow(0f)


    //lateinit var playlistAllSongs: Playlist
    val playlistAllSongs = MutableStateFlow<Playlist?>(null)
    val playlistFavorites = MutableStateFlow<Playlist?>(null)
    init {
      viewModelScope.launch {
          playlistAllSongs.value = musicDao.getPlaylist("All Songs")
          playlistFavorites.value = musicDao.getPlaylist("Favorites")
          songDataGetMusicInfo.getMusicInfo(musicDao,application)

      }

       currentSource.observeForever{
           currentPlayingPlaylist.value = it.currentSource
       }
    }

    fun setBitmap(bitmap:ImageBitmap?){
        println("bitmap set")
        backgroundBitmap.value = bitmap
    }



    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    suspend fun getPlaylistBitmap(playlistWithSongs: PlaylistWithSongs):ImageBitmap?{
        var bitmap: ImageBitmap? = null
        if (playlistWithSongs.playlist.bitmapSource==null){
            println("Searching")
            for(song in playlistWithSongs.songs){
                bitmap = MusicDataMetadata.getBitmap(song.songPath)
                if(bitmap!=null){
                    viewModelScope.launch(Dispatchers.Default) {
                        musicDao.setPlaylistBitmapSource(playlistWithSongs.playlist.id,song.songPath)
                    }
                    break
                }
            }
        }else{
            if(musicDao.getSong(songPath = playlistWithSongs.playlist.bitmapSource)!=null){
                val waitForBitmap = CoroutineScope(Dispatchers.Default).async {
                    bitmap = BitmapLoader.loadBitmapAsync(coroutineScope, playlistWithSongs.playlist.bitmapSource).await()
                    return@async bitmap
                }
                waitForBitmap.await()
                return bitmap
            }else{
                musicDao.setPlaylistBitmapSource(playlistWithSongs.playlist.id,null)
            }
        }

        return bitmap
    }
}