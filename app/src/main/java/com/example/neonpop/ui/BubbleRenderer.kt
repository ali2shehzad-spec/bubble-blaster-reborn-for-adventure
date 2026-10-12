package com.example.neonpop.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.neonpop.model.ActiveProjectile
import com.example.neonpop.model.BossConfig
import com.example.neonpop.model.BubbleCell
import com.example.neonpop.model.BubbleColor
import com.example.neonpop.model.BubbleType
import com.example.neonpop.model.FallingBubble
import com.example.neonpop.model.FloatingText
import com.example.neonpop.model.Particle
import com.example.neonpop.model.TrajectoryRay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object BubbleRenderer {

    /**
     * Renders a single neon glass bubble at (cx, cy) with radius R.
     */
    fun drawBubble(
        drawScope: DrawScope,
        cx: Float,
        cy: Float,
        radius: Float,
        type: BubbleType,
        showSymbols: Boolean = false,
        scale: Float = 1f,
        alpha: Float = 1f
    ) {
        if (alpha <= 0.01f || scale <= 0.01f) return
        val r = radius * scale

        try {
            when (type) {
                is BubbleType.Normal -> drawNormalBubble(drawScope, cx, cy, r, type.color, showSymbols, alpha)
                is BubbleType.Rainbow -> drawRainbowBubble(drawScope, cx, cy, r, alpha)
                is BubbleType.Bomb -> drawBombBubble(drawScope, cx, cy, r, alpha)
                is BubbleType.Lightning -> drawLightningBubble(drawScope, cx, cy, r, alpha)
                is BubbleType.Prism -> drawPrismBubble(drawScope, cx, cy, r, alpha)
                is BubbleType.BlackHole -> drawBlackHoleBubble(drawScope, cx, cy, r, alpha)
                is BubbleType.Freeze -> drawFreezeBubble(drawScope, cx, cy, r, alpha)
                is BubbleType.Paint -> drawNormalBubble(drawScope, cx, cy, r, type.color, showSymbols, alpha, isPaint = true)
                is BubbleType.Meteor -> drawMeteorBubble(drawScope, cx, cy, r, alpha)
                is BubbleType.Ghost -> drawGhostBubble(drawScope, cx, cy, r, alpha)
                is BubbleType.Crystal -> drawCrystalBubble(drawScope, cx, cy, r, type.hitsLeft, alpha)
                is BubbleType.Chain -> {
                    drawNormalBubble(drawScope, cx, cy, r, type.color, showSymbols, alpha)
                    if (type.locked) drawChains(drawScope, cx, cy, r, alpha)
                }
                is BubbleType.BlackVoid -> drawBlackVoidBubble(drawScope, cx, cy, r, alpha)
                is BubbleType.Mystery -> drawMysteryBubble(drawScope, cx, cy, r, alpha)
                is BubbleType.Glitch -> {
                    val color = type.colors.getOrNull(type.activeIndex) ?: BubbleColor.ELECTRIC_BLUE
                    drawNormalBubble(drawScope, cx, cy, r, color, showSymbols, alpha, isGlitch = true)
                }
                is BubbleType.StarTrapped -> drawStarTrappedBubble(drawScope, cx, cy, r, alpha)
            }
        } catch (_: Throwable) {}
    }

    private fun drawNormalBubble(
        drawScope: DrawScope,
        cx: Float,
        cy: Float,
        r: Float,
        color: BubbleColor,
        showSymbols: Boolean,
        alpha: Float,
        isPaint: Boolean = false,
        isGlitch: Boolean = false
    ) {
        val pColor = color.primaryColor.copy(alpha = alpha)
        val hColor = color.highlightColor.copy(alpha = alpha)
        val gColor = color.glowColor.copy(alpha = alpha * 0.7f)

        // 1. Outer Neon Glow
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(gColor, Color.Transparent),
                center = Offset(cx, cy),
                radius = r * 1.35f
            ),
            radius = r * 1.35f,
            center = Offset(cx, cy)
        )

        // 2. Main Glass Sphere Gradient
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(hColor, pColor),
                center = Offset(cx - r * 0.28f, cy - r * 0.28f),
                radius = r * 1.15f
            ),
            radius = r * 0.94f,
            center = Offset(cx, cy)
        )

        // 3. Inner Glass Refraction Rim
        drawScope.drawCircle(
            color = Color.White.copy(alpha = 0.25f * alpha),
            radius = r * 0.92f,
            center = Offset(cx, cy),
            style = Stroke(width = r * 0.08f)
        )

        // 4. Specular Highlight (Reflection)
        drawScope.drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.85f * alpha), Color.Transparent),
                center = Offset(cx - r * 0.32f, cy - r * 0.35f),
                radius = r * 0.45f
            ),
            topLeft = Offset(cx - r * 0.58f, cy - r * 0.62f),
            size = Size(r * 0.52f, r * 0.35f)
        )

        // Paint splatter icon if paint bubble
        if (isPaint) {
            drawScope.drawCircle(Color.White.copy(alpha = 0.9f * alpha), radius = r * 0.25f, center = Offset(cx, cy))
        }

        // Accessibility Colorblind Symbol
        if (showSymbols) {
            // Draw geometric accessibility code
            drawSymbol(drawScope, cx, cy, r * 0.36f, color, alpha)
        }
    }

    private fun drawRainbowBubble(drawScope: DrawScope, cx: Float, cy: Float, r: Float, alpha: Float) {
        val rainbowColors = listOf(
            Color(0xFFFF2E93).copy(alpha = alpha),
            Color(0xFFFFDE00).copy(alpha = alpha),
            Color(0xFF25F05D).copy(alpha = alpha),
            Color(0xFF00E5FF).copy(alpha = alpha),
            Color(0xFFB5179E).copy(alpha = alpha)
        )

        drawScope.drawCircle(
            brush = Brush.sweepGradient(rainbowColors, Offset(cx, cy)),
            radius = r * 0.94f,
            center = Offset(cx, cy)
        )

        drawScope.drawCircle(
            color = Color.White.copy(alpha = 0.85f * alpha),
            radius = r * 0.92f,
            center = Offset(cx, cy),
            style = Stroke(width = r * 0.12f)
        )

        // Specular
        drawScope.drawCircle(
            Color.White.copy(alpha = 0.8f * alpha),
            radius = r * 0.22f,
            center = Offset(cx - r * 0.3f, cy - r * 0.3f)
        )
    }

    private fun drawBombBubble(drawScope: DrawScope, cx: Float, cy: Float, r: Float, alpha: Float) {
        // Red-black pulsing danger
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF3333).copy(alpha = alpha), Color(0xFF2B0000).copy(alpha = alpha)),
                center = Offset(cx - r * 0.2f, cy - r * 0.2f),
                radius = r
            ),
            radius = r * 0.94f,
            center = Offset(cx, cy)
        )

        drawScope.drawCircle(
            color = Color(0xFFFF5252).copy(alpha = alpha),
            radius = r * 0.92f,
            center = Offset(cx, cy),
            style = Stroke(width = r * 0.1f)
        )

        // Bomb spark icon in center
        val path = Path().apply {
            moveTo(cx - r * 0.25f, cy + r * 0.15f)
            lineTo(cx, cy - r * 0.25f)
            lineTo(cx + r * 0.25f, cy + r * 0.15f)
            close()
        }
        drawScope.drawPath(path, color = Color(0xFFFFDE00).copy(alpha = alpha))
        drawScope.drawCircle(Color.White.copy(alpha = alpha), radius = r * 0.1f, center = Offset(cx, cy))
    }

    private fun drawLightningBubble(drawScope: DrawScope, cx: Float, cy: Float, r: Float, alpha: Float) {
        val yellow = Color(0xFFFFDE00).copy(alpha = alpha)
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(yellow, Color(0xFF7A6800).copy(alpha = alpha)),
                center = Offset(cx - r * 0.2f, cy - r * 0.2f),
                radius = r
            ),
            radius = r * 0.94f,
            center = Offset(cx, cy)
        )

        // Lightning bolt icon
        val bolt = Path().apply {
            moveTo(cx + r * 0.05f, cy - r * 0.45f)
            lineTo(cx - r * 0.25f, cy)
            lineTo(cx, cy)
            lineTo(cx - r * 0.05f, cy + r * 0.45f)
            lineTo(cx + r * 0.25f, cy - r * 0.05f)
            lineTo(cx, cy - r * 0.05f)
            close()
        }
        drawScope.drawPath(bolt, color = Color.White.copy(alpha = alpha))
    }

    private fun drawPrismBubble(drawScope: DrawScope, cx: Float, cy: Float, r: Float, alpha: Float) {
        val cyan = Color(0xFF00E5FF).copy(alpha = alpha)
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = alpha), cyan, Color(0xFF0A2B42).copy(alpha = alpha)),
                center = Offset(cx - r * 0.2f, cy - r * 0.2f),
                radius = r
            ),
            radius = r * 0.94f,
            center = Offset(cx, cy)
        )

        // Faceted diamond shape
        val diamond = Path().apply {
            moveTo(cx, cy - r * 0.4f)
            lineTo(cx + r * 0.35f, cy)
            lineTo(cx, cy + r * 0.4f)
            lineTo(cx - r * 0.35f, cy)
            close()
        }
        drawScope.drawPath(diamond, color = Color.White.copy(alpha = 0.85f * alpha), style = Stroke(width = r * 0.08f))
    }

    private fun drawBlackHoleBubble(drawScope: DrawScope, cx: Float, cy: Float, r: Float, alpha: Float) {
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF0D0219).copy(alpha = alpha), Color(0xFF5E00A3).copy(alpha = alpha)),
                center = Offset(cx, cy),
                radius = r
            ),
            radius = r * 0.94f,
            center = Offset(cx, cy)
        )
        drawScope.drawCircle(
            color = Color(0xFFB5179E).copy(alpha = alpha),
            radius = r * 0.9f,
            center = Offset(cx, cy),
            style = Stroke(width = r * 0.12f)
        )
    }

    private fun drawFreezeBubble(drawScope: DrawScope, cx: Float, cy: Float, r: Float, alpha: Float) {
        val ice = Color(0xFF90E0EF).copy(alpha = alpha)
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = alpha), ice),
                center = Offset(cx - r * 0.2f, cy - r * 0.2f),
                radius = r
            ),
            radius = r * 0.94f,
            center = Offset(cx, cy)
        )
        // Snowflake cross
        drawScope.drawLine(Color.White.copy(alpha = alpha), Offset(cx - r * 0.35f, cy), Offset(cx + r * 0.35f, cy), strokeWidth = r * 0.08f)
        drawScope.drawLine(Color.White.copy(alpha = alpha), Offset(cx, cy - r * 0.35f), Offset(cx, cy + r * 0.35f), strokeWidth = r * 0.08f)
    }

    private fun drawMeteorBubble(drawScope: DrawScope, cx: Float, cy: Float, r: Float, alpha: Float) {
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF9E00).copy(alpha = alpha), Color(0xFF701A00).copy(alpha = alpha)),
                center = Offset(cx, cy),
                radius = r
            ),
            radius = r * 0.94f,
            center = Offset(cx, cy)
        )
    }

    private fun drawGhostBubble(drawScope: DrawScope, cx: Float, cy: Float, r: Float, alpha: Float) {
        drawScope.drawCircle(
            color = Color.White.copy(alpha = 0.35f * alpha),
            radius = r * 0.94f,
            center = Offset(cx, cy)
        )
        drawScope.drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = 0.65f * alpha),
            radius = r * 0.92f,
            center = Offset(cx, cy),
            style = Stroke(width = r * 0.08f)
        )
    }

    private fun drawCrystalBubble(drawScope: DrawScope, cx: Float, cy: Float, r: Float, hitsLeft: Int, alpha: Float) {
        val crystalColor = Color(0xFFD6F0FF).copy(alpha = 0.65f * alpha)
        val path = Path().apply {
            moveTo(cx, cy - r * 0.85f)
            lineTo(cx + r * 0.85f, cy - r * 0.2f)
            lineTo(cx + r * 0.55f, cy + r * 0.85f)
            lineTo(cx - r * 0.55f, cy + r * 0.85f)
            lineTo(cx - r * 0.85f, cy - r * 0.2f)
            close()
        }
        drawScope.drawPath(path, color = crystalColor)
        drawScope.drawPath(path, color = Color(0xFF00E5FF).copy(alpha = alpha), style = Stroke(width = r * 0.1f))

        // If damaged (hitsLeft == 1), draw crack
        if (hitsLeft == 1) {
            drawScope.drawLine(Color.White.copy(alpha = alpha), Offset(cx - r * 0.2f, cy - r * 0.4f), Offset(cx + r * 0.2f, cy + r * 0.3f), strokeWidth = r * 0.08f)
        }
    }

    private fun drawChains(drawScope: DrawScope, cx: Float, cy: Float, r: Float, alpha: Float) {
        val chainColor = Color(0xFFB0BEC5).copy(alpha = alpha)
        drawScope.drawLine(chainColor, Offset(cx - r * 0.7f, cy - r * 0.7f), Offset(cx + r * 0.7f, cy + r * 0.7f), strokeWidth = r * 0.12f)
        drawScope.drawLine(chainColor, Offset(cx + r * 0.7f, cy - r * 0.7f), Offset(cx - r * 0.7f, cy + r * 0.7f), strokeWidth = r * 0.12f)
        drawScope.drawCircle(Color(0xFFFFD700).copy(alpha = alpha), radius = r * 0.18f, center = Offset(cx, cy))
    }

    private fun drawBlackVoidBubble(drawScope: DrawScope, cx: Float, cy: Float, r: Float, alpha: Float) {
        drawScope.drawCircle(
            color = Color(0xFF140D26).copy(alpha = alpha),
            radius = r * 0.94f,
            center = Offset(cx, cy)
        )
        drawScope.drawCircle(
            color = Color(0xFF7B2CBF).copy(alpha = alpha),
            radius = r * 0.9f,
            center = Offset(cx, cy),
            style = Stroke(width = r * 0.09f)
        )
    }

    private fun drawMysteryBubble(drawScope: DrawScope, cx: Float, cy: Float, r: Float, alpha: Float) {
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF8A2BE2).copy(alpha = alpha), Color(0xFF240046).copy(alpha = alpha)),
                center = Offset(cx, cy),
                radius = r
            ),
            radius = r * 0.94f,
            center = Offset(cx, cy)
        )
        // Question mark dot
        drawScope.drawCircle(Color(0xFFFFDE00).copy(alpha = alpha), radius = r * 0.12f, center = Offset(cx, cy + r * 0.25f))
    }

    private fun drawStarTrappedBubble(drawScope: DrawScope, cx: Float, cy: Float, r: Float, alpha: Float) {
        // Transparent glass bubble
        drawScope.drawCircle(
            color = Color(0xFF80D8FF).copy(alpha = 0.35f * alpha),
            radius = r * 0.94f,
            center = Offset(cx, cy)
        )
        drawScope.drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = alpha),
            radius = r * 0.92f,
            center = Offset(cx, cy),
            style = Stroke(width = r * 0.08f)
        )
        // Trapped Star
        val path = Path()
        val spikes = 5
        val outerR = r * 0.55f
        val innerR = r * 0.24f
        var rot = -PI / 2.0
        for (i in 0 until spikes * 2) {
            val dist = if (i % 2 == 0) outerR else innerR
            val x = cx + cos(rot).toFloat() * dist
            val y = cy + sin(rot).toFloat() * dist
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            rot += PI / spikes
        }
        path.close()
        drawScope.drawPath(path, color = Color(0xFFFFD700).copy(alpha = alpha))
    }

    private fun drawSymbol(drawScope: DrawScope, cx: Float, cy: Float, size: Float, color: BubbleColor, alpha: Float) {
        val symColor = Color.White.copy(alpha = 0.85f * alpha)
        when (color) {
            BubbleColor.NEON_RED -> { // Heart-like diamond
                val path = Path().apply {
                    moveTo(cx, cy - size)
                    lineTo(cx + size, cy)
                    lineTo(cx, cy + size)
                    lineTo(cx - size, cy)
                    close()
                }
                drawScope.drawPath(path, color = symColor, style = Stroke(width = size * 0.25f))
            }
            BubbleColor.ELECTRIC_BLUE -> { // Diamond
                val path = Path().apply {
                    moveTo(cx, cy - size)
                    lineTo(cx + size * 0.8f, cy)
                    lineTo(cx, cy + size)
                    lineTo(cx - size * 0.8f, cy)
                    close()
                }
                drawScope.drawPath(path, color = symColor, style = Stroke(width = size * 0.25f))
            }
            BubbleColor.ACID_GREEN -> { // Triangle
                val path = Path().apply {
                    moveTo(cx, cy - size)
                    lineTo(cx + size, cy + size * 0.8f)
                    lineTo(cx - size, cy + size * 0.8f)
                    close()
                }
                drawScope.drawPath(path, color = symColor, style = Stroke(width = size * 0.25f))
            }
            BubbleColor.CYBER_YELLOW -> { // Star-like cross
                drawScope.drawLine(symColor, Offset(cx - size, cy), Offset(cx + size, cy), strokeWidth = size * 0.3f)
                drawScope.drawLine(symColor, Offset(cx, cy - size), Offset(cx, cy + size), strokeWidth = size * 0.3f)
            }
            BubbleColor.NEON_PURPLE -> { // Circle
                drawScope.drawCircle(symColor, radius = size * 0.75f, center = Offset(cx, cy), style = Stroke(width = size * 0.25f))
            }
            BubbleColor.SOLAR_ORANGE -> { // Small filled square
                drawScope.drawRect(symColor, topLeft = Offset(cx - size * 0.5f, cy - size * 0.5f), size = Size(size, size))
            }
            BubbleColor.DREAM_PINK -> { // Plus
                drawScope.drawLine(symColor, Offset(cx - size * 0.7f, cy - size * 0.7f), Offset(cx + size * 0.7f, cy + size * 0.7f), strokeWidth = size * 0.25f)
                drawScope.drawLine(symColor, Offset(cx + size * 0.7f, cy - size * 0.7f), Offset(cx - size * 0.7f, cy + size * 0.7f), strokeWidth = size * 0.25f)
            }
        }
    }

    /**
     * Trajectory Laser Aim line with glowing dots and target reticle.
     */
    fun drawTrajectory(drawScope: DrawScope, ray: TrajectoryRay, bubbleRadius: Float, aimColor: Color) {
        if (ray.points.size < 2) return
        try {
            // Draw segmented glowing dots
            val pulseColor = aimColor.copy(alpha = 0.85f)
            val dotRadius = bubbleRadius * 0.18f

            for (i in 0 until ray.points.size - 1) {
                val p1 = ray.points[i]
                val p2 = ray.points[i + 1]
                val dx = p2.x - p1.x
                val dy = p2.y - p1.y
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                val step = bubbleRadius * 0.75f
                val count = (dist / step).toInt().coerceAtLeast(1)

                for (s in 0..count) {
                    val t = s.toFloat() / count
                    val x = p1.x + dx * t
                    val y = p1.y + dy * t

                    drawScope.drawCircle(
                        color = pulseColor,
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                }
            }

            // Target reticle at predicted destination
            val lastPoint = ray.points.last()
            drawScope.drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = bubbleRadius * 0.95f,
                center = Offset(lastPoint.x, lastPoint.y),
                style = Stroke(width = bubbleRadius * 0.1f)
            )
            drawScope.drawCircle(
                color = aimColor.copy(alpha = 0.45f),
                radius = bubbleRadius * 0.45f,
                center = Offset(lastPoint.x, lastPoint.y)
            )
        } catch (_: Throwable) {}
    }

    /**
     * Renders particles (sparks, stars, smoke).
     */
    fun drawParticles(drawScope: DrawScope, particles: List<Particle>) {
        try {
            for (p in particles) {
                if (p.alpha <= 0.01f) continue
                val c = p.color.copy(alpha = p.alpha)
                drawScope.drawCircle(c, radius = p.size, center = Offset(p.x, p.y))
            }
        } catch (_: Throwable) {}
    }

    /**
     * Renders falling detached bubbles.
     */
    fun drawFallingBubbles(drawScope: DrawScope, bubbles: List<FallingBubble>, showSymbols: Boolean) {
        try {
            for (fb in bubbles) {
                drawBubble(drawScope, fb.x, fb.y, fb.size, fb.type, showSymbols, scale = 1f, alpha = fb.alpha)
            }
        } catch (_: Throwable) {}
    }

    /**
     * Renders floating score & combo popups.
     */
    fun drawFloatingTexts(drawScope: DrawScope, texts: List<FloatingText>) {
        try {
            for (ft in texts) {
                if (ft.alpha <= 0.01f) continue
                // Render text marker circle glow
                drawScope.drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ft.color.copy(alpha = ft.alpha * 0.5f), Color.Transparent),
                        center = Offset(ft.x, ft.y),
                        radius = 28f * ft.scale
                    ),
                    radius = 28f * ft.scale,
                    center = Offset(ft.x, ft.y)
                )
            }
        } catch (_: Throwable) {}
    }

    /**
     * Renders the classic Puzzle Bobble golden rotating gear cannon pointer at launcher origin.
     */
    fun drawArcadeLauncher(
        drawScope: DrawScope,
        cx: Float,
        cy: Float,
        radius: Float,
        aimAngleRad: Float,
        loadedBubble: BubbleType?,
        primaryColor: Color
    ) {
        val cosA = kotlin.math.cos(aimAngleRad)
        val sinA = kotlin.math.sin(aimAngleRad)

        // 1. Golden Gear Base Disk
        drawScope.drawCircle(
            color = Color(0xFFFFD54F),
            radius = radius * 1.35f,
            center = Offset(cx, cy)
        )
        drawScope.drawCircle(
            color = Color(0xFFFF8F00),
            radius = radius * 1.35f,
            center = Offset(cx, cy),
            style = Stroke(width = radius * 0.14f)
        )

        // Gear teeth around edge
        val teeth = 8
        for (i in 0 until teeth) {
            val a = (i * 2.0 * Math.PI / teeth).toFloat()
            val tx = cx + (radius * 1.35f) * kotlin.math.cos(a)
            val ty = cy + (radius * 1.35f) * kotlin.math.sin(a)
            drawScope.drawCircle(Color(0xFFFF8F00), radius = radius * 0.16f, center = Offset(tx, ty))
        }

        // 2. Rotating Pointer Barrel / Arrow (pointing in aim direction)
        val barrelLen = radius * 2.4f
        val tipX = cx + barrelLen * cosA
        val tipY = cy + barrelLen * sinA

        // Perpendicular offset for arrow width
        val perpX = -sinA * (radius * 0.45f)
        val perpY = cosA * (radius * 0.45f)

        val barrelPath = Path().apply {
            moveTo(cx + perpX, cy + perpY)
            lineTo(cx - perpX, cy - perpY)
            lineTo(tipX - perpX * 0.5f, tipY - perpY * 0.5f)
            lineTo(tipX - perpX * 0.9f, tipY - perpY * 0.9f)
            lineTo(tipX + cosA * (radius * 0.8f), tipY + sinA * (radius * 0.8f)) // Arrow point
            lineTo(tipX + perpX * 0.9f, tipY + perpY * 0.9f)
            lineTo(tipX + perpX * 0.5f, tipY + perpY * 0.5f)
            close()
        }
        drawScope.drawPath(barrelPath, Color(0xFFFFB300))
        drawScope.drawPath(barrelPath, Color(0xFFFF6F00), style = Stroke(width = radius * 0.1f))

        // Center golden chamber ring
        drawScope.drawCircle(
            color = Color(0xFF424242).copy(alpha = 0.6f),
            radius = radius * 1.05f,
            center = Offset(cx, cy)
        )
        drawScope.drawCircle(
            color = primaryColor,
            radius = radius * 1.05f,
            center = Offset(cx, cy),
            style = Stroke(width = radius * 0.1f)
        )

        // 3. Draw Loaded Bubble in Cannon Chamber (if not currently in flight)
        if (loadedBubble != null) {
            drawBubble(drawScope, cx, cy, radius * 0.95f, loadedBubble, showSymbols = false)
        }
    }
}
