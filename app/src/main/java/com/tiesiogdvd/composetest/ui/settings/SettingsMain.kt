package com.tiesiogdvd.composetest.ui.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import com.tiesiogdvd.composetest.ui.bottomNavBar.NavRoutes
import com.tiesiogdvd.composetest.ui.header.HeaderSettings
import com.tiesiogdvd.composetest.ui.theme.Transitions

@OptIn(ExperimentalAnimationApi::class)
fun SettingsNavigation(navController: NavHostController, navGraphBuilder: NavGraphBuilder) {
    navGraphBuilder.apply {

        composable(NavRoutes.SETTINGS.name, enterTransition = { Transitions.enter }, exitTransition = { Transitions.exit }, popEnterTransition = { Transitions.enter }, popExitTransition = {  Transitions.exit } )
        {
            SettingsMain(navController)
        }

        composable(SettingsType.PLAYER_SCREEN.name, enterTransition = { Transitions.enter }, exitTransition = { Transitions.exit }, popEnterTransition = { Transitions.enter }, popExitTransition = {  Transitions.exit } )
        {
            SettingsPlayer()
        }
    }
}

@Composable
fun SettingsMain(navController: NavHostController) {

    Surface {
        HeaderSettings(headerName = "Settings"){
            Column {
                for(setting in SettingsNavItems.items){
                    SettingsType(onClick = { navController.navigate(setting.route) }, settingName = setting.name, settingsIcon = setting.icon, modifier = Modifier.padding(bottom = 15.dp))
                }

            }

        }
    }
}
