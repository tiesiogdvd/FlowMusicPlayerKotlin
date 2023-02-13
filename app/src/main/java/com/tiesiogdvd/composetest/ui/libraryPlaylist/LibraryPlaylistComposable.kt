package com.tiesiogdvd.composetest.ui.libraryPlaylist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.ui.library.LibraryPlaylistViewModel
import com.tiesiogdvd.composetest.ui.library.LibraryViewModel
import com.tiesiogdvd.composetest.ui.library.PlaylistList
import com.tiesiogdvd.composetest.ui.theme.ComposeTestTheme
import com.tiesiogdvd.composetest.ui.theme.button
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.PlaylistWithSongs
import com.tiesiogdvd.playlistssongstest.data.Song
import kotlinx.coroutines.CoroutineScope




import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.debugInspectorInfo
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.composetest.util.TypeConverter


import kotlin.math.min
import kotlinx.coroutines.delay








@RequiresApi(Build.VERSION_CODES.R)
@Destination
@Composable
fun LibraryPlaylist(navigator: DestinationsNavigator, playlist: Playlist) {
    ComposeTestTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .recomposeHighlighter(),
            color = GetThemeColor.getBackground(isSystemInDarkTheme())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp, start = 25.dp)
                    .clickable {
                    }
            ) {
                Text(playlist.playlistName, fontSize = 40.sp)
                Text("Songs", fontSize = 20.sp)
                SongsList(playlist = playlist)
            }
        }
    }
}



@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongsList(
    viewModel: LibraryPlaylistViewModel = hiltViewModel(),
    playlist: Playlist
){
    println(playlist.playlistName)
    var playlistSongs = viewModel.getPlaylistWithSongs(playlist).collectAsState(initial = listOf()).value

    var isVisible by remember {
        mutableStateOf(true)
    }

    LazyColumn(modifier = Modifier
        .wrapContentHeight()
        .recomposeHighlighter()){
        itemsIndexed(items = playlistSongs, key = {index, song -> song.id}){
                index, song ->
            val density = LocalDensity.current
            Column(modifier = Modifier
                .combinedClickable(
                    onClick = {
                        //isVisible = false
                              //viewModel.removeSong(song)
                        viewModel.onSongSelected(song)
                       // playlist.
                        //playlistSongs = playlistSongs.shuffled()
                        // viewModel.removeSong(song)
                    },
                    onLongClick = {
                        viewModel.removeSong(song)
                        //viewModel.removePlaylist(playlistWithSongs.get(it).playlist)
                    })
                .animateItemPlacement())
            {
                SongItem(playlistSongs.get(index))

            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun SongItem(
    song: Song
){
    println(song.songPath)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 20.dp)
            .padding(vertical = 5.dp)
            .recomposeHighlighter(),
        color = Color.Transparent
    ){
        Row(
            modifier = Modifier,
        ) {
            Surface(shape = RoundedCornerShape(30.dp), modifier = Modifier.align(Alignment.CenterVertically)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(MusicDataMetadata.getBitmap(song.songPath))
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(45.dp),
                    contentDescription = "desc",
                )

            }

            Spacer(modifier = Modifier
                .size(30.dp)
                .align(Alignment.CenterVertically))

            Surface(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(50.dp)
                    .fillMaxWidth(),
                color = GetThemeColor.getButton(isSystemInDarkTheme()),
                shape = RoundedCornerShape(30.dp)) {
                Row(modifier = Modifier
                    .padding(start = 15.dp)
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()) {
                    Column(modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(3.5F)) {
                        Text(text = song.songName.toString(), fontSize = 14.sp, color = GetThemeColor.getText(isSystemInDarkTheme()), modifier = Modifier.wrapContentHeight(
                            Alignment.Bottom))
                        Text(text = song.songArtist.toString(), fontSize = 11.sp, color = GetThemeColor.getText(isSystemInDarkTheme()), modifier = Modifier.wrapContentHeight(
                            Alignment.Bottom))
                    }

                    Text(text = TypeConverter.formatDuration(song.length), fontSize = 11.sp, color = GetThemeColor.getText(isSystemInDarkTheme()), modifier = Modifier
                        .wrapContentHeight()
                        .align(Alignment.CenterVertically)
                        .padding(end = 15.dp)
                        .weight(0.8F))
                }
            }
        }
    }
}



@Stable
fun Modifier.recomposeHighlighter(): Modifier = this.then(recomposeModifier)

// Use a single instance + @Stable to ensure that recompositions can enable skipping optimizations
// Modifier.composed will still remember unique data per call site.
private val recomposeModifier =
    Modifier.composed(inspectorInfo = debugInspectorInfo { name = "recomposeHighlighter" }) {
        // The total number of compositions that have occurred. We're not using a State<> here be
        // able to read/write the value without invalidating (which would cause infinite
        // recomposition).
        val totalCompositions = remember { arrayOf(0L) }
        totalCompositions[0]++

        // The value of totalCompositions at the last timeout.
        val totalCompositionsAtLastTimeout = remember { mutableStateOf(0L) }

        // Start the timeout, and reset everytime there's a recomposition. (Using totalCompositions
        // as the key is really just to cause the timer to restart every composition).
        LaunchedEffect(totalCompositions[0]) {
            delay(3000)
            totalCompositionsAtLastTimeout.value = totalCompositions[0]
        }

        Modifier.drawWithCache {
            onDrawWithContent {
                // Draw actual content.
                drawContent()

                // Below is to draw the highlight, if necessary. A lot of the logic is copied from
                // Modifier.border
                val numCompositionsSinceTimeout =
                    totalCompositions[0] - totalCompositionsAtLastTimeout.value

                val hasValidBorderParams = size.minDimension > 0f
                if (!hasValidBorderParams || numCompositionsSinceTimeout <= 0) {
                    return@onDrawWithContent
                }

                val (color, strokeWidthPx) =
                    when (numCompositionsSinceTimeout) {
                        // We need at least one composition to draw, so draw the smallest border
                        // color in blue.
                        1L -> Color.Blue to 1f
                        // 2 compositions is _probably_ okay.
                        2L -> Color.Green to 2.dp.toPx()
                        // 3 or more compositions before timeout may indicate an issue. lerp the
                        // color from yellow to red, and continually increase the border size.
                        else -> {
                            lerp(
                                Color.Yellow.copy(alpha = 0.8f),
                                Color.Red.copy(alpha = 0.5f),
                                min(1f, (numCompositionsSinceTimeout - 1).toFloat() / 100f)
                            ) to numCompositionsSinceTimeout.toInt().dp.toPx()
                        }
                    }

                val halfStroke = strokeWidthPx / 2
                val topLeft = Offset(halfStroke, halfStroke)
                val borderSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)

                val fillArea = (strokeWidthPx * 2) > size.minDimension
                val rectTopLeft = if (fillArea) Offset.Zero else topLeft
                val size = if (fillArea) size else borderSize
                val style = if (fillArea) Fill else Stroke(strokeWidthPx)

                drawRect(
                    brush = SolidColor(color),
                    topLeft = rectTopLeft,
                    size = size,
                    style = style
                )
            }
        }
    }