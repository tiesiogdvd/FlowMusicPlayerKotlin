package com.tiesiogdvd.composetest.ui.bottomNavBar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tiesiogdvd.composetest.ui.libraryPlaylist.recomposeHighlighter
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.playlistssongstest.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun SongItemBar(song: Song, viewModel: NavbarViewModel = hiltViewModel()) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(song.songPath) {
        withContext(Dispatchers.IO) {
            bitmap = MusicDataMetadata.getBitmap(song.songPath)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 20.dp)
            .padding(vertical = 5.dp)
            .padding(start = 15.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier,
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!,
                        contentDescription = "desc",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(45.dp),
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_group_23_image_6),
                        contentDescription = "desc",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(45.dp),
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(50.dp)
                    .fillMaxWidth(),
                color = Color.Transparent,
                shape = RoundedCornerShape(30.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(3.5F)
                    ) {


                        Text(
                            text = song.songName.toString(),
                            fontSize = 14.sp,
                            color = GetThemeColor.getText(isSystemInDarkTheme()),
                            modifier = Modifier.wrapContentHeight(
                                Alignment.Bottom
                            ),
                            maxLines = 1
                        )

                        if(song.songArtist!=null){
                            Text(
                                text = song.songArtist.toString(),
                                fontSize = 10.sp,
                                color = GetThemeColor.getText(isSystemInDarkTheme()),
                                modifier = Modifier.wrapContentHeight(
                                    Alignment.Bottom
                                )
                            )
                        }
                    }
                    val isPlaying = viewModel.isPlaying.collectAsState().value
                    var resourceId by remember { mutableStateOf(R.drawable.ic_action_play) }
                    println(isPlaying)
                    resourceId = if(isPlaying == true){
                        R.drawable.ic_action_pause
                    }else{
                        R.drawable.ic_action_play
                    }

                    Icon(
                        painter = painterResource(id = resourceId),
                        contentDescription = "icon",
                        modifier = Modifier
                            .height(45.dp)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                viewModel.changePlaybackState()
                            },
                        tint = GetThemeColor.getDrawableBar(isSystemInDarkTheme())
                    )

                }
            }
        }
    }
}