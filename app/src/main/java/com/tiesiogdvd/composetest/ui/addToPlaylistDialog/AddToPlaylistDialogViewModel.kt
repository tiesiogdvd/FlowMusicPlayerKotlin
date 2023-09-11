package com.tiesiogdvd.composetest.ui.addToPlaylistDialog

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.PlaylistWithSongs
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddToPlaylistDialogViewModel @Inject constructor(
    val musicDao: MusicDao,
): ViewModel(){
    val playlists = musicDao.getPlaylistsWithSongs(showHidden = false)
    var isAddPlaylistDialogEnabled = mutableStateOf(false)

    fun toggleAddPlaylistDialog() {
        isAddPlaylistDialogEnabled.value = !isAddPlaylistDialogEnabled.value
    }

    fun toggleAddStatus(playlistId:Int, songs:Map<Int,Song>?, isSelected: Boolean){
        viewModelScope.launch {
            if(!isSelected){
                addSongsToPlaylist(playlistId, songs)
            }else{
                removeSongsFromPlaylist(playlistId,songs)
            }
        }
    }

    fun addPlaylist(playlistName: String, songs:Map<Int,Song>?){
        println("ADDING PLAYLIST")
        viewModelScope.launch {
            musicDao.insertPlaylist(Playlist(playlistName))
            val playlist =  musicDao.getPlaylist(playlistName)
            addSongsToPlaylist(playlist.id,songs)
        }
    }

    suspend fun addSongsToPlaylist(playlistId: Int, songs:Map<Int,Song>?){
        if(songs!=null){
            val songList:ArrayList<Song> = ArrayList()
            for(song in songs){
                if(!musicDao.songExistsInPlaylist(song.value.id, playlistId)){
                    songList.add(song.value)
                }
            }
            musicDao.insertSongsToPlaylist(songs = songList, playlistId = playlistId)
        }
    }

    suspend fun removeSongsFromPlaylist(playlistId: Int, songs: Map<Int, Song>?){
        if(songs!=null){
            val songList:ArrayList<Song> = ArrayList()
            for (song in songs){
                songList.add(song.value)
            }
            musicDao.removeSongsFromPlaylist(playlistId = playlistId, songList)
        }
    }


}