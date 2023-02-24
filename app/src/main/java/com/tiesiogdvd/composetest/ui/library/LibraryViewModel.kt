package com.tiesiogdvd.composetest.ui.library

import android.app.Application
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.composetest.util.SongDataGetMusicInfo
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.PlaylistWithSongs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    val musicDao: MusicDao,
    val application: Application,
    val songDataGetMusicInfo: SongDataGetMusicInfo
): ViewModel(){
    val playlistsWithSongs = musicDao.getPlaylistsWithSongs()

    lateinit var playlistAllSongs: Playlist

    init {
      viewModelScope.launch {
          songDataGetMusicInfo.getMusicInfo(musicDao,application)
          playlistAllSongs = musicDao.getPlaylist("All Songs")
      }
    }

    suspend fun getPlaylistBitmap(playlistWithSongs: PlaylistWithSongs):ImageBitmap?{
        var bitmap: ImageBitmap? = null
        if (playlistWithSongs.playlist.bitmapSource==null){
            println("Searching")
            for(song in playlistWithSongs.songs){
                bitmap = MusicDataMetadata.getBitmap(song.songPath)
                if(bitmap!=null){
                    viewModelScope.launch {
                        musicDao.setPlaylistBitmapSource(playlistWithSongs.playlist.id,song.songPath)
                    }
                    break
                }
            }
        }else{
            if(musicDao.getSong(playlistId = playlistWithSongs.playlist.id, songPath = playlistWithSongs.playlist.bitmapSource)!=null){
                bitmap = MusicDataMetadata.getBitmap(playlistWithSongs.playlist.bitmapSource)
            }else{
                musicDao.setPlaylistBitmapSource(playlistWithSongs.playlist.id,null)
            }
        }
        return bitmap
    }
}