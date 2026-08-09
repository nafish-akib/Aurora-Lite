package com.aurora.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

object AuroraTransitions {
    val screenEnter: EnterTransition = fadeIn(
        tween(AuroraDurations.screenEnter, easing = AuroraEasing.decelerate)
    ) + slideInHorizontally(
        animationSpec = tween(AuroraDurations.screenEnter, easing = AuroraEasing.decelerate),
        initialOffsetX = { it / 8 }
    )

    val screenExit: ExitTransition = fadeOut(
        tween(AuroraDurations.screenExit, easing = AuroraEasing.standard)
    )

    val dialogEnter: EnterTransition = fadeIn(
        tween(AuroraDurations.dialogOpen, easing = AuroraEasing.decelerate)
    ) + slideInVertically(
        animationSpec = tween(AuroraDurations.dialogOpen, easing = AuroraEasing.decelerate),
        initialOffsetY = { it / 4 }
    )

    val dialogExit: ExitTransition = fadeOut(
        tween(AuroraDurations.dialogClose, easing = AuroraEasing.standard)
    )

    val settingsEnter: EnterTransition = fadeIn(
        tween(AuroraDurations.slideIn, easing = AuroraEasing.decelerate)
    ) + slideInVertically(
        animationSpec = tween(AuroraDurations.slideIn, easing = AuroraEasing.decelerate),
        initialOffsetY = { it }
    )

    val settingsExit: ExitTransition = fadeOut(tween(200)) + slideOutVertically(
        animationSpec = tween(200),
        targetOffsetY = { it / 4 }
    )

    val instant: EnterTransition = fadeIn(tween(0))
    val instantExit: ExitTransition = fadeOut(tween(0))
}

fun AnimatedContentTransitionScope<*>.auroraSlideIn(): EnterTransition =
    fadeIn(tween(AuroraDurations.screenEnter)) + slideInHorizontally(
        animationSpec = tween(AuroraDurations.screenEnter),
        initialOffsetX = { it }
    )

fun AnimatedContentTransitionScope<*>.auroraSlideOut(): ExitTransition =
    fadeOut(tween(AuroraDurations.screenExit)) + slideOutHorizontally(
        animationSpec = tween(AuroraDurations.screenExit),
        targetOffsetX = { -it / 4 }
    )
