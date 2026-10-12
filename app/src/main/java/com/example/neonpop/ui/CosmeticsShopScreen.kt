package com.example.neonpop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neonpop.data.CosmeticItem
import com.example.neonpop.game.GameViewModel
import com.example.neonpop.model.GameScreen
import com.example.neonpop.model.MascotMood

@Composable
fun CosmeticsShopScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val saveManager = viewModel.saveManager
    var coins by remember { mutableIntStateOf(saveManager.coins) }
    var equippedSkin by remember { mutableStateOf(saveManager.equippedSkin) }

    val skins = listOf(
        CosmeticItem("classic", "Classic Pip", "skin", 0, "Original dreamy neon companion"),
        CosmeticItem("cyber", "Cyber Pip", "skin", 400, "Futuristic neon cyan cyber visor"),
        CosmeticItem("bunny", "Dream Bunny", "skin", 600, "Pastel pink plush ears & starlight"),
        CosmeticItem("hologram", "Holo Pip", "skin", 850, "Iridescent magenta holographic projection"),
        CosmeticItem("dj", "DJ Pip", "skin", 1200, "Golden neon headphones & sonic aura")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A071E)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(GameScreen.HOME) },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                        .testTag("shop_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = "PIP WARDROBE", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(text = "Pure cosmetics • No pay-to-win", color = Color(0xFF00E5FF), fontSize = 11.sp)
                }
            }

            // Coin counter
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFDE00).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFDE00).copy(alpha = 0.6f))
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

        // Live Preview of Bubble Dragon Mascot
        val dressColor = com.example.neonpop.data.SkinColourDatabase.getSkinColour(saveManager.equippedSkinColourId).color
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            BubbleDragonCharacterView(
                skinColor = dressColor,
                aimAngleRad = -1.57f,
                mood = MascotMood.HAPPY,
                size = 140.dp
            )
        }

        // Skin List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(skins) { item ->
                val isUnlocked = saveManager.isCosmeticUnlocked(item.id)
                val isEquipped = equippedSkin == item.id

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isEquipped) Color(0xFFFF2E93) else if (isUnlocked) Color(0xFF00E5FF).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp
                            )
                        }

                        if (isEquipped) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFF2E93).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF2E93))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFFF2E93), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("EQUIPPED", color = Color(0xFFFF2E93), fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                        } else if (isUnlocked) {
                            Button(
                                onClick = {
                                    saveManager.equippedSkin = item.id
                                    equippedSkin = item.id
                                    viewModel.soundEngine.playUiTap()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("EQUIP", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        } else {
                            val canAfford = coins >= item.price
                            Button(
                                onClick = {
                                    if (canAfford) {
                                        saveManager.coins -= item.price
                                        saveManager.unlockCosmetic(item.id)
                                        saveManager.equippedSkin = item.id
                                        coins = saveManager.coins
                                        equippedSkin = item.id
                                        viewModel.soundEngine.playCoin()
                                    }
                                },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDE00)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${item.price} 🪙",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
}
