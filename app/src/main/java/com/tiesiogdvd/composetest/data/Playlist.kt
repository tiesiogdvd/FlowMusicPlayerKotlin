package com.tiesiogdvd.playlistssongstest.data

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.DateFormat


@Entity(tableName = "playlist_table", indices = [Index(value = ["playlistName"], unique = true)])
@Parcelize
data class Playlist(
    val playlistName: String,
    val created:Long = System.currentTimeMillis(),
    val updated:Long = System.currentTimeMillis(),
    val bitmapSource:String? = null,
    val isHidden: Boolean = false,
    @PrimaryKey(autoGenerate = true) val id:Int = 0
) : Parcelable{
    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)
}