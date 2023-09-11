package com.tiesiogdvd.playlistssongstest.data

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.tiesiogdvd.composetest.data.PlaylistSongCrossRef
import com.tiesiogdvd.composetest.data.PlaylistSortOrder
import com.tiesiogdvd.composetest.data.SongSortOrder
import com.tiesiogdvd.composetest.data.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongsListToPlaylistQuery(songs:List<PlaylistSongCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongs(songs:List<Song>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSong(song:Song)

    suspend fun insertSongsToPlaylist(songs:ArrayList<Song>, playlistId: Int){
        val m_songs = songs.distinctBy{it.songPath}
        if(m_songs.size!=0){
            val list = arrayListOf<PlaylistSongCrossRef>()
            songs.forEach{song ->
                list.add(PlaylistSongCrossRef(playlistId = playlistId, songId = song.id))
            }
            println("Inserting SONGS QUERY")
            songs.forEach{ println(it.songName)}
            insertSongsListToPlaylistQuery(list)
            updatePlaylistModifiedDate(playlistId = playlistId)
        }
    }

    suspend fun insertSongsToPlaylist(playlistId: Int,songIds:List<Long>){
        val m_songs = songIds.distinctBy{it}
        if(m_songs.size!=0){
            val list = arrayListOf<PlaylistSongCrossRef>()
            songIds.forEach{ songId ->
                list.add(PlaylistSongCrossRef(playlistId = playlistId, songId = songId.toInt()))
            }
            insertSongsListToPlaylistQuery(list)
            updatePlaylistModifiedDate(playlistId = playlistId)
        }
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToPlaylistQuery(playlistSongCrossRef: PlaylistSongCrossRef)

    suspend fun insertSongToPlaylist(song:Song, playlistId: Int){
        insertSongToPlaylistQuery(PlaylistSongCrossRef(playlistId = playlistId, songId = song.id))
        updatePlaylistModifiedDate(playlistId = playlistId)
    }



    @Query("SELECT * FROM songs_table WHERE id = :songId")
    fun getSongFromId(songId:Int): Flow<Song>?

    @Query("SELECT * FROM songs_table WHERE songPath = :songPath")
    suspend fun getSong(songPath: String): Song?

    @Query("SELECT EXISTS (SELECT * FROM songs_table WHERE songPath = :songPath)")
    suspend fun songExists(songPath:String): Boolean?

    @Query("SELECT EXISTS(SELECT * FROM playlist_song_cross_reference WHERE songId =:songId AND playlistId =:playlistId)")
    suspend fun songExistsInPlaylist(songId:Int, playlistId:Int): Boolean


    @Query("UPDATE songs_table SET hasBitmap = :hasImage WHERE id = :songId")
    suspend fun setSongHasImage(songId: Int, hasImage: Boolean)


    suspend fun removeSongsFromPlaylist(playlistId: Int, songs: ArrayList<Song>){
        if (songs.isNotEmpty()) {
            for(song in songs){
                removeSongFromPlaylistQuery(playlistId, song.id)
            }
            updatePlaylistModifiedDate(playlistId)
        }
    }
    suspend fun removeSongFromPlaylist(playlistId: Int,song: Song){
        removeSongFromPlaylistQuery(playlistId = playlistId, songId = song.id)
        updatePlaylistModifiedDate(playlistId = playlistId)
    }
    @Query("DELETE FROM playlist_song_cross_reference WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylistQuery(playlistId: Int, songId: Int)


    suspend fun setAreSongsHidden(songs: ArrayList<Song>, isHidden: Boolean, playlistId: Int) {
        if (songs.isNotEmpty()) {
            val songIds = songs.map { it.id }
            setAreSongsHiddenQuery(songIds, isHidden)
            updatePlaylistModifiedDate(playlistId = playlistId)
        }
    }
    @Query("UPDATE songs_table SET isHidden = :isHidden WHERE id IN (:songIds)")
    suspend fun setAreSongsHiddenQuery(songIds: List<Int>, isHidden: Boolean)


    @Query("SELECT * FROM playlist_table WHERE isHidden= :showHidden")
    fun getPlaylists(showHidden: Boolean): Flow<List<Playlist>>

    @Query("SELECT * FROM playlist_table WHERE playlistName = :playlistName")
    suspend fun getPlaylist(playlistName: String): Playlist

    @Query("SELECT * FROM playlist_table WHERE id = :playlistId")
    fun getPlaylist(playlistId: Int): Flow<Playlist?>



    @Query("""
    SELECT songs_table.* 
    FROM songs_table 
    JOIN playlist_song_cross_reference ON songs_table.id = playlist_song_cross_reference.songId 
    WHERE playlist_song_cross_reference.playlistId = :playlistId
    """)
    fun getPlaylistSongs(playlistId: Int?): Flow<List<Song>>


    @Query("SELECT EXISTS(SELECT * FROM playlist_table WHERE playlistName =:playlistName)")
    suspend fun playlistExists(playlistName: String): Boolean

    @Query("UPDATE playlist_table SET bitmapSource = :source WHERE id = :playlistId")
    suspend fun setPlaylistBitmapSource(playlistId: Int, source:String?)

    @Query("UPDATE playlist_table SET isHidden = :isHidden WHERE id IN (:playlistIds)")
    fun setArePlaylistsHidden(playlistIds:List<Int>, isHidden: Boolean)


    @Delete
    suspend fun removePlaylist(playlist: Playlist)
    @Delete
    suspend fun removePlaylists(playlists: ArrayList<Playlist>)

    @Transaction
    @Query("SELECT * FROM playlist_table ORDER BY updated DESC, playlistName")
    fun getPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>

    @Transaction
    @Query("SELECT * FROM playlist_table where id = :playlistId")
    fun getPlaylistWithSongs(playlistId:Int): Flow<PlaylistWithSongs>


    // Fetch songs with dynamic order
    @RawQuery(observedEntities = [Song::class])
    fun fetchPlaylistAndSongsRawQuery(query: SupportSQLiteQuery): Flow<List<Song>>

    fun getPlaylistWithSongs(playlistId: Int, showHidden: Boolean?=false, query:String?="", songSortOrder: SongSortOrder, sortOrder: SortOrder): Flow<PlaylistWithSongs> = flow {
        val songOrderColumn = songSortOrder.key
        val orderDirection = sortOrder.key
        val rawQuery = """
            SELECT songs_table.* 
            FROM songs_table 
            JOIN playlist_song_cross_reference ON songs_table.id = playlist_song_cross_reference.songId
            WHERE playlist_song_cross_reference.playlistId = ? 
            AND (songName LIKE ? OR songArtist LIKE ?) 
            AND isHidden = ?
            ORDER BY $songOrderColumn $orderDirection, songName
        """
        val sqlQuery = SimpleSQLiteQuery(rawQuery, arrayOf(playlistId,"%$query%", "%$query%", if(showHidden == true) 1 else 0))
        val playlist = getPlaylistWithSongs(playlistId).first()
        val sortedSongs = fetchPlaylistAndSongsRawQuery(sqlQuery).first()

        emit(PlaylistWithSongs(playlist.playlist, sortedSongs))
    }

    @Transaction
    @Query("SELECT * FROM playlist_table WHERE isHidden= :isHidden ORDER BY updated DESC, playlistName")
    fun getPlaylistsWithSongs(isHidden: Boolean): Flow<List<PlaylistWithSongs>>


    @Query("UPDATE playlist_table SET updated=:time WHERE id= :playlistId")
    suspend fun updatePlaylistModifiedDate(playlistId: Int, time:Long = System.currentTimeMillis())

    @RawQuery(observedEntities = [Playlist::class])
    fun fetchPlaylistsWithSongsWithRawQuery(query: SupportSQLiteQuery): Flow<List<PlaylistWithSongs>>
    fun getPlaylistsWithSongs(showHidden: Boolean?=false, query: String? = "", playlistSortOrder: PlaylistSortOrder? = PlaylistSortOrder.BY_DATE_UPDATED, sortOrder: SortOrder? = SortOrder.Z_A): Flow<List<PlaylistWithSongs>> {
        val columnName = playlistSortOrder?.key ?: PlaylistSortOrder.BY_DATE_UPDATED
        val orderDirection = sortOrder?.key ?: SortOrder.Z_A
        val rawQuery = """
            SELECT * 
            FROM playlist_table 
            WHERE playlistName LIKE ?
            AND isHidden = ? 
            ORDER BY $columnName $orderDirection, playlistName
        """
        val sqlQuery = SimpleSQLiteQuery(rawQuery, arrayOf("%$query%", if(showHidden == true) 1 else 0))
        return fetchPlaylistsWithSongsWithRawQuery(sqlQuery)
    }


    @RawQuery(observedEntities = [Song::class])
    fun fetchSongsWithRawQuery(query: SupportSQLiteQuery): Flow<List<Song>>
    suspend fun getPlaylistSongs(playlistId: Int, query: String, songSortOrder: SongSortOrder, sortOrder: SortOrder, showHidden:Boolean = false): Flow<List<Song>> {
        val columnName = songSortOrder.key
        val orderDirection = sortOrder.key
        val rawQuery = """
            SELECT songs_table.* 
            FROM songs_table 
            JOIN playlist_song_cross_reference ON songs_table.id = playlist_song_cross_reference.songId
            WHERE playlist_song_cross_reference.playlistId = ? 
            AND (songName LIKE ? OR songArtist LIKE ?) 
            AND isHidden = ?
            ORDER BY $columnName $orderDirection, songName
        """
        val sqlQuery = SimpleSQLiteQuery(rawQuery, arrayOf(playlistId, "%$query%", "%$query%", if(showHidden) 1 else 0))
        return fetchSongsWithRawQuery(sqlQuery)
    }

}