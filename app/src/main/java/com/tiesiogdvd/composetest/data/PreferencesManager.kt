package com.tiesiogdvd.composetest.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

enum class SongSortOrder{
    BY_NAME, BY_ARTIST, BY_ALBUM_ARTIST, BY_ALBUM, BY_GENRE, BY_TRACK_NUMBER, BY_YEAR, BY_LENGTH, BY_FOLDER, BY_ADDED_TO_PLAYLIST
}

enum class PlaylistSortOrder{
    BY_NAME, BY_DATE_CREATED, BY_DATE_UPDATED
}

enum class RepeatMode{
    REPEAT_SONG, REPEAT_PLAYLIST, REPEAT_DISABLED
}


data class CurrentSourcePreferences(val currentSource:String, val currentSongID:Int)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_preferences")

@Singleton
class PreferencesManager @Inject constructor(context: Context){
    private val dataStore: DataStore<Preferences> = context.dataStore

    val preferencesFlow = dataStore.data.catch {
        exception -> if(exception is IOException){
        Log.e(TAG, "Error reading preferences", exception)
        emit(emptyPreferences())
    }else{
        throw exception
    }
    }.map { preferences->
        val currentSource = preferences[PreferencesKeys.CURRENT_SOURCE] ?: "All Songs"
        val currentSongId = preferences[PreferencesKeys.CURRENT_SONG_ID] ?: -1
        CurrentSourcePreferences(currentSource, currentSongId as Int)
    }


    suspend fun updateSource(currentSource: String){
        dataStore.edit {
            preferences -> preferences[PreferencesKeys.CURRENT_SOURCE] = currentSource
        }
    }

    suspend fun updateCurrentSongID(currentSongID: Int){
        dataStore.edit {
                preferences -> preferences[PreferencesKeys.CURRENT_SONG_ID] = currentSongID
        }
    }


    private object PreferencesKeys{
        val CURRENT_SOURCE = stringPreferencesKey("latest_source")
        val CURRENT_SONG_ID = intPreferencesKey("current_song_id")
        val IS_SHUFFLED = stringPreferencesKey("is_shuffled")
    }
}