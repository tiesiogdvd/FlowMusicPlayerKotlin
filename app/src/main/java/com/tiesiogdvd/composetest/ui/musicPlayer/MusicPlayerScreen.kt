@file:UnstableApi
@file:OptIn(ExperimentalFoundationApi::class)

package com.tiesiogdvd.composetest.ui.musicPlayer

import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.palette.graphics.Palette
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import com.ramcosta.composedestinations.annotation.Destination
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.ui.addToPlaylistDialog.AddToPlaylistDialog
import com.tiesiogdvd.composetest.ui.theme.*
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.composetest.util.TypeConverter
import com.tiesiogdvd.playlistssongstest.data.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import java.util.*
import kotlin.math.absoluteValue
import kotlin.ranges.coerceIn


@Destination
@Composable
fun MusicPlayer(
    viewModel: MusicPlayerViewModel = hiltViewModel(),
    amplitudaViewModel: AmplitudaViewModel = hiltViewModel()

) {
    FlowPlayerTheme {
        val settings = viewModel.playerScreenSettings.collectAsState(initial = null).value
        val currentSong = viewModel.currentSource
        val enableAddToPlaylist = viewModel.playlistMenuEnabled.value
        if(enableAddToPlaylist) {
            AddToPlaylistDialog(singleSong = currentSong, onDismiss = { viewModel.togglePlaylistMenu() }, songList = null)
        }

        if(settings?.enableViewPager?:true){
            MusicPlayerBackground(viewModel = viewModel, amplitudaViewModel = amplitudaViewModel)
        }else{
            MusicPlayerBackground2(viewModel = viewModel, amplitudaViewModel = amplitudaViewModel)
        }

    }
}


@Composable
fun MusicPlayerBackground2(
    viewModel: MusicPlayerViewModel = hiltViewModel(),
    amplitudaViewModel: AmplitudaViewModel = hiltViewModel()
) {

    val isDarkTheme = isSystemInDarkTheme()
    val currentSong = viewModel.currentSource.collectAsState(null).value

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
                }
            }

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)
                .fillMaxWidth()) {

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(
                        text = currentSong?.songName ?: "",
                        fontSize = 16.sp,
                        color = textColorByBrightness,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong?.songArtist ?: "",
                        fontSize = 12.sp,
                        color = textColorByBrightness,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val isSongFavorite by remember { viewModel.isSongInFavorites }
                Icon(
                    painter = painterResource(id = if(!isSongFavorite) R.drawable.ic_action_favorite else R.drawable.ic_action_favorite_filled),
                    contentDescription = "icon",
                    modifier = Modifier
                        .height(25.dp)
                        .width(25.dp)
                        .align(Alignment.Top)
                        .clickable {
                            currentSong?.let { viewModel.toggleFavorite(it) }
                        },
                    tint = textColorByBrightness
                )
            }


            currentSong?.let {
                MediaPlayerSeekBar(it, textColorByBrightness, viewModel = viewModel)
            }

            currentSong?.let { PlaybackItems(currentSong = it, viewModel = viewModel, amplitudaViewModel = amplitudaViewModel) }

        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicPlayerBackground(
    viewModel: MusicPlayerViewModel = hiltViewModel(),
    amplitudaViewModel: AmplitudaViewModel = hiltViewModel()
) {
    val currentSong = viewModel.currentSource.collectAsState(null).value
    val songList = viewModel.songsList.collectAsState(null).value

    val source = viewModel.sourceSettings.collectAsState(initial = null).value

    val curSongIndex = viewModel.curSongIndex.collectAsState().value
    val pagerState = rememberPagerState(initialPage = curSongIndex ?: viewModel.indexFromMusicSource() ?: 0, initialPageOffsetFraction = 0f)
    viewModel.pagerState = pagerState

    val fling = PagerDefaults.flingBehavior(state = pagerState, pagerSnapDistance = PagerSnapDistance.atMost(1))


    val shuffleArray = viewModel.shuffleList.collectAsState().value
    print(shuffleArray)



    LaunchedEffect(curSongIndex) {
      //  pagerState.animateScrollToPage(curSongIndex ?: viewModel.indexFromMusicSource() ?: 0)
        if(viewModel.shuffleStatus.value == false){
            pagerState.animateScrollToPage(curSongIndex ?: viewModel.indexFromMusicSource() ?: 0)
        }else{
            pagerState.animateScrollToPage(shuffleArray.indexOf(curSongIndex) ?: viewModel.indexFromMusicSource() ?: 0)
        }

    }

    LaunchedEffect(pagerState.currentPage) {
        println("page change")
        delay(500)
        println(curSongIndex)
        if (currentSong != null) {
            viewModel.playPage(pagerState.currentPage, currentSong)
        }
  /*      if (pagerState.currentPage != curSongIndex || viewModel.getSongIndex(currentSong) != curSongIndex) {
            viewModel.playIndex(pagerState.currentPage)
            println("test")
        }*/
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {


       println(songList)

        songList?.size?.let { size ->
            HorizontalPager(
                pageCount = size, state = pagerState, flingBehavior = fling, modifier = Modifier
            ) {
                Box(contentAlignment = Alignment.TopCenter, modifier = Modifier


                    .graphicsLayer {
                        val pageOffset = (
                                (pagerState.currentPage - it) + pagerState
                                    .currentPageOffsetFraction
                                ).absoluteValue

                        // We animate the alpha, between 50% and 100%
                        alpha = lerp(
                            start = 0f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )

                        scaleX = lerp(
                            start = 2f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )

                        scaleY = lerp(
                            start = 2f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )

                        rotationY = lerp(
                            start = -20f,
                            stop = 0f,
                            fraction = 1f - pageOffset.coerceIn(0f, 10f)
                        )

                        translationX = lerp(
                            start = -150.dp.toPx(),
                            stop = 0.dp.toPx(),
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        clip = false
                    }
                ) {
                    println(viewModel.shuffleStatus.value)
                    println(viewModel.shuffleList)
                    SongPagerItem(index = it, song = if(viewModel.shuffleStatus.value == false) songList.get(it) else songList.get(shuffleArray.get(it)), viewModel = viewModel)

                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(bottom = 100.dp), verticalArrangement = Arrangement.Bottom
        ) {
            currentSong?.let {
                //MediaPlayerSeekBar(it, textColorByBrightness)
                MediaPlayerSeekBar(it, viewModel = viewModel)
            }
            currentSong?.let { PlaybackItems(currentSong = it, viewModel = viewModel, amplitudaViewModel = amplitudaViewModel) }
        }
    }
}


@Composable
fun ExpandingTopLine(
    modifier: Modifier = Modifier,
    color: Color = GetThemeColor.getPurple(isSystemInDarkTheme()),
    textColor: Color = GetThemeColor.getPurple(isSystemInDarkTheme()),
    content: @Composable BoxScope.() -> Unit
    ) {

    var boxHeightTarget by remember { mutableStateOf(25.dp) }
    val boxHeight by animateDpAsState(
        targetValue = boxHeightTarget,
        animationSpec = spring(stiffness = 100f)
    )

    var boxWidthTarget by remember { mutableStateOf(50.dp) }
    val boxWidth by animateDpAsState(
        targetValue = boxWidthTarget,
        animationSpec = spring(stiffness = 100f)
    )

    val opacityTarget = if (boxWidthTarget == 50.dp) 0f else 0.8f
    val opacity by animateFloatAsState(
        targetValue = opacityTarget,
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(boxHeight) {
        if (boxHeight > 100.dp) {
            boxWidthTarget = 400.dp
        } else {
            boxWidthTarget = 50.dp
        }
    }

    val minHeight = 25.dp
    val maxHeight = 400.dp
    val openFromHeight = 50.dp
    val closeFromHeight = 350.dp
    val dragSensitivity = 30f
    val invisibleDragAreaHeight = 400.dp

    val previouslyOpened = remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(top = 20.dp)
    ) {
        // Wrapper box for the drag gesture
        Box(contentAlignment = Alignment.TopEnd,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures(onDragEnd = {
                        when {
                            boxHeight <= openFromHeight -> {
                                boxHeightTarget = minHeight
                                previouslyOpened.value = false
                            }
                            boxHeight > openFromHeight && boxHeight < closeFromHeight -> {
                                if (previouslyOpened.value) {
                                    boxHeightTarget = minHeight
                                    previouslyOpened.value = false
                                } else {
                                    boxHeightTarget = maxHeight
                                    previouslyOpened.value = true
                                }
                            }
                            boxHeight >= closeFromHeight -> {
                                boxHeightTarget = maxHeight
                                previouslyOpened.value = true
                            }
                        }
                    }) { change, dragAmount ->
                        val newHeight = boxHeight + (dragAmount.y.toDp() * dragSensitivity)
                        boxHeightTarget = newHeight.coerceIn(minHeight, maxHeight)
                        change.consume()
                    }

                }
        ) {
            // Invisible drag area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(invisibleDragAreaHeight)
            )

            // Visible box
            Box(
                modifier = Modifier
                    .width(boxWidth)
                    .height(boxHeight)
                    .blur(radius = 20.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    .alpha(opacity)
                    .background(color)
            )
        }
        
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight), content = content)

        Box(contentAlignment = Alignment.TopEnd,
            modifier = Modifier
                .fillMaxWidth()
        ){
            AnimatedVisibility(visible = boxHeight<200.dp, enter = Transitions.enter, exit = Transitions.exit) {
                Box(contentAlignment = Alignment.Center,modifier = Modifier.clickable(onClick = {boxHeightTarget = maxHeight}, indication = rememberRipple(bounded = false), interactionSource = remember {
                    MutableInteractionSource()
                })) {
                    // Background box with blur effect
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(40.dp)
                            .blur(radius = 15.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                            .clip(RoundedCornerShape(50.dp))
                            .background(color)
                            .alpha(0.9f)
                    )

                    // Content box without blur effect
                    Box() {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Lyrics", color = textColor, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 10.dp))

                            Icon(
                                painter = painterResource(id = R.drawable.ic_action_lyrics),
                                contentDescription = "icon",
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(20.dp)
                                    .zIndex(4f)
                                    .scale(scaleY = 1f, scaleX = 0.8f),
                                tint = textColor
                            )
                        }
                    }
                }


            }

            AnimatedVisibility(visible = boxHeight>200.dp) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_action_close),
                    contentDescription = "icon",
                    modifier = Modifier
                        .height(40.dp)
                        .width(40.dp)
                        .zIndex(4f)
                        .padding(top = 10.dp, end = 10.dp)
                        .scale(scaleY = 1f, scaleX = 0.8f)
                        .clickable(
                            onClick = { boxHeightTarget = minHeight },
                            indication = rememberRipple(bounded = false),
                            interactionSource = remember {
                                MutableInteractionSource()
                            }),
                    tint = textColor)
            }

        }
    }
}



@Composable
fun SongPagerItem(
    index:Int,
    song: Song,
    viewModel: MusicPlayerViewModel = hiltViewModel()
) {

    val currentSongLyrics = viewModel.curSongLyrics.collectAsState(null).value
    val curLyricsIndex = viewModel.curLyricsIndex.collectAsState().value ?: -1
    val totalLyricsCount = viewModel.totalLyricsCount.collectAsState().value

    val currentSong = viewModel.currentSource.collectAsState(null).value


    val isDarkTheme = isSystemInDarkTheme()
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(Unit) {
        bitmap = MusicDataMetadata.getBitmap(song.songPath)
    }

    var palette by remember { mutableStateOf<Palette?>(null) }
    var gradientColor by remember { mutableStateOf<Color>(GetThemeColor.getBackground(isDarkTheme)) }
    var gradientSecondary by remember {
        mutableStateOf<Color>(
            GetThemeColor.getBackground(
                isDarkTheme
            )
        )
    }

    var textColor by remember { mutableStateOf<Color>(GetThemeColor.getText(isDarkTheme)) }
    var textColorByBrightness by remember {
        mutableStateOf<Color>(
            GetThemeColor.getBackground(
                isDarkTheme
            )
        )
    }

    LaunchedEffect(bitmap) {
        bitmap?.let {
            withContext(Dispatchers.Default) {
                val newPalette = Palette.from(it.asAndroidBitmap()).generate()
                val newDominantSwatch = newPalette.dominantSwatch
                val newGradientColor =
                    newDominantSwatch?.rgb?.let { Color(it) } ?: GetThemeColor.getBackground(
                        isDarkTheme
                    )

                if (GetThemeColor.isDark(newGradientColor)) {
                    val newGradientSecondary = newPalette.darkMutedSwatch?.rgb?.let { Color(it) }
                        ?: GetThemeColor.getBackground(isDarkTheme)
                    gradientSecondary = newGradientSecondary
                } else {
                    //val newGradientSecondary = newPalette.darkMutedSwatch?.rgb?.let { Color(it) } ?: GetThemeColor.getBackground(isDarkTheme)
                    gradientSecondary = newGradientColor
                }

                val newTextColor =
                    newDominantSwatch?.rgb?.let { Color(it) } ?: GetThemeColor.getText(isDarkTheme)
                val newTextColorBrightness = if (GetThemeColor.isDark(newGradientColor)) {
                    GetThemeColor.getText(true)
                } else {
                    GetThemeColor.getText(false)
                }
                palette = newPalette
                gradientColor = newGradientColor
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


    Box(modifier = Modifier
        .fillMaxWidth(0.9f)
        .zIndex(1.01f)
    ) {
        ExpandingTopLine(modifier = Modifier.align(Alignment.TopCenter), color = gradientSecondary, textColor = textColorByBrightness) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Wrap the LazyColumn with a Box and apply padding to the bottom
                Box(modifier = Modifier.padding(bottom = 20.dp)) {
                    LazyColumn(contentPadding = PaddingValues(10.dp),modifier = Modifier.padding(bottom = 5.dp)) {
                        item {

                            /*Text(text = "Lyrics", textAlign = TextAlign.Center, color = textColorByBrightness, modifier = Modifier
                                .padding(bottom = 30.dp)
                                .clipToBounds())*/

                            Row() {
                                val lessLyricsAvailable = derivedStateOf { curLyricsIndex != -1 && totalLyricsCount != null && curLyricsIndex > 0 }
                                val moreLyricsAvailable = derivedStateOf { curLyricsIndex != -1 && totalLyricsCount != null && curLyricsIndex + 1 < totalLyricsCount }
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_action_navigate_previous),
                                    contentDescription = "icon",
                                    modifier = Modifier
                                        .height(30.dp)
                                        .width(30.dp)
                                        .align(Alignment.CenterVertically)
                                        .clickable {
                                            if (lessLyricsAvailable.value) {
                                                viewModel.loadLyricsIndex(curLyricsIndex - 1)
                                            }
                                        },
                                    tint = textColorByBrightness.copy(alpha = if(lessLyricsAvailable.value) 1f else 0.3f),
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_action_navigate_next),
                                    contentDescription = "icon",
                                    modifier = Modifier
                                        .height(30.dp)
                                        .width(30.dp)
                                        .align(Alignment.CenterVertically)
                                        .clickable {
                                            if (moreLyricsAvailable.value) {
                                                viewModel.loadLyricsIndex(curLyricsIndex + 1)
                                            }
                                        },
                                    tint = textColorByBrightness.copy(alpha = if(moreLyricsAvailable.value) 1f else 0.3f),
                                )

                            //GlideImage(model = "", contentDescription = )
                            }

                            SelectionContainer() {
                                Row() {
                                    Text(text = currentSongLyrics.toString(), fontFamily = Jost, fontSize = 15.sp, color = textColorByBrightness, maxLines = 800)
                                }

                            }
                        }
                    }
                }
            }
        }
    }



    Surface(
    modifier = Modifier
        .fillMaxSize(),
    color = gradientColor
    ) {
        Box(
            modifier = Modifier
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
    }

    Column(modifier = Modifier.fillMaxSize()


    ) {
        val zoomState = rememberZoomableState()

        Box(modifier = Modifier.fillMaxHeight(0.57f)) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!,
                    contentDescription = "desc",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .zoomable(state = zoomState)
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


        }


        /*AnimatedVisibility(bitmap!=null, enter = Transitions.enter, exit = Transitions.exit) {
        Image(
            bitmap = bitmap!!,
            contentDescription = "desc",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize(),
        )
    }*/

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
            .fillMaxWidth()) {

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = currentSong?.songName ?: "",
                    fontSize = 16.sp,
                    color = textColorByBrightness,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentSong?.songArtist ?: "",
                    fontSize = 12.sp,
                    color = textColorByBrightness,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val isSongFavorite by remember { viewModel.isSongInFavorites }
            Icon(
                painter = painterResource(id = if(!isSongFavorite) R.drawable.ic_action_favorite else R.drawable.ic_action_favorite_filled),
                contentDescription = "icon",
                modifier = Modifier
                    .height(25.dp)
                    .width(25.dp)
                    .align(Alignment.Top)
                    .clickable {
                        currentSong?.let { viewModel.toggleFavorite(it) }
                    },
                tint = textColorByBrightness
            )
        }
    }
}


@Composable
fun MediaPlayerSeekBar(song: Song, textColor:Color = Color.Black, viewModel: MusicPlayerViewModel = hiltViewModel()) {
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
fun PlaybackItems(
    currentSong:Song,
    viewModel: MusicPlayerViewModel = hiltViewModel(),
    amplitudaViewModel: AmplitudaViewModel = hiltViewModel()
){
    val isPlaying = viewModel.isPlaying.collectAsState().value?:false
    val isDarkTheme = isSystemInDarkTheme()
    val amplituda = amplitudaViewModel.amplituda.collectAsState().value
    var playbackPosition = viewModel.currentPosition.collectAsState().value

    val index = viewModel.curSongIndex.collectAsState().value

    val settings = viewModel.playerScreenSettings.collectAsState(initial = null).value

    val timer = remember { mutableStateOf<Timer?>(null) }
    val coroutineScope = rememberCoroutineScope()

    var sync = remember { 0 }

    LaunchedEffect(isPlaying, currentSong){
        viewModel.currentPosition.update {
            viewModel.getCurrentPosition().toInt()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            timer.value?.cancel()
        }
    }

    LaunchedEffect(playbackPosition) {
        delay(500)
        viewModel.currentPosition.update {
            viewModel.getCurrentPosition().toInt()
        }
    }

    LaunchedEffect(isPlaying, currentSong) {
        if (isPlaying) {
            timer.value = kotlin.concurrent.timer("PlaybackTimer", false, 0L, 5) {
                viewModel.currentPosition.value += 5
                sync += 5

                if (sync > 1000) {
                    // Use the coroutineScope to launch a coroutine
                    coroutineScope.launch {
                        viewModel.currentPosition.value = viewModel.getCurrentPosition().toInt()
                        sync = 0
                    }
                }
            }
        } else {
            timer.value?.cancel()
        }
    }
    
    Box(modifier = Modifier.height(150.dp), contentAlignment = Alignment.Center) {

        if(settings?.showSeekbar?:true){
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
        }

        val shuffleList = viewModel.shuffleList.collectAsState().value
        val shuffleState = viewModel.shuffleStatus.collectAsState().value
        val scope = rememberCoroutineScope()
        val repeatMode = viewModel.repeatMode
        val icon = remember { when(repeatMode.value){
            Player.REPEAT_MODE_ALL -> R.drawable.ic_action_repeat
            Player.REPEAT_MODE_ONE -> R.drawable.ic_action_repeat_one
            Player.REPEAT_MODE_OFF -> R.drawable.ic_action_no_repeat
            else -> R.drawable.ic_action_no_repeat
        } }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            if(settings?.showAddToPlaylist?:true) {
                PlayerAction(icon = R.drawable.ic_action_playlist, onClick = {viewModel.togglePlaylistMenu()}, contentDescription = "Playlists", iconScale = 0.75f, size = 40.dp)
            }
            PlayerAction(icon = R.drawable.ic_action_previous, onClick = {
               /* if(shuffleState==true) {
                    shuffleList.indexOf(index?.minus(1))
                }else{
                    index?.minus(1)
                }*/
                scope.launch {
                   // viewModel.pagerState.animateScrollToPage(viewModel.pagerState.currentPage-1)
                }
                viewModel.playPrev()
                //viewModel.curSongIndex.update { index?.minus(1) }
                //viewModel.playPrevDelay(500)
                //viewModel.playIndexDelay(500)
                                                                         }, contentDescription = "Previous", iconScale = 0.65f, size = 50.dp)
            PlayerAction(icon = if(isPlaying || (viewModel.getPlaybackState() == ExoPlayer.STATE_BUFFERING && viewModel.getPlaybackState() != ExoPlayer.STATE_IDLE)){R.drawable.ic_action_pause}else{R.drawable.ic_action_play}, onClick = {viewModel.changePlaybackState()}, contentDescription = "Play/Pause", iconScale = 0.5f, size = 70.dp)
            PlayerAction(icon = R.drawable.ic_action_next, onClick = {
                scope.launch {
                 //   viewModel.pagerState.animateScrollToPage(viewModel.pagerState.currentPage+1)
                }
                viewModel.playNext()

              /*  viewModel.curSongIndex.update {
                    index?.plus(1)
                }
                viewModel.playNextDelay(500)*/
                //viewModel.playIndexDelay(500)
                                                                     }, contentDescription = "Next", iconScale = 0.65f, size = 50.dp)
            if(settings?.showRepeatMode?:true){
                PlayerAction(icon =
                when(repeatMode.value){
                    Player.REPEAT_MODE_ALL -> R.drawable.ic_action_repeat
                    Player.REPEAT_MODE_ONE -> R.drawable.ic_action_repeat_one
                    Player.REPEAT_MODE_OFF -> R.drawable.ic_action_no_repeat
                    else -> R.drawable.ic_action_no_repeat},
                    onClick = {viewModel.toggleRepeatMode()}, contentDescription = "Mix", iconScale = 0.75f, size = 40.dp)
            }

        }
    }
}

@Composable
fun PlayerAction(
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


