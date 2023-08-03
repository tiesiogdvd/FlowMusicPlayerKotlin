@file:OptIn(ExperimentalAnimationApi::class)

package com.tiesiogdvd.composetest.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin

object Transitions {

    val enter2 = slideInVertically(
        initialOffsetY = { -40 }
    ) + expandVertically(
        expandFrom = Alignment.CenterVertically
    ) + scaleIn(
        // Animate scale from 0f to 1f using the top center as the pivot point.
        transformOrigin = TransformOrigin(
            1.5f, 0f)
    ) + fadeIn(initialAlpha = 0f)

    val enter = scaleIn(tween(300, easing = EaseInOut),initialScale = 1.15f) + fadeIn(tween(400, easing = EaseInOut))

    val exit = fadeOut(tween(600, easing = EaseInOut)) + scaleOut(tween(300, easing = EaseInOut),targetScale = 1.15f)


    val enterSwing = scaleIn(tween(300, easing = EaseInOut),initialScale = 1.25f) + fadeIn(tween(400, easing = EaseInOut))
}