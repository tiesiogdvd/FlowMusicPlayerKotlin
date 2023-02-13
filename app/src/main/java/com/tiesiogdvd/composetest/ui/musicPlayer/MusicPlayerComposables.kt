package com.tiesiogdvd.composetest.ui.musicPlayer

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.tiesiogdvd.composetest.ui.library.PlaylistList
import com.tiesiogdvd.composetest.ui.theme.ComposeTestTheme
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor

@RequiresApi(Build.VERSION_CODES.R)
@Destination
@Composable
fun MusicPlayer(navigator: DestinationsNavigator) {
    //navigator.popBackStack()
    ComposeTestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = GetThemeColor.getBackground(isSystemInDarkTheme())
        ) {
            Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 25.dp, start = 25.dp)
        ) {
            Text("Library", fontSize = 40.sp)
            Text("Recent Playlists", fontSize = 20.sp)
            PlaylistList(navigator = navigator)
        }
        }

    }
}