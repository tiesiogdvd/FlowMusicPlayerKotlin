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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.tiesiogdvd.composetest.ui.bottomNavBar.BottomNavItem
import com.tiesiogdvd.composetest.ui.NavGraphs
import com.tiesiogdvd.composetest.ui.bottomNavBar.BottomNavItems
import com.tiesiogdvd.composetest.ui.bottomNavBar.BottomNavigationBar
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
                        navController.popBackStack("library", true)
                        navController.popBackStack("player", true)
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
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            DestinationsNavHost(navGraph = NavGraphs.root)
        }
        composable("equalizer") {
            Equalizer()
        }
        composable("settings") {
            Settings()
        }
        composable("player"){
            MusicPlayer()
        }
        composable("yt_download"){
            YtDownloadScreen()
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






