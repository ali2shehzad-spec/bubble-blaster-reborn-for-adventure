package com.example.neonpop.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.neonpop.model.MascotMood
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Classic Arcade Bubble Dragon Mascot (inspired by Puzzle Bobble / Bust-A-Move).
 * Features animated body bounce, tail wag, stepping feet, blinking anime eyes,
 * hand crank rotation while aiming, and dynamic bubble-throwing launch animation!
 */
@Composable
fun BubbleDragonCharacterView(
    skinColor: Color = Color(0xFF25F05D),
    aimAngleRad: Float = -1.57f,
    mood: MascotMood = MascotMood.IDLE,
    isFiring: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 130.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dragon_anim")

    // 1. Idle breathing and body bounce
    val bodyBounce by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dragon_bounce"
    )

    // 2. Tail wagging angle
    val tailWag by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dragon_tail_wag"
    )

    // 3. Stepping feet animation
    val stepCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dragon_step"
    )

    // 4. Eye blink animation
    val blinkAnim by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dragon_blink"
    )

    // 5. Crank handle rotation while aiming
    val crankRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dragon_crank"
    )

    // 6. Firing spring recoil and throwing action
    val fireRecoil = remember { Animatable(0f) }
    LaunchedEffect(isFiring) {
        if (isFiring) {
            fireRecoil.snapTo(1f)
            fireRecoil.animateTo(0f, animationSpec = spring(dampingRatio = 0.45f, stiffness = 550f))
        }
    }

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val recoilVal = fireRecoil.value
            val isVictory = mood == MascotMood.VICTORY || mood == MascotMood.HAPPY
            val isShocked = mood == MascotMood.SHOCKED

            drawBubbleDragon(
                skinColor = skinColor,
                aimAngleRad = aimAngleRad,
                bodyBounce = bodyBounce,
                tailWag = tailWag,
                stepCycle = stepCycle,
                blinkVal = if (isVictory || isShocked) 1f else blinkAnim,
                crankRotation = crankRotation,
                recoil = recoilVal,
                mood = mood
            )
        }
    }
}

private fun DrawScope.drawBubbleDragon(
    skinColor: Color,
    aimAngleRad: Float,
    bodyBounce: Float,
    tailWag: Float,
    stepCycle: Float,
    blinkVal: Float,
    crankRotation: Float,
    recoil: Float,
    mood: MascotMood
) {
    val w = size.width
    val h = size.height

    // Belly patch color (warm sunny cream yellow or lighter pastel tint)
    val bellyColor = Color(0xFFFFE57F)
    // Dragon back spines color (vivid orange / golden)
    val spineColor = Color(0xFFFF6D00)
    // Cheek blush
    val blushColor = Color(0xFFFF4081).copy(alpha = 0.55f)

    val isVictory = mood == MascotMood.VICTORY || mood == MascotMood.HAPPY
    val isShocked = mood == MascotMood.SHOCKED

    // Dynamic vertical position with jump when firing or victorious
    val jumpOffset = if (isVictory) -12f else (-recoil * 18f)
    val baseY = h * 0.52f + bodyBounce + jumpOffset
    val centerX = w * 0.46f

    // 0. Soft Ground Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.28f),
        topLeft = Offset(centerX - w * 0.32f, h * 0.88f),
        size = Size(w * 0.64f, h * 0.10f)
    )

    // 1. Dragon Tail (drawn behind body)
    val tailBaseX = centerX - w * 0.18f
    val tailBaseY = baseY + h * 0.12f
    val tailTipX = tailBaseX - w * 0.22f
    val tailTipY = tailBaseY - h * 0.08f + (tailWag * 0.4f)

    val tailPath = Path().apply {
        moveTo(tailBaseX, tailBaseY - h * 0.06f)
        quadraticBezierTo(
            tailBaseX - w * 0.12f, tailBaseY - h * 0.14f,
            tailTipX, tailTipY
        )
        quadraticBezierTo(
            tailBaseX - w * 0.10f, tailBaseY + h * 0.04f,
            tailBaseX, tailBaseY + h * 0.08f
        )
        close()
    }
    drawPath(tailPath, skinColor)
    drawPath(tailPath, Color.Black.copy(alpha = 0.15f), style = Stroke(width = 2.5f))

    // Tail tip spine
    val tailSpine = Path().apply {
        moveTo(tailTipX, tailTipY)
        lineTo(tailTipX - 8f, tailTipY - 10f)
        lineTo(tailTipX + 4f, tailTipY - 6f)
        close()
    }
    drawPath(tailSpine, spineColor)

    // 2. Back Spine Ridges (3 cute dorsal fin scales)
    val spinePoints = listOf(
        Pair(centerX - w * 0.16f, baseY - h * 0.12f),
        Pair(centerX - w * 0.19f, baseY - h * 0.02f),
        Pair(centerX - w * 0.20f, baseY + h * 0.08f)
    )
    for ((sx, sy) in spinePoints) {
        val spineP = Path().apply {
            moveTo(sx, sy - 9f)
            lineTo(sx - 12f, sy)
            lineTo(sx, sy + 9f)
            close()
        }
        drawPath(spineP, spineColor)
        drawPath(spineP, Color(0xFFFFD54F), style = Stroke(width = 1.5f))
    }

    // 3. Cute Chubby Dinosaur Feet (with stepping motion)
    val footWidth = w * 0.18f
    val footHeight = h * 0.11f
    val leftFootY = h * 0.82f + (stepCycle * 4f)
    val rightFootY = h * 0.82f + ((1f - stepCycle) * 4f)

    // Left foot (back)
    drawOval(
        color = skinColor,
        topLeft = Offset(centerX - w * 0.22f, leftFootY),
        size = Size(footWidth, footHeight)
    )
    // Left foot cute toe claws
    drawCircle(Color.White, radius = 3.5f, center = Offset(centerX - w * 0.19f, leftFootY + footHeight - 2f))
    drawCircle(Color.White, radius = 3.5f, center = Offset(centerX - w * 0.13f, leftFootY + footHeight - 2f))

    // Right foot (front)
    drawOval(
        color = skinColor,
        topLeft = Offset(centerX + w * 0.02f, rightFootY),
        size = Size(footWidth, footHeight)
    )
    // Right foot cute toe claws
    drawCircle(Color.White, radius = 3.5f, center = Offset(centerX + w * 0.06f, rightFootY + footHeight - 2f))
    drawCircle(Color.White, radius = 3.5f, center = Offset(centerX + w * 0.13f, rightFootY + footHeight - 2f))

    // 4. Cute Pear-Shaped Dragon Body
    val bodyRect = Path().apply {
        moveTo(centerX - w * 0.20f, baseY - h * 0.08f)
        // curve down to lower tummy
        cubicTo(
            centerX - w * 0.26f, baseY + h * 0.18f,
            centerX + w * 0.26f, baseY + h * 0.18f,
            centerX + w * 0.20f, baseY - h * 0.08f
        )
        // curve top shoulder
        quadraticBezierTo(centerX, baseY - h * 0.15f, centerX - w * 0.20f, baseY - h * 0.08f)
        close()
    }
    drawPath(bodyRect, skinColor)
    // Body subtle neon glow outline
    drawPath(bodyRect, Color.White.copy(alpha = 0.2f), style = Stroke(width = 2f))

    // 5. Cute Tummy / Belly Plate (sunny yellow / cream oval)
    val bellyPath = Path().apply {
        moveTo(centerX - w * 0.06f, baseY - h * 0.05f)
        cubicTo(
            centerX - w * 0.16f, baseY + h * 0.14f,
            centerX + w * 0.18f, baseY + h * 0.14f,
            centerX + w * 0.10f, baseY - h * 0.05f
        )
        close()
    }
    drawPath(bellyPath, bellyColor)
    // Belly ridges (subtle horizontal lines)
    drawLine(
        color = Color(0xFFFFB300).copy(alpha = 0.5f),
        start = Offset(centerX - w * 0.08f, baseY + h * 0.02f),
        end = Offset(centerX + w * 0.10f, baseY + h * 0.02f),
        strokeWidth = 2f
    )
    drawLine(
        color = Color(0xFFFFB300).copy(alpha = 0.5f),
        start = Offset(centerX - w * 0.09f, baseY + h * 0.08f),
        end = Offset(centerX + w * 0.09f, baseY + h * 0.08f),
        strokeWidth = 2f
    )

    // 6. Cute Chubby Dragon Head with Snout
    val headRadius = w * 0.25f
    val headCenterX = centerX + w * 0.04f
    val headCenterY = baseY - h * 0.16f

    // Head base circle
    drawCircle(
        color = skinColor,
        radius = headRadius,
        center = Offset(headCenterX, headCenterY)
    )

    // Cute chubby snout pointing forward/right
    val snoutPath = Path().apply {
        moveTo(headCenterX + w * 0.05f, headCenterY - h * 0.08f)
        quadraticBezierTo(
            headCenterX + w * 0.28f, headCenterY - h * 0.04f,
            headCenterX + w * 0.26f, headCenterY + h * 0.08f
        )
        quadraticBezierTo(
            headCenterX + w * 0.12f, headCenterY + h * 0.12f,
            headCenterX, headCenterY + h * 0.08f
        )
        close()
    }
    drawPath(snoutPath, skinColor)

    // Head top spine ridge
    val headSpine = Path().apply {
        moveTo(headCenterX - w * 0.14f, headCenterY - h * 0.12f)
        lineTo(headCenterX - w * 0.22f, headCenterY - h * 0.18f)
        lineTo(headCenterX - w * 0.10f, headCenterY - h * 0.20f)
        close()
    }
    drawPath(headSpine, spineColor)

    // Cute Nostril
    drawCircle(
        color = Color.Black.copy(alpha = 0.35f),
        radius = 2.5f,
        center = Offset(headCenterX + w * 0.23f, headCenterY)
    )

    // 7. Kawaii Anime Dragon Eyes
    val eyeWidth = w * 0.14f
    val eyeHeight = (h * 0.16f) * (if (blinkVal < 0.2f) 0.1f else 1f)
    val eyeLeft = headCenterX + w * 0.02f
    val eyeTop = headCenterY - h * 0.11f

    if (isVictory) {
        // Joyful Crescent Eye (^^)
        drawArc(
            color = Color(0xFF1B1B1B),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(eyeLeft, eyeTop),
            size = Size(eyeWidth, h * 0.10f),
            style = Stroke(width = 4.5f, cap = StrokeCap.Round)
        )
    } else if (isShocked) {
        // Wide Shocked Eye (O)
        drawOval(
            color = Color.White,
            topLeft = Offset(eyeLeft, eyeTop),
            size = Size(eyeWidth, eyeHeight)
        )
        drawCircle(
            color = Color.Black,
            radius = 3.5f,
            center = Offset(eyeLeft + eyeWidth / 2f, eyeTop + eyeHeight / 2f)
        )
    } else {
        // Cute Anime Eye with Catchlights (Glossy Bub Dragon Eyes)
        drawOval(
            color = Color.White,
            topLeft = Offset(eyeLeft, eyeTop),
            size = Size(eyeWidth, eyeHeight)
        )
        drawOval(
            color = Color.Black.copy(alpha = 0.15f),
            topLeft = Offset(eyeLeft, eyeTop),
            size = Size(eyeWidth, eyeHeight),
            style = Stroke(width = 2f)
        )

        if (blinkVal >= 0.25f) {
            // Dark large anime pupil
            val pupilRadius = eyeWidth * 0.36f
            val pupilCenter = Offset(eyeLeft + eyeWidth * 0.55f, eyeTop + eyeHeight * 0.52f)
            drawCircle(
                color = Color(0xFF161616),
                radius = pupilRadius,
                center = pupilCenter
            )
            // Big white catchlight reflection
            drawCircle(
                color = Color.White,
                radius = pupilRadius * 0.45f,
                center = Offset(pupilCenter.x - pupilRadius * 0.35f, pupilCenter.y - pupilRadius * 0.35f)
            )
            // Small secondary sparkle
            drawCircle(
                color = Color.White,
                radius = pupilRadius * 0.22f,
                center = Offset(pupilCenter.x + pupilRadius * 0.35f, pupilCenter.y + pupilRadius * 0.35f)
            )
        }
    }

    // 8. Cheeks & Mouth
    // Cheerful blush
    drawOval(
        color = blushColor,
        topLeft = Offset(headCenterX + w * 0.14f, headCenterY + h * 0.02f),
        size = Size(w * 0.08f, h * 0.045f)
    )

    // Cute smiling dragon mouth
    val mouthPath = Path().apply {
        if (recoil > 0.3f || isVictory) {
            // Open shouting/cheering mouth
            moveTo(headCenterX + w * 0.08f, headCenterY + h * 0.06f)
            quadraticBezierTo(
                headCenterX + w * 0.18f, headCenterY + h * 0.14f,
                headCenterX + w * 0.24f, headCenterY + h * 0.05f
            )
            close()
        } else {
            // Cute smile
            moveTo(headCenterX + w * 0.10f, headCenterY + h * 0.06f)
            quadraticBezierTo(
                headCenterX + w * 0.18f, headCenterY + h * 0.10f,
                headCenterX + w * 0.23f, headCenterY + h * 0.05f
            )
        }
    }
    if (recoil > 0.3f || isVictory) {
        drawPath(mouthPath, Color(0xFFD81B60))
        // cute white baby tooth
        drawCircle(Color.White, radius = 3f, center = Offset(headCenterX + w * 0.16f, headCenterY + h * 0.07f))
    } else {
        drawPath(mouthPath, Color(0xFF212121), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
    }

    // 9. Interactive Crank Wheel & Paws (Operating the Launcher just like in Puzzle Bobble video!)
    val crankBaseX = centerX + w * 0.32f
    val crankBaseY = baseY + h * 0.08f
    val crankRadius = w * 0.12f

    // Golden/Brass Crank Gear Base
    drawCircle(
        color = Color(0xFFFFD54F),
        radius = crankRadius,
        center = Offset(crankBaseX, crankBaseY)
    )
    drawCircle(
        color = Color(0xFFFF8F00),
        radius = crankRadius,
        center = Offset(crankBaseX, crankBaseY),
        style = Stroke(width = 3f)
    )
    // Crank center axle
    drawCircle(
        color = Color(0xFF5D4037),
        radius = 4f,
        center = Offset(crankBaseX, crankBaseY)
    )

    // Rotating crank arm & wooden knob handle
    val rotAngle = crankRotation * (PI.toFloat() / 180f)
    val handleX = crankBaseX + (crankRadius * 0.65f) * cos(rotAngle)
    val handleY = crankBaseY + (crankRadius * 0.65f) * sin(rotAngle)

    drawLine(
        color = Color(0xFFFF8F00),
        start = Offset(crankBaseX, crankBaseY),
        end = Offset(handleX, handleY),
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = Color(0xFF8D6E63),
        radius = 6f,
        center = Offset(handleX, handleY)
    )

    // 10. Dragon Arms & Paws
    // Right arm holding / operating the crank handle or throwing bubble
    if (recoil > 0.2f) {
        // Throwing / Firing Bubble motion: arms thrown forward & upward!
        val throwArm = Path().apply {
            moveTo(centerX + w * 0.08f, baseY - h * 0.02f)
            quadraticBezierTo(
                centerX + w * 0.22f, baseY - h * 0.14f,
                centerX + w * 0.36f, baseY - h * 0.16f
            )
        }
        drawPath(throwArm, skinColor, style = Stroke(width = w * 0.09f, cap = StrokeCap.Round))
        // cute dragon paw
        drawCircle(skinColor, radius = w * 0.055f, center = Offset(centerX + w * 0.36f, baseY - h * 0.16f))

        // Cute glowing projectile energy bubble being thrown!
        val thrownBubbleX = centerX + w * 0.44f
        val thrownBubbleY = baseY - h * 0.22f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color(0xFF00E5FF), Color(0xFF00B0FF).copy(alpha = 0.5f)),
                center = Offset(thrownBubbleX - 4f, thrownBubbleY - 4f),
                radius = 16f
            ),
            radius = 14f,
            center = Offset(thrownBubbleX, thrownBubbleY)
        )
        // Bubble specular highlight
        drawCircle(Color.White, radius = 4f, center = Offset(thrownBubbleX - 4f, thrownBubbleY - 4f))

        // Speed / wind motion lines
        drawLine(
            color = Color(0xFF00E5FF).copy(alpha = 0.7f),
            start = Offset(thrownBubbleX - 20f, thrownBubbleY + 12f),
            end = Offset(thrownBubbleX - 6f, thrownBubbleY + 4f),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    } else if (isVictory) {
        // Victory double paw cheer!
        drawLine(
            color = skinColor,
            start = Offset(centerX + w * 0.08f, baseY),
            end = Offset(centerX + w * 0.22f, baseY - h * 0.16f),
            strokeWidth = w * 0.08f,
            cap = StrokeCap.Round
        )
        drawCircle(skinColor, radius = w * 0.05f, center = Offset(centerX + w * 0.22f, baseY - h * 0.16f))

        drawLine(
            color = skinColor,
            start = Offset(centerX - w * 0.08f, baseY),
            end = Offset(centerX - w * 0.20f, baseY - h * 0.16f),
            strokeWidth = w * 0.08f,
            cap = StrokeCap.Round
        )
        drawCircle(skinColor, radius = w * 0.05f, center = Offset(centerX - w * 0.20f, baseY - h * 0.16f))

        // Joyful sparkles / stars
        drawStar(centerX + w * 0.30f, baseY - h * 0.22f, 8f, Color(0xFFFFDE00))
        drawStar(centerX - w * 0.26f, baseY - h * 0.20f, 6f, Color(0xFFFF4081))
    } else {
        // Normal state: Paws holding and turning the crank handle!
        val pawArm = Path().apply {
            moveTo(centerX + w * 0.08f, baseY)
            quadraticBezierTo(
                centerX + w * 0.20f, baseY + h * 0.02f,
                handleX, handleY
            )
        }
        drawPath(pawArm, skinColor, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
        drawCircle(skinColor, radius = w * 0.05f, center = Offset(handleX, handleY))
        // cute little paw pads / fingers
        drawCircle(Color.White.copy(alpha = 0.4f), radius = 2.5f, center = Offset(handleX, handleY))
    }
}

private fun DrawScope.drawStar(cx: Float, cy: Float, radius: Float, color: Color) {
    val path = Path()
    val points = 4
    val innerRadius = radius * 0.35f
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = (i * PI / points).toFloat()
        val x = cx + r * cos(angle)
        val y = cy + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}
