package com.tiesiogdvd.playlistssongstest.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Query("SELECT EXISTS(SELECT * FROM playlist_table WHERE playlistName =:playlistName)")
    suspend fun playlistExists(playlistName: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM songs_table WHERE songPath =:songPath AND playlistId =:playlistId)")
    suspend fun songExistsInPlaylist(songPath: String, playlistId:Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song, playlist: Playlist)

    @Query("SELECT * FROM songs_table")
    fun getSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs_table")
    suspend fun getSongsNonFlow(): List<Song>


    @Query("SELECT * FROM songs_table WHERE id = :songId")
    fun getSongFromId(songId:Int): Flow<Song>?


    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId")
    fun getPlaylistSongs(playlistId: Int): Flow<List<Song>>

    @Query("SELECT * FROM playlist_table")
    fun getPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlist_table WHERE playlistName = :playlistName")
    suspend fun getPlaylist(playlistName: String): Playlist

    @Delete
    suspend fun removePlaylist(playlist: Playlist)

    @Delete
    suspend fun removeSong(song: Song)

    @Transaction
    @Query("SELECT * FROM playlist_table")
    fun getPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>

    @Transaction
    @Query("SELECT * FROM playlist_table where id = :playlistId")
    fun getPlaylistWithSongs(playlistId:Int): Flow<PlaylistWithSongs>

    @Transaction
    @Query("SELECT * FROM playlist_table WHERE playlistName = :playlistName")
    fun getPlaylistsWithSongs(playlistName: String): Flow<List<PlaylistWithSongs>>


}