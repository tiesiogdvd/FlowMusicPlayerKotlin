@file:OptIn(ExperimentalFoundationApi::class)

package com.tiesiogdvd.composetest.ui.libraryPlaylist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.ui.library.LibraryPlaylistViewModel
import com.tiesiogdvd.composetest.ui.theme.FlowPlayerTheme
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.Song


import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*

import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.debugInspectorInfo
import com.tiesiogdvd.composetest.ui.addToPlaylistDialog.AddToPlaylistDialog
import com.tiesiogdvd.composetest.ui.header.Header
import com.tiesiogdvd.composetest.ui.header.HeaderOptions
import com.tiesiogdvd.composetest.ui.selectionBar.SelectionBarComposable
import com.tiesiogdvd.composetest.ui.selectionBar.SelectionBarList
import com.tiesiogdvd.composetest.ui.selectionBar.SelectionType
import com.tiesiogdvd.composetest.ui.sortOrderDialog.SortOrderSongsDialog
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.composetest.util.TypeConverter
import kotlinx.coroutines.*


import kotlin.math.min


@Destination
@Composable
fun LibraryPlaylist(navigator: DestinationsNavigator, playlist: Playlist, viewModel: LibraryPlaylistViewModel = hiltViewModel()) {
    FlowPlayerTheme {
        val isSelectionBarVisible = viewModel.isSelectionBarVisible.collectAsState().value
        val totalSize = viewModel.songs?.size?:0
        val selectionListSize = viewModel.selection.collectAsState().value

        viewModel.setSource(playlist.id)
        println("SOURCE PLAYLIST ID ${playlist.id}")

        if(viewModel.isSortDialogShown){
            SortOrderSongsDialog(onDismiss = {
                viewModel.dismissSortDialog()
            },
                onSongSortSelected = {
                    viewModel.updateSongSortOrder(it)
                },
                onSortTypeSelected = {
                    viewModel.updateSortOrder(it)
                },
                songSortOrder = viewModel.songSortOrder.collectAsState().value,
                sortOrder = viewModel.sortOrder.collectAsState().value
            )
        }

        if(viewModel.isPlaylistsDialogShown){
            AddToPlaylistDialog(onDismiss = {
                viewModel.dismissPlaylistsDialog()
            }, songList = viewModel.selectionListFlow)}


        Scaffold(bottomBar = {
            AnimatedVisibility(
                visible = isSelectionBarVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    SelectionBarComposable(items = SelectionBarList.list,
                        selectionType = when(playlist.playlistName){
                        "All Songs"->SelectionType.ALL_SONGS
                        "Favorites"->SelectionType.FAVORITES
                        else -> SelectionType.PLAYLIST },
                        onItemClick = {
                            when(it.name){
                                "Remove from playlist" -> viewModel.removeSongs()
                                "Hide" -> viewModel.hideSongs()
                                "Set playlist cover" -> viewModel.setPlaylistCover()
                                "Add to playlist" -> viewModel.openPlaylistsDialog()
                                else -> println("haha")

                            }
                    }, onCheckChange = {viewModel.toggleSelectAll()}, noOfSelected = selectionListSize, totalSize = totalSize)
                }
            }

        }, content = {
                padding-> Column(modifier = Modifier.padding(padding))
        {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = GetThemeColor.getBackground(isSystemInDarkTheme())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    SongsList(playlist = playlist)
                }
            }
        }
        })
    }
}


@Composable
fun SongsList(
    viewModel: LibraryPlaylistViewModel = hiltViewModel(),
    playlist: Playlist
){

    val playlistSongs = viewModel.playlistFlow.collectAsState(initial = listOf()).value
    val isSelectionBarVisible = viewModel.isSelectionBarVisible.collectAsState().value

    BackHandler(onBack = {
        if(isSelectionBarVisible){
            viewModel.isSelectionBarSelected(false)
        }
    }, enabled = isSelectionBarVisible)

    LazyColumn(modifier = Modifier
        .wrapContentHeight()
        /*.recomposeHighlighter()*/){
        item {
            Header(bitmapSource = viewModel.bitmap, headerName = playlist.playlistName)
        }

        stickyHeader {
            HeaderOptions(
                onOpenSortDialog = { viewModel.openSortDialog() },
                text = viewModel.searchQuery
            )
        }


        itemsIndexed(items = playlistSongs, key = {index, song -> song.id}){
                index, song ->
            val animatedOpacity = remember { androidx.compose.animation.core.Animatable(0f) }
            val isSelected = viewModel.selectionListFlow.collectAsState().value.get(song.id)!=null
            LaunchedEffect(Unit) {
                launch {
                    animatedOpacity.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 300)
                    )
                }
            }

            Column(modifier = Modifier
                .padding(start = 25.dp)
                .alpha(animatedOpacity.value)
                .combinedClickable(
                    onClick = {
                        if (!isSelectionBarVisible) {
                            viewModel.onSongSelected(song)
                        } else {
                            viewModel.toggleSelection(song)
                        }

                    },
                    onLongClick = {
                        // viewModel.removeSong(song)
                        //viewModel.hideSong(song)
                        viewModel.toggleSelection(song)
                        viewModel.isSelectionBarSelected(true)

                    })
                .animateItemPlacement())
            {
                SongItem(playlistSongs.get(index), isSelected = isSelected)
            }
        }

        item {
            Spacer(modifier = Modifier.padding(100.dp))
        }


    }
}


@Composable
fun SongItem(
    song: Song,
    isSelected: Boolean,
){
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    val initialColor = if(!isSelected){GetThemeColor.getButton(isSystemInDarkTheme())}else{GetThemeColor.getPurple(isSystemInDarkTheme())}
    var surfaceColor by remember { mutableStateOf(initialColor) }
    val targetColor = if(!isSelected){GetThemeColor.getButton(isSystemInDarkTheme())}else{GetThemeColor.getPurple(isSystemInDarkTheme())}
    val animatedColor by animateColorAsState(targetValue = targetColor, tween(durationMillis = 400, easing = EaseIn))

    LaunchedEffect(song.songPath) {
        withContext(Dispatchers.IO) {
            bitmap = MusicDataMetadata.getBitmap(song.songPath)
        }
    }

    // Update the Surface color to the animated color
    LaunchedEffect(animatedColor) {
        surfaceColor = animatedColor
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 20.dp)
            .padding(vertical = 5.dp),
        color = Color.Transparent
    ){
        Row(
            modifier = Modifier,
        ) {
            Surface(shape = RoundedCornerShape(30.dp), modifier = Modifier.align(Alignment.CenterVertically)) {
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

            Spacer(modifier = Modifier
                .size(30.dp)
                .align(Alignment.CenterVertically))
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(50.dp)
                    .fillMaxWidth(),
                color = surfaceColor,
                shape = RoundedCornerShape(30.dp)) {
                Row(modifier = Modifier
                    .padding(start = 15.dp)
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()) {
                    Column(modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(3.5F)) {
                        Text(text = song.songName.toString(), fontSize = 14.sp, color = if(isSelected){GetThemeColor.getText(!isSystemInDarkTheme())} else {GetThemeColor.getText(isSystemInDarkTheme())}, modifier = Modifier.wrapContentHeight(
                            Alignment.Bottom))
                        if(song.songArtist!=null){
                            Text(text = song.songArtist.toString(), fontSize = 11.sp, color = if(isSelected){GetThemeColor.getText(!isSystemInDarkTheme())} else {GetThemeColor.getText(isSystemInDarkTheme())}, modifier = Modifier.wrapContentHeight(
                                Alignment.Bottom))
                        }
                    }

                    Text(text = TypeConverter.formatDuration(song.length), fontSize = 11.sp, color = if(isSelected){GetThemeColor.getText(!isSystemInDarkTheme())} else {GetThemeColor.getText(isSystemInDarkTheme())}, modifier = Modifier
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