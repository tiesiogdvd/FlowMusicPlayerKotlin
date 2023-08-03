@file:OptIn(ExperimentalFoundationApi::class)

package com.tiesiogdvd.composetest.ui.libraryPlaylists

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.ui.addToPlaylistDialog.AddToPlaylistDialog
import com.tiesiogdvd.composetest.ui.destinations.LibraryPlaylistDestination
import com.tiesiogdvd.composetest.ui.header.Header
import com.tiesiogdvd.composetest.ui.header.HeaderOptions
import com.tiesiogdvd.composetest.ui.selectionBar.SelectionBarComposable
import com.tiesiogdvd.composetest.ui.selectionBar.SelectionBarList
import com.tiesiogdvd.composetest.ui.selectionBar.SelectionType
import com.tiesiogdvd.composetest.ui.sortOrderDialog.SortOrderPlaylistsDialog
import com.tiesiogdvd.composetest.ui.sortOrderDialog.SortOrderSongsDialog
import com.tiesiogdvd.composetest.ui.theme.FlowPlayerTheme
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.playlistssongstest.data.PlaylistWithSongs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Destination
@Composable
fun LibraryPlaylistsScreen(navigator: DestinationsNavigator, viewModel: LibraryPlaylistsViewModel = hiltViewModel()){
    FlowPlayerTheme {
        val isSelectionBarVisible = viewModel.isSelectionBarVisible.collectAsState().value
        val totalSize = viewModel.playlistFlow.collectAsState(initial = emptyList()).value.size
        val selectionListSize = viewModel.selection.collectAsState().value
        val source = viewModel.playlistFlow

        Scaffold(bottomBar = {
            AnimatedVisibility(
                visible = isSelectionBarVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    SelectionBarComposable(items = SelectionBarList.list,
                        selectionType = SelectionType.PLAYLISTS,
                        onItemClick = {
                            when(it.name){
                                "Remove playlist" -> viewModel.removePlaylists()
                                "Add to playlist" -> viewModel.openPlaylistsDialog()
                                else -> println("haha")
                            }
                        }, onCheckChange = {viewModel.toggleSelectAll()}, noOfSelected = selectionListSize, totalSize = totalSize, onRangeSelected = {})
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
                    PlaylistList(navigator)
                }
            }
        }
        })
    }
}



@Composable
fun PlaylistList(
    navigator: DestinationsNavigator,
    viewModel: LibraryPlaylistsViewModel = hiltViewModel()
){

    val scrollOffset = remember {mutableStateOf(0f)}
    val headerScrollConnection = remember{
        object: NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaY = available.y
                scrollOffset.value += deltaY * 0.5f // Change 0.5f to adjust the parallax speed
                println(scrollOffset)
                return super.onPreScroll(available, source)
            }
        }
    }

    val playlistsWithSongs = viewModel.playlistFlow.collectAsState(initial = listOf()).value
    val isSelectionBarVisible = viewModel.isSelectionBarVisible.collectAsState().value


    if(viewModel.isSortDialogShown){
        SortOrderPlaylistsDialog(onDismiss = {
            viewModel.dismissSortDialog()
        },
            onPlaylistSortOrderSelected = {
                viewModel.updatePlaylistSortOrder(it)
            },
            onSortTypeSelected = {
                viewModel.updateSortOrder(it)
            },
            playlistsSortOrder = viewModel.playlistSortOrder.collectAsState().value,
            sortOrder = viewModel.sortOrder.collectAsState().value
        )
    }

    if(viewModel.isPlaylistsDialogShown){
        AddToPlaylistDialog(onDismiss = {
            viewModel.dismissPlaylistsDialog()
        }, songList = viewModel.getSelectedSongs())
    }


    BackHandler(onBack = {
        if(isSelectionBarVisible){
            viewModel.isSelectionBarSelected(false)
        }
    }, enabled = isSelectionBarVisible)

    LazyColumn(modifier = Modifier
        .nestedScroll(headerScrollConnection)
        .wrapContentHeight()){
        item {
            Header(bitmapSource = viewModel.bitmap, headerName = "Playlists", scrollOffsetY = scrollOffset)
        }

        stickyHeader {
            HeaderOptions(
                onOpenSortDialog = { viewModel.openSortDialog() },
                text = viewModel.searchQuery,
                onClickPlay = {},
                onClickMix = {}
            )
        }

        itemsIndexed(items = playlistsWithSongs, key = { index, playlistWithSongs -> playlistWithSongs}){
                index, playlistWithSongs ->
            val animatedOpacity = remember { Animatable(0f) }
            val isSelected = viewModel.selectionListFlow.collectAsState().value.get(playlistWithSongs.playlist.id)!=null
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
                            //navigate to playlist
                            navigator.navigate(LibraryPlaylistDestination(playlist = playlistWithSongs.playlist))
                        } else {
                            viewModel.toggleSelection(playlistWithSongs.playlist)
                        }

                    },
                    onLongClick = {
                        // viewModel.removeSong(song)
                        //viewModel.hideSong(song)
                        viewModel.toggleSelection(playlistWithSongs.playlist)
                        viewModel.isSelectionBarSelected(true)

                    })
                .animateItemPlacement())
            {
                PlaylistItem(playlistsWithSongs.get(index), isSelected = isSelected)
            }
        }

        item {
            Spacer(modifier = Modifier.padding(100.dp))
        }


    }
}




@Composable
fun PlaylistItem(
    playlistWithSongs: PlaylistWithSongs,
    isSelected: Boolean,
    libraryPlaylistsViewModel: LibraryPlaylistsViewModel = hiltViewModel()
){
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    //var bitmap = libraryPlaylistsViewModel.bitmap.collectAsState().value

    val initialColor = if(!isSelected){GetThemeColor.getButton(isSystemInDarkTheme())}else{GetThemeColor.getPurple(isSystemInDarkTheme())}
    var surfaceColor by remember { mutableStateOf(initialColor) }
    val targetColor = if(!isSelected){GetThemeColor.getButton(isSystemInDarkTheme())}else{GetThemeColor.getPurple(isSystemInDarkTheme())}
    val animatedColor by animateColorAsState(targetValue = targetColor, tween(durationMillis = 400, easing = EaseIn))

    LaunchedEffect(playlistWithSongs) {
        withContext(Dispatchers.IO) {
            bitmap = MusicDataMetadata.getBitmap(playlistWithSongs.playlist.bitmapSource)
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
            Surface(shape = RoundedCornerShape(30.dp), modifier = Modifier
                .align(Alignment.CenterVertically)
                .height(80.dp)) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!,
                        contentDescription = "desc",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(120.dp),
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_group_23_image_6),
                        contentDescription = "desc",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(120.dp),
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
                shape = RoundedCornerShape(30.dp)
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween,modifier = Modifier
                    .padding(start = 15.dp)
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()) {
                    Column(modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .align(Alignment.CenterVertically)) {
                        Text(text = playlistWithSongs.playlist.playlistName, fontSize = 14.sp, color = if(isSelected){GetThemeColor.getText(!isSystemInDarkTheme())} else {GetThemeColor.getText(isSystemInDarkTheme())}, modifier = Modifier.wrapContentHeight(
                            Alignment.Bottom))
                    }

                    Text(text = playlistWithSongs.songs.size.toString(), fontSize = 11.sp, color = if(isSelected){GetThemeColor.getText(!isSystemInDarkTheme())} else {GetThemeColor.getText(isSystemInDarkTheme())}, modifier = Modifier
                        .wrapContentHeight()
                        .align(Alignment.CenterVertically)
                        .padding(end = 15.dp))
                }
            }
        }
    }
}
