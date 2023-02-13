@file:OptIn(ExperimentalMaterialApi::class, ExperimentalCoroutinesApi::class)

package com.tiesiogdvd.composetest

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle

import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.datastore.preferences.core.emptyPreferences
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.session.MediaBrowser
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.tiesiogdvd.composetest.ui.BottomNavItem
import com.tiesiogdvd.composetest.ui.MainViewModel
import com.tiesiogdvd.composetest.ui.NavGraphs
import com.tiesiogdvd.composetest.ui.destinations.Destination
import com.tiesiogdvd.composetest.ui.library.LibraryViewModel
import com.tiesiogdvd.composetest.ui.libraryPlaylist.recomposeHighlighter
import com.tiesiogdvd.composetest.ui.theme.*
import com.tiesiogdvd.composetest.util.MusicDataMetadata
import com.tiesiogdvd.composetest.util.PermissionsRetriever
import com.tiesiogdvd.composetest.util.TypeConverter
import com.tiesiogdvd.playlistssongstest.data.Song
import com.tiesiogdvd.service.MusicService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


private lateinit var mediaBrowser: MediaBrowser

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @RequiresApi(Build.VERSION_CODES.R)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionsRetriever.checkPermissions(this)
        setContent {
            val systemUiController = rememberSystemUiController()
            systemUiController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            systemUiController.isSystemBarsVisible = false // Status & Navigation bars
            val navController = rememberNavController()
            Scaffold(bottomBar = {
                BottomNavigationBar(
                    items = listOf(
                        BottomNavItem(
                            name = "Library",
                            route = "library",
                            icon = R.drawable.ic_action_navbar_library2
                        ),
                        BottomNavItem(
                            name = "Equalizer",
                            route = "equalizer",
                            icon = R.drawable.ic_action_navbar_equalizer1
                        ),
                        BottomNavItem(
                            name = "Settings",
                            route = "settings",
                            icon = R.drawable.ic_action_navbar_settings
                        )
                        ),
                    navController = navController,
                    onItemClick = {
                        navController.popBackStack("library", true)
                        navController.popBackStack("settings", true)
                        navController.popBackStack("equalizer", true)
                        navController.navigate(it.route)
                    })
            }
            ) {
                Navigation(navController = navController)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavItem) -> Unit,

    viewModel: MainViewModel = hiltViewModel(),
) {
    val backStackEntry =
        navController.currentBackStackEntryAsState() //Important for recomposition when route changes
    val currentSong = viewModel.currentSource.collectAsState(
        initial = Song("Loading", "Loading", playlistId = 0)
    ).value
    Surface(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .wrapContentHeight()
            .clickable {
                //  viewModel.skipToNextSong()
                viewModel.playSource()
                println("HOHOHOHO")

            },
        shape = RoundedCornerShape(30.dp),
        color = GetThemeColor.getBackgroundThird(isSystemInDarkTheme())
    ) {

        Column() {
            Surface(modifier = Modifier.height(50.dp), color = Color.Transparent) {
                if (currentSong!=null){
                    SongItemBar(currentSong)
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
                        val selected = item.route == backStackEntry.value?.destination?.route
                        BottomNavigationItem(selected = selected,
                            onClick = { onItemClick(item) },
                            selectedContentColor = GetThemeColor.getDrawableBar(isSystemInDarkTheme()),
                            unselectedContentColor = GetThemeColor.getDrawableMenu(
                                isSystemInDarkTheme()
                            ),
                            icon = {
                                Column(horizontalAlignment = CenterHorizontally) {
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


@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "library") {
        //navController.popBackStack()
        composable("library") {
            // navController.popBackStack("library",true)
            //navController.popBackStack("library",false)
            DestinationsNavHost(navGraph = NavGraphs.root)
        }
        composable("equalizer") {
            //navController.popBackStack("equalizer",false)
            Equalizer()
            //DestinationsNavHost(navGraph = NavGraphs.root)
        }
        composable("settings") {
            //navController.popBackStack("settings",false)
            Settings()
            // DestinationsNavHost(navGraph = NavGraphs.root)
        }
    }

}

@Composable
fun Equalizer() {
    Surface() {
        Text(text = "Equalizer")
    }
}


@Composable
fun Settings() {
    Surface() {
        Text(text = "Equalizer")
    }
}


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun SongItemBar(song: Song) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 20.dp)
            .padding(vertical = 5.dp)
            .recomposeHighlighter()
            .padding(start = 15.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier,
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(MusicDataMetadata.getBitmap(song.songPath))
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(id = R.drawable.ic_group_23_image_6),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(40.dp),
                    contentDescription = "desc",
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(50.dp)
                    .fillMaxWidth(),
                color = Color.Transparent,
                shape = RoundedCornerShape(30.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(3.5F)
                    ) {
                        Text(
                            text = song.songName.toString(),
                            fontSize = 14.sp,
                            color = GetThemeColor.getText(isSystemInDarkTheme()),
                            modifier = Modifier.wrapContentHeight(
                                Alignment.Bottom
                            )
                        )
                        Text(
                            text = song.songArtist.toString(),
                            fontSize = 10.sp,
                            color = GetThemeColor.getText(isSystemInDarkTheme()),
                            modifier = Modifier.wrapContentHeight(
                                Alignment.Bottom
                            )
                        )
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.ic_action_pause),
                        contentDescription = "icon",
                        modifier = Modifier
                            .height(35.dp)
                            .align(Alignment.CenterVertically),
                        tint = GetThemeColor.getDrawableBar(isSystemInDarkTheme())
                    )

                }
            }
        }
    }
}



