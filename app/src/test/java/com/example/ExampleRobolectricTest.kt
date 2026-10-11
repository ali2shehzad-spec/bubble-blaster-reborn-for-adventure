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
}
