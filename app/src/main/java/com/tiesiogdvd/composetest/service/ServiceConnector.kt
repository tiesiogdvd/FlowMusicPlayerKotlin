package com.tiesiogdvd.composetest.service

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@UnstableApi class ServiceConnector(
    context:Context
) {
    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected

    private val _currentSong = MutableLiveData<MediaMetadata?>()
    val currentSong:  LiveData<MediaMetadata?>  = _currentSong

    private val _playbackState = MutableStateFlow<Boolean?>(false)
    val playbackState: StateFlow<Boolean?> = _playbackState

    val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))

    private var controllerFuture: ListenableFuture<MediaController>
    val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null


    init {
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({setController()}, MoreExecutors.directExecutor())

    }


    private fun setController() {
        val controller = this.controller?: return
        controller.addListener(object: Player.Listener{
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.update { isPlaying }
                super.onIsPlayingChanged(isPlaying)
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
               super.onMediaMetadataChanged(mediaMetadata)
                println("METADATA CHANGED")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                println("PLAYBACK STATE")
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                println(error)
            }

        })

        println("controller set")
        println(controller.currentMediaItem?.mediaId)
        controller.addListener(object: Player.Listener{})
    }

}