package com.tiesiogdvd.service

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import androidx.media3.ui.PlayerNotificationManager
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import com.tiesiogdvd.composetest.MusicApplication
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MusicServiceSecondImpl: MediaLibraryService(), Player.Listener {
    @Inject
    lateinit var dataSourceFactory: DefaultDataSource.Factory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var musicDao: MusicDao

    @Inject
    lateinit var musicSource: MusicSource


    private val librarySessionCallback = CustomMediaLibrarySessionCallback()

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var  notificationBuilderCompat: NotificationCompat.Builder

    private lateinit var activityIntent: PendingIntent

    private lateinit var mediaLibrarySession: MediaLibrarySession

    private lateinit var mediaSession:android.media.session.MediaSession

    private var curPlayingSong: MediaMetadata? = null

    private lateinit var playerNotificationManager: PlayerNotificationManager

    var isServiceRunning = false

    var currentSong : MediaItem? = null


    override fun onEvents(player: Player, events: Player.Events) {
        super.onEvents(player, events)

    }

    override fun onUpdateNotification(session: MediaSession) {
        //super.onUpdateNotification(session)

        if(session.player.currentMediaItem!=currentSong){
            //updateNotification(session)
            currentSong = session.player.currentMediaItem
        }
        playerNotificationManager.setPlayer(exoPlayer)
        playerNotificationManager.setUsePlayPauseActions(true)
       // startForeground(1,notificationBuilderCompat.build())
    }


    fun updateNotification(session: MediaSession): MediaNotification {
        println("UPDATING NOTIFICATION")
        notificationBuilderCompat = NotificationCompat.Builder(this, MusicApplication.CHANNEL_ID_1)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setSmallIcon(R.drawable.ic_group_23)
            .setContentIntent(activityIntent)
            .setStyle(
                MediaStyleNotificationHelper.MediaStyle(session).setShowActionsInCompactView(
                    Player.COMMAND_SEEK_TO_PREVIOUS,
                    Player.COMMAND_PLAY_PAUSE,
                    Player.COMMAND_SEEK_TO_NEXT
                ))

        return MediaNotification(1, notificationBuilderCompat.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {



        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()


        serviceScope.launch {
            musicSource.getFlow()
            musicSource.fetchSource()
        }

        activityIntent = packageManager.getLaunchIntentForPackage(packageName).let {
            //.let{} passes activityIntent as it parameter to this PendingIntent
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        } //Gives intent that leads to the activity

        mediaLibrarySession = MediaLibrarySession.Builder(this, exoPlayer, librarySessionCallback).build()
        updateNotification(mediaLibrarySession)

       // mediaSession = android.media.session.MediaSession(this, SERVICE_TAG)
        //mediaSession.setpl
        updateNotification(mediaLibrarySession)

        //mediaSession.



       // playerNotificationManager = PlayerNotificationManager.Builder(
       //     this,
       //     1,
      //      MusicApplication.CHANNEL_ID_1
       // )
       //     .build()
        playerNotificationManager.setMediaSessionToken(mediaLibrarySession.sessionCompatToken as android.support.v4.media.session.MediaSessionCompat.Token)



        /*setMediaNotificationProvider(object: MediaNotification.Provider{
            override fun createNotification(
                mediaSession: MediaSession,
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {
                return updateNotification(mediaSession)
            }
            override fun handleCustomCommand(
                session: MediaSession,
                action: String,
                extras: Bundle
            ): Boolean = false
        })*/



        musicSource.fetchState.observeForever({
            if(it){
                println("OBSERVER ACTIVATED")
                exoPlayer.setMediaSource(musicSource.asMediaSource(dataSourceFactory))
                exoPlayer.prepare()
                exoPlayer.seekTo(10,0)
                exoPlayer.playWhenReady = true
            }
        })
        println("CREATED SERVICE")
    }


    private fun preparePlayer(
        songs: List<MediaItem>,
        itemToPlay: MediaItem?,
        playNow: Boolean
    ) {
        val curSongIndex = if(curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.setMediaSource(musicSource.asMediaSource(dataSourceFactory))
        exoPlayer.prepare()
        exoPlayer.seekTo(curSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
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




    private inner class CustomMediaLibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            return super.onConnect(session, controller)

        }

        override fun onDisconnected(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ) {
            super.onDisconnected(session, controller)
        }

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            super.onPostConnect(session, controller)
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return super.onGetLibraryRoot(session, browser, params)
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return super.onGetChildren(session, browser, parentId, page, pageSize, params)
        }

        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {
            return when (playerCommand) {
                Player.COMMAND_PLAY_PAUSE -> {
                    if (session.player.playWhenReady) {
                        session.player.pause()
                    } else {
                        session.player.play()
                    }
                    SessionResult.RESULT_INFO_SKIPPED
                }
                else -> super.onPlayerCommandRequest(session, controller, playerCommand)
            }
        }
    }


}