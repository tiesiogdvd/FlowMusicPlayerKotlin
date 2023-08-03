package com.tiesiogdvd.composetest.ui.musicPlayer

import android.app.Application
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.service.ServiceConnector
import com.tiesiogdvd.composetest.ui.libraryPlaylist.BitmapLoader
import com.tiesiogdvd.composetest.util.convertListToArray
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import linc.com.amplituda.*
import java.io.File
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class AmplitudaViewModel @Inject constructor(
    preferencesManager: PreferencesManager,
    private val musicDao: MusicDao,
    val context: Application

): ViewModel() {
    var amplituda = MutableStateFlow<IntArray?>(null)
    var songDuration = MutableStateFlow<Long>(0)
    val sourcePreferencesFlow = preferencesManager.currentSongFlow
    val currentSource = sourcePreferencesFlow.flatMapLatest {
        musicDao.getSongFromId(it.currentSongID)!!
    }

    val amplitudaProcess = Amplituda(context)

    val amplitudaProgressListener = object : AmplitudaProgressListener() {
        override fun onProgress(operation: ProgressOperation?, progress: Int) {}
        override fun onStartProgress() {
            super.onStartProgress()
        }

        override fun onStopProgress() {
            super.onStopProgress()
        }
    }



    init {
        currentSource.asLiveData().observeForever {
            viewModelScope.launch {
                amplituda.value = null
                if (it != null && it.songName != null) {
                    getAmplituda(it)
                }
            }
        }
    }


    suspend fun getAmplituda(song: Song) = withContext(Dispatchers.Default) {
        delay(1000)
        if (currentSource.first().songName == song.songName) {
            val amplituda = amplitudaProcess.processAudio(File(song.songPath), Compress.withParams(Compress.AVERAGE, 1), Cache.withParams(Cache.REUSE), amplitudaProgressListener).get().amplitudesAsList()

            this@AmplitudaViewModel.amplituda.value = null
            this@AmplitudaViewModel.songDuration.value = song.length
            this@AmplitudaViewModel.amplituda.value = convertListToArray(amplituda)
        }
    }
}