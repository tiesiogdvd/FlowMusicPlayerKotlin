package com.tiesiogdvd.composetest.shared.currentSongsData

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.service.ServiceConnector
import com.tiesiogdvd.composetest.ui.libraryPlaylist.BitmapLoader
import com.tiesiogdvd.composetest.util.convertListToArray
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import linc.com.amplituda.Cache
import linc.com.amplituda.Compress
import javax.inject.Inject

@HiltViewModel
class CurrentSongsViewModel @Inject constructor(
    private val serviceConnector: ServiceConnector,
    preferencesManager: PreferencesManager,
    private val musicDao: MusicDao,
    val context: Application
): ViewModel(){

    val sourcePreferencesFlow = preferencesManager.currentSongFlow
    val currentSource = sourcePreferencesFlow.flatMapLatest { musicDao.getSongFromId(it.currentSongID)!! }

    init {
        currentSource.asLiveData().observeForever{
            loadSongBitmap(it)
        }
    }

    var bitmap = mutableStateOf<ImageBitmap?>(null)

    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    private fun loadSongBitmap(song:Song){
        val job = coroutineScope.launch {
            val result = BitmapLoader.loadBitmapAsync(coroutineScope, song.songPath,).await()
            bitmap.value = result
        }
    }
}