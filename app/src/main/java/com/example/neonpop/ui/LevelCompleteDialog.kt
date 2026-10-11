package com.example.neonpop.ui

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neonpop.game.GameViewModel
import com.example.neonpop.model.GameMode
import com.example.neonpop.model.GameScreen
import com.example.neonpop.model.MascotMood

@Composable
fun LevelCompleteDialog(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val stars = uiState.levelCompletedStars
    val score = uiState.score
    val bestCombo = uiState.levelBestCombo
    val accuracy = uiState.levelAccuracyPercent
    val bubblesPopped = uiState.levelBubblesPopped
    val coinsEarned = uiState.levelCoinsEarned
    val levelNum = uiState.levelConfig.levelNumber

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF130A2A),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bubble Dragon in victory celebration
                val skinColor = com.example.neonpop.data.SkinColourDatabase.getSkinColour(uiState.equippedSkinColourId).color
                BubbleDragonCharacterView(
                    skinColor = skinColor,
                    aimAngleRad = -1.57f,
                    mood = MascotMood.VICTORY,
                    size = 120.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "YOU DESTROYED THIS LEVEL!",
                    color = Color(0xFFFFDE00),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Level $levelNum Completed",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stars Display
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) { i ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i < stars) Color(0xFFFFDE00) else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Stats Grid
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        StatRow(label = "FINAL SCORE", value = "$score PTS", color = Color(0xFFFFDE00))
                        StatRow(label = "BEST COMBO", value = "x$bestCombo", color = Color(0xFFFF2E93))
                        StatRow(label = "SHOT ACCURACY", value = "$accuracy%", color = Color(0xFF00E5FF))
                        StatRow(label = "BUBBLES POPPED", value = "$bubblesPopped", color = Color(0xFF25F05D))
                        StatRow(label = "REWARD", value = "+$coinsEarned COINS 🪙", color = Color(0xFFFFDE00))
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // ONLY 2 Action Buttons: Next Level and Back to Home
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Next Level Button
                    Button(
                        onClick = {
                            if (levelNum < 300) {
                                viewModel.startLevel(levelNum + 1, GameMode.CLASSIC)
                            } else {
                                viewModel.navigateTo(GameScreen.HOME)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("win_next_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2E93)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (levelNum < 300) "NEXT LEVEL" else "VICTORY! HOME",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next Level", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    // 2. Back to Home Button
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(GameScreen.HOME) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("win_home_button"),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BACK TO HOME",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}
