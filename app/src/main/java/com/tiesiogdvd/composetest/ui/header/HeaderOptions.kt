package com.tiesiogdvd.composetest.ui.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.ui.library.LibraryPlaylistViewModel
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun HeaderOptions(
    onOpenSortDialog:()->Unit,
    text:MutableStateFlow<String>,
    onClickPlay:()->Unit,
    onClickMix:()->Unit,
){
    val textState = text.collectAsState().value

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(start = 25.dp), verticalAlignment = Alignment.CenterVertically){
        Surface(modifier = Modifier.padding(end = 15.dp), shape = RoundedCornerShape(30.dp), color = GetThemeColor.getButton(
            isSystemInDarkTheme()
        )) {
            Icon(
                painter = painterResource(id = R.drawable.ic_action_sort),
                contentDescription = "icon",
                modifier = Modifier
                    .height(25.dp)
                    .width(25.dp)
                    .align(Alignment.CenterVertically)
                    .clickable { onOpenSortDialog() }
                    .offset(y = 3.dp),
                tint = GetThemeColor.getDrawableBar(isSystemInDarkTheme())
            )
        }

        Surface(modifier = Modifier, shape = RoundedCornerShape(30.dp), color = GetThemeColor.getButton(
            isSystemInDarkTheme()
        )) {
            Icon(
                painter = painterResource(id = R.drawable.ic_action_mix),
                contentDescription = "icon",
                modifier = Modifier

                    .height(25.dp)
                    .width(25.dp)
                    .align(Alignment.CenterVertically)
                    .clickable {onClickMix()}
                    .offset(x = 1.dp),
                tint = GetThemeColor.getDrawableBar(isSystemInDarkTheme())
            )
        }

        Surface(modifier = Modifier
            .weight(8f)
            .offset(x = 15.dp)
            .height(25.dp), shape = RoundedCornerShape(30.dp), color = GetThemeColor.getButton(
            isSystemInDarkTheme()
        )) {
            BasicTextField(value = textState,
                enabled = true,
                singleLine = true,
                onValueChange = { newText ->
                    text.value = newText},
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 3.dp)
                    .align(Alignment.CenterVertically)
                    .padding(start = 10.dp),
                textStyle = MaterialTheme.typography.body1.copy(fontSize = 12.sp, color = GetThemeColor.getText(
                    isSystemInDarkTheme()
                )))
        }


        Surface(modifier = Modifier
            .padding(end = 20.dp)
            .size(40.dp), shape = RoundedCornerShape(30.dp), color = GetThemeColor.getPurple(
            isSystemInDarkTheme()
        )) {
            Icon(
                painter = painterResource(id = R.drawable.ic_action_play),
                contentDescription = "icon",
                modifier = Modifier
                    .height(20.dp)
                    .width(20.dp)
                    .align(Alignment.CenterVertically)
                    .scale(0.7f)
                    .clickable {onClickPlay()},
                tint = GetThemeColor.getDrawableBar(isSystemInDarkTheme())
            )
        }
    }

}
