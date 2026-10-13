package com.example.neonpop.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neonpop.game.GameViewModel
import com.example.neonpop.levels.LevelDatabase
import com.example.neonpop.model.BubbleType
import com.example.neonpop.model.GameScreen
import com.example.neonpop.model.LevelObjective
import kotlinx.coroutines.isActive

@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val world = LevelDatabase.getWorld(uiState.levelConfig.worldId)
    val colorblindEnabled = viewModel.saveManager.colorblindSymbolsEnabled
    val equippedSkin = viewModel.saveManager.equippedSkin

    // 60 FPS continuous render ticker ensuring Canvas redraws every frame for smooth physics
    var frameTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { time ->
                frameTick = time
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (uiState.isFeverActive) {
                        listOf(Color(0xFF2A0845), Color(0xFF6441A5), Color(0xFF130722))
                    } else {
                        uiState.levelConfig.backgroundColors
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 560.dp)
                .fillMaxWidth()
        ) {
            val density = LocalDensity.current
            val boardW = constraints.maxWidth.toFloat()
            val boardH = constraints.maxHeight.toFloat()
            val topOffset = minOf(with(density) { 135.dp.toPx() }, boardH * 0.18f)
            val launcherY = boardH - with(density) { 80.dp.toPx() }

            LaunchedEffect(boardW, boardH, topOffset, launcherY) {
                viewModel.setBoardDimensions(boardW, boardH, topOffset, launcherY)
            }

        // 1. Main Game Board Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_board_canvas")
                .pointerInput(launcherY) {
                    try {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            viewModel.onAimDrag(down.position.x, down.position.y)
                            var isReleased = false
                            while (!isReleased) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                if (change == null || !change.pressed) {
                                    isReleased = true
                                    viewModel.onAimRelease()
                                } else {
                                    change.consume()
                                    viewModel.onAimDrag(change.position.x, change.position.y)
                                }
                            }
                        }
                    } catch (_: Throwable) {}
                }
        ) {
            // Read frame tick to force redraw on every vsync
            @Suppress("UNUSED_VARIABLE")
            val frame = frameTick
            // Draw Venetian "Blind Type" Background Theme Slats
            val blindCount = 26
            val blindHeight = size.height / blindCount
            val accent = uiState.levelConfig.primaryAccent
            for (i in 0 until blindCount) {
                if (i % 2 == 1) {
                    val y = i * blindHeight
                    drawRect(
                        color = Color.Black.copy(alpha = 0.18f),
                        topLeft = Offset(0f, y),
                        size = androidx.compose.ui.geometry.Size(size.width, blindHeight * 0.88f)
                    )
                    drawLine(
                        color = accent.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.5f
                    )
                }
            }

            val boardWidthActual = size.width
            val radius = viewModel.physics.getBubbleRadius(boardWidthActual)

            // Draw ceiling bar
            drawRect(
                color = accent.copy(alpha = 0.5f),
                topLeft = Offset(0f, topOffset - 12f),
                size = androidx.compose.ui.geometry.Size(boardW, 6f)
            )

            // Draw Grid Bubbles
            for (r in 0 until viewModel.physics.maxRows) {
                val cols = viewModel.physics.getColsForRow(r)
                for (c in 0 until cols) {
                    val cell = viewModel.getCellSafe(r, c) ?: continue
                    val (bx, by) = viewModel.physics.getBubbleCenter(r, c, radius, 0f, topOffset)
                    BubbleRenderer.drawBubble(
                        this,
                        cx = bx,
                        cy = by,
                        radius = radius,
                        type = cell.type,
                        showSymbols = colorblindEnabled,
                        scale = cell.animScale,
                        alpha = cell.pulseAlpha
                    )
                }
            }

            // Draw Trajectory Aim Laser
            val ray = uiState.trajectory
            if (ray != null && uiState.projectile == null) {
                val aimColor = world.primaryColor
                BubbleRenderer.drawTrajectory(this, ray, radius, aimColor)
            }

            // Draw Arcade Rotating Gear Cannon (Puzzle Bobble style)
            BubbleRenderer.drawArcadeLauncher(
                this,
                cx = size.width / 2f,
                cy = launcherY,
                radius = radius,
                aimAngleRad = uiState.aimAngleRad,
                loadedBubble = if (uiState.projectile == null) uiState.currentBubble else null,
                primaryColor = world.primaryColor
            )

            // Draw Active Projectile in Flight with upward propulsion glow trail
            val proj = uiState.projectile
            if (proj != null) {
                // Upward blaster exhaust trail
                drawLine(
                    brush = Brush.verticalGradient(
                        listOf(Color.Transparent, world.primaryColor.copy(alpha = 0.6f), Color.White)
                    ),
                    start = Offset(proj.x - proj.vx * 2f, proj.y - proj.vy * 2f),
                    end = Offset(proj.x, proj.y),
                    strokeWidth = radius * 0.4f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.45f),
                    radius = radius * 1.25f,
                    center = Offset(proj.x, proj.y),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                )

                BubbleRenderer.drawBubble(
                    this,
                    cx = proj.x,
                    cy = proj.y,
                    radius = radius,
                    type = proj.type,
                    showSymbols = colorblindEnabled
                )
            }

            // Draw Falling Detached Bubbles (Thread-safe snapshot)
            BubbleRenderer.drawFallingBubbles(this, viewModel.getFallingBubblesSnapshot(), colorblindEnabled)

            // Draw Particles (Sparks, stars, pops - Thread-safe snapshot)
            BubbleRenderer.drawParticles(this, viewModel.getParticlesSnapshot())

            // Draw Floating Texts (Thread-safe snapshot)
            BubbleRenderer.drawFloatingTexts(this, viewModel.getFloatingTextsSnapshot())
        }

        // 2. Left Center Remaining Balloons Bubble / Button (Requirement 6)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF130A2A).copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    if (uiState.shotsRemaining <= 5) Color(0xFFFF3366) else Color(0xFF00E5FF)
                ),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(76.dp)
                    .testTag("remaining_balloons_bubble")
                    .clickable {
                        viewModel.soundEngine.playPop(1)
                        viewModel.haptics.pop()
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${uiState.shotsRemaining}",
                        color = if (uiState.shotsRemaining <= 5) Color(0xFFFF5252) else Color(0xFFFFDE00),
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "BALLOONS",
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // 3. Top Header HUD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause Button
                IconButton(
                    onClick = { viewModel.navigateTo(GameScreen.PAUSED) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .testTag("pause_button")
                ) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White)
                }

                // Level & Objective Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, world.primaryColor.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LVL ${uiState.levelConfig.levelNumber}: ${uiState.levelConfig.title}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Shots Remaining Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (uiState.shotsRemaining <= 5) Color(0xFFFF3366).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.shotsRemaining} SHOTS",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Score and Combo Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SCORE",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${uiState.score}",
                        color = Color(0xFFFFDE00),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Fever Meter
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (uiState.isFeverActive) "🔥 FEVER MODE 2X! 🔥" else "FEVER CHARGE",
                        color = if (uiState.isFeverActive) Color(0xFFFF6D00) else Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(uiState.feverProgress)
                                .height(10.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF2E93), Color(0xFFFFDE00), Color(0xFF00E5FF))
                                    )
                                )
                        )
                    }
                }

                // Coins
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🪙 ${viewModel.saveManager.coins}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Boss HP Bar (if Boss level)
            if (uiState.bossMaxHp > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "BOSS: ${uiState.levelConfig.boss?.name}",
                                color = Color(0xFFFF5252),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${uiState.bossHp} / ${uiState.bossMaxHp} HP",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val hpPercent = (uiState.bossHp.toFloat() / uiState.bossMaxHp).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { hpPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFFF3366),
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )
                    }
                }
            }
        }

        // 3. Combo / Trick Shot Banner Overlay
        AnimatedVisibility(
            visible = uiState.bannerText != null,
            enter = fadeIn(tween(150)) + scaleIn(tween(250, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(200)) + scaleOut(tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFDE00)),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.bannerText ?: "",
                        color = Color(0xFFFFDE00),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    if (uiState.bannerSubtext != null) {
                        Text(
                            text = uiState.bannerSubtext ?: "",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Bottom Controls, Launcher, Pip Mascot & Boosters
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Booster Shortcuts (Only 1 chance to use 1 option per round; fades after use)
            val canUseBooster = !uiState.boosterUsedThisRound
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BoosterButton(
                    icon = Icons.Default.Whatshot,
                    label = "BOMB",
                    color = Color(0xFFFF5252),
                    testTag = "booster_bomb",
                    enabled = canUseBooster,
                    onClick = { viewModel.applyBooster(BubbleType.Bomb) }
                )
                BoosterButton(
                    icon = Icons.Default.ColorLens,
                    label = "RAINBOW",
                    color = Color(0xFFFF2E93),
                    testTag = "booster_rainbow",
                    enabled = canUseBooster,
                    onClick = { viewModel.applyBooster(BubbleType.Rainbow) }
                )
                BoosterButton(
                    icon = Icons.Default.Bolt,
                    label = "LASER",
                    color = Color(0xFFFFDE00),
                    testTag = "booster_laser",
                    enabled = canUseBooster,
                    onClick = { viewModel.applyBooster(BubbleType.Lightning) }
                )
            }

            // Launcher Base & Classic Bubble Dragon Mascot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Classic Bubble Dragon mascot moving in game level and throwing bubble (from user video)
                val dragonSkinColor = com.example.neonpop.data.SkinColourDatabase.getSkinColour(uiState.equippedSkinColourId).color
                BubbleDragonCharacterView(
                    skinColor = dragonSkinColor,
                    aimAngleRad = uiState.aimAngleRad,
                    mood = uiState.mascotMood,
                    isFiring = uiState.projectile != null,
                    size = 120.dp
                )

                // Current Loaded Bubble with Swap Affordance
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, world.primaryColor.copy(alpha = 0.7f)),
                    modifier = Modifier.clickable { viewModel.swapBubbles() }.testTag("swap_bubble_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current Bubble Display
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                BubbleRenderer.drawBubble(
                                    this,
                                    cx = size.width / 2f,
                                    cy = size.height / 2f,
                                    radius = size.width * 0.42f,
                                    type = uiState.currentBubble,
                                    showSymbols = colorblindEnabled
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            Icons.Default.SwapVert,
                            contentDescription = "Swap",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        // Next Bubble in Queue
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                BubbleRenderer.drawBubble(
                                    this,
                                    cx = size.width / 2f,
                                    cy = size.height / 2f,
                                    radius = size.width * 0.42f,
                                    type = uiState.nextBubble,
                                    showSymbols = colorblindEnabled
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun BoosterButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    testTag: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.28f
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Black.copy(alpha = if (enabled) 0.55f else 0.22f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (enabled) color.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.12f)
        ),
        modifier = Modifier
            .clickable(enabled = enabled) { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = color.copy(alpha = alpha), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = Color.White.copy(alpha = alpha),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
