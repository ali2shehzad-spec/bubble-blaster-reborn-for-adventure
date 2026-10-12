package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.neonpop.game.GameViewModel
import com.example.neonpop.model.GameScreen
import com.example.neonpop.ui.AchievementsScreen
import com.example.neonpop.ui.CosmeticsShopScreen
import com.example.neonpop.ui.DailyChallengeScreen
import com.example.neonpop.ui.GamePlayScreen
import com.example.neonpop.ui.HomeScreen
import com.example.neonpop.ui.LevelCompleteDialog
import com.example.neonpop.ui.LevelFailedDialog
import com.example.neonpop.ui.SkinColourScreen
import com.example.neonpop.ui.PauseDialog
import com.example.neonpop.ui.SettingsDialog
import com.example.neonpop.ui.WorldMapScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Global crash guard to ensure application resilience against sudden crashes
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("CrashGuard", "Intercepted uncaught error on thread ${thread.name}: ${throwable.message}", throwable)
            try {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    try {
                        gameViewModel.navigateTo(GameScreen.HOME)
                    } catch (_: Throwable) {}
                }
            } catch (_: Throwable) {}
        }

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF070414)
                ) {
                    NeonPopApp(viewModel = gameViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gameViewModel.resumeGameLoop()
        if (gameViewModel.saveManager.musicEnabled) {
            gameViewModel.soundEngine.startDynamicMusic()
        }
    }

    override fun onPause() {
        super.onPause()
        gameViewModel.pauseGameLoop()
        gameViewModel.soundEngine.stopDynamicMusic()
    }
}

@Composable
fun NeonPopApp(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle Android system back gesture properly
    BackHandler(enabled = uiState.currentScreen != GameScreen.HOME) {
        when (uiState.currentScreen) {
            GameScreen.PLAYING -> viewModel.navigateTo(GameScreen.PAUSED)
            GameScreen.PAUSED -> viewModel.navigateTo(GameScreen.PLAYING)
            GameScreen.LEVEL_COMPLETE, GameScreen.LEVEL_FAILED -> viewModel.navigateTo(GameScreen.HOME)
            else -> viewModel.navigateTo(GameScreen.HOME)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF070414))) {
        when (uiState.currentScreen) {
            GameScreen.HOME -> HomeScreen(viewModel = viewModel)
            GameScreen.WORLD_MAP -> WorldMapScreen(viewModel = viewModel)
            GameScreen.PLAYING -> GamePlayScreen(viewModel = viewModel)
            GameScreen.PAUSED -> {
                GamePlayScreen(viewModel = viewModel)
                PauseDialog(viewModel = viewModel)
            }
            GameScreen.LEVEL_COMPLETE -> {
                GamePlayScreen(viewModel = viewModel)
                LevelCompleteDialog(viewModel = viewModel)
            }
            GameScreen.LEVEL_FAILED -> {
                GamePlayScreen(viewModel = viewModel)
                LevelFailedDialog(viewModel = viewModel)
            }
            GameScreen.COSMETICS -> CosmeticsShopScreen(viewModel = viewModel)
            GameScreen.SKIN_COLOUR -> SkinColourScreen(viewModel = viewModel)
            GameScreen.ACHIEVEMENTS -> AchievementsScreen(viewModel = viewModel)
            GameScreen.DAILY_CHALLENGE -> DailyChallengeScreen(viewModel = viewModel)
            GameScreen.SETTINGS -> SettingsDialog(viewModel = viewModel)
        }
    }
}
