package com.example.neonpop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.neonpop.game.GameViewModel
import com.example.neonpop.levels.LevelDatabase
import com.example.neonpop.model.GameMode
import com.example.neonpop.model.GameScreen
import kotlinx.coroutines.launch

/**
 * Screen showing levels in a circular design with numbers (1 to 300).
 * Players can choose any level, including replaying from Level 1,
 * and be rewarded 10 coins upon every completion.
 */
@Composable
fun WorldMapScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveManager = viewModel.saveManager
    val unlockedLvl = saveManager.currentUnlockedLevel
    val selectedPlayLevel = saveManager.selectedPlayLevel
    val coins = uiState.totalCoins
    val completedCount = saveManager.completedLevelsCount

    val allLevels = LevelDatabase.ALL_LEVELS
    var selectedTierIndex by remember { mutableIntStateOf(0) }
    val tierRanges = listOf(
        Pair(1, 50),
        Pair(51, 100),
        Pair(101, 150),
        Pair(151, 200),
        Pair(201, 250),
        Pair(251, 300)
    )

    val currentRange = tierRanges[selectedTierIndex]
    val displayedLevels = allLevels.filter { it.levelNumber in currentRange.first..currentRange.second }

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0C0721), Color(0xFF1B0D38), Color(0xFF070414))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(GameScreen.HOME) },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                        .testTag("world_map_back_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "LEVEL SELECT",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "300 Levels • Tap Circle to Play",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Coin Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFDE00).copy(alpha = 0.16f),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFFDE00).copy(alpha = 0.8f))
            ) {
                Text(
                    text = "🪙 $coins",
                    color = Color(0xFFFFDE00),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Quick Action Bar: "START FROM LEVEL 1" button + Completed Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start from Level 1 Button (lets player restart from Level 1 anytime while keeping coins!)
            Button(
                onClick = {
                    saveManager.resetToLevelOne()
                    viewModel.startLevel(1, GameMode.CLASSIC)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2E93)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .height(40.dp)
                    .testTag("start_from_level_1_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PLAY LEVEL 1",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Completed Levels Count pill
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF25F05D).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25F05D).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF25F05D), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$completedCount / 300 CLEARED",
                        color = Color(0xFF25F05D),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Level Tier Tabs (1-50, 51-100, 101-150, 151-200, 201-250, 251-300)
        ScrollableTabRow(
            selectedTabIndex = selectedTierIndex,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF00E5FF),
            edgePadding = 16.dp,
            divider = {}
        ) {
            tierRanges.forEachIndexed { index, range ->
                val isSelected = selectedTierIndex == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        selectedTierIndex = index
                        coroutineScope.launch { gridState.scrollToItem(0) }
                    },
                    text = {
                        Text(
                            text = "${range.first}-${range.second}",
                            color = if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.5f),
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Circular Levels Grid with Numbers
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(displayedLevels) { lvl ->
                val isUnlocked = lvl.levelNumber <= unlockedLvl
                val isSelected = lvl.levelNumber == selectedPlayLevel
                val stars = saveManager.getLevelStars(lvl.levelNumber)
                val isBoss = lvl.boss != null

                CircularLevelNode(
                    levelNumber = lvl.levelNumber,
                    isUnlocked = isUnlocked,
                    isSelected = isSelected,
                    stars = stars,
                    isBoss = isBoss,
                    accentColor = lvl.primaryAccent,
                    onClick = {
                        if (isUnlocked) {
                            saveManager.selectedPlayLevel = lvl.levelNumber
                            viewModel.startLevel(lvl.levelNumber, GameMode.CLASSIC)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Circular Design Level Node with Level Number centered inside.
 */
@Composable
fun CircularLevelNode(
    levelNumber: Int,
    isUnlocked: Boolean,
    isSelected: Boolean,
    stars: Int,
    isBoss: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val circleSize = 60.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = isUnlocked) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(
                    if (isUnlocked) {
                        Brush.radialGradient(
                            listOf(
                                if (isBoss) Color(0xFFFF3366) else accentColor,
                                Color(0xFF0F0826)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.08f), Color.Black.copy(alpha = 0.5f))
                        )
                    }
                )
                .border(
                    width = if (isSelected) 2.5.dp else if (isUnlocked) 1.8.dp else 1.dp,
                    color = when {
                        isSelected -> Color(0xFFFFDE00)
                        isBoss && isUnlocked -> Color(0xFFFF5252)
                        isUnlocked -> accentColor.copy(alpha = 0.9f)
                        else -> Color.White.copy(alpha = 0.15f)
                    },
                    shape = CircleShape
                )
                .testTag("circular_level_node_$levelNumber"),
            contentAlignment = Alignment.Center
        ) {
            if (!isUnlocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isBoss) {
                        Text(
                            text = "👑",
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = "$levelNumber",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = if (levelNumber >= 100) 14.sp else 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Stars display under circle
        if (isUnlocked) {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(3) { i ->
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i < stars) Color(0xFFFFDE00) else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        } else {
            Text(
                text = "LOCK",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
