package com.tiesiogdvd.composetest.ui.addToPlaylistDialog

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.PlaylistWithSongs
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddToPlaylistDialogViewModel @Inject constructor(
    val musicDao: MusicDao,
): ViewModel(){
    val playlists = musicDao.getPlaylistsWithSongs(false)
    var isAddPlaylistDialogEnabled = mutableStateOf(false)


    suspend fun isSongInPlaylist(playlist:Playlist,song: Song):Boolean{
        return musicDao.songExistsInPlaylist(song.songPath, playlist.id)
    }

    fun toggleAddPlaylistDialog() {
        isAddPlaylistDialogEnabled.value = !isAddPlaylistDialogEnabled.value
    }

    fun playlistExists(playlistName:String, playlistsWithSongs: List<PlaylistWithSongs>):Boolean{
        for(playlistWithSongs in playlistsWithSongs){
            if(playlistName==playlistWithSongs.playlist.playlistName){
                return true
            }
        }
        return false
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
            val playlistId =  musicDao.getPlaylist(playlistName).id
            addSongsToPlaylist(playlistId,songs)
        }
    }

    suspend fun addSongsToPlaylist(playlistId: Int, songs:Map<Int,Song>?){
        if(songs!=null){
            val songList:ArrayList<Song> = ArrayList()
            for(song in songs){
                val tempSong = Song(
                    songName = song.value.songName,
                    songPath = song.value.songPath,
                    folder = song.value.folder,
                    length = song.value.length,
                    isHidden = song.value.isHidden,
                    hasBitmap = song.value.hasBitmap,
                    isBitmapCached = song.value.isBitmapCached,
                    inFavorites = song.value.inFavorites,
                    inAllSongs = song.value.inAllSongs,
                    songArtist = song.value.songArtist,
                    albumArtist = song.value.albumArtist,
                    album = song.value.album,
                    genre = song.value.genre,
                    trackNumber = song.value.trackNumber,
                    year = song.value.year,
                    playlistId = playlistId)
                songList.add(tempSong)
            }
            musicDao.insertSongsToPlaylist(songList)
        }
    }

    suspend fun removeSongsFromPlaylist(playlistId: Int, songs: Map<Int, Song>?){
        if(songs!=null){
            val songList:ArrayList<Song> = ArrayList()
            for(song in songs){
                val tempSong = Song(
                    songName = song.value.songName,
                    songPath = song.value.songPath,
                    folder = song.value.folder,
                    length = song.value.length,
                    isHidden = song.value.isHidden,
                    hasBitmap = song.value.hasBitmap,
                    isBitmapCached = song.value.isBitmapCached,
                    inFavorites = song.value.inFavorites,
                    inAllSongs = song.value.inAllSongs,
                    songArtist = song.value.songArtist,
                    albumArtist = song.value.albumArtist,
                    album = song.value.album,
                    genre = song.value.genre,
                    trackNumber = song.value.trackNumber,
                    year = song.value.year,
                    playlistId = playlistId)
                songList.add(tempSong)
            }
            musicDao.removeSongs(songList)
        }
    }


}