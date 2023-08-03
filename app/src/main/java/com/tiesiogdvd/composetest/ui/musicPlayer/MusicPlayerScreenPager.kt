package com.tiesiogdvd.composetest.ui.musicPlayer

import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.palette.graphics.Palette
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import com.ramcosta.composedestinations.annotation.Destination
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.ui.theme.FlowPlayerTheme
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.composetest.util.TypeConverter
import com.tiesiogdvd.playlistssongstest.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

@Destination
@Composable
fun MusicPlayerPager() {
    FlowPlayerTheme {
        MusicPlayerBackground()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicPlayerBackgroundPager(
    viewModel: MusicPlayerViewModel = hiltViewModel()
) {
    val currentSong = viewModel.currentSource.collectAsState(null).value

    val songList = viewModel.songsList.collectAsState(null).value

    val isDarkTheme = isSystemInDarkTheme()
    //var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    val bitmap = viewModel.bitmap.collectAsState().value
    var palette by remember { mutableStateOf<Palette?>(null) }
    var gradientColor by remember { mutableStateOf<Color>(GetThemeColor.getBackground(isDarkTheme)) }
    var gradientSecondary by remember { mutableStateOf<Color>(GetThemeColor.getBackground(isDarkTheme)) }

    var textColor by remember { mutableStateOf<Color>(GetThemeColor.getText(isDarkTheme)) }
    var textColorByBrightness by remember { mutableStateOf<Color>(GetThemeColor.getBackground(isDarkTheme)) }

    LaunchedEffect(bitmap) {
        bitmap?.let {
            withContext(Dispatchers.IO) {
                val newPalette = Palette.from(it.asAndroidBitmap()).generate()
                val newDominantSwatch = newPalette.dominantSwatch
                val newGradientColor = newDominantSwatch?.rgb?.let { Color(it) } ?: GetThemeColor.getBackground(isDarkTheme)

                if(GetThemeColor.isDark(newGradientColor)){
                    val newGradientSecondary = newPalette.darkMutedSwatch?.rgb?.let { Color(it) } ?: GetThemeColor.getBackground(isDarkTheme)
                    gradientSecondary = newGradientSecondary
                }else{
                    //val newGradientSecondary = newPalette.darkMutedSwatch?.rgb?.let { Color(it) } ?: GetThemeColor.getBackground(isDarkTheme)
                    gradientSecondary = newGradientColor
                }

                val newTextColor = newDominantSwatch?.rgb?.let { Color(it) } ?: GetThemeColor.getText(isDarkTheme)
                val newTextColorBrightness = if(GetThemeColor.isDark(newGradientColor)){GetThemeColor.getText(true)}else{GetThemeColor.getText(false)}
                palette = newPalette
                gradientColor = newGradientColor
               // gradientSecondary = newGradientSecondary
                textColor = newTextColor
                textColorByBrightness = newTextColorBrightness
            }
        }
        if (bitmap == null) {
            gradientColor = GetThemeColor.getBackground(isDarkTheme)
            gradientSecondary = GetThemeColor.getBackground(isDarkTheme)
            textColor = GetThemeColor.getText(isDarkTheme)
            textColorByBrightness = GetThemeColor.getText(isDarkTheme)
        }
    }



    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = gradientColor
    ) {

        Surface(
            color = Color.Transparent, modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0F to Color.Transparent,
                        0.7F to gradientSecondary.copy(0f),
                        1F to gradientSecondary.copy(1f), tileMode = TileMode.Clamp
                    )
                )
        ) {
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Crossfade(targetState = bitmap, animationSpec = tween(durationMillis = 500)) { currentBitmap ->
                Box(modifier = Modifier.fillMaxHeight(0.57f)) {
                    if (currentBitmap != null) {
                        Image(
                            bitmap = currentBitmap,
                            contentDescription = "desc",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize(),
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_bg_8),
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


                    val pagerState = rememberPagerState(initialPage = 5)
                    Surface(color = Color.Black,modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)) {
                        songList?.size?.let {size ->
                            HorizontalPager(pageCount = size, state = pagerState) {
                                songList.get(it).songName?.let { it1 -> Text(text = it1) }
                            }
                    }


                    }
                }
            }





            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)
                .fillMaxWidth()){
                Column(modifier = Modifier, verticalArrangement = Arrangement.Center) {
                    Text(if(currentSong!=null){currentSong.songName.toString()}else{""}, fontSize = 16.sp, color = textColorByBrightness, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier)
                    Text(if(currentSong!=null && currentSong.songArtist==null){""}else{ currentSong?.songArtist.toString()} , fontSize = 12.sp, color = textColorByBrightness, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier)
                }

                Icon(
                    painter = painterResource(id = R.drawable.ic_action_favorite),
                    contentDescription = "icon",
                    modifier = Modifier
                        .height(25.dp)
                        .width(25.dp)
                        .align(Alignment.CenterVertically)
                        .clickable {},
                    tint = textColorByBrightness
                )
            }


            currentSong?.let {
                MediaPlayerSeekBarPager(it, textColorByBrightness)
            }

            currentSong?.let { PlaybackItems(currentSong = it) }

        }
    }
}


@Composable
fun MediaPlayerSeekBarPager(song: Song, textColor:Color, viewModel: MusicPlayerViewModel = hiltViewModel()) {
    val seekbarPosition = viewModel.currentPosition.collectAsState().value

    Slider(
        value = seekbarPosition.toFloat(),
        valueRange = 0f..(song.length.toFloat().coerceAtLeast(1f)),
        onValueChange = { value ->
            viewModel.currentPosition.update {value.toInt()}},
        onValueChangeFinished = {
            viewModel.seekToPosition(seekbarPosition.toLong())
                                },
        enabled = true,
        colors = SliderDefaults.colors(
            activeTrackColor = GetThemeColor.waveProgress(isSystemInDarkTheme()).copy(0.4f),
            inactiveTrackColor = GetThemeColor.waveBackground(isSystemInDarkTheme()).copy(0.4f),
            thumbColor = GetThemeColor.getPurple(isSystemInDarkTheme()).copy(0.7f)
        ),
        modifier = Modifier
            .offset(y = 10.dp)
    )
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
        .padding(horizontal = 10.dp)
        .offset(y = -5.dp)
        .fillMaxWidth()) {
        Text(text = TypeConverter.formatDuration(seekbarPosition.toLong()), color = textColor, fontSize = 12.sp)
        Text(text = TypeConverter.formatDuration(song.length), color = textColor, fontSize = 12.sp)
    }
}




@Composable
fun PlaybackItemsPager(
    currentSong:Song,
    viewModel: MusicPlayerViewModel = hiltViewModel(),
    amplitudaViewModel: AmplitudaViewModel = hiltViewModel()
){
    val isPlaying = viewModel.isPlaying.collectAsState().value?:false
    val isDarkTheme = isSystemInDarkTheme()
    val amplituda = amplitudaViewModel.amplituda.collectAsState().value
    var playbackPosition = viewModel.currentPosition.collectAsState().value

    LaunchedEffect(isPlaying){
        viewModel.currentPosition.update {
            viewModel.getCurrentPosition().toInt()
        }
    }

    LaunchedEffect(currentSong){
        viewModel.currentPosition.update {
            viewModel.getCurrentPosition().toInt()
        }
    }

    LaunchedEffect(playbackPosition){
        delay(1000)
        if(isPlaying){
            viewModel.currentPosition.update {viewModel.getCurrentPosition().toInt()}
        }
    }


    LaunchedEffect(playbackPosition){
        if(isPlaying){
            viewModel.currentPosition.update {it+10}
        }
    }
    
    Box(modifier = Modifier.height(150.dp), contentAlignment = Alignment.Center) {
        AnimatedVisibility(visible = amplituda!=null, enter = fadeIn(), exit = fadeOut()) {
            AndroidView(factory = {context ->
                WaveformSeekBar(context).apply {
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 600.dp.value.toInt())
                    setBackgroundColor(Color.Transparent.toArgb())
                    waveWidth = 17.dp.value
                    waveGap= 9.dp.value
                    waveMinHeight = 20.dp.value
                    waveCornerRadius= 50.dp.value
                    setOnClickListener(object :OnClickListener{
                        override fun onClick(v: View?) {
                            viewModel.seekToPosition(playbackPosition.toLong())
                        }
                    })

                    onProgressChanged = object: SeekBarOnProgressChanged{
                        override fun onProgressChanged(
                            waveformSeekBar: WaveformSeekBar,
                            progress: Float,
                            fromUser: Boolean
                        ) {
                            if(fromUser){
                                viewModel.currentPosition.update {progress.toInt()}
                                playbackPosition = progress.toInt()
                            }
                        }
                    }
                    waveBackgroundColor = GetThemeColor.waveBackground(isDarkTheme).toArgb()
                    waveProgressColor = GetThemeColor.waveProgress(isDarkTheme).toArgb()
                    sample = amplituda
                    maxProgress = currentSong.length.toFloat()
                    visibleProgress = currentSong.length.toFloat()/3
                }
            }){
                if (amplituda != null) {
                    if(!it.sample.contentEquals(amplituda)){
                        it.setSampleFrom(amplituda)
                    }
                    it.maxProgress = currentSong.length.toFloat()
                    it.visibleProgress = currentSong.length.toFloat()/3
                    it.progress = playbackPosition.toFloat()
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            PlayerActionPager(icon = R.drawable.ic_action_playlist, onClick = {}, contentDescription = "Playlists", iconScale = 0.75f, size = 40.dp)
            PlayerActionPager(icon = R.drawable.ic_action_previous, onClick = {viewModel.playPrev()}, contentDescription = "Previous", iconScale = 0.65f, size = 50.dp)
            PlayerActionPager(icon = if(isPlaying || (viewModel.getPlaybackState() == ExoPlayer.STATE_BUFFERING && viewModel.getPlaybackState() != ExoPlayer.STATE_IDLE)){R.drawable.ic_action_pause}else{R.drawable.ic_action_play}, onClick = {viewModel.changePlaybackState()}, contentDescription = "Play/Pause", iconScale = 0.5f, size = 70.dp)
            PlayerActionPager(icon = R.drawable.ic_action_next, onClick = {viewModel.playNext()}, contentDescription = "Next", iconScale = 0.65f, size = 50.dp)
            PlayerActionPager(icon = R.drawable.ic_action_mix, onClick = {}, contentDescription = "Mix", iconScale = 0.75f, size = 40.dp)
        }
    }
}

@Composable
fun PlayerActionPager(
    size: Dp = 30.dp,
    icon: Int,
    contentDescription: String = "PlayerAction",
    iconScale:Float = 0.7f,
    onClick:() -> Unit

    ){
    Surface(shape = RoundedCornerShape(size), color =GetThemeColor.getButton(
        isSystemInDarkTheme()).copy(0.6f),
        modifier = Modifier
            .clickable {
                onClick()
            }
            .size(size)
            .border(
                BorderStroke(2.dp, GetThemeColor.getButtonSecondary(isSystemInDarkTheme())),
                shape = RoundedCornerShape(size)
            )
            .padding(3.dp)
            .border(
                BorderStroke(2.dp, GetThemeColor.getButtonSecondary(isSystemInDarkTheme())),
                shape = RoundedCornerShape(size)
            )) {
        Icon(painterResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier
                .size(40.dp)
                .scale(iconScale), tint = Color.White
        )
    }
}


