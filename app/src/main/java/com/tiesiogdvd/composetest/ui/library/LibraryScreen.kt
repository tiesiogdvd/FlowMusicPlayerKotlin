package com.tiesiogdvd.composetest.ui.library

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import com.tiesiogdvd.composetest.R
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.*
import com.airbnb.lottie.compose.*
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.tiesiogdvd.composetest.ui.destinations.LibraryPlaylistDestination
import com.tiesiogdvd.composetest.ui.destinations.LibraryPlaylistsScreenDestination
import com.tiesiogdvd.composetest.ui.lottieAnimation.LottieCustom
import com.tiesiogdvd.composetest.ui.musicPlayer.MusicPlayerViewModel
import com.tiesiogdvd.composetest.ui.theme.FlowPlayerTheme
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import com.tiesiogdvd.composetest.ui.theme.Transitions
import com.tiesiogdvd.playlistssongstest.data.PlaylistWithSongs
import kotlinx.coroutines.*


@RootNavGraph(start = true)
@Destination
@Composable
fun Library(navigator: DestinationsNavigator, viewModel: LibraryViewModel = hiltViewModel(),) {
    val backgroundImage = viewModel.backgroundBitmap.collectAsState().value

    FlowPlayerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = GetThemeColor.getBackground(isSystemInDarkTheme())
        ) {
            Crossfade(targetState = backgroundImage, animationSpec = tween(durationMillis = 500)) { image ->
                val currentImage = image ?: painterResource(id = R.drawable.img_bg_8)
                val contentDescription = if (image != null) "Background Image" else "Default Image"

                when (currentImage) {
                    is ImageBitmap -> {
                        Image(
                            bitmap = currentImage,
                            contentDescription = contentDescription,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .graphicsLayer {
                                    renderEffect = BlurEffect(200f, 200f, TileMode.Mirror)
                                }
                                .fillMaxSize(),
                        )
                    }
                    is Painter -> {
                        Image(
                            painter = currentImage,
                            contentDescription = contentDescription,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .graphicsLayer {
                                    renderEffect = BlurEffect(200f, 200f, TileMode.Mirror)
                                }
                                .fillMaxSize(),
                        )
                    }
                }
            }



            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp, start = 25.dp)
            ) {
            Text("Library", fontSize = 45.sp, modifier = Modifier.padding(bottom = 15.dp))
            Text("Recent Playlists", fontSize = 22.sp, modifier = Modifier.padding(bottom = 3.dp))
            PlaylistList(navigator = navigator)
            menuItem(text = "All Songs", onClick = {
                if(viewModel.playlistAllSongs.value!=null){
                    navigator.navigate(LibraryPlaylistDestination(viewModel.playlistAllSongs.value!!))}
            })
            menuItem(text = "Playlists", onClick = { navigator.navigate(LibraryPlaylistsScreenDestination)})
            menuItem(text = "Favorites", onClick = {
                if(viewModel.playlistFavorites.value!=null){
                    navigator.navigate(LibraryPlaylistDestination(viewModel.playlistFavorites.value!!))}
            })
            menuItem(text = "Folders", onClick = {})
            menuItem(text = "Storage", onClick = {})
                
            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistList(
    viewModel: LibraryViewModel = hiltViewModel(),
    viewModelMusicPlayer: MusicPlayerViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
){
    val playlistWithSongs = viewModel.playlistsWithSongs.collectAsState(initial = listOf()).value
    var isVisible by remember { mutableStateOf(true) }
    val scrollOffset = viewModel.currentScroll.collectAsState().value

    val curSongBitmap = viewModelMusicPlayer.bitmap.collectAsState().value

    val listState = rememberLazyListState()

    val headerScrollConnection = remember{
        object: NestedScrollConnection {

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaX = available.x
                viewModel.currentScroll.value+=deltaX
                if(viewModel.currentScroll.value>0f){
                    viewModel.currentScroll.value=0f
                }
                println(viewModel.currentScroll.value)

                /*scrollOffset.value += deltaX // Change 0.5f to adjust the parallax speed
                if(scrollOffset.value>0){
                    scrollOffset.value=0f
                }*/
               // println(available)

                return super.onPreScroll(available, source)
            }

        }
    }
    LazyRow(state = listState,modifier = Modifier
        .nestedScroll(headerScrollConnection)

        .wrapContentHeight()
        .padding(bottom = 15.dp)){
        items(playlistWithSongs.size, key = {playlistWithSongs.get(it).playlist.id}){index ->
            val density = LocalDensity.current
            var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.Default) {
                    bitmap = viewModel.getPlaylistBitmap(playlistWithSongs.get(index))
                }
            }

            LaunchedEffect(listState.firstVisibleItemIndex, bitmap){

                if(listState.firstVisibleItemIndex==0){
                    viewModel.setBitmap(curSongBitmap)
                }else{
                    if(listState.firstVisibleItemIndex+1==index){
                        viewModel.setBitmap(bitmap)
                        println(index)
                        println(listState.firstVisibleItemIndex)
                    }
                }
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInHorizontally {
                    // Slide in from 40 dp from the top.
                    with(density) { -40.dp.roundToPx() }
                } + expandHorizontally(
                    expandFrom = Alignment.End
                ) + fadeIn(
                    initialAlpha = 0.35f
                ),
                exit = slideOutHorizontally() + shrinkHorizontally(shrinkTowards = Alignment.End) +  fadeOut()
            ) {
                //Text("Hello", Modifier.fillMaxWidth().height(200.dp))
                Column(modifier = Modifier
                    .combinedClickable(
                        onClick = {
                            //isVisible = false
                            navigator.navigate(
                                LibraryPlaylistDestination(
                                    playlistWithSongs.get(
                                        index
                                    ).playlist
                                )
                            )
                        },

                        onLongClick = {
                            isVisible = false
                            //viewModel.removePlaylist(playlistWithSongs.get(it).playlist)
                        })
                    .animateItemPlacement())

                {
                    PlaylistItem(
                        playlistWithSongs = playlistWithSongs.get(index),
                        viewModel,
                        scrollOffsetX = scrollOffset,
                        index = index,
                        bitmap = bitmap)
                }
            }
        }

    }
}


@Composable
fun menuItem(
    text:String,
    onClick: () -> Unit
){
    Surface(shape = RoundedCornerShape(30.dp),
        border = BorderStroke(2.dp, GetThemeColor.getBackgroundThird(isSystemInDarkTheme()).copy(0.5f)),
        color = GetThemeColor.getButton(isSystemInDarkTheme()), modifier = Modifier
            .height(50.dp)
            .padding(top = 15.dp)
            .padding(end = 20.dp)
            .fillMaxWidth()
            .clickable { onClick() }) {
        Box(
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(text = text, fontSize = 20.sp, color = GetThemeColor.getText(isSystemInDarkTheme()), modifier = Modifier
                .padding(start = 10.dp, bottom = 1.dp)
                .height(50.dp)
                .wrapContentSize())
        }
    }
}


@Composable
fun PlaylistItem(
    playlistWithSongs:PlaylistWithSongs,
    viewModel: LibraryViewModel,
    scrollOffsetX: Float,
    index:Int,
    bitmap:ImageBitmap?){


    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val orgOffset = with(LocalDensity.current) { -scrollOffsetX.toDp() }
    val itemOffset = orgOffset -145.dp.times(index)
    val ratio = screenWidthDp / 40.dp
    val adjustedOffset = itemOffset.div(ratio)


    val isPlaying = viewModel.currentPlayingPlaylist.collectAsState().value == playlistWithSongs.playlist.id && viewModel.isPlaying.collectAsState(initial = false).value == true


    Surface( border = BorderStroke(2.dp, GetThemeColor.getBackground(isSystemInDarkTheme())),
        modifier = Modifier
            .padding(end = 5.dp),
        shape = RoundedCornerShape(10.dp),
        color = GetThemeColor.getButton(isSystemInDarkTheme())
    ){
        Box(modifier = Modifier.size(150.dp)){
            Column(
                modifier = Modifier
                    .padding(5.dp),
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!,
                        contentDescription = "desc",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(130.dp)
                            .scale(1.6f)
                            .offset(x = adjustedOffset),
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_bg_8),
                        contentDescription = "desc",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(130.dp)
                            .scale(1.6f)
                            .offset(x = adjustedOffset),
                    )
                }
            }

            AnimatedVisibility(visible = isPlaying, enter = Transitions.enter, exit = Transitions.exit) {
                Box(modifier = Modifier
                    .size(150.dp)
                    .background(Color.Black.copy(0.4f))
                ) {}
            }


            LottieCustom(
                lottieCompositionSpec = LottieCompositionSpec.RawRes(R.raw.eq4),
                color = GetThemeColor.getText(isSystemInDarkTheme()),
                isHidden = !isPlaying,
                speed = 1f
            )

            Box(modifier = Modifier
                .size(150.dp)
                .background(
                    Brush.verticalGradient(
                        0F to Color.Transparent,
                        0.7F to Color.Transparent,
                        1F to GetThemeColor.getText(!isSystemInDarkTheme()),
                        tileMode = TileMode.Clamp
                    )
                ),
                contentAlignment = Alignment.BottomStart
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = playlistWithSongs.playlist.playlistName, fontSize = 10.sp, color = GetThemeColor.getText(isSystemInDarkTheme()), modifier = Modifier.wrapContentHeight(Alignment.Bottom))
                    Text(text = playlistWithSongs.songs.size.toString(), fontSize = 10.sp, color = GetThemeColor.getText(isSystemInDarkTheme()), modifier = Modifier.wrapContentHeight(Alignment.Bottom))
                }
            }
        }


    }
}


