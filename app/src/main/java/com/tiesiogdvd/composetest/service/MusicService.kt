package com.tiesiogdvd.composetest.service

import android.app.Application
import android.app.Notification
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder
import androidx.media3.session.*
import androidx.media3.session.MediaStyleNotificationHelper.MediaStyle
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.tiesiogdvd.composetest.MusicApplication.Companion.CHANNEL_ID_1
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.data.PreferencesManager
import com.tiesiogdvd.composetest.data.RepeatMode
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.random.Random

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
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private lateinit var  notificationBuilderCompat: NotificationCompat.Builder

    private lateinit var activityIntent: PendingIntent

    private lateinit var mediaLibrarySession: MediaLibrarySession

    private lateinit var playerNotificationManager: CustomPlayerNotificationManager

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
            if(preferencesManager.getCurrentShuffleOrder()){
                exoPlayer.shuffleModeEnabled = true
                exoPlayer.setShuffleOrder(ShuffleOrder.DefaultShuffleOrder(exoPlayer.mediaItemCount,preferencesManager.getCurrentShuffleSeed().toLong()))
                val shuffleOrder = ShuffleOrder.DefaultShuffleOrder(exoPlayer.mediaItemCount,preferencesManager.getCurrentShuffleSeed().toLong())
            }
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
            musicSource.currentSongIndex = session.player.currentMediaItemIndex
            if(currentSong!=null){
                serviceScope.launch(Dispatchers.IO) {
                    println("updating song")
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


    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        val newMediaMetadata = mediaMetadata.buildUpon()
            .setArtworkData(getPlaceholderArtwork(), MediaMetadata.PICTURE_TYPE_FRONT_COVER) // Replace the artwork with your placeholder
            .build()

      //  this.onMediaMetadataChanged(newMediaMetadata)

    }


    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        super.onUpdateNotification(session, startInForegroundRequired)
    }


    private fun getPlaceholderArtwork(): ByteArray {
            return ByteArray(0)
    }

    override fun onCreate() {
        super.onCreate()

        activityIntent = packageManager.getLaunchIntentForPackage(packageName).let {
            //.let{} passes activityIntent as it parameter to this PendingIntent
            PendingIntent.getActivity(this,0,it,PendingIntent.FLAG_IMMUTABLE)
        } //Gives intent that leads to the activity

        mediaLibrarySession = MediaLibrarySession.Builder(this, exoPlayer, librarySessionCallback).setSessionActivity(activityIntent).build()
        exoPlayer.addListener(this)

        playerNotificationManager = CustomPlayerNotificationManager(
            context = this,
            notificationId = 1,
            channelId = CHANNEL_ID_1,
            customMediaDescriptionAdapter = CustomMediaDescriptionAdapter(this,activityIntent),
            notificationListener = this
        )

      /*  playerNotificationManager = PlayerNotificationManager.Builder(this, 1, CHANNEL_ID_1)
            .setNotificationListener(this)
            .setChannelImportance(IMPORTANCE_HIGH)
            .setMediaDescriptionAdapter(
                object:MediaDescriptionAdapter{
                    override fun getCurrentContentTitle(player: Player): CharSequence {
                        return "aasd"
                    }

                    override fun createCurrentContentIntent(player: Player): PendingIntent {
                        return activityIntent
                    }

                    override fun getCurrentContentText(player: Player): CharSequence {
                        return "aasd"
                    }

                    override fun getCurrentLargeIcon(
                        player: Player,
                        callback: PlayerNotificationManager.BitmapCallback
                    ): Bitmap? {

                        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
                            eraseColor(Color.TRANSPARENT)
                        }
                    }
                }
            )
            .setPlayActionIconResourceId(R.drawable.ic_action_play)
            .setPauseActionIconResourceId(R.drawable.ic_action_pause)
            .setPreviousActionIconResourceId(R.drawable.ic_action_previous)
            .setNextActionIconResourceId(R.drawable.ic_action_next)
            .build()*/

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