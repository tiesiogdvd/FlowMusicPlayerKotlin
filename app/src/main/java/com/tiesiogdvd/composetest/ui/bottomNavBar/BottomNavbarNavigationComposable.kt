package com.tiesiogdvd.composetest.ui.bottomNavBar

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tiesiogdvd.composetest.ui.libraryPlaylist.recomposeHighlighter
import com.tiesiogdvd.composetest.ui.musicPlayer.MusicPlayerViewModel
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.playlistssongstest.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.exp

@ExperimentalCoroutinesApi
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavItem) -> Unit,
    viewModelPlayer: MusicPlayerViewModel = hiltViewModel(),
    viewModel: NavbarViewModel = hiltViewModel(),
) {
    //var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    val bitmap = viewModelPlayer.bitmap.collectAsState().value

    //val bitmap2 = viewModelPlayer.bitmap.collectAsState()

    val isNavbarVisible = viewModel.isNavbarVisible.collectAsState().value
    val isSongbarVisible = viewModel.isSongbarVisible.collectAsState().value
    val backStackEntry = navController.currentBackStackEntryAsState() //Important for recomposition when route changes
    val currentSong = viewModel.currentSource.collectAsState(initial = Song("Loading", "Loading")).value
    val currentDestination = backStackEntry.value?.destination?.route

    val expanded = remember { mutableStateOf(false) }
    val expandedvalue = remember { derivedStateOf{ expanded.value } }
    val coroutineScope = rememberCoroutineScope()

    val showSongItem = isSongbarVisible==true && currentDestination!=MusicPlayerItem.item.route

    val targetHeight = if(showSongItem){if (expandedvalue.value) 100.dp else 50.dp}else{50.dp}
    //val targetHeight = if (expandedvalue.value) 100.dp else 50.dp
    val animatedHeight by animateDpAsState(targetHeight)




    /*LaunchedEffect(currentSong.songPath) {
        withContext(Dispatchers.IO) {
        //    bitmap = MusicDataMetadata.getBitmap(currentSong.songPath)
        }
    }*/


    AnimatedVisibility(visible = isNavbarVisible, enter = fadeIn(), exit = fadeOut()) {
        Surface(
            modifier = Modifier
          //      .recomposeHighlighter()
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .height(animatedHeight)
                .draggable(orientation = Orientation.Vertical, state = rememberDraggableState {

                },
                    onDragStopped = { velocity ->
                        coroutineScope.launch {
                            if (velocity < -200) {
                                println(velocity)
                                expanded.value = true
                            }

                            if (velocity > 200) {
                                expanded.value = false
                            }

                        }
                    })
                .wrapContentHeight()
                .clickable {
                    onItemClick(MusicPlayerItem.item)
                },
            shape = RoundedCornerShape(30.dp),
            color = GetThemeColor.getBackgroundThird(isSystemInDarkTheme())
        ) {
            if(bitmap!=null){
                Image(
                    bitmap = bitmap!!,
                    contentDescription = "background",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                  //      .recomposeHighlighter()
                        .fillMaxWidth()
                        .graphicsLayer {
                            renderEffect = BlurEffect(400f, 400f, TileMode.Mirror)
                        },
                )
            }


            Column {
                if(currentSong!=null){
                    AnimatedVisibility(visible = showSongItem, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        Surface(modifier = Modifier
                 //           .recomposeHighlighter()
                            .height(50.dp), color = Color.Transparent) {
                            SongItemBar(currentSong)
                        }
                    }
                }
                //---------------------------------------------------------------------------
                navItems(items = items, onItemClick = {onItemClick(it)}, currentDestination = currentDestination)
                //---------------------------------------------------------------------------
            }


        }

    }
}


@Composable
fun navItems(
    items: List<BottomNavItem>,
    onItemClick: (BottomNavItem) -> Unit,
    currentDestination: String?
    ){


    //val currentDestination = backStackEntry.value?.destination?.route


    BottomNavigation(
        // modifier = modifier.blur(20.dp, BlurredEdgeTreatment.Rectangle),
        modifier = Modifier
            .fillMaxWidth()
       //     .recomposeHighlighter(),
                ,
        elevation = 0.dp,
        backgroundColor = Color.Transparent
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            items.forEach { item ->
                val selected by remember(item.route, currentDestination) { derivedStateOf { item.route == currentDestination } }

                BottomNavigationItem(selected = selected,
                    onClick = { onItemClick(item) },
                    selectedContentColor = GetThemeColor.getDrawableBar(
                        isSystemInDarkTheme()
                    ),
                    unselectedContentColor = GetThemeColor.getDrawableBar(
                        isSystemInDarkTheme()
                    ),
                    icon = {

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painterResource(id = item.icon),
                                contentDescription = item.name,
                                modifier = Modifier
                      //              .recomposeHighlighter()
                                    .height(40.dp)
                                    .padding(top = 7.dp, bottom = 0.dp)
                            )
                            AnimatedVisibility(visible = selected) {
                                Text(
                                    modifier = Modifier
                        //                .recomposeHighlighter()
                                        .offset(y = -8.dp)
                                        .padding(bottom = 0.dp),
                                    text = item.name,
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp
                                )
                            }

                        }
                    })
            }
        }
    }
}
