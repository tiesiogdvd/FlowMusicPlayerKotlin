package com.tiesiogdvd.composetest.data

import androidx.room.Entity
import androidx.room.ForeignKey
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.Song

@Entity(tableName = "playlist_song_cross_reference",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(entity = Playlist::class, parentColumns = ["id"], childColumns = ["playlistId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Song::class, parentColumns = ["id"], childColumns = ["songId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class PlaylistSongCrossRef(
    val playlistId: Int,
    val songId: Int
)
