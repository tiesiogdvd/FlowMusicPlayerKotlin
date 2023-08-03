package com.tiesiogdvd.composetest.data.settings

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.tiesiogdvd.composetest.data.*
import com.tiesiogdvd.composetest.ui.settings.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "Player Menu"

private val Context.playerMenu: DataStore<Preferences> by preferencesDataStore("player_menu")

data class PlayerMenuPreferences(
    val showLyrics: Boolean? = true,
    val showSeekbar: Boolean? = true,
    val showFavorite: Boolean? = true,
    val showAddToPlaylist: Boolean? = true,
    val showRepeatMode:Boolean? = true,
    val enableViewPager:Boolean? = true,
    val hideNavigationSeconds: Int? = 3
    )

@Singleton
class SettingsManager @Inject constructor(private val context: Context){
    val playerMenuSettingsDataStore: DataStore<Preferences> = context.playerMenu

    val playerSettingsFlow = playerMenuSettingsDataStore.data.catch {
            exception -> if(exception is IOException){
        Log.e(TAG, "Error reading preferences", exception)
        emit(emptyPreferences())
    }else{
        throw exception
    }
    }.map { preferences ->
        val showLyrics = preferences[PreferencesKeysPlayerMenu.SHOW_LYRICS_BUTTON] ?: true
        val showSeekbar = preferences[PreferencesKeysPlayerMenu.SHOW_SEEKBAR] ?: true
        val showFavorite = preferences[PreferencesKeysPlayerMenu.SHOW_FAVORITE] ?: true
        val showAddToPlaylist = preferences[PreferencesKeysPlayerMenu.SHOW_ADD_TO_PLAYLIST] ?: true
        val showRepeatMode = preferences[PreferencesKeysPlayerMenu.SHOW_REPEAT_MODE] ?: true
        val enableViewPager = preferences[PreferencesKeysPlayerMenu.ENABLE_VIEWPAGER] ?: true
        val hideNavigationSeconds = preferences[PreferencesKeysPlayerMenu.HIDE_NAVIGATION_SECONDS] ?: 3
        PlayerMenuPreferences(showLyrics, showSeekbar, showFavorite, showAddToPlaylist, showRepeatMode, enableViewPager, hideNavigationSeconds)
    }

    object PreferencesKeysPlayerMenu{
        val SHOW_LYRICS_BUTTON = booleanPreferencesKey("player_show_lyrics_button")
        val SHOW_SEEKBAR = booleanPreferencesKey("player_show_seekbar")
        val SHOW_FAVORITE = booleanPreferencesKey("player_show_favorite")
        val SHOW_ADD_TO_PLAYLIST = booleanPreferencesKey("player_show_add_to_playlist")
        val SHOW_REPEAT_MODE = booleanPreferencesKey("player_show_repeat_mode")
        val ENABLE_VIEWPAGER = booleanPreferencesKey("player_enable_viewpager")
        val HIDE_NAVIGATION_SECONDS = intPreferencesKey("player_hide_navigation_seconds")
    }

    suspend fun clearSettings(){
        playerMenuSettingsDataStore.edit {preferences ->
            preferences.clear()
            preferences[PreferencesKeysPlayerMenu.SHOW_LYRICS_BUTTON] = true
            preferences[PreferencesKeysPlayerMenu.SHOW_SEEKBAR] = true
            preferences[PreferencesKeysPlayerMenu.SHOW_FAVORITE] = true
            preferences[PreferencesKeysPlayerMenu.SHOW_ADD_TO_PLAYLIST] = true
            preferences[PreferencesKeysPlayerMenu.SHOW_REPEAT_MODE] = true
            preferences[PreferencesKeysPlayerMenu.ENABLE_VIEWPAGER] = true
            preferences[PreferencesKeysPlayerMenu.HIDE_NAVIGATION_SECONDS] = 3


        }
    }

    inline suspend fun <reified T> updateSettingsItem(preferencesKey: Preferences.Key<T>, value: T) {
        playerMenuSettingsDataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    fun <T> getPreferenceValue(preferencesKey: Preferences.Key<T>): Flow<T?> {
        return playerMenuSettingsDataStore.data.map { preferences ->
            preferences[preferencesKey]
        }
    }
}


object PlayerSettingsItemsList{
    val playerSettings = listOf(
        SettingsItem(
            name = "Show Lyrics Button",
            settingInfo = "Enables or disables the button for lyrics, swiping down works either way",
            settingsMethod = SettingsMethod.SWITCH,
            preferencesKey = SettingsManager.PreferencesKeysPlayerMenu.SHOW_LYRICS_BUTTON,
        ),
        SettingsItem(
            name = "Show seekbar",
            settingInfo = "Hides the scrollable amplitude seekbar",
            settingsMethod = SettingsMethod.SWITCH,
            preferencesKey = SettingsManager.PreferencesKeysPlayerMenu.SHOW_SEEKBAR,
        ),
        SettingsItem(
            name = "Show add to playlist",
            settingInfo = "Enables or disables the button for lyrics, swiping down works either way",
            settingsMethod = SettingsMethod.SWITCH,
            preferencesKey = SettingsManager.PreferencesKeysPlayerMenu.SHOW_ADD_TO_PLAYLIST,
        ),
        SettingsItem(
            name = "Show repeat mode",
            settingInfo = "Hides the scrollable amplitude seekbar",
            settingsMethod = SettingsMethod.SWITCH,
            preferencesKey = SettingsManager.PreferencesKeysPlayerMenu.SHOW_REPEAT_MODE,
        ),
        SettingsItem(
            name = "Viewpager enabled",
            settingInfo = "Removes option to add songs to favorites",
            settingsMethod = SettingsMethod.SWITCH,
            preferencesKey = SettingsManager.PreferencesKeysPlayerMenu.ENABLE_VIEWPAGER,
        ),
        SettingsItem(
            name = "Hide navigation seconds",
            settingInfo = "Removes option to add songs to favorites",
            settingsMethod = SettingsMethod.RANGE_SLIDER,
            preferencesKey = SettingsManager.PreferencesKeysPlayerMenu.HIDE_NAVIGATION_SECONDS,
            minRange = 0,
            maxRange = 100,
            rangeText = "seconds",
            enabled = true
        )
    )
}