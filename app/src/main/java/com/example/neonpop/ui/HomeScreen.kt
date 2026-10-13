package com.example.neonpop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neonpop.data.SkinColourDatabase
import com.example.neonpop.game.GameViewModel
import com.example.neonpop.model.GameMode
import com.example.neonpop.model.GameScreen
import com.example.neonpop.model.MascotMood

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveManager = viewModel.saveManager
    val unlockedLevel = uiState.currentUnlockedLevel
    val selectedLevel = uiState.selectedPlayLevel
    val completedCount = saveManager.completedLevelsCount
    val coins = uiState.totalCoins
    val equippedDressColor = SkinColourDatabase.getSkinColour(uiState.equippedSkinColourId).color
    var showLevelSelectDialog by remember { mutableStateOf(false) }
    val levelRowScrollState = rememberScrollState()

    // Smoothly focus on selected level in horizontal row
    LaunchedEffect(selectedLevel) {
        val targetIdx = (selectedLevel - 1).coerceAtLeast(0)
        levelRowScrollState.animateScrollTo((targetIdx * 160).coerceAtLeast(0))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C0721),
                        Color(0xFF1B0D38),
                        Color(0xFF070414)
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with ONLY Setting Button and Coin Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Setting Button
                IconButton(
                    onClick = { viewModel.navigateTo(GameScreen.SETTINGS) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Coin Button (displays real coins earned from completed levels)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFDE00).copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFDE00).copy(alpha = 0.7f)),
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(GameScreen.SKIN_COLOUR) }
                        .testTag("home_coin_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🪙 $coins",
                            color = Color(0xFFFFDE00),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Game Logo Title: Bubble Blaster (Reborn for Adventure)
            Text(
                text = "BUBBLE BLASTER",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Color.White
            )
            Text(
                text = "★ REBORN FOR ADVENTURE ★",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.5.sp,
                color = Color(0xFF00E5FF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Classic Arcade Bubble Dragon Mascot
            BubbleDragonCharacterView(
                skinColor = equippedDressColor,
                aimAngleRad = -1.57f,
                mood = MascotMood.HAPPY,
                size = 135.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // LEVEL PROGRESS & SELECTION SECTION (CIRCLE DESIGN WITH NUMBERS)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF130926).copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF25F05D).copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_levels_completed_button")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    // Header row: Completed count and All 300 dialog trigger
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Levels Completed",
                                tint = Color(0xFF25F05D),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LEVELS: $completedCount / 300",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                        }

                        // Tap to open full 300-level circular grid
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.16f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.7f)),
                            modifier = Modifier
                                .clickable { showLevelSelectDialog = true }
                                .testTag("home_all_levels_button")
                        ) {
                            Text(
                                text = "ALL 300 ➔",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horizontal Scrollable Row of Levels in Circle Design With Numbers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(levelRowScrollState),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val maxDisplay = minOf(unlockedLevel + 6, 300)
                        for (lvl in 1..maxDisplay) {
                            val isCompleted = lvl < unlockedLevel
                            val isSelected = lvl == selectedLevel
                            val isUnlocked = lvl <= unlockedLevel

                            LevelCircleBadge(
                                levelNumber = lvl,
                                isCompleted = isCompleted,
                                isSelected = isSelected,
                                isUnlocked = isUnlocked,
                                onClick = {
                                    viewModel.selectPlayLevel(lvl)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom info & Quick Jump to Level 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level $selectedLevel Selected (+10 🪙 per win)",
                            color = Color(0xFFFFDE00),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (selectedLevel != 1) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFF2E93).copy(alpha = 0.22f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF2E93)),
                                modifier = Modifier
                                    .clickable { viewModel.selectPlayLevel(1) }
                                    .testTag("home_chip_level_1")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Level 1",
                                        tint = Color(0xFFFF2E93),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "↺ Lvl 1",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Play Level Button (Plays currently selected level, e.g. Level 1 or Level 50)
            Button(
                onClick = { viewModel.startLevel(selectedLevel, GameMode.CLASSIC) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .testTag("home_play_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF2E93)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAY LEVEL $selectedLevel",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }

            // Start Again from Level 1 button (if player has cleared levels and wants to start again from level 1)
            if (completedCount > 0 && selectedLevel != 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectPlayLevel(1) }
                        .testTag("home_start_again_from_level_1_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Start Again",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "START AGAIN FROM LEVEL 1 (Earn +10 🪙 each level)",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SKIN COLOUR Button
            Button(
                onClick = { viewModel.navigateTo(GameScreen.SKIN_COLOUR) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("home_skin_colour_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF).copy(alpha = 0.22f)
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.ColorLens,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SKIN COLOUR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Worlds Map Exploration
            Button(
                onClick = { viewModel.navigateTo(GameScreen.WORLD_MAP) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .testTag("home_nav_worlds"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.6f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = "Worlds",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EXPLORE WORLDS MAP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Full 300 Levels Circle Dialog
        if (showLevelSelectDialog) {
            LevelSelectDialog(
                viewModel = viewModel,
                onDismiss = { showLevelSelectDialog = false }
            )
        }
    }
}
