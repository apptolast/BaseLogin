package com.apptolast.customlogin.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

object NavTransitions {

    private const val DURATION = 400
    private const val FADE_DURATION = 300

    /**
     * Transición al entrar a una nueva pantalla (hacia adelante)
     */
    val enter: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = FADE_DURATION,
            easing = FastOutSlowInEasing
        )
    )

    /**
     * Transición al salir de una pantalla (hacia adelante)
     */
    val exit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / 3 },
        animationSpec = tween(
            durationMillis = DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = FADE_DURATION,
            easing = FastOutSlowInEasing
        )
    )

    /**
     * Transición al volver a una pantalla (back)
     */
    val popEnter: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / 3 },
        animationSpec = tween(
            durationMillis = DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = FADE_DURATION,
            easing = FastOutSlowInEasing
        )
    )

    /**
     * Transición al salir de una pantalla (back)
     */
    val popExit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = FADE_DURATION,
            easing = FastOutSlowInEasing
        )
    )
}