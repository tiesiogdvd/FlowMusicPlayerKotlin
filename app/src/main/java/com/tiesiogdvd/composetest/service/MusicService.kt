package com.tiesiogdvd.composetest.service

import android.app.Application
import android.app.Notification
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.session.*
import androidx.media3.session.MediaStyleNotificationHelper.MediaStyle
import androidx.media3.ui.PlayerNotificationManager
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import com.tiesiogdvd.composetest.MusicApplication.Companion.CHANNEL_ID_1
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.data.RepeatMode
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_TAG = "MusicService"

@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
@UnstableApi @AndroidEntryPoint
class MusicService: MediaLibraryService(), Player.Listener, PlayerNotificationManager.NotificationListener {


    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var musicDao: MusicDao

    @Inject
    lateinit var musicSource: MusicSource

    @Inject
    lateinit var context: Application

    private val librarySessionCallback = CustomMediaLibrarySessionCallback()

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var  notificationBuilderCompat: NotificationCompat.Builder

    private lateinit var activityIntent: PendingIntent

    private lateinit var mediaLibrarySession: MediaLibrarySession

    private lateinit var playerNotificationManager: PlayerNotificationManager

    var isServiceRunning = false

    var currentSong : MediaItem? = null

    override fun onNotificationCancelled(
        notificationId: Int,
        dismissedByUser: Boolean
    ) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
        if(dismissedByUser){
            stopForeground(STOP_FOREGROUND_REMOVE)
            isServiceRunning = false
            stopSelf()
        }else{
            exoPlayer.playWhenReady = false
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        println("ITEM TRANSITION")
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        if(ongoing && !isServiceRunning){
            println(exoPlayer.mediaItemCount)
            if(exoPlayer.mediaItemCount>0 && exoPlayer.currentMediaItem?.mediaMetadata?.isPlayable == true){
                ContextCompat.startForegroundService(this@MusicService, Intent(applicationContext, this::class.java))
                notification.fullScreenIntent = activityIntent
                notification.contentIntent= activityIntent
                startForeground(1, notification)
                isServiceRunning = true
            }
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        println("onEvent")
        super.onEvents(player, events)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        println("onIsPlayingChanged")
        println("PositionEXO " + exoPlayer.currentMediaItem?.mediaId + "  " +  exoPlayer.currentMediaItemIndex)
        super.onIsPlayingChanged(isPlaying)
    }

    override fun onTracksChanged(tracks: Tracks) {
        println("onTracksChanged")
        super.onTracksChanged(tracks)
    }

    override fun onUpdateNotification(session: MediaSession) {
        //super.onUpdateNotification(session)
        if(session.player.currentMediaItem!=currentSong){
            //updateNotification(session)
            currentSong = session.player.currentMediaItem
            musicSource.currentSong = session.player.currentMediaItem
            if(currentSong!=null){
                serviceScope.launch {
                    preferencesManager.updateCurrentSongID(currentSong!!.mediaId.toInt())
                }
            }
        }
        playerNotificationManager.setPlayer(exoPlayer)
       // startForeground(1,notificationBuilderCompat.build())
    }


    fun updateNotification(session: MediaSession): MediaNotification {
        println("UPDATING NOTIFICATION")
        notificationBuilderCompat = NotificationCompat.Builder(this, CHANNEL_ID_1)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setSmallIcon(R.drawable.ic_group_23)
            .setOngoing(true)
            .setContentIntent(activityIntent)
            .setStyle(MediaStyle(session).setShowActionsInCompactView(Player.COMMAND_SEEK_TO_PREVIOUS,Player.COMMAND_PLAY_PAUSE,Player.COMMAND_SEEK_TO_NEXT))
        return MediaNotification(1, notificationBuilderCompat.build())
    }

    override fun onCreate() {
        super.onCreate()
        activityIntent = packageManager.getLaunchIntentForPackage(packageName).let {
            //.let{} passes activityIntent as it parameter to this PendingIntent
            PendingIntent.getActivity(this,0,it,PendingIntent.FLAG_IMMUTABLE)
        } //Gives intent that leads to the activity

        mediaLibrarySession = MediaLibrarySession.Builder(this, exoPlayer, librarySessionCallback).setSessionActivity(activityIntent).build()
        exoPlayer.addListener(this)
        playerNotificationManager = PlayerNotificationManager.Builder(this, 1, CHANNEL_ID_1)
            .setNotificationListener(this)
            .setChannelImportance(IMPORTANCE_HIGH)
            .setPlayActionIconResourceId(R.drawable.ic_action_play)
            .setPauseActionIconResourceId(R.drawable.ic_action_pause)
            .setPreviousActionIconResourceId(R.drawable.ic_action_previous)
            .setNextActionIconResourceId(R.drawable.ic_action_next)
            .build()
        playerNotificationManager.setUseStopAction(true)
        playerNotificationManager.setMediaSessionToken(mediaLibrarySession.sessionCompatToken)
        println("CREATED SERVICE")
    }



    override fun onDestroy() {
        exoPlayer.release()
        mediaLibrarySession.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    private inner class CustomMediaLibrarySessionCallback : MediaLibrarySession.Callback{
    }
}