package com.tiesiogdvd.composetest.service


import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap

import android.graphics.drawable.Drawable

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerNotificationManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tiesiogdvd.composetest.MusicApplication.Companion.CHANNEL_ID_1
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import java.io.File

@UnstableApi class MusicPlayerNotification(
    private val context: Context,
    sessionToken: SessionToken,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newSongCallback: () -> Unit
) {
    private val notificationManager: PlayerNotificationManager


    init {
        //val mediaController = MediaController(context, sessionToken)

        notificationManager = PlayerNotificationManager.Builder(context, 2, CHANNEL_ID_1)
            .setChannelDescriptionResourceId(R.string.notification_channel_description)
            .setChannelNameResourceId(R.string.notification_channel_name)
            //.setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .setSmallIconResourceId(R.drawable.ic_group_23)
            .build()
        //notificationManager.setMediaSessionToken(sessionToken)
        notificationManager.setUsePlayPauseActions(false)

    }

    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    @UnstableApi private inner class DescriptionAdapter(private val mediaController: MediaController) : PlayerNotificationManager.MediaDescriptionAdapter {
        private var currentSongTitle: CharSequence? = null
        private var currentSongSubtitle: CharSequence? = null
        private var cachedBitmap: Bitmap? = null

        override fun getCurrentContentTitle(player: Player): CharSequence {
            if (currentSongTitle == null || currentSongTitle != mediaController.mediaMetadata.title) {
                currentSongTitle = mediaController.mediaMetadata.title
                newSongCallback()
            }
            return currentSongTitle!!
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence {
            if (currentSongSubtitle == null || currentSongSubtitle != mediaController.mediaMetadata.subtitle) {
                currentSongSubtitle = mediaController.mediaMetadata.subtitle
            }
            return currentSongSubtitle!!
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {

            return null
        }
    }

    private fun getBitmap(songPath: Uri): ImageBitmap? {
        return MusicDataMetadata.getBitmap(songPath.path)
    }
}