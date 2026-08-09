package com.aurora.motion

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween

object AuroraTokens {
    val FocusEnter: AnimationSpec<Float> get() = AuroraSpec.focusEnter
    val FocusExit: AnimationSpec<Float> get() = AuroraSpec.focusExit
    val CardLift: AnimationSpec<Float> get() = AuroraSpec.cardLift
    val CardPress: AnimationSpec<Float> get() = AuroraSpec.fast
    val ToolbarReveal: AnimationSpec<Float> get() = AuroraSpec.toolbarReveal
    val ToolbarHide: AnimationSpec<Float> get() = AuroraSpec.toolbarHide
    val SearchGlow: AnimationSpec<Float> get() = AuroraSpec.normal
    val DialogOpen: AnimationSpec<Float> get() = AuroraSpec.dialogOpen
    val DialogClose: AnimationSpec<Float> get() = AuroraSpec.dialogClose
    val ScreenEnter: AnimationSpec<Float> get() = AuroraSpec.screenEnter
    val ScreenExit: AnimationSpec<Float> get() = AuroraSpec.screenExit
    val FadeIn: AnimationSpec<Float> get() = AuroraSpec.fadeIn
    val FadeOut: AnimationSpec<Float> get() = AuroraSpec.fadeOut
    val SlideIn: AnimationSpec<Float> get() = AuroraSpec.slideIn
    val Pulse: AnimationSpec<Float> get() = infiniteRepeatable(AuroraSpec.pulse, RepeatMode.Reverse)
    val Glow: AnimationSpec<Float> get() = infiniteRepeatable(AuroraSpec.fast, RepeatMode.Reverse)
    val Ambient: AnimationSpec<Float> get() = infiniteRepeatable(AuroraSpec.ambientPulse, RepeatMode.Reverse)
    val Shimmer: AnimationSpec<Float> get() = infiniteRepeatable(AuroraSpec.shimmer)
    val CursorBlink: AnimationSpec<Float> get() = infiniteRepeatable(tween(250), RepeatMode.Reverse)
    val VoicePulse: AnimationSpec<Float> get() = infiniteRepeatable(tween(625), RepeatMode.Reverse)
}
