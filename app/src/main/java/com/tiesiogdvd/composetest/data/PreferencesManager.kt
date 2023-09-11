package com.tiesiogdvd.composetest.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

enum class SongSortOrder(val songSortOrderText: String, val key:String){
    BY_NAME("Name", "songName"),
    BY_ARTIST("Artist", "length"),
    BY_ALBUM_ARTIST("Album Artist" ,"albumArtist"),
    BY_ALBUM("Album","album"),
    BY_GENRE("Genre","genre"),
    BY_TRACK_NUMBER("Track number","trackNumber"),
    BY_YEAR("Year","year"),
    BY_LENGTH("Length","length"),
    BY_FOLDER("Folder","folder"),
    BY_ADDED_TO_PLAYLIST("Date added","created")
}
enum class SortOrder(val sortOrderText: String, val key:String){
    A_Z ("A-Z", "ASC"),
    Z_A ("Z-A", "DESC")
}

enum class PlaylistSortOrder(val playlistSortOrderText: String, val key:String){
    BY_NAME("Name","playlistName"),
    BY_DATE_CREATED("Date Created","created"),
    BY_DATE_UPDATED("Date Updated","updated")
}

enum class RepeatMode{
    REPEAT_SONG, REPEAT_PLAYLIST, REPEAT_DISABLED
}
data class SongPreferences(val currentSongID:Int)
data class SourcePreferences(val currentSource: Int, val songSortOrder: SongSortOrder, val sortOrder: SortOrder)
data class PlayModePreferences(
    val isShuffleEnabled: Boolean,
    val repeatMode: RepeatMode,
    val songSortOrder: SongSortOrder,
    val sortOrder: SortOrder,
    val shuffleSeed: Int? = 0
    )


private val Context.dataStoreSong: DataStore<Preferences> by preferencesDataStore("current_song")
private val Context.dataStoreSourcePrefs: DataStore<Preferences> by preferencesDataStore("current_source")
private val Context.dataStorePlayModePrefs: DataStore<Preferences> by preferencesDataStore("play_mode")

@Singleton
class PreferencesManager @Inject constructor(context: Context){
    private val dataStoreSong: DataStore<Preferences> = context.dataStoreSong
    private val dataStoreSourcePrefs: DataStore<Preferences> = context.dataStoreSourcePrefs
    private val dataStorePlayModePrefs: DataStore<Preferences> = context.dataStorePlayModePrefs


    val currentSongFlow = dataStoreSong.data.catch {
        exception -> if(exception is IOException){
        Log.e(TAG, "Error reading preferences", exception)
        emit(emptyPreferences())
    }else{
        throw exception
    }
    }.map { preferences->
        val currentSongId = preferences[PreferencesKeysCurrentSong.CURRENT_SONG_ID] ?: 0
        SongPreferences(currentSongId)
    }


    val currentSourceFlow = dataStoreSourcePrefs.data.catch {
            exception -> if(exception is IOException){
        Log.e(TAG, "Error reading preferences", exception)
        emit(emptyPreferences())
    }else{
        throw exception
    }
    }.map { preferences->
        val currentSource = preferences[PreferencesKeysSourcePrefs.CURRENT_PLAYLIST_ID] ?: 0
        val songSortOrder = SongSortOrder.valueOf(preferences[PreferencesKeysSourcePrefs.SONG_SORT_ORDER] ?: SongSortOrder.BY_NAME.name)
        val sortOrder = SortOrder.valueOf(preferences[PreferencesKeysSourcePrefs.SORT_ORDER] ?: SortOrder.A_Z.name)
        SourcePreferences(currentSource,songSortOrder,sortOrder)
    }


    val currentPlayModeFlow = dataStorePlayModePrefs.data.catch {
            exception -> if(exception is IOException){
        Log.e(TAG, "Error reading preferences", exception)
        emit(emptyPreferences())
    }else{
        throw exception
    }
    }.map { preferences->
        val shuffleOrder = preferences[PreferencesKeysPlayPrefs.IS_SHUFFLED] ?: false
        val shuffleSeed = preferences[PreferencesKeysPlayPrefs.SHUFFLE_SEED] ?: 0
        val repeatMode = RepeatMode.valueOf(preferences[PreferencesKeysPlayPrefs.REPEAT_MODE] ?: RepeatMode.REPEAT_DISABLED.name)
        val songSortOrder = SongSortOrder.valueOf(preferences[PreferencesKeysPlayPrefs.SONG_SORT_ORDER] ?: SongSortOrder.BY_NAME.name)
        val sortOrder = SortOrder.valueOf(preferences[PreferencesKeysPlayPrefs.SORT_ORDER] ?: SortOrder.Z_A.name)
        PlayModePreferences(shuffleOrder,repeatMode,songSortOrder,sortOrder, shuffleSeed)
    }

    fun getShuffleSeed(): Flow<Int> {
        return dataStorePlayModePrefs.data.map { preferences ->
            preferences[PreferencesKeysPlayPrefs.SHUFFLE_SEED]?:0
        }
    }

    fun getSongID(): Int? = runBlocking{
        return@runBlocking dataStoreSong.data.map { preferences -> preferences[PreferencesKeysCurrentSong.CURRENT_SONG_ID]}.first()
    }

    fun getCurrentPlaylistID(): Int = runBlocking{
        return@runBlocking dataStoreSourcePrefs.data.map { preferences -> preferences[PreferencesKeysSourcePrefs.CURRENT_PLAYLIST_ID]}.first() ?: 0
    }


    fun getCurrentShuffleOrder(): Boolean = runBlocking{
        return@runBlocking dataStorePlayModePrefs.data.map { preferences -> preferences[PreferencesKeysPlayPrefs.IS_SHUFFLED]}.first() ?: false
    }

    fun getCurrentShuffleSeed(): Int = runBlocking{
        return@runBlocking dataStorePlayModePrefs.data.map { preferences -> preferences[PreferencesKeysPlayPrefs.SHUFFLE_SEED]}.first() ?: 0
    }



    fun getCurrentRepeatMode(): RepeatMode = runBlocking{
        return@runBlocking dataStorePlayModePrefs.data.map { preferences -> RepeatMode.valueOf(preferences[PreferencesKeysPlayPrefs.REPEAT_MODE] ?:RepeatMode.REPEAT_DISABLED.name) }.first()
    }
    fun getCurrentSortMode(): SortOrder = runBlocking{
        return@runBlocking dataStoreSourcePrefs.data.map { preferences -> SortOrder.valueOf(preferences[PreferencesKeysSourcePrefs.SORT_ORDER] ?:SortOrder.A_Z.name) }.first()
    }
    fun getCurrentSongSortMode(): SongSortOrder = runBlocking{
        return@runBlocking dataStoreSourcePrefs.data.map { preferences -> SongSortOrder.valueOf(preferences[PreferencesKeysSourcePrefs.SONG_SORT_ORDER] ?:SongSortOrder.BY_NAME.name) }.first()
    }

    suspend fun updateCurrentSongID(currentSongID: Int) = withContext(Dispatchers.IO){
        if(getSongID()!=currentSongID){
            dataStoreSong.edit {
                    preferences -> preferences[PreferencesKeysCurrentSong.CURRENT_SONG_ID] = currentSongID
            }
        }
    }
    suspend fun updateSource(currentSource: Int, sortOrder: SortOrder, songSortOrder: SongSortOrder){
        println("SOURCE $currentSource   SORT ORDER ${sortOrder.name}   SONG SORT ORDER ${songSortOrder.name}")
        dataStoreSourcePrefs.edit {
            preferences -> preferences[PreferencesKeysSourcePrefs.CURRENT_PLAYLIST_ID] = currentSource
            preferences[PreferencesKeysSourcePrefs.SORT_ORDER] = sortOrder.name
            preferences[PreferencesKeysSourcePrefs.SONG_SORT_ORDER] = songSortOrder.name
        }
    }

    suspend fun updatePlaylistSource(currentSource: Int){
        dataStoreSourcePrefs.edit {
                preferences -> preferences[PreferencesKeysSourcePrefs.CURRENT_PLAYLIST_ID] = currentSource
        }
    }


    suspend fun updateShuffleOrder(shuffleOrder: Boolean){
        dataStorePlayModePrefs.edit {
                preferences -> preferences[PreferencesKeysPlayPrefs.IS_SHUFFLED] = shuffleOrder
        }
    }

    suspend fun updateShuffleSeed(shuffleSeed: Int){
        dataStorePlayModePrefs.edit {
                preferences -> preferences[PreferencesKeysPlayPrefs.SHUFFLE_SEED] = shuffleSeed
        }
    }

    suspend fun updateRepeatMode(repeatMode: RepeatMode){
        dataStorePlayModePrefs.edit {
                preferences -> preferences[PreferencesKeysPlayPrefs.REPEAT_MODE] = repeatMode.name
        }
    }

    suspend fun updateSongSortOrder(songSortOrder: SongSortOrder){
        dataStoreSourcePrefs.edit {
            preferences -> preferences[PreferencesKeysSourcePrefs.SONG_SORT_ORDER] = songSortOrder.name
        }
    }

    suspend fun updateSortOrder(sortOrder: SortOrder){
        dataStoreSourcePrefs.edit {
                preferences -> preferences[PreferencesKeysSourcePrefs.SORT_ORDER] = sortOrder.name
        }
    }


    private object PreferencesKeysCurrentSong{
        val CURRENT_SONG_ID = intPreferencesKey("current_song_id")
    }
    private object PreferencesKeysSourcePrefs{
        val CURRENT_PLAYLIST_ID = intPreferencesKey("latest_source")
        val SONG_SORT_ORDER = stringPreferencesKey("song_sort_order")
        val SORT_ORDER = stringPreferencesKey("sort_order")
    }
    private object PreferencesKeysPlayPrefs{
        val IS_SHUFFLED = booleanPreferencesKey("is_shuffled")
        val SHUFFLE_SEED = intPreferencesKey("shuffle_seed")
        val REPEAT_MODE = stringPreferencesKey("repeat_mode")
        val SONG_SORT_ORDER = stringPreferencesKey("song_sort_order")
        val SORT_ORDER = stringPreferencesKey("sort_order")
    }
}