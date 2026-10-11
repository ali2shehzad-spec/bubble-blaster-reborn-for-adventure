package com.example.neonpop.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.neonpop.model.MascotMood
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom vector-rendered animated mascot "Pip" for Neon Dreamscape.
 * Dynamic expressions, antenna bounce, pupil tracking, and mood animations.
 */
@Composable
fun PipMascotView(
    mood: MascotMood,
    aimAngleRad: Float = -1.57f,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    skinId: String = "classic"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pip_anim")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pip_float"
    )

    val blinkState by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pip_blink"
    )

    val antennaWobble by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pip_antenna"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pip_aura"
    )

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f + (if (mood == MascotMood.EXCITED) floatOffset * 2.2f else floatOffset)
            val radius = this.size.width * 0.34f

            drawPip(
                cx = cx,
                cy = cy,
                radius = radius,
                mood = mood,
                aimAngleRad = aimAngleRad,
                blinkProgress = blinkState,
                antennaWobble = antennaWobble,
                auraPulse = auraPulse,
                skinId = skinId
            )
        }
    }
}

private fun DrawScope.drawPip(
    cx: Float,
    cy: Float,
    radius: Float,
    mood: MascotMood,
    aimAngleRad: Float,
    blinkProgress: Float,
    antennaWobble: Float,
    auraPulse: Float,
    skinId: String
) {
    // Determine colors by skin
    val (bodyColor1, bodyColor2, glowColor) = when (skinId) {
        "cyber" -> Triple(Color(0xFF00E5FF), Color(0xFF1DE9B6), Color(0x6600E5FF))
        "hologram" -> Triple(Color(0xFFE040FB), Color(0xFF7C4DFF), Color(0x66E040FB))
        "bunny" -> Triple(Color(0xFFFF80AB), Color(0xFFFF4081), Color(0x66FF80AB))
        "dj" -> Triple(Color(0xFFFFD600), Color(0xFFFF6D00), Color(0x66FFD600))
        else -> Triple(Color(0xFFE8DDFF), Color(0xFFB388FF), Color(0x66B388FF))
    }

    // 1. Soft Glowing Aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor, Color.Transparent),
            center = Offset(cx, cy),
            radius = radius * 1.55f * auraPulse
        ),
        radius = radius * 1.55f * auraPulse,
        center = Offset(cx, cy)
    )

    // 2. Star Antennae
    val leftAntennaAngle = -0.55f + antennaWobble
    val rightAntennaAngle = 0.55f - antennaWobble
    val antennaLen = radius * 0.75f

    // Left Antenna
    val ax1 = cx - radius * 0.45f
    val ay1 = cy - radius * 0.7f
    val tip1X = ax1 + sin(leftAntennaAngle) * antennaLen
    val tip1Y = ay1 - cos(leftAntennaAngle) * antennaLen
    drawLine(
        color = bodyColor2,
        start = Offset(ax1, ay1),
        end = Offset(tip1X, tip1Y),
        strokeWidth = radius * 0.12f,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )
    drawStar(tip1X, tip1Y, radius * 0.28f, Color(0xFFFFD700))

    // Right Antenna
    val ax2 = cx + radius * 0.45f
    val ay2 = cy - radius * 0.7f
    val tip2X = ax2 + sin(rightAntennaAngle) * antennaLen
    val tip2Y = ay2 - cos(rightAntennaAngle) * antennaLen
    drawLine(
        color = bodyColor2,
        start = Offset(ax2, ay2),
        end = Offset(tip2X, tip2Y),
        strokeWidth = radius * 0.12f,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )
    drawStar(tip2X, tip2Y, radius * 0.28f, Color(0xFFFFD700))

    // 3. Cute Floating Sprite Body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(bodyColor1, bodyColor2),
            center = Offset(cx - radius * 0.25f, cy - radius * 0.25f),
            radius = radius * 1.3f
        ),
        radius = radius,
        center = Offset(cx, cy)
    )

    // Body Glass highlight
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
            center = Offset(cx - radius * 0.35f, cy - radius * 0.35f),
            radius = radius * 0.5f
        ),
        radius = radius * 0.45f,
        center = Offset(cx - radius * 0.3f, cy - radius * 0.3f)
    )

    // 4. Blush Cheeks
    val blushColor = Color(0xFFFF4081).copy(alpha = 0.55f)
    drawCircle(blushColor, radius = radius * 0.18f, center = Offset(cx - radius * 0.52f, cy + radius * 0.18f))
    drawCircle(blushColor, radius = radius * 0.18f, center = Offset(cx + radius * 0.52f, cy + radius * 0.18f))

    // 5. Expressive Eyes
    val isBlinking = blinkProgress < 0.08f && mood != MascotMood.SHOCKED && mood != MascotMood.EXCITED
    val eyeRadiusX = radius * 0.18f
    val eyeRadiusY = if (isBlinking || mood == MascotMood.SLEEPY) radius * 0.03f else radius * 0.22f

    val eyeLeftX = cx - radius * 0.32f
    val eyeRightX = cx + radius * 0.32f
    val eyeY = cy - radius * 0.05f

    // Pupil offset tracking aim
    val lookX = cos(aimAngleRad) * (radius * 0.08f)
    val lookY = sin(aimAngleRad) * (radius * 0.06f)

    if (mood == MascotMood.HAPPY || mood == MascotMood.VICTORY) {
        // Happy arch eyes "^ ^"
        drawArc(
            color = Color(0xFF2C194D),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(eyeLeftX - eyeRadiusX, eyeY - eyeRadiusY),
            size = androidx.compose.ui.geometry.Size(eyeRadiusX * 2, eyeRadiusY * 2),
            style = Stroke(width = radius * 0.09f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFF2C194D),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(eyeRightX - eyeRadiusX, eyeY - eyeRadiusY),
            size = androidx.compose.ui.geometry.Size(eyeRadiusX * 2, eyeRadiusY * 2),
            style = Stroke(width = radius * 0.09f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    } else {
        // Dark anime style eyes with sparkles
        drawOval(
            color = Color(0xFF200F3E),
            topLeft = Offset(eyeLeftX - eyeRadiusX, eyeY - eyeRadiusY),
            size = androidx.compose.ui.geometry.Size(eyeRadiusX * 2, eyeRadiusY * 2)
        )
        drawOval(
            color = Color(0xFF200F3E),
            topLeft = Offset(eyeRightX - eyeRadiusX, eyeY - eyeRadiusY),
            size = androidx.compose.ui.geometry.Size(eyeRadiusX * 2, eyeRadiusY * 2)
        )

        if (!isBlinking && mood != MascotMood.SLEEPY) {
            // White highlight sparkles
            drawCircle(Color.White, radius = eyeRadiusX * 0.45f, center = Offset(eyeLeftX + lookX - eyeRadiusX * 0.2f, eyeY + lookY - eyeRadiusY * 0.25f))
            drawCircle(Color.White, radius = eyeRadiusX * 0.25f, center = Offset(eyeLeftX + lookX + eyeRadiusX * 0.25f, eyeY + lookY + eyeRadiusY * 0.25f))

            drawCircle(Color.White, radius = eyeRadiusX * 0.45f, center = Offset(eyeRightX + lookX - eyeRadiusX * 0.2f, eyeY + lookY - eyeRadiusY * 0.25f))
            drawCircle(Color.White, radius = eyeRadiusX * 0.25f, center = Offset(eyeRightX + lookX + eyeRadiusX * 0.25f, eyeY + lookY + eyeRadiusY * 0.25f))
        }
    }

    // 6. Cute Mouth
    val mouthY = cy + radius * 0.28f
    when (mood) {
        MascotMood.SHOCKED -> {
            drawCircle(
                color = Color(0xFF200F3E),
                radius = radius * 0.12f,
                center = Offset(cx, mouthY)
            )
        }
        MascotMood.HAPPY, MascotMood.EXCITED, MascotMood.VICTORY -> {
            // Open happy smiling mouth with tongue
            val mouthPath = Path().apply {
                moveTo(cx - radius * 0.18f, mouthY)
                quadraticTo(cx, mouthY + radius * 0.22f, cx + radius * 0.18f, mouthY)
                close()
            }
            drawPath(mouthPath, color = Color(0xFF200F3E), style = Fill)
            drawCircle(Color(0xFFFF4081), radius = radius * 0.08f, center = Offset(cx, mouthY + radius * 0.12f))
        }
        else -> {
            // Subtle sweet smile
            val smilePath = Path().apply {
                moveTo(cx - radius * 0.14f, mouthY)
                quadraticTo(cx, mouthY + radius * 0.1f, cx + radius * 0.14f, mouthY)
            }
            drawPath(
                smilePath,
                color = Color(0xFF200F3E),
                style = Stroke(width = radius * 0.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}

private fun DrawScope.drawStar(cx: Float, cy: Float, size: Float, color: Color) {
    val path = Path()
    val spikes = 5
    val outerR = size
    val innerR = size * 0.45f
    var rot = -PI / 2.0

    for (i in 0 until spikes * 2) {
        val r = if (i % 2 == 0) outerR else innerR
        val x = cx + cos(rot).toFloat() * r
        val y = cy + sin(rot).toFloat() * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        rot += PI / spikes
    }
    path.close()
    drawPath(path, color = color)
}

private const val PI = 3.141592653589793
