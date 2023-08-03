@file:OptIn(ExperimentalMaterialApi::class, ExperimentalCoroutinesApi::class,
    ExperimentalAnimationApi::class
)

package com.tiesiogdvd.composetest

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.os.Build
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.tiesiogdvd.composetest.ui.NavGraphs
import com.tiesiogdvd.composetest.ui.bottomNavBar.BottomNavItems
import com.tiesiogdvd.composetest.ui.bottomNavBar.BottomNavigationBar
import com.tiesiogdvd.composetest.ui.bottomNavBar.NavRoutes
import com.tiesiogdvd.composetest.ui.musicPlayer.MusicPlayer
import com.tiesiogdvd.composetest.ui.settings.SettingsNavigation
import com.tiesiogdvd.composetest.ui.theme.*
import com.tiesiogdvd.composetest.ui.ytDownload.YtDownloadScreen
import com.tiesiogdvd.composetest.util.PermissionsRetriever
import com.tiesiogdvd.playlistssongstest.data.Song
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi




@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionsRetriever.checkPermissions(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContent {

            val context = LocalContext.current
            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager

            // For dark mode
            uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_YES)
           // uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)

            // For light mode
            //uiModeManager.nightMode = UiModeManager.MODE_NIGHT_NO
            val systemUiController = rememberSystemUiController()
            systemUiController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            systemUiController.isSystemBarsVisible = false // Status & Navigation bars
            val navController = rememberAnimatedNavController()
            Scaffold(bottomBar = {
                BottomNavigationBar(
                    items = BottomNavItems.BottomNavItems,
                    navController = navController,
                    onItemClick = {
                        val currentDest = navController.currentDestination?.route
                        val itDest = it.route
                        val matches = currentDest==NavRoutes.LIBRARY.name && itDest==NavRoutes.LIBRARY.name

                        navController.navigate(it.route,
                            navOptions = NavOptions.Builder()
                                .setPopUpTo(navController.currentDestination!!.id, !matches, saveState = !matches)
                                .setRestoreState(!matches)
                                .setLaunchSingleTop(!matches)
                                .build(),
                            navigatorExtras = null)
                    })
            }
            ) {
                Navigation(navController = navController)
            }

        }

    }
}


@Composable
fun Navigation(navController: NavHostController) {
    AnimatedNavHost(navController = navController, startDestination = NavRoutes.LIBRARY.name) {
        composable(NavRoutes.LIBRARY.name, enterTransition = { Transitions.enter }, exitTransition = { Transitions.exit }, popEnterTransition = { Transitions.enter }, popExitTransition = { Transitions.exit } )

        {
            DestinationsNavHost(navGraph = NavGraphs.root)
        }

        composable(NavRoutes.EQUALIZER.name, enterTransition = { Transitions.enter }, exitTransition = { Transitions.exit }, popEnterTransition = { Transitions.enter }, popExitTransition = {  Transitions.exit } )
        {
            Equalizer(){}
        }

        /*composable(NavRoutes.SETTINGS.name, enterTransition = { Transitions.enter }, exitTransition = { Transitions.exit }, popEnterTransition = { Transitions.enter }, popExitTransition = {  Transitions.exit } )
        {
            Settings(navController)
        }*/

        composable(NavRoutes.PLAYER.name,enterTransition = { Transitions.enter  }, exitTransition = {  Transitions.exit  }, popEnterTransition = { Transitions.enter }, popExitTransition = {  Transitions.exit } )
        {
            MusicPlayer()
        }

        composable(NavRoutes.YT_DOWNLOAD.name, enterTransition = { Transitions.enter }, exitTransition = {  Transitions.exit }, popEnterTransition = { Transitions.enter }, popExitTransition = {  Transitions.exit } )
        {
            YtDownloadScreen(navController)
        }

        SettingsNavigation(navController = navController, navGraphBuilder = this)

    }
}



@Composable
fun Equalizer(
    content: @Composable BoxScope.() -> Unit
) {

    val dummyList = ArrayList<Song>()
    for(index in 1..100){
        dummyList.add(Song(songArtist = "", songPath = "", songName = index.toString(), playlistId = 5))
    }

    FlowPlayerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = GetThemeColor.getBackground(isSystemInDarkTheme())
        ) {
            Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 25.dp, start = 25.dp)) {
            Text("Library", fontSize = 45.sp, modifier = Modifier.padding(bottom = 15.dp))
            Text("Recent Playlists", fontSize = 22.sp, modifier = Modifier.padding(bottom = 3.dp))

            Box(modifier = Modifier
                .fillMaxWidth(), content = content)

             }
        }
    }
}







