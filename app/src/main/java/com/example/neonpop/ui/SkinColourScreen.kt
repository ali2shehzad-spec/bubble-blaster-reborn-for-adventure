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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neonpop.data.SkinColourDatabase
import com.example.neonpop.game.GameViewModel
import com.example.neonpop.model.GameScreen
import com.example.neonpop.model.MascotMood

@Composable
fun SkinColourScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveManager = viewModel.saveManager
    val coins = uiState.totalCoins
    var previewId by remember { mutableIntStateOf(uiState.equippedSkinColourId) }
    val equippedId = uiState.equippedSkinColourId
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: 6,000 Chroma, 2: 30,000 Elite, 3: Owned

    val allColours = SkinColourDatabase.ALL_SKIN_COLOURS
    val previewItem = SkinColourDatabase.getSkinColour(previewId)
    val displayedColours = remember(selectedTab, equippedId) {
        when (selectedTab) {
            1 -> allColours.filter { it.price == 6000 }
            2 -> allColours.filter { it.price == 30000 }
            3 -> allColours.filter { saveManager.isSkinColourUnlocked(it.id) }
            else -> allColours
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C071E)),
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
                        .testTag("skin_colour_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "SKIN COLOUR",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "500 Dragon Mascot Colors",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Coin Balance Pill
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

        // Live Dragon Mascot Preview Box
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.45f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, previewItem.color.copy(alpha = 0.7f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BubbleDragonCharacterView(
                    skinColor = previewItem.color,
                    aimAngleRad = -1.57f,
                    mood = MascotMood.HAPPY,
                    size = 115.dp
                )

                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(
                        text = previewItem.name,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Colour #${previewItem.id + 1} of 500",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val isUnlocked = saveManager.isSkinColourUnlocked(previewId)
                    val isEquipped = equippedId == previewId

                    if (isEquipped) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF25F05D).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25F05D))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF25F05D), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("EQUIPPED", color = Color(0xFF25F05D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else if (isUnlocked) {
                        Button(
                            onClick = {
                                viewModel.equipSkinColour(previewId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("EQUIP DRESS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    } else {
                        val canAfford = coins >= previewItem.price
                        Button(
                            onClick = {
                                viewModel.buySkinColour(previewId, previewItem.price)
                            },
                            enabled = canAfford,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDE00)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("BUY (${previewItem.price} 🪙)", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Filter Category Chips (All 1,000, 6k Chroma, 30k Elite, Owned)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                "All (1,000)",
                "Chroma (6k 🪙)",
                "Elite (30k 🪙)",
                "Owned"
            )
            tabs.forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFFFF4081) else Color.White.copy(alpha = 0.08f),
                    modifier = Modifier
                        .clickable { selectedTab = index }
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 1,000 Colours Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 74.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayedColours) { item ->
                val isUnlocked = saveManager.isSkinColourUnlocked(item.id)
                val isEquipped = equippedId == item.id
                val isSelected = previewId == item.id

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected || isEquipped) 2.dp else 1.dp,
                        color = when {
                            isEquipped -> Color(0xFF25F05D)
                            isSelected -> Color(0xFFFF4081)
                            isUnlocked -> Color.White.copy(alpha = 0.4f)
                            else -> Color.White.copy(alpha = 0.12f)
                        }
                    ),
                    modifier = Modifier
                        .clickable { previewId = item.id }
                        .testTag("skin_color_${item.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(item.color)
                                .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isUnlocked) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.Black.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (isEquipped) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Equipped",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "#${item.id + 1}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        val priceLabel = if (isUnlocked) "OWNED" else if (item.price >= 1000) "${item.price / 1000}k 🪙" else "${item.price} 🪙"
                        Text(
                            text = priceLabel,
                            color = if (isUnlocked) Color(0xFF25F05D) else Color(0xFFFFDE00),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
}
