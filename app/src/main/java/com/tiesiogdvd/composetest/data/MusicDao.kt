package com.tiesiogdvd.playlistssongstest.data

import androidx.room.*
import com.tiesiogdvd.composetest.data.PlaylistSortOrder
import com.tiesiogdvd.composetest.data.SongSortOrder
import com.tiesiogdvd.composetest.data.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongsListQuery(songs:List<Song>)
    suspend fun insertSongsList(songs:List<Song>){
        insertSongsListQuery(songs)
        if(songs.size!=0){updatePlaylistModifiedDate(songs.get(0).playlistId)}
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongQuery(song: Song){updatePlaylistModifiedDate(song.playlistId)}
    suspend fun insertSong(song:Song){
        insertSong(song)
        updatePlaylistModifiedDate(song.playlistId)
    }

    suspend fun insertSongToPlaylist(song:Song, playlistId: Int){
        val tempSong = song.copy(playlistId =  playlistId)
        insertSong(tempSong)
    }

    suspend fun insertSongsToPlaylist(songs:ArrayList<Song>){
        val m_songs = songs.distinctBy{it.songPath}
        insertSongsList(m_songs)

    }


    @Query("SELECT * FROM songs_table")
    fun getSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs_table WHERE id = :songId")
    fun getSongFromId(songId:Int): Flow<Song>?

    @Query("SELECT * FROM songs_table WHERE songPath = :songPath AND playlistId= :playlistId")
    suspend fun getSong(songPath:String, playlistId: Int): Song?

    @Query("SELECT EXISTS(SELECT * FROM songs_table WHERE songPath =:songPath AND playlistId =:playlistId)")
    suspend fun songExistsInPlaylist(songPath: String, playlistId:Int): Boolean

    @Query("UPDATE songs_table SET hasBitmap = :hasImage WHERE id = :songId")
    suspend fun setSongHasImage(songId: Int, hasImage: Boolean)

    suspend fun removeSongs(songs: ArrayList<Song>){
        if (songs.isNotEmpty()) {
            removeSongsQuery(songs)
            updatePlaylistModifiedDate(songs.get(0).playlistId)
        }
    }
    @Delete
    suspend fun removeSongsQuery(songs: ArrayList<Song>)

    suspend fun setAreSongsHidden(songs: ArrayList<Song>, isHidden: Boolean) {
        if (songs.isNotEmpty()) {
            val songIds = songs.map { it.id }
            setAreSongsHiddenQuery(songIds, isHidden)
            updatePlaylistModifiedDate(songs.get(0).playlistId)
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

    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId")
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

    @Transaction
    @Query("SELECT * FROM playlist_table WHERE isHidden= :isHidden ORDER BY updated DESC, playlistName")
    fun getPlaylistsWithSongs(isHidden: Boolean): Flow<List<PlaylistWithSongs>>


    fun getPlaylistsWithSongs(showHidden: Boolean, query: String, playlistSortOrder: PlaylistSortOrder, sortOrder: SortOrder):Flow<List<PlaylistWithSongs>> =
        when(sortOrder){
            SortOrder.A_Z -> {
                when (playlistSortOrder) {
                    PlaylistSortOrder.BY_NAME -> { getPlaylistsWithSongsByName(query = query, showHidden = showHidden) }
                    PlaylistSortOrder.BY_DATE_CREATED -> { getPlaylistsWithSongsByDateCreated(query = query, showHidden = showHidden) }
                    PlaylistSortOrder.BY_DATE_UPDATED -> { getPlaylistsWithSongsByDateUpdated(query = query, showHidden = showHidden) }
                    else -> { getPlaylistsWithSongsByName(query = query, showHidden = showHidden) }
                }
            }
            SortOrder.Z_A ->{
                when(playlistSortOrder){
                    PlaylistSortOrder.BY_NAME ->{getPlaylistsWithSongsByNameDesc(query = query, showHidden = showHidden)}
                    PlaylistSortOrder.BY_DATE_CREATED -> {getPlaylistsWithSongsByDateCreatedDesc(query = query, showHidden = showHidden)}
                    PlaylistSortOrder.BY_DATE_UPDATED -> {getPlaylistsWithSongsByDateUpdatedDesc(query = query, showHidden = showHidden)}
                    else -> {getPlaylistsWithSongsByNameDesc(query = query, showHidden = showHidden)}
                }
            }
        }

    @Transaction
    @Query("SELECT * FROM playlist_table WHERE playlistName LIKE '%' ||:query || '%' AND isHidden = :showHidden ORDER BY playlistName ASC, playlistName")
    fun getPlaylistsWithSongsByName(showHidden: Boolean, query: String):Flow<List<PlaylistWithSongs>>

    @Transaction
    @Query("SELECT * FROM playlist_table WHERE playlistName LIKE '%' ||:query || '%' AND isHidden = :showHidden ORDER BY created ASC, playlistName")
    fun getPlaylistsWithSongsByDateCreated(showHidden: Boolean, query: String):Flow<List<PlaylistWithSongs>>

    @Transaction
    @Query("SELECT * FROM playlist_table WHERE playlistName LIKE '%' ||:query || '%' AND isHidden = :showHidden ORDER BY updated ASC, playlistName")
    fun getPlaylistsWithSongsByDateUpdated(showHidden: Boolean, query: String):Flow<List<PlaylistWithSongs>>

    @Transaction
    @Query("SELECT * FROM playlist_table WHERE playlistName LIKE '%' ||:query || '%' AND isHidden = :showHidden ORDER BY playlistName DESC, playlistName")
    fun getPlaylistsWithSongsByNameDesc(showHidden: Boolean, query: String):Flow<List<PlaylistWithSongs>>

    @Transaction
    @Query("SELECT * FROM playlist_table WHERE playlistName LIKE '%' ||:query || '%' AND isHidden = :showHidden ORDER BY created DESC, playlistName")
    fun getPlaylistsWithSongsByDateCreatedDesc(showHidden: Boolean, query: String):Flow<List<PlaylistWithSongs>>

    @Transaction
    @Query("SELECT * FROM playlist_table WHERE playlistName LIKE '%' ||:query || '%' AND isHidden = :showHidden ORDER BY updated DESC, playlistName")
    fun getPlaylistsWithSongsByDateUpdatedDesc(showHidden: Boolean, query: String):Flow<List<PlaylistWithSongs>>




    fun getPlaylistSongs(playlistId: Int, query: String, songSortOrder: SongSortOrder, sortOrder: SortOrder, showHidden:Boolean = false): Flow<List<Song>> =
        when(sortOrder){
            SortOrder.A_Z -> {
                when(songSortOrder){
                    SongSortOrder.BY_NAME->{getPlaylistSongsByName(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_LENGTH->{getPlaylistSongsByLength(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_ARTIST->{getPlaylistSongsByArtist(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_ALBUM->{getPlaylistSongsByAlbum(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_TRACK_NUMBER->{getPlaylistSongsByTrackNumber(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_YEAR->{getPlaylistSongsByYear(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_ALBUM_ARTIST->{getPlaylistSongsByAlbumArtist(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_ADDED_TO_PLAYLIST->{getPlaylistSongsByAddedToPlaylist(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_GENRE->{getPlaylistSongsByGenre(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_FOLDER->{getPlaylistSongsByGenre(playlistId = playlistId, query = query, showHidden = showHidden)}
                    else -> {getPlaylistSongsByName(playlistId, query, showHidden = showHidden)}
                }
            }

            SortOrder.Z_A -> {
                when(songSortOrder){
                    SongSortOrder.BY_NAME->{getPlaylistSongsByNameDesc(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_LENGTH->{getPlaylistSongsByLengthDesc(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_ARTIST->{getPlaylistSongsByArtistDesc(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_ALBUM->{getPlaylistSongsByAlbumDesc(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_TRACK_NUMBER->{getPlaylistSongsByTrackNumberDesc(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_YEAR->{getPlaylistSongsByYearDesc(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_ALBUM_ARTIST->{getPlaylistSongsByAlbumArtistDesc(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_ADDED_TO_PLAYLIST->{getPlaylistSongsByAddedToPlaylistDesc(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_GENRE->{getPlaylistSongsByGenreDesc(playlistId = playlistId, query = query, showHidden = showHidden)}
                    SongSortOrder.BY_FOLDER->{getPlaylistSongsByGenre(playlistId = playlistId, query = query, showHidden = showHidden)}
                    else -> {getPlaylistSongsByName(playlistId, query, showHidden = showHidden)}
                }
            }
        }


    @Query("UPDATE playlist_table SET updated=:time WHERE id= :playlistId")
    suspend fun updatePlaylistModifiedDate(playlistId: Int, time:Long = System.currentTimeMillis())

    @Query("SELECT * FROM songs_table " +
            "WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden " +
            "OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden " +
            "ORDER BY songName ASC, songName")
    fun getPlaylistSongsByName(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>


    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY length ASC, songName")
    fun getPlaylistSongsByLength(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY songArtist ASC, songName")
    fun getPlaylistSongsByArtist(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY albumArtist ASC, songName")
    fun getPlaylistSongsByAlbumArtist(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY trackNumber ASC, songName")
    fun getPlaylistSongsByTrackNumber(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY year ASC, songName")
    fun getPlaylistSongsByYear(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY playlistId ASC, songName")
    fun getPlaylistSongsByPlaylist(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY album ASC, songName")
    fun getPlaylistSongsByAlbum(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY created ASC, songName")
    fun getPlaylistSongsByAddedToPlaylist(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY genre ASC, songName")
    fun getPlaylistSongsByGenre(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY folder ASC, songName")
    fun getPlaylistSongsByFolder(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>



    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY songName DESC, songName")
    fun getPlaylistSongsByNameDesc(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY length DESC, songName")
    fun getPlaylistSongsByLengthDesc(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY songArtist DESC, songName")
    fun getPlaylistSongsByArtistDesc(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY albumArtist DESC, songName")
    fun getPlaylistSongsByAlbumArtistDesc(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY trackNumber DESC, songName")
    fun getPlaylistSongsByTrackNumberDesc(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY year DESC, songName")
    fun getPlaylistSongsByYearDesc(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY playlistId DESC, songName")
    fun getPlaylistSongsByPlaylistDesc(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY album DESC, songName")
    fun getPlaylistSongsByAlbumDesc(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY created DESC, songName")
    fun getPlaylistSongsByAddedToPlaylistDesc(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY genre DESC, songName")
    fun getPlaylistSongsByGenreDesc(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>
    @Query("SELECT * FROM songs_table WHERE playlistId = :playlistId AND songName LIKE '%' ||:query || '%' AND isHidden = :showHidden OR playlistId = :playlistId AND songArtist LIKE '%' ||:query || '%' AND isHidden = :showHidden  ORDER BY folder DESC, songName")
    fun getPlaylistSongsByFolderDesc(playlistId: Int, query: String, showHidden:Boolean): Flow<List<Song>>


}