package com.tiesiogdvd.composetest.ui.library

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.Song
import com.tiesiogdvd.service.ServiceConnector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryPlaylistViewModel @Inject constructor(
    val musicDao: MusicDao,
    val application: Application,
    val preferencesManager: PreferencesManager,
    private val serviceConnector: ServiceConnector,
): ViewModel(){

    val playlistsWithSongs = musicDao.getPlaylistWithSongs(0)

    fun addPlaylist(playlist: Playlist){
        viewModelScope.launch {
            println("Adding Playlist")
            musicDao.insertPlaylist(playlist = playlist)
        }
    }

    fun removeSong(song: Song){
        viewModelScope.launch {
            musicDao.removeSong(song)
        }
    }

    fun getPlaylistWithSongs(playlist: Playlist):Flow<List<Song>>{
        return musicDao.getPlaylistSongs(playlistId = playlist.id)
    }

    fun onSongSelected(song: Song){
        viewModelScope.launch {
            preferencesManager.updateCurrentSongID(song.id)

        }
        serviceConnector.controller?.seekTo(song.id-1,0)
        serviceConnector.controller?.prepare()
        serviceConnector.controller?.play()
    }


}