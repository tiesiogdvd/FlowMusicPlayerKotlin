package com.tiesiogdvd.composetest.ui.lottieAnimation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.compose.*
import com.tiesiogdvd.composetest.ui.theme.Transitions

@Composable
fun LottieCustom(
    lottieCompositionSpec: LottieCompositionSpec,
    isPlaying:Boolean = true,
    isHidden:Boolean = false,
    speed:Float = 0.3f,
    reverseOnRepeat:Boolean = true,
    iterations:Int = LottieConstants.IterateForever,
    restartOnPlay: Boolean = true,
    color: Color = Color.Black,
    enterTransition: EnterTransition = Transitions.enter,
    exitTransition: ExitTransition = Transitions.exit
){
    val composition by rememberLottieComposition(spec = lottieCompositionSpec)
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(color.toArgb()),
            keyPath = arrayOf("**")
        )
    )
    val progressAnimation by animateLottieCompositionAsState(composition = composition, isPlaying = isPlaying, iterations = iterations, speed = speed, restartOnPlay = restartOnPlay, reverseOnRepeat = reverseOnRepeat)
    AnimatedVisibility(visible = !isHidden, enter = enterTransition , exit = exitTransition) {
        LottieAnimation(composition = composition, progress = {progressAnimation}, dynamicProperties = dynamicProperties)

    }
}