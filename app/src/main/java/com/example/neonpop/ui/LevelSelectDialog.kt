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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
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
import com.example.neonpop.model.GameMode

@Composable
fun LevelSelectDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveManager = viewModel.saveManager
    val unlockedLevel = uiState.currentUnlockedLevel
    val selectedLevel = uiState.selectedPlayLevel
    val completedCount = saveManager.completedLevelsCount
    val gridState = rememberLazyGridState()

    // Auto-scroll to selected level when opened
    LaunchedEffect(selectedLevel) {
        val targetIndex = (selectedLevel - 1).coerceAtLeast(0)
        gridState.scrollToItem((targetIndex - 5).coerceAtLeast(0))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF120826),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF).copy(alpha = 0.8f)),
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SELECT LEVEL (1 - 300)",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$completedCount Completed • +10 Coins Every Cleared Level",
                            color = Color(0xFFFFDE00),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            .testTag("close_level_select_button")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Prominent "START FROM LEVEL 1" Action
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFF2E93).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF2E93)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectPlayLevel(1)
                            viewModel.startLevel(1, GameMode.CLASSIC)
                            onDismiss()
                        }
                        .testTag("dialog_start_level_1_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Start from Level 1",
                            tint = Color(0xFFFF2E93),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START FROM LEVEL 1 (+10 COINS REWARD)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Grid of Circles with Numbers (1..300)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 54.dp),
                    state = gridState,
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(300) { index ->
                        val levelNum = index + 1
                        val isCompleted = levelNum < unlockedLevel
                        val isSelected = levelNum == selectedLevel
                        val isUnlocked = levelNum <= unlockedLevel

                        LevelCircleBadge(
                            levelNumber = levelNum,
                            isCompleted = isCompleted,
                            isSelected = isSelected,
                            isUnlocked = isUnlocked,
                            onClick = {
                                if (isUnlocked) {
                                    viewModel.selectPlayLevel(levelNum)
                                    viewModel.startLevel(levelNum, GameMode.CLASSIC)
                                    onDismiss()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LevelCircleBadge(
    levelNumber: Int,
    isCompleted: Boolean,
    isSelected: Boolean,
    isUnlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isSelected -> Color(0xFFFF2E93)
        isCompleted -> Color(0xFF25F05D)
        isUnlocked -> Color(0xFF00E5FF)
        else -> Color.White.copy(alpha = 0.18f)
    }

    val borderWidth = if (isSelected) 3.dp else 1.5.dp

    val bgColor = when {
        isSelected -> Color(0xFFFF2E93).copy(alpha = 0.35f)
        isCompleted -> Color(0xFF25F05D).copy(alpha = 0.18f)
        isUnlocked -> Color(0xFF00E5FF).copy(alpha = 0.14f)
        else -> Color.White.copy(alpha = 0.04f)
    }

    val textColor = when {
        isSelected -> Color.White
        isCompleted -> Color(0xFF25F05D)
        isUnlocked -> Color(0xFF00E5FF)
        else -> Color.White.copy(alpha = 0.28f)
    }

    Box(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(borderWidth, borderColor, CircleShape)
            .clickable(enabled = isUnlocked) { onClick() }
            .testTag("level_circle_$levelNumber"),
        contentAlignment = Alignment.Center
    ) {
        if (!isUnlocked) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color.White.copy(alpha = 0.22f),
                modifier = Modifier.size(20.dp)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$levelNumber",
                    color = textColor,
                    fontSize = if (levelNumber > 99) 13.sp else 16.sp,
                    fontWeight = FontWeight.Black
                )
                if (isCompleted) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Completed",
                        tint = Color(0xFFFFDE00),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}
