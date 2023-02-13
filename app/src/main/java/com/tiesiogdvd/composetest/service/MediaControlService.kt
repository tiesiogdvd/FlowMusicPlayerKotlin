package com.tiesiogdvd.composetest.service

class MediaControlService

/*
import android.app.Notification
import android.app.Service
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaController
import android.media.session.MediaSession
import android.net.Uri
import android.os.Binder
import android.view.KeyEvent
import java.io.File

class MediaControlService : Service(), AudioManager.OnAudioFocusChangeListener {
    var mp: MediaPlayer? = MediaPlayer()
    var musicPath: String? = "none"
    var mediaSessionTag = "mediaSession"
    var isMediaReady = false
    var sourcePlaylist: Playlist? = null
    var sourcePaths: ArrayList<String>? = null
    var sourceIndex: Int? = null
    var notification: Notification? = null
    var notificationBuilder: Notification.Builder? = null
    var mediaSession: MediaSession? = null
    var mediaController: MediaController? = null
    var notificationManager: NotificationManager? = null
    var audioManager: AudioManager? = null
    var focusRequest: AudioFocusRequest? = null
    var musicDataMetadata: MusicDataMetadata? = null
    var metadataBuilder: MediaMetadata.Builder? = null
    var playbackstateBuilder: PlaybackState.Builder? = null
    var notificationIntent: Intent? = null
    var prevIntent: Intent? = null
    var playIntent: Intent? = null
    var nextIntent: Intent? = null
    var pendingIntent: PendingIntent? = null
    var actionPrev: Notification.Action? = null
    var actionPlay: Notification.Action? = null
    var actionNext: Notification.Action? = null
    var shuffleEnabled: Boolean? = null
    var m_state = 0


    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN ->                 //resume playback
                if (mp != null && !mp.isPlaying()) {
                    mp.start()
                    setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }
            AudioManager.AUDIOFOCUS_LOSS ->                 //stop playback
                if (mp != null && mp.isPlaying()) {
                    mp.pause()
                    setPlaybackState(PlaybackStateCompat.STATE_STOPPED)
                }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->                 //pause playback
                if (mp != null && mp.isPlaying()) {
                    mp.pause()
                    setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->                 //lower volume
                if (mp != null && mp.isPlaying()) {
                    mp.setVolume(0.1f, 0.1f)
                }
        }
    }

    internal inner class MyServiceBinder : Binder() {
        val service: MediaControlService
            get() = this@MediaControlService
    }

    private val mBinder: IBinder = MyServiceBinder()
    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.getAction() != null) {
            if (intent.getAction() == ACTION_PREVIOUS) {
                playPrev()
            }
            if (intent.getAction() == ACTION_NEXT) {
                playNext()
            }
            if (intent.getAction() == ACTION_PLAY) {
                if (mp.isPlaying()) {
                    pauseMedia()
                    mediaSession!!.isActive = true
                } else {
                    playMedia()
                    mediaSession!!.isActive = true
                }
            }
        }
        return START_NOT_STICKY //START_... will change what action is taken when the service is killed  NOT_STICKY means the service will not be restarted
    }

    override fun onCreate() { //Could put the same things as in onStartCommand, but it will only create the notification once
        mediaSession = MediaSession(this, mediaSessionTag)
        mediaController = MediaController(this, mediaSession!!.sessionToken)
        mediaSession!!.setCallback(object : MediaSession.Callback() {
            override fun onPlay() {
                super.onPlay()
                println("Play")
                playMedia()
                setPlaybackState(PlaybackState.STATE_PLAYING)
            }

            override fun onPause() {
                super.onPause()
                println("Pause")
                pauseMedia()
                setPlaybackState(PlaybackState.STATE_PAUSED)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                println("Next")
                //setPlaybackState(PlaybackState.STATE_PLAYING);
                playNext()
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                println("Previous")
                //setPlaybackState(PlaybackState.STATE_PLAYING);
                playPrev()
            }

            override fun onStop() {
                super.onStop()
            }

            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                mediaSeekTo(pos.toInt())
            }

            override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
                val action: String = mediaButtonEvent.getAction()
                if (action != null) {
                    if (action == Intent.ACTION_MEDIA_BUTTON) {
                        val keyEvent =
                            mediaButtonEvent.getParcelableExtra<Parcelable>(Intent.EXTRA_KEY_EVENT) as KeyEvent
                        if (keyEvent != null) {
                            if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                                when (keyEvent.keyCode) {
                                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> if (mp.isPlaying()) {
                                        pauseMedia()
                                        mediaSession!!.isActive = true
                                    } else {
                                        playMedia()
                                        mediaSession!!.isActive = true
                                    }
                                    KeyEvent.KEYCODE_MEDIA_NEXT -> playNext()
                                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> playPrev()
                                }
                            }
                        }
                    }
                }
                return super.onMediaButtonEvent(mediaButtonEvent)
            }
        })
        mediaSession!!.isActive = true
        audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager
        notificationBuilder = Notification.Builder(this, CHANNEL_ID_1)
        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        setIntents()
        setActions()
        buildNotification()
        super.onCreate()
    }

    private fun setIntents() {
        notificationIntent = Intent(
            this,
            MainActivity::class.java
        ) //This intent is used to open Activity on click of notification
        pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        prevIntent = Intent(this, MediaControlService::class.java)
        prevIntent.setAction(ACTION_PREVIOUS)
        playIntent = Intent(this, MediaControlService::class.java)
        playIntent.setAction(ACTION_PLAY)
        nextIntent = Intent(this, MediaControlService::class.java)
        nextIntent.setAction(ACTION_NEXT)
    }

    private fun setActions() {
        actionPrev = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_action_previous),
            "Previous",
            PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_IMMUTABLE)
        ).build()
        actionPlay = if (mp.isPlaying()) {
            Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.ic_action_pause),
                "Pause",
                PendingIntent.getService(this, 1, playIntent, PendingIntent.FLAG_IMMUTABLE)
            ).build()
        } else {
            Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.ic_action_play),
                "Pause",
                PendingIntent.getService(this, 1, playIntent, PendingIntent.FLAG_IMMUTABLE)
            ).build()
        }
        actionNext = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_action_next),
            "Play",
            PendingIntent.getService(this, 1, nextIntent, PendingIntent.FLAG_IMMUTABLE)
        ).build()
    }

    private fun setPlaybackState(state: Int) {
        if (state == PlaybackState.STATE_PLAYING) {
            setPlayStatus(true)
            m_state = state
        }
        if (state == PlaybackState.STATE_PAUSED || state == PlaybackState.STATE_STOPPED) {
            setPlayStatus(false)
            m_state = state
        }
        playbackstateBuilder = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                        PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_STOP or
                        PlaybackState.ACTION_SEEK_TO
            )
        playbackstateBuilder.setState(state, mp.getCurrentPosition().toLong(), 1f)
        mediaSession!!.setPlaybackState(playbackstateBuilder.build())
    }

    override fun onDestroy() {
        mp.stop()
        mp.release()
        mp = null
        super.onDestroy()
    }

    fun setSources(playlistSource: Playlist?, index: Int?) {
        sourcePlaylist = playlistSource
        sourceIndex = index
    }

    fun playMedia(path: String?, source: Playlist?, index: Int?) {
        sourcePlaylist = source
        sourceIndex = index
        actionPlay()
    }

    fun playMedia(path: String) {
        //sourceIndex = null;
        sourcePlaylist = null
        actionPlay(path)
    }

    fun playPrev() {
        if (sourcePlaylist != null && sourcePlaylist.getLength() !== 0 && sourceIndex != null && sourceIndex != -1 && sourceIndex != 0) {
            val tempPath: String = sourcePlaylist.getSong(sourceIndex!! - 1).getPath()
            sourceIndex = sourceIndex!! - 1
            actionPlay()
            setPlaybackState(PlaybackState.STATE_PLAYING)
            //Settings.setLastSongIndex(sourceIndex);
            broadcast("isPlaying", true, "SOURCECHANGED")
        } else if (sourcePaths != null && sourcePaths!!.size != 0 && sourceIndex != null && sourceIndex != 0) {
            sourceIndex = sourceIndex!! - 1
            val path = sourcePaths!![sourceIndex!!]
            actionPlay(path)
            setPlaybackState(PlaybackState.STATE_PLAYING)
            broadcast("isPlaying", true, "SOURCECHANGED")
        }
    }

    fun playNext() {
        if (sourcePlaylist != null && sourcePlaylist.getLength() !== 0 && sourceIndex != null && sourceIndex != -1 && sourceIndex !== sourcePlaylist.getLength() - 1) {
            if (sourcePlaylist.getSong(sourceIndex!! + 1) != null) {
                val tempPath: String = sourcePlaylist.getSong(sourceIndex!! + 1).getPath()
                sourceIndex = sourceIndex!! + 1
                actionPlay()
                setPlaybackState(PlaybackState.STATE_PLAYING)
                broadcast("isPlaying", true, "SOURCECHANGED")
            }
        } else if (sourcePaths != null && sourcePaths!!.size != 0 && sourceIndex != null && sourceIndex != -1 && sourceIndex != sourcePaths!!.size - 1) {
            sourceIndex = sourceIndex!! + 1
            val path = sourcePaths!![sourceIndex!!]
            actionPlay(path)
            setPlaybackState(PlaybackState.STATE_PLAYING)
            broadcast("isPlaying", true, "SOURCECHANGED")
        }
    }

    fun playMedia() {
        val requestAudioFocusResult = requestFocus()
        if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mp.start()
            setPlaybackState(PlaybackState.STATE_PLAYING)
            println("PLAY MEDIA")
            broadcast("isPlaying", true, "PLAYPAUSE")
        }
    }

    fun pauseMedia() {
        mp.pause()
        setPlaybackState(PlaybackState.STATE_PAUSED)
        println("PAUSE MEDIA")
        broadcast("isPlaying", false, "PLAYPAUSE")
        if (focusRequest != null) {
            audioManager.abandonAudioFocusRequest(focusRequest)
        }
    }

    fun requestFocus(): Int {
        val audioAttributes =
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(this)
            .build()
        return audioManager.requestAudioFocus(focusRequest)
    }

    fun mediaSeekTo(position: Int) {
        //finished = false;
        mp.seekTo(position)
        if (playbackstateBuilder != null) {
            playbackstateBuilder.setState(m_state, position.toLong(), 1f)
            mediaSession!!.setPlaybackState(playbackstateBuilder.build())
        }
    }

    val isMediaPlaying: Boolean
        get() = if (mp != null) {
            mp.isPlaying()
        } else {
            false
        }

    fun mediaDuration(): Int {
        if (isMediaReady) {
            println(mp.getDuration())
            return mp.getDuration()
        }
        return 0
    }

    fun mediaRemaining(): Int {
        return if (isMediaReady) {
            //System.out.println(mp.getCurrentPosition());
            mp.getCurrentPosition()
        } else 0
    }

    fun mediaCompletionListener() {
        mp.setOnCompletionListener(OnCompletionListener { mp: MediaPlayer ->
            mp.seekTo(0)
            broadcast("isFinished", true, "STATE")
        })
    }

    private fun setMediaSessionMetadata(path: String) {
        musicDataMetadata = MusicDataMetadata()
        metadataBuilder = MediaMetadata.Builder()
        musicDataMetadata.setAllData(path)
        if (musicDataMetadata.bitmap != null) {
            metadataBuilder!!.putBitmap(
                MediaMetadata.METADATA_KEY_ALBUM_ART,
                musicDataMetadata.bitmap
            )
        } else {
            metadataBuilder!!.putBitmap(
                MediaMetadata.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(
                    resources, R.drawable.ic_group_23_image_6
                )
            )
        }
        metadataBuilder!!.putString(MediaMetadata.METADATA_KEY_TITLE, musicDataMetadata.title)
        metadataBuilder!!.putString(MediaMetadata.METADATA_KEY_ARTIST, musicDataMetadata.artist)
        metadataBuilder!!.putLong(
            MediaMetadata.METADATA_KEY_DURATION,
            musicDataMetadata.length.toLong()
        )
        mediaSession!!.setMetadata(metadataBuilder!!.build())
        notificationBuilder!!.setProgress(1000, 10000, false)
        notificationManager.notify(1, notificationBuilder!!.build())
    }

    private fun setPlayStatus(playStatus: Boolean) {
        actionPlay = if (playStatus) {
            Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.ic_action_pause),
                "Pause",
                PendingIntent.getService(this, 1, playIntent, PendingIntent.FLAG_IMMUTABLE)
            ).build()
        } else {
            Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.ic_action_play),
                "Pause",
                PendingIntent.getService(this, 1, playIntent, PendingIntent.FLAG_IMMUTABLE)
            ).build()
        }
        notificationBuilder!!.setActions(actionPrev, actionPlay, actionNext)
        notificationManager.notify(1, notificationBuilder!!.build())
    }

    private fun actionPlay(path: String) {
        musicPath = path
        if (File(path).exists()) {
            mp.reset()
            mp = MediaPlayer.create(this, Uri.parse(path))
            mp.start()
            mp.setLooping(false)
            isMediaReady = true
            musicPath = path
            val sharedPreferences: SharedPreferences =
                getSharedPreferences("lastMusic", MODE_PRIVATE)
            sharedPreferences.edit().putString("lastSongSource", null).apply()
            sharedPreferences.edit().putString("lastSongPath", path).apply()
            //buildNotification(path);
            setMediaSessionMetadata(path)
        }
    }

    private fun actionPlay() {
        val requestAudioFocusResult = requestFocus()
        if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (sourcePlaylist.getLength() !== 0) {
                val source: String = sourcePlaylist.getSongPath(sourceIndex)
                musicPath = source
                if (musicPath != null) {
                    mp.reset()
                    mp = MediaPlayer.create(this, Uri.parse(source))
                    mp.start()
                    mp.setLooping(false)
                    isMediaReady = true
                    musicPath = source
                    val sharedPreferences: SharedPreferences =
                        getSharedPreferences("lastMusic", MODE_PRIVATE)
                    sharedPreferences.edit().putString("lastSongPath", source).apply()
                    mediaCompletionListener()
                    //buildNotification(source);
                    setMediaSessionMetadata(source)
                }
            } else {
                actionPlay(Settings.getLastSongPath())
            }
        }
    }

    private fun buildNotification() {
        notificationBuilder = Notification.Builder(this, CHANNEL_ID_1)
        notificationBuilder!!
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_group_23)
            .setShowWhen(false)
            .setContentIntent(pendingIntent) //Enables clicking notification
            .setOngoing(true)
            .addAction(actionPrev)
            .addAction(actionPlay)
            .addAction(actionNext)
            .setStyle(
                Notification.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession!!.sessionToken)
            ) //this will start the service
        notification = notificationBuilder!!.build()
        startForeground(
            1,
            notificationBuilder!!.build()
        ) //but this is important to keep in running in the foreground
    }

    private fun broadcast(key: String, payload: Boolean, action: String) {
        val intent = Intent()
        intent.putExtra(key, payload)
        intent.setAction(action)
        intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        sendBroadcast(intent)
    }
}*/