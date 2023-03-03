@file:OptIn(ExperimentalMaterialApi::class, ExperimentalCoroutinesApi::class)

package com.tiesiogdvd.composetest

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.tiesiogdvd.composetest.ui.bottomNavBar.BottomNavItem
import com.tiesiogdvd.composetest.ui.NavGraphs
import com.tiesiogdvd.composetest.ui.bottomNavBar.BottomNavItems
import com.tiesiogdvd.composetest.ui.bottomNavBar.BottomNavigationBar
import com.tiesiogdvd.composetest.ui.bottomNavBar.NavRoutes
import com.tiesiogdvd.composetest.ui.musicPlayer.MusicPlayer
import com.tiesiogdvd.composetest.ui.theme.*
import com.tiesiogdvd.composetest.ui.ytDownload.YtDownloadScreen
import com.tiesiogdvd.composetest.util.PermissionsRetriever
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi




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
                    items = BottomNavItems.BottomNavItems,
                    navController = navController,
                    onItemClick = {
                        navController.navigate(it.route,
                            navOptions = NavOptions.Builder()
                                .setPopUpTo(navController.currentDestination!!.id, true, saveState = true)
                                .setRestoreState(true)
                                .setLaunchSingleTop(true)
                                .build(),
                            navigatorExtras = null )
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
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavRoutes.LIBRARY.name) {
        composable(NavRoutes.LIBRARY.name, ) {
            DestinationsNavHost(navGraph = NavGraphs.root)
        }
        composable(NavRoutes.EQUALIZER.name) {
            Equalizer()
        }
        composable(NavRoutes.SETTINGS.name) {
            Settings()
        }
        composable(NavRoutes.PLAYER.name){
            MusicPlayer()
        }
        composable(NavRoutes.YT_DOWNLOAD.name){
            YtDownloadScreen(navController)
        }
    }

}

@Composable
fun Equalizer() {
    Surface {
        Text(text = "Equalizer")
    }
}


@Composable
fun Settings() {
    Surface {
        Text(text = "Equalizer")
    }
}






