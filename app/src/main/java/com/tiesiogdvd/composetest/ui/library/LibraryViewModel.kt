package com.tiesiogdvd.composetest.ui.library

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.service.ServiceConnector
import com.tiesiogdvd.composetest.ui.libraryPlaylist.BitmapLoader
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.composetest.util.SongDataGetMusicInfo
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.PlaylistWithSongs
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    val musicDao: MusicDao,
    val application: Application,
    val songDataGetMusicInfo: SongDataGetMusicInfo,
    val preferencesManager: PreferencesManager,
    val connector: ServiceConnector
): ViewModel(){
    val playlistsWithSongs = musicDao.getPlaylistsWithSongs()
    val backgroundBitmap = MutableStateFlow<ImageBitmap?>(null)

    val currentSource = preferencesManager.currentSourceFlow.asLiveData()
    val currentPlayingPlaylist = MutableStateFlow(-1)

    val isPlaying = connector.playbackState

    val currentScroll = MutableStateFlow(0f)


    lateinit var playlistAllSongs: Playlist

    init {
      viewModelScope.launch {
          songDataGetMusicInfo.getMusicInfo(musicDao,application)
          playlistAllSongs = musicDao.getPlaylist("All Songs")
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
            if(musicDao.getSong(playlistId = playlistWithSongs.playlist.id, songPath = playlistWithSongs.playlist.bitmapSource)!=null){
               // job = coroutineScope.launch {
                 //   bitmap = MusicDataMetadata.getBitmap(playlistWithSongs.playlist.bitmapSource)
               // }
                val waitForBitmap = CoroutineScope(Dispatchers.Default).async {
                    //bitmap = MusicDataMetadata.getBitmap(playlistWithSongs.playlist.bitmapSource)
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