package com.tiesiogdvd.composetest.ui.settings

import android.content.res.Resources
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor

@Composable
fun SettingsType(
    onClick: () -> Unit,
    settingName: String,
    settingsIcon: Int? = null,
    modifier: Modifier = Modifier
){
    Box(modifier = modifier
        .clip(RoundedCornerShape(20.dp))
        .background(color = GetThemeColor.getButton(isSystemInDarkTheme()))
        .fillMaxWidth()
        .height(35.dp)
        .clickable { onClick() }, contentAlignment = Alignment.CenterStart){

        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth()) {
            Text(text = settingName, fontSize = 20.sp, modifier = Modifier.padding(bottom = 2.dp))

            if(settingsIcon!=null){
                Icon(
                    painter = painterResource(id = settingsIcon),
                    contentDescription = "icon",
                    modifier = Modifier.size(30.dp),
                    tint = GetThemeColor.getBackgroundThird(isSystemInDarkMode = isSystemInDarkTheme())
                )
            }
        }
    }
}