package com.tiesiogdvd.composetest.service

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@UnstableApi class MusicSource @Inject constructor(
    musicDao: MusicDao,
    private var dataSourceFactory: DefaultDataSource.Factory,
    preferencesManager: PreferencesManager
){

    val currentPlaylistID = preferencesManager.currentSourceFlow
    val sourcePlaylist = currentPlaylistID.flatMapLatest {
        musicDao.getPlaylistSongs(it.currentSource, "", sortOrder = it.sortOrder, songSortOrder = it.songSortOrder)
    }
    var songs = emptyList<MediaItem>()
    val sourceLiveData = sourcePlaylist.asLiveData()
    val concatenatingSource = MutableLiveData(ConcatenatingMediaSource())

    var songToPlay:Song? = null

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    init {
        val observer = Observer<List<Song>> {
            serviceScope.launch {
                fetchSource()
            }
        }
        sourceLiveData.observeForever(observer)
    }


    suspend fun fetchSource() = withContext(Dispatchers.IO) {
        println("STARTING FETCH")
        songs = sourceLiveData.value!!.map { song ->
            MediaItem.Builder().setMediaId(song.id.toString()).setUri(song.songPath).setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.songName)
                    .setAlbumTitle(song.album)
                    .setAlbumArtist(song.albumArtist)
                    .setGenre(song.genre)
                    .setTrackNumber(song.trackNumber?.toInt())
                    .setReleaseYear(song.year?.toInt())
                    .setArtist(song.songArtist)
                    .setIsPlayable(true)
                    .build()
            ).build()
        }
        val concatenatingMediaSource = ConcatenatingMediaSource()
        //concatenatingMediaSource.clear()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(song)
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        concatenatingSource.postValue(concatenatingMediaSource)
    }

    fun itemIndexById(int: Int?):Int?{
        val index = sourceLiveData.value?.indexOfFirst{
            it.id == int
        }
        if(index==null){
            return null
        }else{
            return index
        }
    }

}