package com.aurora.motion

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing

object AuroraEasing {
    val linear = LinearEasing
    val standard = FastOutSlowInEasing
    val decelerate = Easing { t -> 1f - (1f - t) * (1f - t) * (1f - t) }
    val accelerate = Easing { t -> t * t * t }
    val emphasize = Easing { t -> if (t < 0.5f) 4f * t * t * t else 1f - (1f - t) * (1f - t) * (1f - t) * (1f - t).let { it * it } }
    val snap = Easing { t -> if (t < 1f) 0f else 1f }
}
