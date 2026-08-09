package com.aurora.motion

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object AuroraSpec {
    val fast by lazy { tween<Float>(AuroraDurations.fast, easing = AuroraEasing.standard) }
    val normal by lazy { tween<Float>(AuroraDurations.normal, easing = AuroraEasing.standard) }
    val medium by lazy { tween<Float>(AuroraDurations.medium, easing = AuroraEasing.decelerate) }
    val slow by lazy { tween<Float>(AuroraDurations.slow, easing = AuroraEasing.decelerate) }
    val xslow by lazy { tween<Float>(AuroraDurations.xslow, easing = AuroraEasing.decelerate) }

    val springBouncy by lazy { spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow) }
    val springStiff by lazy { spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium) }

    val focusEnter by lazy { tween<Float>(AuroraDurations.focusEnter, easing = AuroraEasing.decelerate) }
    val focusExit by lazy { tween<Float>(AuroraDurations.focusExit, easing = AuroraEasing.standard) }
    val cardLift by lazy { tween<Float>(AuroraDurations.cardLift, easing = AuroraEasing.decelerate) }
    val fadeIn by lazy { tween<Float>(AuroraDurations.fadeIn, easing = AuroraEasing.standard) }
    val fadeOut by lazy { tween<Float>(AuroraDurations.fadeOut, easing = AuroraEasing.standard) }
    val toolbarReveal by lazy { tween<Float>(AuroraDurations.toolbarReveal, easing = AuroraEasing.decelerate) }
    val toolbarHide by lazy { tween<Float>(AuroraDurations.toolbarHide, easing = AuroraEasing.standard) }
    val dialogOpen by lazy { tween<Float>(AuroraDurations.dialogOpen, easing = AuroraEasing.decelerate) }
    val dialogClose by lazy { tween<Float>(AuroraDurations.dialogClose, easing = AuroraEasing.standard) }
    val screenEnter by lazy { tween<Float>(AuroraDurations.screenEnter, easing = AuroraEasing.decelerate) }
    val screenExit by lazy { tween<Float>(AuroraDurations.screenExit, easing = AuroraEasing.standard) }
    val slideIn by lazy { tween<Float>(AuroraDurations.slideIn, easing = AuroraEasing.decelerate) }

    val pulse by lazy { tween<Float>(AuroraDurations.pulsePeriod.toInt() / 2, easing = AuroraEasing.linear) }
    val shimmer by lazy { tween<Float>(AuroraDurations.shimmerPeriod, easing = AuroraEasing.linear) }
    val ambientPulse by lazy { tween<Float>(AuroraDurations.ambientPeriod.toInt() / 2, easing = AuroraEasing.linear) }
    val stagger by lazy { tween<Float>(AuroraDurations.staggerBase, easing = AuroraEasing.decelerate) }
}

object AuroraSpringSpec {
    val cardLift by lazy { spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow) }
    val focusGlow by lazy { spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium) }
    val snap by lazy { spring<Float>(Spring.DampingRatioNoBouncy, Spring.StiffnessHigh) }
}
