package com.tiesiogdvd.composetest.ui.bottomNavBar

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import com.tiesiogdvd.playlistssongstest.data.Song
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavItem) -> Unit,

    viewModel: NavbarViewModel = hiltViewModel(),
) {

    val isNavbarVisible = viewModel.isNavbarVisible.collectAsState().value
    val isSongbarVisible = viewModel.isSongbarVisible.collectAsState().value
    val backStackEntry = navController.currentBackStackEntryAsState() //Important for recomposition when route changes
    val currentSong = viewModel.currentSource.collectAsState(initial = Song("Loading", "Loading", playlistId = 0)).value
    val currentDestination = backStackEntry.value?.destination?.route

    AnimatedVisibility(visible = isNavbarVisible, enter = fadeIn(), exit = fadeOut()) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .wrapContentHeight()
                .clickable {
                    onItemClick(MusicPlayerItem.item)
                },
            shape = RoundedCornerShape(30.dp),
            color = GetThemeColor.getBackgroundThird(isSystemInDarkTheme())
        ) {

            Column {
                if(currentSong!=null){
                    AnimatedVisibility(visible = isSongbarVisible==true && currentDestination!=MusicPlayerItem.item.route, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        Surface(modifier = Modifier.height(50.dp), color = Color.Transparent) {
                            SongItemBar(currentSong)
                        }
                    }
                }


                //---------------------------------------------------------------------------
                BottomNavigation(
                    // modifier = modifier.blur(20.dp, BlurredEdgeTreatment.Rectangle),
                    modifier = modifier.fillMaxWidth(),
                    elevation = 0.dp,
                    backgroundColor = Color.Transparent
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        items.forEach { item ->
                            val selected = item.route == currentDestination
                            BottomNavigationItem(selected = selected,
                                onClick = { onItemClick(item) },
                                selectedContentColor = GetThemeColor.getDrawableBar(
                                    isSystemInDarkTheme()
                                ),
                                unselectedContentColor = GetThemeColor.getDrawableMenu(
                                    isSystemInDarkTheme()
                                ),
                                icon = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            painterResource(id = item.icon),
                                            contentDescription = item.name,
                                            modifier = Modifier
                                                .height(40.dp)
                                                .padding(top = 7.dp, bottom = 0.dp)
                                        )
                                        if (selected) {
                                            Text(
                                                modifier = Modifier
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
                //---------------------------------------------------------------------------
            }


        }

    }





}