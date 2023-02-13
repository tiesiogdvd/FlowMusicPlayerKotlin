package com.tiesiogdvd.composetest.ui.library

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import com.tiesiogdvd.composetest.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.tiesiogdvd.composetest.ui.destinations.LibraryDestination
import com.tiesiogdvd.composetest.ui.destinations.LibraryPlaylistDestination
import com.tiesiogdvd.composetest.ui.libraryPlaylist.LibraryPlaylist
import com.tiesiogdvd.composetest.ui.theme.ComposeTestTheme
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import com.tiesiogdvd.composetest.ui.theme.button
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.PlaylistWithSongs
import kotlinx.coroutines.*


@RequiresApi(Build.VERSION_CODES.R)
@RootNavGraph(start = true)
@Destination
@Composable
fun Library(navigator: DestinationsNavigator) {
    //navigator.popBackStack()
    ComposeTestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = GetThemeColor.getBackground(isSystemInDarkTheme())
        ) {Column(
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


@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistList(
    viewModel: LibraryViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
){
    val playlistWithSongs = viewModel.playlistsWithSongs.collectAsState(initial = listOf()).value
    var isVisible by remember { mutableStateOf(true) }
    LazyRow(modifier = Modifier.wrapContentHeight()){
        items(playlistWithSongs.size, key = {it}){

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
                    PlaylistItem(playlist = playlistWithSongs.get(it).playlist, viewModel)
                }
            }
        }

    }
}




@Composable
fun PlaylistItem(playlist:Playlist, viewModel: LibraryViewModel){
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
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

            Image(

                modifier = Modifier.size(130.dp),
                painter = painterResource(id = R.drawable.img_bg_6),
                contentDescription = "desc",
                contentScale = ContentScale.Crop,
            )
            Text(text = playlist.playlistName, fontSize = 10.sp, color = GetThemeColor.getText(isSystemInDarkTheme()), modifier = Modifier.wrapContentHeight(Alignment.Bottom))
            Text(text = viewModel.getSongsNumber(playlist).collectAsState(initial = listOf()).value.size.toString(), fontSize = 10.sp, color = GetThemeColor.getText(isSystemInDarkTheme()), modifier = Modifier.wrapContentHeight(Alignment.Bottom))
        }
    }
}








@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistListTest(
    viewModel: LibraryViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
){
    val playlistWithSongs = viewModel.playlistsWithSongs.collectAsState(initial = listOf()).value
    var isVisible by remember { mutableStateOf(true) }
    LazyRow(modifier = Modifier.wrapContentHeight()){



        itemsIndexed(items = playlistWithSongs){
            index, song ->
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
                            navigator.navigate(
                                LibraryPlaylistDestination(
                                    playlistWithSongs.get(
                                        index
                                    ).playlist
                                )
                            )
                        },

                        onLongClick = {
                            viewModel.removePlaylist(playlistWithSongs.get(index).playlist)
                        })
                    .animateItemPlacement())

                {
                    PlaylistItem(playlist = playlistWithSongs.get(index).playlist, viewModel)
                }
            }
        }

    }
}