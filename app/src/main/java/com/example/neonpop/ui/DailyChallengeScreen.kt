package com.example.neonpop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neonpop.game.GameViewModel
import com.example.neonpop.model.GameMode
import com.example.neonpop.model.GameScreen
import com.example.neonpop.model.MascotMood

@Composable
fun DailyChallengeScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val saveManager = viewModel.saveManager
    val streak = saveManager.dailyStreak

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D071E))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(GameScreen.HOME) },
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
                    .testTag("daily_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(text = "DAILY DROP & STREAK", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(text = "Log in every day for legendary rewards", color = Color(0xFFFF9E00), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bubble Dragon Mascot Excited
        val skinColor = com.example.neonpop.data.SkinColourDatabase.getSkinColour(saveManager.equippedSkinColourId).color
        BubbleDragonCharacterView(
            skinColor = skinColor,
            aimAngleRad = -1.57f,
            mood = MascotMood.EXCITED,
            size = 135.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "🔥 ${streak}-DAY STREAK ACTIVE!",
            color = Color(0xFFFF9E00),
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 7-Day Calendar Streak Nodes
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF9E00).copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "WEEKLY STREAK BONUS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (day in 1..7) {
                        val isReached = day <= (streak % 7).let { if (it == 0 && streak > 0) 7 else it }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isReached) Color(0xFFFF9E00) else Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isReached) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                    } else {
                                        Text(text = "$day", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${day * 20}🪙",
                                color = if (isReached) Color(0xFFFFDE00) else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Daily Drop Challenge Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF1F0F3D),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "TODAY'S SPECIAL PUZZLE",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cosmic Warp: Limited Angle Trick",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = "Clear the board using tricky bank shots off both walls. Massive 500 coin reward!",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Launch Daily Drop (uses special level preset, e.g. level 18 or 21)
                        viewModel.startLevel(21, GameMode.DAILY)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_daily_drop_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2E93)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "PLAY DAILY DROP (+500 🪙)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
