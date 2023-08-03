package com.tiesiogdvd.composetest

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class MusicApplication : Application(){

    companion object {
        const val CHANNEL_ID_1 = "mediaPlayerChannel1"
        const val CHANNEL_ID_2 = "mediaPlayerChannel2"
        const val ACTION_PREVIOUS = "PREVIOUS"
        const val ACTION_NEXT = "NEXT"
        const val ACTION_PLAY = "PLAY"
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }


        CoroutineScope(Dispatchers.Default).launch {
            Python.start(AndroidPlatform(applicationContext))
        }
    }


    private fun createNotificationChannel() {
        println("CREATING")
        val serviceChannel1 = NotificationChannel(CHANNEL_ID_1, "MP Channel 1", NotificationManager.IMPORTANCE_HIGH)


        serviceChannel1.description = "MP Channel 1 description"
        serviceChannel1.setSound(null, null)
        serviceChannel1.vibrationPattern = null
        serviceChannel1.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        val serviceChannel2 = NotificationChannel(CHANNEL_ID_2, "MP Channel 2", NotificationManager.IMPORTANCE_DEFAULT)
        serviceChannel2.description = "MP Channel 2 description"
        serviceChannel2.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        serviceChannel2.setSound(null, null)
        serviceChannel2.vibrationPattern = null

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel1)
        manager.createNotificationChannel(serviceChannel2)
    }


}