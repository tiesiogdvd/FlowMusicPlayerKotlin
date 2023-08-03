package com.tiesiogdvd.composetest.service

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import com.tiesiogdvd.composetest.R

@UnstableApi class CustomMediaDescriptionAdapter(
    private val context: Context,
    private val activityIntent: PendingIntent
) : PlayerNotificationManager.MediaDescriptionAdapter {

    fun getPlaceholderIcon(): Int {
        return R.drawable.ic_group_23_image_6 // Replace this with your actual placeholder icon
    }

    override fun getCurrentContentTitle(player: Player): CharSequence {
        return "aaa"
    }


    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return activityIntent
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return "aaa"
    }

    /*override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {

       // return BitmapFactory.decodeResource(context.resources, R.drawable.ic_action_previous)

        //return ResourcesCompat.getDrawable(context.resources, R.drawable.ic_group_23_image_6, null)?.toBitmap(500,500, Bitmap.Config.ARGB_8888 )

        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLACK)
        }
    }*/

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inScaled = false // Prevent automatic scaling
            outWidth = 200
            outHeight = 200
        }

        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_group_23, options)
        callback.onBitmap(bitmap)
        return bitmap
    }
}