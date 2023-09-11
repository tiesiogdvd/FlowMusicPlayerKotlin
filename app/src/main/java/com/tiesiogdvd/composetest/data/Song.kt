package com.tiesiogdvd.playlistssongstest.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.DateFormat

@Entity(tableName = "songs_table", indices = [Index(value = ["songPath"], unique = true)])
@Parcelize
data class Song(
    val songName: String? = null,
    val songPath: String,
    val folder: String? = null,
    val length: Long = 0,
    val isHidden: Boolean = false,

    val hasBitmap: Boolean? = null,
    val isBitmapCached: Boolean? = null,

    val songArtist: String? = null,
    val albumArtist: String? = null,
    val album: String? = null,
    val genre: String? = null,
    val trackNumber: String? = null,
    val year: String? = null,


    val created:Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id:Int = 0
) : Parcelable{
    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)
}

