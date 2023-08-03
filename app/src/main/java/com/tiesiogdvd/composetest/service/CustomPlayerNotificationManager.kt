package com.tiesiogdvd.composetest.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager

import com.tiesiogdvd.composetest.R

@UnstableApi
class CustomPlayerNotificationManager(
    private val context: Context,
    channelId: String,
    notificationId: Int,
    private val customMediaDescriptionAdapter: CustomMediaDescriptionAdapter,
    notificationListener: NotificationListener? = null,
    customActionReceiver: CustomActionReceiver? = null,
    smallIconResourceId:Int = R.drawable.ic_group_23,
    playActionIconResourceId:Int = R.drawable.ic_action_play,
    pauseActionIconResourceId:Int = R.drawable.ic_action_pause,
    stopActionIconResourceId:Int = androidx.media3.ui.R.drawable.exo_notification_stop,
    rewindActionIconResourceId:Int = androidx.media3.ui.R.drawable.exo_notification_rewind,
    fastForwardActionIconResourceId:Int = androidx.media3.ui.R.drawable.exo_icon_fastforward,
    previousActionIconResourceId:Int = R.drawable.ic_action_previous,
    nextActionIconResourceId:Int = R.drawable.ic_action_next,
    groupKey:String? = null


    ) : PlayerNotificationManager(
    context,
    channelId,
    notificationId,
    customMediaDescriptionAdapter,
    notificationListener,
    customActionReceiver,
    smallIconResourceId,
    playActionIconResourceId,
    pauseActionIconResourceId,
    stopActionIconResourceId,
    rewindActionIconResourceId,
    fastForwardActionIconResourceId,
    previousActionIconResourceId,
    nextActionIconResourceId,
    groupKey
) {


    override fun createNotification(
        player: Player,
        builder: NotificationCompat.Builder?,
        ongoing: Boolean,
        largeIcon: Bitmap?
    ): NotificationCompat.Builder? {

        builder?.setSilent(true)
       /* builder?.setLargeIcon(
            BitmapFactory.decodeResource(context.resources,customMediaDescriptionAdapter.getPlaceholderIcon())
        )*/
        return super.createNotification(player, builder, ongoing, largeIcon)
    }


}