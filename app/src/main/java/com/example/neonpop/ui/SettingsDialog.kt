package com.example.neonpop.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neonpop.game.GameViewModel
import com.example.neonpop.model.GameScreen

@Composable
fun SettingsDialog(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val saveManager = viewModel.saveManager

    var sfx by remember { mutableStateOf(saveManager.sfxEnabled) }
    var music by remember { mutableStateOf(saveManager.musicEnabled) }
    var haptics by remember { mutableStateOf(saveManager.hapticsEnabled) }
    var colorblind by remember { mutableStateOf(saveManager.colorblindSymbolsEnabled) }
    var volume by remember { mutableFloatStateOf(saveManager.masterVolume) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF140D26),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SETTINGS",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = { viewModel.navigateTo(GameScreen.HOME) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            .testTag("settings_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Sound Effects Toggle
                SettingsToggleRow(
                    title = "Sound Effects",
                    subtitle = "Procedural arcade pops & chords",
                    checked = sfx,
                    testTag = "settings_toggle_sfx",
                    onCheckedChange = {
                        sfx = it
                        saveManager.sfxEnabled = it
                        viewModel.soundEngine.sfxEnabled = it
                        if (it) viewModel.soundEngine.playPop(2)
                    }
                )

                // Dynamic Music Toggle
                SettingsToggleRow(
                    title = "Ambient Music",
                    subtitle = "Procedural cyber-dreamscape loop",
                    checked = music,
                    testTag = "settings_toggle_music",
                    onCheckedChange = {
                        music = it
                        saveManager.musicEnabled = it
                        viewModel.soundEngine.musicEnabled = it
                    }
                )

                // Master Volume Slider
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Master Volume", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${(volume * 100).toInt()}%", color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = volume,
                        onValueChange = {
                            volume = it
                            saveManager.masterVolume = it
                            viewModel.soundEngine.masterVolume = it
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("settings_volume_slider")
                    )
                }

                // Haptic Feedback Toggle
                SettingsToggleRow(
                    title = "Haptic Vibration",
                    subtitle = "Tactile pop & combo response",
                    checked = haptics,
                    testTag = "settings_toggle_haptics",
                    onCheckedChange = {
                        haptics = it
                        saveManager.hapticsEnabled = it
                        viewModel.haptics.hapticsEnabled = it
                        if (it) viewModel.haptics.pop()
                    }
                )

                // Colorblind Accessibility Pattern Symbols Toggle
                SettingsToggleRow(
                    title = "Colorblind Patterns",
                    subtitle = "Display geometric symbols on bubbles",
                    checked = colorblind,
                    testTag = "settings_toggle_colorblind",
                    onCheckedChange = {
                        colorblind = it
                        saveManager.colorblindSymbolsEnabled = it
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFF2E93),
                checkedTrackColor = Color(0xFFFF2E93).copy(alpha = 0.5f),
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color.DarkGray
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
