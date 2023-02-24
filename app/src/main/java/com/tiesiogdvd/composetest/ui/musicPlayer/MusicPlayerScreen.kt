package com.tiesiogdvd.composetest.ui.musicPlayer

import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.ColorUtils
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.palette.graphics.Palette
import com.ramcosta.composedestinations.annotation.Destination
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.ui.theme.FlowPlayerTheme
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.composetest.util.TypeConverter
import com.tiesiogdvd.playlistssongstest.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


@RequiresApi(Build.VERSION_CODES.R)
@Destination
@Composable
fun MusicPlayer(viewModel: MusicPlayerViewModel = hiltViewModel()) {
    FlowPlayerTheme {
        MusicPlayerBackground()
    }
}

@Composable
fun MusicPlayerBackground(
    viewModel: MusicPlayerViewModel = hiltViewModel()
) {
    val currentSong = viewModel.currentSource.collectAsState(Song("null", playlistId = 0, songPath = "null")).value
    val isDarkTheme = isSystemInDarkTheme()
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var palette by remember { mutableStateOf<Palette?>(null) }
    var gradientColor by remember { mutableStateOf<Color>(GetThemeColor.getBackground(isDarkTheme)) }
    var textColor by remember { mutableStateOf<Color>(GetThemeColor.getText(isDarkTheme)) }
    var oppositeColor by remember { mutableStateOf<Color>(GetThemeColor.getBackground(isDarkTheme)) }

    LaunchedEffect(bitmap) {
        bitmap?.let {
            withContext(Dispatchers.IO) {
                val newPalette = Palette.from(it.asAndroidBitmap()).generate()
                val newDominantSwatch = newPalette.dominantSwatch
                val newGradientColor = newDominantSwatch?.rgb?.let { Color(it) } ?: GetThemeColor.getBackground(isDarkTheme)
                //val newTextColor = newDominantSwatch?.bodyTextColor?.let { Color(it) } ?: GetThemeColor.getText(isDarkTheme)
                val newTextColor = newPalette.lightMutedSwatch?.rgb?.let { Color(it) } ?: GetThemeColor.getText(isDarkTheme)
                val newOppositeColor = ColorUtils.blendARGB(newGradientColor.toArgb(), newTextColor.toArgb(), 0.2f).let { Color(it) }

                palette = newPalette
                gradientColor = newGradientColor
                textColor = newTextColor
                oppositeColor = newOppositeColor
            }
        }
        if (bitmap == null) {
            gradientColor = GetThemeColor.getBackground(isDarkTheme)
            textColor = GetThemeColor.getText(isDarkTheme)
            oppositeColor = GetThemeColor.getBackground(isDarkTheme)
        }
    }
    LaunchedEffect(currentSong.songPath) {
        withContext(Dispatchers.IO) {
            bitmap = MusicDataMetadata.getBitmap(currentSong.songPath)
        }
    }


    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = gradientColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxHeight(0.57f)) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!,
                        contentDescription = "desc",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize(),
                    )
                } else {
                    Image(
                        painter = painterResource(id = com.tiesiogdvd.composetest.R.drawable.img_bg_6),
                        contentDescription = "desc",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize(),
                    )
                }

                Surface(
                    color = Color.Transparent, modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0F to Color.Transparent,
                                0.7F to (gradientColor.copy(0f)),
                                1F to (gradientColor.copy(1f))
                            )
                        )
                ) {
                }
                Column(
                    modifier = Modifier
                        .padding(start = 25.dp, bottom = 40.dp)
                        .align(Alignment.BottomStart)
                ) {

                }
            }

            MediaPlayerSeekBar(mediaController = viewModel.controller)
            Text(currentSong.songName.toString(), fontSize = 16.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 10.dp))
            Text(if(currentSong.songArtist==null){""}else{ currentSong.songArtist.toString()} , fontSize = 12.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 10.dp))

            PlaybackButtons()
        }
    }
}


@Composable
fun MediaPlayerSeekBar(mediaController: MediaController?) {
    val playerState by remember(mediaController) { mutableStateOf(mediaController?.playbackState) }
    var currentPosition by remember(mediaController) { mutableStateOf(mediaController?.currentPosition ?: 0L) }
    var duration by remember(mediaController) { mutableStateOf(mediaController?.duration ?:1L)}


    val seekbarPosition = remember { mutableStateOf(0L) }
    LaunchedEffect(currentPosition) {
        seekbarPosition.value = currentPosition
    }

    LaunchedEffect(mediaController?.playbackState) {
        var position = currentPosition
        mediaController?.playbackState?.let { state ->
            while (position < mediaController.currentPosition) {
                position = mediaController.currentPosition
                currentPosition = position
                delay(16) // Update the position approximately every 16ms
            }
        }
    }

    LaunchedEffect(mediaController?.currentMediaItem) {
        duration = mediaController?.duration ?: 0L
    }

    LaunchedEffect(mediaController?.currentPosition) {
        delay(100) // Wait for 100ms after seeking
        currentPosition = mediaController?.currentPosition ?: 0L
    }


    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
        .padding(horizontal = 10.dp)
        .fillMaxWidth()) {
        Text(text = TypeConverter.formatDuration(currentPosition), color = Color.White, fontSize = 12.sp)
        Text(text = TypeConverter.formatDuration(duration), color = Color.White, fontSize = 12.sp)
    }

    Slider(
        value = currentPosition.toFloat(),
        valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
        onValueChange = { currentPosition = it.toLong() },
        onValueChangeFinished = { mediaController?.seekTo(currentPosition) },
        enabled = true,
        colors = SliderDefaults.colors(activeTrackColor = GetThemeColor.getButtonSecondary(isSystemInDarkTheme()),
            thumbColor = GetThemeColor.getPurple(isSystemInDarkTheme()).copy(0.7f)),
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .fillMaxWidth()
    )
}




@Composable
fun PlaybackButtons(viewModel: MusicPlayerViewModel = hiltViewModel()){

    var isPlaying = viewModel.isPlaying.collectAsState().value?:false

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(30.dp), color =GetThemeColor.getBackground(
            isSystemInDarkTheme()).copy(0.7f),
            modifier = Modifier
                .clickable { }
                .size(40.dp)
                .border(
                    BorderStroke(1.dp, GetThemeColor.getButton(isSystemInDarkTheme())),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(3.dp)
                .border(
                    BorderStroke(2.dp, GetThemeColor.getButton(isSystemInDarkTheme())),
                    shape = RoundedCornerShape(30.dp)
                )) {
            Icon(painterResource(id = R.drawable.ic_action_playlist),
                contentDescription = "playlists",
                modifier = Modifier
                    .size(40.dp)
                    .scale(0.75f), tint = Color.White
            )
        }


        Surface(shape = RoundedCornerShape(30.dp), color =GetThemeColor.getBackground(
            isSystemInDarkTheme()).copy(0.7f),
            modifier = Modifier
                .clickable { viewModel.playPrev() }
                .size(50.dp)
                .border(
                    BorderStroke(1.dp, GetThemeColor.getButton(isSystemInDarkTheme())),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(3.dp)
                .border(
                    BorderStroke(2.dp, GetThemeColor.getButton(isSystemInDarkTheme())),
                    shape = RoundedCornerShape(30.dp)
                )) {
            Icon(painterResource(id = R.drawable.ic_action_previous),
                contentDescription = "playlists",
                modifier = Modifier
                    .size(50.dp)
                    .scale(0.65f), tint = Color.White
            )
        }


        Surface(shape = RoundedCornerShape(30.dp), color =GetThemeColor.getBackground(
            isSystemInDarkTheme()).copy(0.7f),
            modifier = Modifier
                .size(60.dp)
                .clickable { viewModel.changePlaybackState() }
                .border(
                    BorderStroke(1.dp, GetThemeColor.getButton(isSystemInDarkTheme())),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(3.dp)
                .border(
                    BorderStroke(2.dp, GetThemeColor.getButton(isSystemInDarkTheme())),
                    shape = RoundedCornerShape(30.dp)
                )) {

            if(isPlaying){
                Icon(painterResource(id = R.drawable.ic_action_pause),
                    contentDescription = "playlists",
                    modifier = Modifier
                        .size(60.dp)
                        .scale(0.6f), tint = Color.White
                )
            }else {
                Icon(painterResource(id = R.drawable.ic_action_play),
                    contentDescription = "playlists",
                    modifier = Modifier
                        .size(60.dp)
                        .scale(0.6f), tint = Color.White
                )

            }

        }


        Surface(shape = RoundedCornerShape(30.dp), color =GetThemeColor.getBackground(
            isSystemInDarkTheme()).copy(0.7f),
            modifier = Modifier
                .clickable { viewModel.playNext() }
                .size(50.dp)
                .border(
                    BorderStroke(1.dp, GetThemeColor.getButton(isSystemInDarkTheme())),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(3.dp)
                .border(
                    BorderStroke(2.dp, GetThemeColor.getButton(isSystemInDarkTheme())),
                    shape = RoundedCornerShape(30.dp)
                )) {
            Icon(painterResource(id = R.drawable.ic_action_next),
                contentDescription = "playlists",
                modifier = Modifier
                    .size(50.dp)
                    .scale(0.65f), tint = Color.White
            )
        }


        Surface(shape = RoundedCornerShape(30.dp), color =GetThemeColor.getBackground(
            isSystemInDarkTheme()).copy(0.7f),
            modifier = Modifier
                .size(40.dp)
                .border(
                    BorderStroke(1.dp, GetThemeColor.getButton(isSystemInDarkTheme())),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(3.dp)
                .border(
                    BorderStroke(2.dp, GetThemeColor.getButton(isSystemInDarkTheme())),
                    shape = RoundedCornerShape(30.dp)
                )) {
            Icon(painterResource(id = R.drawable.ic_action_mix),
                contentDescription = "playlists",
                modifier = Modifier
                    .size(40.dp)
                    .scale(0.75f), tint = Color.White
            )
        }
    }

}




