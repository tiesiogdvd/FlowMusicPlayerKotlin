package com.tiesiogdvd.playlistssongstest.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaylistWithSongs(
    @Embedded val playlist:Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId"
    )
    val songs: List<Song>
): Parcelable
