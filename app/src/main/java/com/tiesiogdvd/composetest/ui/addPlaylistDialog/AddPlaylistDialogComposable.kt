package com.tiesiogdvd.composetest.ui.addPlaylistDialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor



@Composable
fun AddPlaylistDialog(
    onDismiss: () -> Unit,
    onRequestAddPlaylist:(String) -> Unit
    ){
        var text by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true)) {
            Card(
                elevation = 5.dp,
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.width(300.dp)
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
                    Text(text = "Create new playlist", fontSize = 25.sp, modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp), textAlign = TextAlign.Start)
                    OutlinedTextField(colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = GetThemeColor.getPurple(isSystemInDarkTheme()),
                        focusedBorderColor = GetThemeColor.getPurple(isSystemInDarkTheme())),
                        value = text, onValueChange = { newText-> text= newText},
                        placeholder = { Text(text = "Playlist name")},
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                        .scale(1f, 1f)
                        .padding(bottom = 10.dp), singleLine = true)

                    Surface(shape = RoundedCornerShape(30.dp), color = GetThemeColor.getBackground(isSystemInDarkTheme()), modifier = Modifier.wrapContentWidth()) {
                        Text(text = "Add playlist", modifier = Modifier
                            .padding(vertical = 10.dp)
                            .width(100.dp)
                            .align(Alignment.CenterHorizontally)
                            .clickable {onRequestAddPlaylist(text)}, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }