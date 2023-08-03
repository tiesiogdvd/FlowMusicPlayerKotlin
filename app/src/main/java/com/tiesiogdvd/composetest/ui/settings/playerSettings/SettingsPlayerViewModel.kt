package com.tiesiogdvd.composetest.ui.settings.playerSettings

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiesiogdvd.composetest.data.settings.PlayerMenuPreferences
import com.tiesiogdvd.composetest.data.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsPlayerViewModel  @Inject constructor(
    val settingsManager: SettingsManager
): ViewModel(){
    init {
        viewModelScope.launch {
            settingsManager.clearSettings()

            settingsManager.playerSettingsFlow.collect {
                playerMenuPreferences.value = it
            }
        }
    }

    val settings = settingsManager.playerSettingsFlow

    val playerMenuPreferences = MutableStateFlow<PlayerMenuPreferences?>(null)

    fun <T> getPrefValue(preferencesKey: Preferences.Key<T>): Flow<T?> {
        return settingsManager.getPreferenceValue(preferencesKey)
    }

    inline fun <reified T : Any> updateSetting(
        preferencesKey: Preferences.Key<T>,
        value: T
    ) {
        viewModelScope.launch {
            settingsManager.updateSettingsItem(preferencesKey, value)
        }
    }
}