@file:OptIn(ExperimentalMaterialApi::class)

package com.tiesiogdvd.composetest.ui.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingSlider(
    settingsItem: SettingsItem,
    currentRange: Int,
    onSlide: (Float) -> Unit
    ){
    val sliderValue = remember { mutableStateOf(currentRange.toFloat()) }

    LaunchedEffect(currentRange) {
        sliderValue.value = currentRange.toFloat()
    }


    Box(modifier = Modifier){
        Column {
            Text(text = settingsItem.name, fontSize = 18.sp, color = GetThemeColor.getText(isSystemInDarkMode = isSystemInDarkTheme()))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = settingsItem.minRange.toString() + "" + settingsItem.rangeText.toString(), fontSize = 12.sp, color = GetThemeColor.getText(isSystemInDarkMode = isSystemInDarkTheme()))
                Text(text = sliderValue.value.toString() + settingsItem.rangeText.toString(), fontSize = 12.sp, color = GetThemeColor.getText(isSystemInDarkMode = isSystemInDarkTheme()))
            }
            isSystemInDarkTheme()
            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                Slider(value = sliderValue.value,
                    onValueChange = { value ->
                        sliderValue.value = value
                    },
                    onValueChangeFinished = {
                        onSlide(sliderValue.value)
                    },
                    valueRange = settingsItem.minRange!!.toFloat() ..(settingsItem.maxRange!!.toFloat().coerceAtLeast(1f)),
                    colors = SliderDefaults.colors(
                        activeTrackColor = GetThemeColor.getText(isSystemInDarkTheme()).copy(1f),
                        inactiveTrackColor = GetThemeColor.getTextSecondary(isSystemInDarkTheme()).copy(0.5f),
                        thumbColor = GetThemeColor.getSettingsPrimary(isSystemInDarkTheme()).copy(1f)
                    ),
                    enabled = true,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 10.dp)
                        .padding(horizontal = 0.dp)
                        .height(20.dp)
                        .fillMaxWidth())
            }

            Text(text = settingsItem.settingInfo.toString(), fontSize = 11.sp, color = GetThemeColor.getTextSecondary(isSystemInDarkMode = isSystemInDarkTheme()))
        }
    }
}

@Composable
fun SettingSwitch(
    settingsItem: SettingsItem,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
){
    Box(modifier = Modifier){
        Column() {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = settingsItem.name, fontSize = 18.sp, color = GetThemeColor.getText(isSystemInDarkMode = isSystemInDarkTheme()))
                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    Switch(checked = enabled, onCheckedChange = {value -> onCheckedChange(value)}, colors = SwitchDefaults.colors(checkedThumbColor = GetThemeColor.getSettingsPrimary(isSystemInDarkTheme()), uncheckedThumbColor = GetThemeColor.getButtonSecondary(isSystemInDarkTheme())))
                }
            }
            Text(text = settingsItem.settingInfo.toString(), fontSize = 11.sp, color = GetThemeColor.getTextSecondary(isSystemInDarkMode = isSystemInDarkTheme()))
        }
    }
}

@Composable
fun SettingMultiSelect(){
   /* SettingsItemsList.audioDiscoverySettings.items.forEach{
        it.
    }*/
}

@Composable
fun SettingSingleSelect(){

}