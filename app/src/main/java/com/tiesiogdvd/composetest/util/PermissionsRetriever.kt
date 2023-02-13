package com.tiesiogdvd.composetest.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionsRetriever {
    fun checkPermissions(context: Context): Int {

        val permissionRequest: MutableList<String> = ArrayList()


        val resultStorageGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val resultRecordAudioGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val resultInternetAccessGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED


        if (!resultStorageGranted) { permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE) }
        if (!resultRecordAudioGranted) { permissionRequest.add(Manifest.permission.RECORD_AUDIO) }
        if (!resultInternetAccessGranted) { permissionRequest.add(Manifest.permission.INTERNET) }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val resultMediaReadGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            val resultPostNotificationGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if(!resultMediaReadGranted){
                permissionRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if(!resultPostNotificationGranted){
                permissionRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (!permissionRequest.isEmpty()) { ActivityCompat.requestPermissions((context as Activity), permissionRequest.toTypedArray(), 111)
        }
        if (resultStorageGranted && resultRecordAudioGranted) { return 1 }
        if (resultStorageGranted) { return 2 }
        return if (resultRecordAudioGranted) { 3 } else 0
    }
}