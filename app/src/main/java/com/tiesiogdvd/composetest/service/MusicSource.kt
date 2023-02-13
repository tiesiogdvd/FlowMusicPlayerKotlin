package com.tiesiogdvd.composetest.service

import android.annotation.SuppressLint

import android.media.MediaMetadata.*
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.PICTURE_TYPE_FRONT_COVER
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@UnstableApi class MusicSource @Inject constructor(
    private val musicDao: MusicDao
){


    var songs = emptyList<MediaItem>()
    val source = MutableStateFlow("")
    val fetchState = MutableLiveData(false)


    var songsFlow: List<Song> = emptyList()


    suspend fun getFlow(){
        songsFlow = musicDao.getSongsNonFlow()
    }

    suspend fun fetchFlow(){
        songsFlow = musicDao.getSongsNonFlow()
    }

    fun fetchSource(){
        fetchState.postValue(false)
        println(songsFlow.size)
        println("STARTING FETCH")
        songs = songsFlow.map { song ->
            println(song.songPath)
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
        fetchState.postValue(true)
    }


    fun asMediaSource(dataSourceFactory: DefaultDataSource.Factory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            println(song.mediaMetadata.title)
            println(song.mediaMetadata.artist)
            println(song.mediaMetadata.title)
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(song)
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }
}