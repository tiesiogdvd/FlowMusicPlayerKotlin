package com.tiesiogdvd.composetest.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.navigation.compose.hiltViewModel
import com.tiesiogdvd.composetest.data.settings.PlayerSettingsItemsList
import com.tiesiogdvd.composetest.data.settings.SettingsManager
import com.tiesiogdvd.composetest.ui.header.HeaderSettings
import com.tiesiogdvd.composetest.ui.settings.SettingsMethod.*
import com.tiesiogdvd.composetest.ui.settings.playerSettings.SettingsPlayerViewModel

@Composable
fun SettingsPlayer(viewModel: SettingsPlayerViewModel = hiltViewModel()) {
    val currentSettings = viewModel.settings.collectAsState(initial = null)
    Surface {
        HeaderSettings(headerName = "Player Screen"){
            val setting = SettingsItem(
                settingsMethod = RANGE_SLIDER,
                settingInfo = "Sets the time for the navigation to be hidden in player screen",
                maxRange = 9000,
                rangeText = "seconds",
                name = "Navigation hide seconds",
                minRange = 0,
                preferencesKey = SettingsManager.PreferencesKeysPlayerMenu.HIDE_NAVIGATION_SECONDS
            )

            val test = SettingsItem(
                name = "Show add to playlist",
                settingInfo = "Enables or disables the button for lyrics, swiping down works either way",
                settingsMethod = SWITCH,
                preferencesKey = SettingsManager.PreferencesKeysPlayerMenu.SHOW_ADD_TO_PLAYLIST
            )
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                /*SettingSlider(
                    settingsItem = setting,
                    currentRange = currentSettings.value?.hideNavigationSeconds ?: 0,
                    onSlide = { value ->
                        viewModel.updateSetting(setting.preferencesKey as Preferences.Key<Any>, value.toInt())
                    }
                )

                SettingSwitch(settingsItem = test, enabled = currentSettings.value?.showAddToPlaylist?:true, onCheckedChange = {value ->
                    viewModel.updateSetting(test.preferencesKey as Preferences.Key<Any>, value)
                })*/

                PlayerSettingsItemsList.playerSettings.forEach {setting ->
                    val value = viewModel.getPrefValue(setting.preferencesKey as Preferences.Key<Any>).collectAsState(initial = null).value
                    when(setting.settingsMethod){
                        SWITCH -> {
                            SettingSwitch(settingsItem = setting, enabled = value == true, onCheckedChange = {
                                viewModel.updateSetting(setting.preferencesKey as Preferences.Key<Any>, it)
                            })
                        }
                        RANGE_SLIDER -> {
                            SettingSlider(settingsItem = setting, currentRange = (value?:0) as Int, onSlide = {
                                viewModel.updateSetting(setting.preferencesKey as Preferences.Key<Any>, it.toInt())
                            })
                        }
                        SELECT_MULTIPLE -> TODO()
                        SELECT_SINGLE -> TODO()
                        CHECKBOX -> TODO()
                        COLOR_SELECTION -> TODO()
                        INPUT_TEXT -> TODO()
                    }

                }
            }

        }
    }
}
