package com.tiesiogdvd.playlistssongstest.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import java.text.DateFormat

@Parcelize
data class PlaylistWithSongs(
    @Embedded val playlist:Playlist,
    @Relation(
        parentColumn = "id", //In use compares the playlist name in playlist and playlist name in song
        entityColumn = "playlistId"
    )
    val songs: List<Song>
): Parcelable {
}
