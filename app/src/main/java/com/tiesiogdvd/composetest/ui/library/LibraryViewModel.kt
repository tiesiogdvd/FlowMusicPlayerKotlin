package com.tiesiogdvd.composetest.ui.library

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tiesiogdvd.composetest.util.SongDataGetMusicInfo
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.PlaylistWithSongs
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    val musicDao: MusicDao,
    val application: Application,
    val songDataGetMusicInfo: SongDataGetMusicInfo
): ViewModel(){
    val playlists = musicDao.getPlaylists()
    val playlistsWithSongs = musicDao.getPlaylistsWithSongs()
   // init {
     //   viewModelScope.launch {
         //   songDataGetMusicInfo.getMusicInfo()
      //  }

  //  }
    init {
      viewModelScope.launch {
          songDataGetMusicInfo.getMusicInfo(musicDao,application)
      }
    }


    fun getSongsNumber(playlist: Playlist): Flow<List<Song>> {
        return musicDao.getPlaylistSongs(playlist.id)
    }


    fun removePlaylist(playlist: Playlist){
        viewModelScope.launch {
            musicDao.removePlaylist(playlist)
        }
    }

    fun addPlaylist(playlist: Playlist){
        viewModelScope.launch {
            println("Adding Playlist")
            musicDao.insertPlaylist(playlist = playlist)
        }
    }

}