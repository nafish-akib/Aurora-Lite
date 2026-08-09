package com.aurora.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aurora.motion.AuroraMotion
import com.aurora.motion.AuroraParticle
import com.aurora.motion.AuroraStaggerDelay
import com.aurora.motion.auroraAmbient
import com.aurora.motion.auroraFade
import com.aurora.motion.auroraFocus
import com.aurora.motion.auroraGlass
import com.aurora.motion.auroraHover
import com.aurora.motion.auroraLift
import com.aurora.motion.auroraShimmer
import com.aurora.motion.auroraSweep
import com.aurora.motion.generateParticleBurst as motionGenerateParticleBurst

typealias Particle = AuroraParticle
typealias StaggerDelay = AuroraStaggerDelay

@Composable fun rememberBrandDotPulse() = AuroraMotion.Pulse()
@Composable fun rememberOnlineDotPulse() = AuroraMotion.Pulse()
@Composable fun rememberFocusGlowAlpha() = AuroraMotion.Glow()
@Composable fun rememberAmbientPulseOffset(index: Int) = AuroraMotion.DriftOffset(index)
@Composable fun rememberVoicePulse() = AuroraMotion.VoicePulse()
@Composable fun rememberSparkleRotation() = AuroraMotion.SparkleRotation()
@Composable fun rememberStaggerAlpha(stagger: StaggerDelay) = AuroraMotion.StaggerAlpha(stagger)
@Composable fun rememberCursorBlink() = AuroraMotion.CursorBlink()
@Composable fun rememberAnimatedCounter(target: Int, durationMs: Long = 500) = AuroraMotion.Counter(target, durationMs)
@Composable fun rememberSettingsAlpha() = AuroraMotion.SettingsSlide()

fun Modifier.cardLift(isFocused: Boolean) = auroraLift(isFocused)
fun Modifier.focusScale(isFocused: Boolean, scaleTarget: Float = 1.03f) = auroraFocus(isFocused, scaleTarget)
fun Modifier.lightSweep(isFocused: Boolean) = auroraSweep(isFocused)
fun Modifier.ambientPulse(index: Int = 0) = auroraAmbient(index)
fun Modifier.shimmer() = auroraShimmer()
fun Modifier.glassReflection() = auroraGlass()

fun generateParticleBurst(x: Float, y: Float) = motionGenerateParticleBurst(x, y)
