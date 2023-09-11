@file:OptIn(ExperimentalFoundationApi::class)

package com.tiesiogdvd.composetest.ui.libraryPlaylist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*



import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*

import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.tiesiogdvd.composetest.ui.addToPlaylistDialog.AddToPlaylistDialog
import com.tiesiogdvd.composetest.ui.header.Header
import com.tiesiogdvd.composetest.ui.header.HeaderOptions
import com.tiesiogdvd.composetest.ui.lottieAnimation.LottieCustom
import com.tiesiogdvd.composetest.ui.selectionBar.SelectionBarComposable
import com.tiesiogdvd.composetest.ui.selectionBar.SelectionBarList
import com.tiesiogdvd.composetest.ui.selectionBar.SelectionType
import com.tiesiogdvd.composetest.ui.sortOrderDialog.SortOrderSongsDialog
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import com.tiesiogdvd.composetest.ui.theme.Transitions
import com.tiesiogdvd.composetest.util.BitmapLoader
import com.tiesiogdvd.composetest.util.TypeConverter
import kotlinx.coroutines.*
import kotlin.math.*


@Destination
@Composable
fun LibraryPlaylist(navigator: DestinationsNavigator, playlist: Playlist, viewModel: LibraryPlaylistViewModel = hiltViewModel()) {
    FlowPlayerTheme {
        val isSelectionBarVisible = viewModel.isSelectionBarVisible.collectAsState().value
        val totalSize = viewModel.songsAll?.size?:0
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
                                "Remove from favorites" -> viewModel.removeSongs()
                                "Hide" -> viewModel.hideSongs()
                                "Set playlist cover" -> viewModel.setPlaylistCover()
                                "Set favorites cover" -> viewModel.setPlaylistCover()
                                "Add to playlist" -> viewModel.openPlaylistsDialog()
                                else -> println("haha")

                            }
                    }, onCheckChange = {viewModel.toggleSelectAll()}, noOfSelected = selectionListSize, totalSize = totalSize, onRangeSelected = {viewModel.toggleSelectRange()})
                }
            }

        }, content = {
                padding-> Column(modifier = Modifier.padding(padding))
        {
            Box(
                modifier = Modifier
                    .background(color = GetThemeColor.getBackground(isSystemInDarkTheme()))
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    SongsList(playlist = playlist)
                    //DummyList()
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
    val scrollOffset = remember {mutableStateOf(0f)}
    val playingSongID = viewModel.currentSongId.collectAsState().value
    val headerScrollConnection = remember{
        object:NestedScrollConnection{
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaY = available.y
                scrollOffset.value += deltaY * 0.5f // Change 0.5f to adjust the parallax speed
                if(scrollOffset.value>0){
                    scrollOffset.value=0f
                }
                println(scrollOffset)
                println(available)
                return super.onPreScroll(available, source)
            }

        }
    }
    val scrollState = rememberLazyListState()
    val playlistSongs = viewModel.playlistFlow.collectAsState(initial = listOf()).value
    val isSelectionBarVisible = viewModel.isSelectionBarVisible.collectAsState().value

    BackHandler(onBack = {
        if(isSelectionBarVisible){
            viewModel.isSelectionBarSelected(false)
        }
    }, enabled = isSelectionBarVisible)



    Box(modifier = Modifier.fillMaxSize()){
        LazyColumn(state = scrollState,modifier = Modifier
            .nestedScroll(headerScrollConnection)
            //.fillMaxHeight()
            /*.recomposeHighlighter()*/){
            item {
                Header(bitmapSource = viewModel.bitmap, headerName = playlist.playlistName, scrollOffsetY = scrollOffset)
            }

            stickyHeader {
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(color = GetThemeColor.getBackground(isSystemInDarkTheme()))){
                    HeaderOptions(
                        onOpenSortDialog = { viewModel.openSortDialog() },
                        text = viewModel.searchQuery,
                        onClickMix = {viewModel.playMix()},
                        onClickPlay = {viewModel.playFirst()}
                    )
                }
            }

           /* stickyHeader {
                Layout(modifier = Modifier,
                    content = {
                        Box(modifier = Modifier
                            .zIndex(200f)
                            .wrapContentHeight()
                            .align(Alignment.CenterEnd)) {
                            CustomScrollbar(songs = playlistSongs,scrollState)
                        }
                    }
                ) { measurables, constraints ->
                    val placeable = measurables.first().measure(constraints)

                    // Set the width and height of the Layout to zero
                    layout(0, 0) {
                        placeable.placeRelative(0, 0)
                    }
                }
            }*/



            itemsIndexed(items = playlistSongs, key = {index, song -> song.id}){
                    index, song ->
                val animatedOpacity = remember { Animatable(0f) }
                val isSelected = viewModel.selectionListFlow.collectAsState().value.get(song.id)!=null
                val isPlaying = playingSongID == song.id


                Column(modifier = Modifier
                    .padding(start = 25.dp)
                    //   .alpha(animatedOpacity.value)
                    /*   .drawBehind {
                        drawRect(color = Color.Transparent, alpha = animatedOpacity.value)
                    }*/

                    .combinedClickable(
                        onClick = {
                            if (!isSelectionBarVisible) {
                                viewModel.onSongSelected(song)
                            } else {
                                viewModel.toggleSelection(song)
                            }

                        },
                        onLongClick = {
                            viewModel.toggleSelection(song)
                            viewModel.isSelectionBarSelected(true)

                        })
                    .animateItemPlacement())
                {
                    /*Row(modifier = Modifier.height(50.dp)) {
                        Text(text = song.songName.toString())
                    }*/
                    SongItem(song, isSelected = isSelected, isPlaying = isPlaying)
                }
            }

            item {
                Spacer(modifier = Modifier.padding(100.dp))
            }


        }
        val scope = rememberCoroutineScope()

        Box(modifier = Modifier
            .zIndex(200f)
            .wrapContentHeight()
            .align(Alignment.CenterEnd)) {
            CustomScrollbar(songs = playlistSongs,
                //scrollState = scrollState,
                scrollToItem = {
                scope.launch {
                    if(it>=0){
                        scrollState.scrollToItem(it)
                    }
                }
            })
        }
        /*Layout(modifier = Modifier,
            content = {
                Box(modifier = Modifier.zIndex(200f).wrapContentHeight().align(Alignment.CenterEnd)) {
                    CustomScrollbar(songs = playlistSongs,scrollState)
                }
            }
        ) { measurables, constraints ->
            val placeable = measurables.first().measure(constraints)

            // Set the width and height of the Layout to zero
            layout(0, 0) {
                placeable.placeRelative(0, 0)
            }
        }*/


    }







}


@Composable
fun CustomScrollbar(
    songs:List<Song>,
    scrollToItem: (Int) -> Unit,
   // scrollState: LazyListState
){

    val scope = rememberCoroutineScope()

    val firstIndexesByLetter = remember {mutableMapOf<Char, Int>()}
    val indexes = remember {mutableListOf<Int>()}
        firstIndexesByLetter[Char(0x25CB)] = 0
        indexes.add(0)
        for (i in songs.indices) {
            val firstChar = songs[i].songName?.firstOrNull()?.uppercaseChar() ?: continue
            if (firstChar in 'A'..'Z' && firstChar !in firstIndexesByLetter) {
                firstIndexesByLetter[firstChar] = i
                indexes.add(i)
            }
        }

    Box(modifier = Modifier.fillMaxSize()){

        Column(verticalArrangement = Arrangement.Bottom,horizontalAlignment = Alignment.End, modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 50.dp)
            .width(50.dp)
            .padding(end = 5.dp)) {

            val density = LocalDensity.current
            val height = remember { mutableStateOf(0.dp) }
            val position = remember { mutableStateOf(0.dp) }
            val distanceFromEdge = remember { mutableStateOf(0.dp)}
            val prev = remember{ mutableStateOf(-1)}
            val selectedItem = remember { mutableStateOf(-1)}
            val itemHeight = remember { mutableStateOf(0.dp) }

            val selectionEnabled = remember { mutableStateOf(false)}




            fun updateSelectedIndexIfNeeded(position: Dp) {
                selectedItem.value = (position/(itemHeight.value)).toInt()
                scope.coroutineContext.cancelChildren()
                scope.launch {
                  //  delay(50)
                    if(selectedItem.value!=prev.value && selectedItem.value>=0){
                        //println(indexes)
                        delay(10)
                        scrollToItem(indexes.get(selectedItem.value))
                        //scrollState.scrollToItem(indexes.get(selectedItem.value))
                        prev.value = selectedItem.value

                    }

                }
            }

            LaunchedEffect(firstIndexesByLetter){
                println("INDEXES CHANGE")
            }



            Column(modifier = Modifier
                .pointerInput(Unit) {
                    detectDragGestures(onDragEnd = {
                        selectionEnabled.value = false
                        // selectedItem.value = -10
                    }, onDrag = { change, dragAmount ->
                        selectionEnabled.value = true
                        distanceFromEdge.value = change.position.x.toDp()
                        position.value = change.position.y.toDp()
                        println(position.value.toString() + " DRAG")
                        updateSelectedIndexIfNeeded(position.value)
                        change.consume()

                    })
                }
                .onGloballyPositioned { coordinates ->
                    height.value = with(density) {
                        println(height)
                        coordinates.size.height.toDp()
                    }
                    itemHeight.value = height.value / firstIndexesByLetter.size
                }

            ){
                firstIndexesByLetter.entries.forEachIndexed { index, row ->
                    val rowIndex = index

                    val distanceFromSelection = (rowIndex - selectedItem.value).absoluteValue

                    val target = if (distanceFromSelection <= 11 && selectedItem.value >= 0 && selectionEnabled.value) {
                        val maxOffset = 70.dp - distanceFromEdge.value.times(1.2f)
                        val scaleFactor = (PI / 22.0) * distanceFromSelection
                        val sineValue = sin(PI - scaleFactor)
                        maxOffset * sineValue.toFloat() - maxOffset
                    } else {
                        0.dp
                    }
                    val animatedOffset by animateDpAsState(targetValue = target, tween(durationMillis = 300, easing = EaseOutCubic))

                    Row(modifier = Modifier
                        .offset {
                            IntOffset(
                                x = animatedOffset
                                    .toPx()
                                    .toInt(), y = 0
                            )
                        }
                        // .offset(x = animatedOffset)
                        .zIndex(100f)
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                if (row.value >= 0) {
                                    scrollToItem(row.value)
                                }

                            })
                        }){

                        Box(){
                            Layout(modifier = Modifier,
                                content = {
                                    Column(modifier = Modifier
                                        .wrapContentHeight()
                                        .align(Alignment.Center)) {
                                        if(distanceFromSelection==0 && selectionEnabled.value) {
                                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(50.dp).offset(x = -80.dp, y = -10.dp).clip(RoundedCornerShape(20.dp))
                                                        .background(
                                                        GetThemeColor.getText(
                                                            isSystemInDarkTheme()
                                                        )
                                                    )){
                                                        Text(textAlign = TextAlign.Center,modifier = Modifier
                                                            .align(Alignment.Center),text = row.key.toString(), fontSize = 25.sp, color = if(rowIndex==selectedItem.value){GetThemeColor.getPurple(isSystemInDarkTheme())}else{Color.White})
                                                    }



                                        }
                                    }
                                }
                            ) { measurables, constraints ->
                                val placeable = measurables.first().measure(constraints)
                                layout(0, 0) {
                                    placeable.placeRelative(0, 0)
                                }
                            }

                            Text(text = row.key.toString(), fontSize = if(rowIndex==selectedItem.value){16.sp}else{13.sp}, color = if(rowIndex==selectedItem.value){GetThemeColor.getPurple(isSystemInDarkTheme())}else{Color.White})
                        }
                    }

                }
            }


        }
    }

    println(firstIndexesByLetter)
}





@Composable
fun SongItem(
    song: Song,
    isSelected: Boolean,
    isPlaying:Boolean = false,
){
    val coroutineScope = rememberCoroutineScope({ Dispatchers.Default })
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    val animatedColor by animateColorAsState(targetValue = if(!isSelected){
        if(!isPlaying){
            GetThemeColor.getButton(isSystemInDarkTheme())
        }else{
            GetThemeColor.getPurple(isSystemInDarkTheme()).copy(0.7f)
        }
    }else{
        GetThemeColor.getPurple(isSystemInDarkTheme())
         }, tween(durationMillis = 400, easing = EaseIn))

    val size by animateFloatAsState(targetValue = if(isSelected){0.9f}else{1f}, tween(300, 0, EaseInOutBounce))




    DisposableEffect(song) {
        val job = coroutineScope.launch {
            delay(150)

                val result = BitmapLoader.loadBitmapAsync(
                    coroutineScope,
                    song.songPath
                ).await()
                println("Bitmap laoded")
                bitmap = result
            }
        onDispose {
            job.cancel()
            bitmap = null
        }
    }


    // Update the Surface color to the animated color
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 20.dp)
            .padding(vertical = 5.dp)
    ){
        Row(
            modifier = Modifier,
        ) {
            Box(modifier = Modifier
                .size(45.dp)
                .align(Alignment.CenterVertically)
                .clip(shape = RoundedCornerShape(45.dp))) {

                androidx.compose.animation.AnimatedVisibility(visible = bitmap!=null, enter = Transitions.enter, exit = Transitions.exit) {
                    Image(
                        bitmap = bitmap!!,
                        contentDescription = "desc",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(45.dp),
                    )
                }

                androidx.compose.animation.AnimatedVisibility(visible = bitmap==null, enter = Transitions.enter, exit = Transitions.exit) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_group_23_image_6),
                        contentDescription = "desc",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(45.dp),
                    )
                }

                if(isPlaying){
                    LottieCustom(
                        lottieCompositionSpec = LottieCompositionSpec.RawRes(R.raw.playing),
                        color = GetThemeColor.getText(isSystemInDarkTheme()),
                        isHidden = !isPlaying,
                        speed = 0.3f
                    )
                }
            }

            Spacer(modifier = Modifier
                .size(30.dp)
                .align(Alignment.CenterVertically))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(50.dp)
                    .scale(size)
                    .fillMaxWidth()
                    /*.drawBehind {
                        drawRoundRect(color = animatedColor, cornerRadius = CornerRadius(x = 1f, y = 1f))
                    }*/
                    .clip(RoundedCornerShape(30.dp))
                    .background(animatedColor))

            {
                Row(modifier = Modifier
                    .padding(start = 15.dp)
                    .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically) {
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