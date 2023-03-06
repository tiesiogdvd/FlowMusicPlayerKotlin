package com.tiesiogdvd.composetest.ui.library

import androidx.compose.animation.*
import androidx.compose.foundation.*
import com.tiesiogdvd.composetest.R
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.tiesiogdvd.composetest.ui.destinations.LibraryPlaylistDestination
import com.tiesiogdvd.composetest.ui.destinations.LibraryPlaylistsScreenDestination
import com.tiesiogdvd.composetest.ui.libraryPlaylists.LibraryPlaylistsScreen
import com.tiesiogdvd.composetest.ui.theme.FlowPlayerTheme
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import com.tiesiogdvd.playlistssongstest.data.PlaylistWithSongs
import kotlinx.coroutines.*


@RootNavGraph(start = true)
@Destination
@Composable
fun Library(navigator: DestinationsNavigator, viewModel: LibraryViewModel = hiltViewModel(),) {
    //navigator.popBackStack()
    FlowPlayerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = GetThemeColor.getBackground(isSystemInDarkTheme())
        ) {Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp, start = 25.dp)
            ) {
            Text("Library", fontSize = 45.sp, modifier = Modifier.padding(bottom = 15.dp))
            Text("Recent Playlists", fontSize = 22.sp, modifier = Modifier.padding(bottom = 3.dp))
            PlaylistList(navigator = navigator)
            menuItem(text = "All Songs", onClick = { navigator.navigate(LibraryPlaylistDestination(viewModel.playlistAllSongs))})
            menuItem(text = "Playlists", onClick = { navigator.navigate(LibraryPlaylistsScreenDestination)})
            menuItem(text = "Favorites", onClick = { navigator.navigate(LibraryPlaylistDestination(viewModel.playlistAllSongs))})
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
    navigator: DestinationsNavigator
){
    val playlistWithSongs = viewModel.playlistsWithSongs.collectAsState(initial = listOf()).value
    var isVisible by remember { mutableStateOf(true) }
    LazyRow(modifier = Modifier
        .wrapContentHeight()
        .padding(bottom = 15.dp)){
        items(playlistWithSongs.size, key = {playlistWithSongs.get(it).playlist.id}){
            val density = LocalDensity.current
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInHorizontally {
                    // Slide in from 40 dp from the top.
                    with(density) { -40.dp.roundToPx() }
                } + expandHorizontally(
                    expandFrom = Alignment.End
                ) + fadeIn(
                    initialAlpha = 0.3f
                ),
                exit = slideOutHorizontally() + shrinkHorizontally(shrinkTowards = Alignment.End) +  fadeOut()
            ) {
                //Text("Hello", Modifier.fillMaxWidth().height(200.dp))
                Column(modifier = Modifier
                    .combinedClickable(
                        onClick = {
                            //isVisible = false
                            navigator.navigate(LibraryPlaylistDestination(playlistWithSongs.get(it).playlist))
                        },

                        onLongClick = {
                            isVisible = false
                            //viewModel.removePlaylist(playlistWithSongs.get(it).playlist)
                        })
                    .animateItemPlacement())

                {
                    PlaylistItem(playlistWithSongs = playlistWithSongs.get(it), viewModel)
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
    Surface(shape = RoundedCornerShape(30.dp), color = GetThemeColor.getButton(isSystemInDarkTheme()), modifier = Modifier
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
fun PlaylistItem(playlistWithSongs:PlaylistWithSongs, viewModel: LibraryViewModel){
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(playlistWithSongs) {
        withContext(Dispatchers.IO) {
            bitmap = viewModel.getPlaylistBitmap(playlistWithSongs)
        }
    }

    Surface(
        modifier = Modifier
            .padding(end = 5.dp),
        shape = RoundedCornerShape(10.dp),
        color = GetThemeColor.getButton(isSystemInDarkTheme())
    ){
        Column(
            modifier = Modifier
                .padding(5.dp),
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!,
                    contentDescription = "desc",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(130.dp),
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_bg_6),
                    contentDescription = "desc",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(130.dp),
                )
            }

            Text(text = playlistWithSongs.playlist.playlistName, fontSize = 10.sp, color = GetThemeColor.getText(isSystemInDarkTheme()), modifier = Modifier.wrapContentHeight(Alignment.Bottom))
            Text(text = playlistWithSongs.songs.size.toString(), fontSize = 10.sp, color = GetThemeColor.getText(isSystemInDarkTheme()), modifier = Modifier.wrapContentHeight(Alignment.Bottom))
           // Text(text = viewModel.getSongsNumber(playlistWithSongs).collectAsState(initial = listOf()).value.size.toString(), fontSize = 10.sp, color = GetThemeColor.getText(isSystemInDarkTheme()), modifier = Modifier.wrapContentHeight(Alignment.Bottom))
        }
    }
}
