package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.neonpop.data.SaveManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("bubble blaster(reborn for adventure)", appName)
  }

  @Test
  fun `test save manager persistence defaults`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val saveManager = SaveManager(context)
    assertEquals(1, saveManager.currentUnlockedLevel)
    assertNotNull(saveManager.coins)
  }

  @Test
  fun `test player completing 50 levels with 500 coins can restart from level 1 and earn coins`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val saveManager = SaveManager(context)
    saveManager.currentUnlockedLevel = 51 // 50 levels completed
    saveManager.coins = 500
    assertEquals(500, saveManager.coins)
    assertEquals(50, saveManager.completedLevelsCount)

    // Player chooses to start again from Level 1
    saveManager.selectLevel(1)
    assertEquals(1, saveManager.selectedPlayLevel)
    // 50 levels remain unlocked
    assertEquals(true, saveManager.isLevelUnlocked(1))
    assertEquals(true, saveManager.isLevelUnlocked(50))

    // Player wins level 1 and earns +10 coins
    saveManager.coins += 10
    assertEquals(510, saveManager.coins)
  }

  @Test
  fun `test anti-cheat in-memory secure int and save signature verification`() {
    val secInt = com.example.neonpop.security.AntiCheatGuard.SecureInt(100)
    assertEquals(100, secInt.get())
    secInt.add(50)
    assertEquals(150, secInt.get())

    // Aim angle sanitization prevents illegal downward/backward shots
    val illegalDownward = 1.0f // positive radians is downward
    val sanitized = com.example.neonpop.security.AntiCheatGuard.sanitizeAimAngle(illegalDownward)
    org.junit.Assert.assertTrue(sanitized < 0f)

    // Level skip validation prevents jumping directly to locked levels
    val clampedLevel = com.example.neonpop.security.AntiCheatGuard.validateLevelSelect(250, 5)
    assertEquals(5, clampedLevel)

    // Save integrity signature verification
    val sig = com.example.neonpop.security.AntiCheatGuard.computeSaveSignature(10, 100, 500, 0)
    val isValid = com.example.neonpop.security.AntiCheatGuard.verifySaveIntegrity(10, 100, 500, 0, sig)
    org.junit.Assert.assertTrue(isValid)

    // Forged values fail signature check
    val isForgedValid = com.example.neonpop.security.AntiCheatGuard.verifySaveIntegrity(10, 999999, 500, 0, sig)
    org.junit.Assert.assertFalse(isForgedValid)
  }

  @Test
  fun `test crash-proof physics boundary safe checks`() {
    val physics = com.example.neonpop.physics.HexGridPhysics(maxRows = 16, evenCols = 8, oddCols = 7)
    val grid: Array<Array<com.example.neonpop.model.BubbleCell?>> = Array(16) { arrayOfNulls(8) }

    // Test out of bounds access doesn't throw ArrayIndexOutOfBoundsException
    val safeCell = physics.getCellSafe(grid, 99, 99)
    org.junit.Assert.assertNull(safeCell)

    val safeNegative = physics.getCellSafe(grid, -1, -5)
    org.junit.Assert.assertNull(safeNegative)

    // Test finding hits with empty grid doesn't crash
    val hit = physics.findHitBubble(100f, 100f, 30f, 0f, 100f, grid)
    org.junit.Assert.assertNull(hit)

    // Test floating clusters with empty grid returns empty set without crashing
    val clusters = physics.findFloatingClusters(grid)
    org.junit.Assert.assertTrue(clusters.isEmpty())
  }

  @Test
  fun `test achievement coin reward full claiming without truncation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val saveManager = SaveManager(context)
    val initialCoins = saveManager.coins

    // Unlock and claim boss_slayer achievement (500 coins)
    saveManager.unlockAchievement("boss_slayer")
    val reward = saveManager.claimAchievement("boss_slayer")
    assertEquals(500, reward)
    assertEquals(initialCoins + 500, saveManager.coins)

    // Cannot double claim
    val doubleReward = saveManager.claimAchievement("boss_slayer")
    assertEquals(0, doubleReward)
  }

  @Test
  fun `test continue with extra shots flow in ViewModel`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.neonpop.game.GameViewModel(application)

    viewModel.startLevel(1, com.example.neonpop.model.GameMode.CLASSIC)
    val initialShots = viewModel.uiState.value.shotsRemaining

    // Purchase +5 shots continue
    viewModel.continueWithExtraShots(5)
    assertEquals(initialShots + 5, viewModel.uiState.value.shotsRemaining)
    assertEquals(com.example.neonpop.model.GameScreen.PLAYING, viewModel.uiState.value.currentScreen)
  }

  @Test
  fun `test user settings persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val saveManager = SaveManager(context)

    saveManager.sfxEnabled = false
    saveManager.musicEnabled = false
    saveManager.hapticsEnabled = false
    saveManager.masterVolume = 0.5f

    // New instance reads persisted values
    val reloaded = SaveManager(context)
    assertEquals(false, reloaded.sfxEnabled)
    assertEquals(false, reloaded.musicEnabled)
    assertEquals(false, reloaded.hapticsEnabled)
    assertEquals(0.5f, reloaded.masterVolume, 0.01f)
  }

  @Test
  fun `test shot limits across progression in ViewModel`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.neonpop.game.GameViewModel(application)
    // Unlock all levels in saveManager so AntiCheatGuard permits testing all levels
    viewModel.saveManager.currentUnlockedLevel = 300

    // Level <= 15: 10 shots
    viewModel.startLevel(1, com.example.neonpop.model.GameMode.CLASSIC)
    assertEquals(10, viewModel.uiState.value.shotsRemaining)
    viewModel.startLevel(15, com.example.neonpop.model.GameMode.CLASSIC)
    assertEquals(10, viewModel.uiState.value.shotsRemaining)

    // Level 16..25: 20 shots
    viewModel.startLevel(16, com.example.neonpop.model.GameMode.CLASSIC)
    assertEquals(20, viewModel.uiState.value.shotsRemaining)
    viewModel.startLevel(25, com.example.neonpop.model.GameMode.CLASSIC)
    assertEquals(20, viewModel.uiState.value.shotsRemaining)

    // After Level 25 (26+): 30 shots
    viewModel.startLevel(26, com.example.neonpop.model.GameMode.CLASSIC)
    assertEquals(30, viewModel.uiState.value.shotsRemaining)
    viewModel.startLevel(50, com.example.neonpop.model.GameMode.CLASSIC)
    assertEquals(30, viewModel.uiState.value.shotsRemaining)
    viewModel.startLevel(300, com.example.neonpop.model.GameMode.CLASSIC)
    assertEquals(30, viewModel.uiState.value.shotsRemaining)
  }
}
