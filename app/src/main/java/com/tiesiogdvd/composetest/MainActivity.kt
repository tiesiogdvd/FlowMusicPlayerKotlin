@file:OptIn(ExperimentalMaterialApi::class, ExperimentalCoroutinesApi::class,
    ExperimentalAnimationApi::class
)

package com.tiesiogdvd.composetest

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*

import androidx.compose.material.*
import androidx.compose.runtime.*
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
import com.tiesiogdvd.composetest.ui.theme.*
import com.tiesiogdvd.composetest.ui.ytDownload.YtDownloadScreen
import com.tiesiogdvd.composetest.util.PermissionsRetriever
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi




@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionsRetriever.checkPermissions(this)
        setContent {
            val systemUiController = rememberSystemUiController()
            systemUiController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
                        println(matches)
                        navController.navigate(it.route,
                            navOptions = NavOptions.Builder()
                                .setPopUpTo(navController.currentDestination!!.id, !matches, saveState = !matches)
                                .setRestoreState(!matches)
                                .setLaunchSingleTop(!matches)
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


@Composable
fun Navigation(navController: NavHostController) {
    AnimatedNavHost(navController = navController, startDestination = NavRoutes.LIBRARY.name) {
        composable(NavRoutes.LIBRARY.name, enterTransition = { Transitions.enter }, exitTransition = { Transitions.exit }, popEnterTransition = { Transitions.enter }, popExitTransition = { Transitions.exit } )

        {
            DestinationsNavHost(navGraph = NavGraphs.root)
        }


        composable(NavRoutes.EQUALIZER.name, enterTransition = { Transitions.enter }, exitTransition = { Transitions.exit }, popEnterTransition = { Transitions.enter }, popExitTransition = {  Transitions.exit } )
        {
            Equalizer()
        }

        composable(NavRoutes.SETTINGS.name, enterTransition = { Transitions.enter }, exitTransition = { Transitions.exit }, popEnterTransition = { Transitions.enter }, popExitTransition = {  Transitions.exit } )
        {
            Settings()
        }

        composable(NavRoutes.PLAYER.name,enterTransition = { Transitions.enter  }, exitTransition = {  Transitions.exit  }, popEnterTransition = { Transitions.enter }, popExitTransition = {  Transitions.exit } )
        {
            MusicPlayer()
        }

        composable(NavRoutes.YT_DOWNLOAD.name, enterTransition = { Transitions.enter }, exitTransition = {  Transitions.exit }, popEnterTransition = { Transitions.enter }, popExitTransition = {  Transitions.exit } )
        {
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






