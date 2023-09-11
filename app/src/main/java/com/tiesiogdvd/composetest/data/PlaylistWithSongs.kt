package com.tiesiogdvd.playlistssongstest.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.tiesiogdvd.composetest.data.PlaylistSongCrossRef
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,
    @Relation(
        entity = Song::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "songId"
        )
    )
    val songs: List<Song>
): Parcelable{
    fun containsSong(song: Song): Boolean {
        return songs.contains(song)
    }

    fun containsAllSongs(songsList: List<Song>): Boolean {
        return songs.containsAll(songsList)
    }

    fun containsSongsFromMap(songMap: Map<Int, Song>): Boolean {
        return songs.map { it.id }.containsAll(songMap.keys)
    }

}


object EmptyPlaylist{
    val playlistWithSongs = PlaylistWithSongs(playlist = Playlist("dummy", id = -1), songs = emptyList())
}