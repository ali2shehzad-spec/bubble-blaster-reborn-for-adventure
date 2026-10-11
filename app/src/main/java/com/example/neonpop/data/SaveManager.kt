package com.example.neonpop.data

import android.content.Context
import android.content.SharedPreferences
import com.example.neonpop.security.AntiCheatGuard
import org.json.JSONObject

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val icon: String,
    var isUnlocked: Boolean = false,
    var isClaimed: Boolean = false
)

data class CosmeticItem(
    val id: String,
    val name: String,
    val type: String, // "skin" or "trail"
    val price: Int,
    val description: String
)

class SaveManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("neon_pop_save", Context.MODE_PRIVATE)

    // In-memory obfuscated values protecting against GameGuardian / memory scanners
    private val secureCoins = AntiCheatGuard.SecureInt(0)
    private val secureUnlockedLevel = AntiCheatGuard.SecureInt(1)

    init {
        verifyAndSynchronizeIntegrity()
    }

    private fun verifyAndSynchronizeIntegrity() {
        val storedLevel = prefs.getInt("unlocked_level", 1).coerceIn(1, 300)
        val storedCoins = if (!prefs.contains("coins_initial_reset")) {
            val realCoins = (storedLevel - 1).coerceIn(0, 300) * 10
            prefs.edit().putBoolean("coins_initial_reset", true).putInt("coins", realCoins).apply()
            realCoins
        } else {
            prefs.getInt("coins", 0).coerceAtLeast(0)
        }
        val storedXp = prefs.getInt("xp", 0).coerceAtLeast(0)
        val storedSkin = prefs.getInt("equipped_skin_colour_id", 0)
        val storedSig = prefs.getString("save_hmac_sig", null)

        if (storedSig != null) {
            val isValid = AntiCheatGuard.verifySaveIntegrity(storedLevel, storedCoins, storedXp, storedSkin, storedSig)
            if (!isValid) {
                // External XML tampering detected! Restore legitimate progress from completed levels
                val sanitizedCoins = (storedLevel - 1).coerceIn(0, 300) * 10
                prefs.edit().putInt("coins", sanitizedCoins).apply()
                secureCoins.set(sanitizedCoins)
                secureUnlockedLevel.set(storedLevel)
                recomputeSignature()
                return
            }
        }

        secureCoins.set(storedCoins)
        secureUnlockedLevel.set(storedLevel)
        recomputeSignature()
    }

    private fun recomputeSignature() {
        val lvl = prefs.getInt("unlocked_level", 1).coerceIn(1, 300)
        val c = prefs.getInt("coins", 0).coerceAtLeast(0)
        val xp = prefs.getInt("xp", 0).coerceAtLeast(0)
        val skin = prefs.getInt("equipped_skin_colour_id", 0)
        val sig = AntiCheatGuard.computeSaveSignature(lvl, c, xp, skin)
        prefs.edit().putString("save_hmac_sig", sig).apply()
    }

    var currentUnlockedLevel: Int
        get() = secureUnlockedLevel.get().coerceIn(1, 300)
        set(value) {
            val safe = value.coerceIn(1, 300)
            secureUnlockedLevel.set(safe)
            prefs.edit().putInt("unlocked_level", safe).apply()
            recomputeSignature()
        }

    var selectedPlayLevel: Int
        get() = prefs.getInt("selected_play_level", currentUnlockedLevel)
        set(value) = prefs.edit().putInt("selected_play_level", value.coerceIn(1, 300)).apply()

    fun resetToLevelOne() {
        selectedPlayLevel = 1
    }

    val completedLevelsCount: Int
        get() = (currentUnlockedLevel - 1).coerceIn(0, 300)

    var equippedSkinColourId: Int
        get() = prefs.getInt("equipped_skin_colour_id", 0)
        set(value) {
            prefs.edit().putInt("equipped_skin_colour_id", value).apply()
            recomputeSignature()
        }

    fun isSkinColourUnlocked(id: Int): Boolean {
        if (id == 0) return true
        return prefs.getBoolean("skin_color_unlocked_$id", false)
    }

    fun unlockSkinColour(id: Int) {
        prefs.edit().putBoolean("skin_color_unlocked_$id", true).apply()
    }

    fun isLevelUnlocked(level: Int): Boolean {
        return level in 1..currentUnlockedLevel
    }

    fun selectLevel(level: Int) {
        selectedPlayLevel = AntiCheatGuard.validateLevelSelect(level, currentUnlockedLevel)
    }

    var coins: Int
        get() = secureCoins.get()
        set(value) {
            val safe = value.coerceAtLeast(0)
            secureCoins.set(safe)
            prefs.edit().putInt("coins", safe).apply()
            recomputeSignature()
        }

    var totalXp: Int
        get() = prefs.getInt("xp", 0)
        set(value) {
            val safe = value.coerceAtLeast(0)
            prefs.edit().putInt("xp", safe).apply()
            recomputeSignature()
        }

    var sfxEnabled: Boolean
        get() = prefs.getBoolean("sfx_enabled", true)
        set(value) = prefs.edit().putBoolean("sfx_enabled", value).apply()

    var musicEnabled: Boolean
        get() = prefs.getBoolean("music_enabled", true)
        set(value) = prefs.edit().putBoolean("music_enabled", value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean("haptics_enabled", true)
        set(value) = prefs.edit().putBoolean("haptics_enabled", value).apply()

    var colorblindSymbolsEnabled: Boolean
        get() = prefs.getBoolean("colorblind_enabled", false)
        set(value) = prefs.edit().putBoolean("colorblind_enabled", value).apply()

    var reducedMotionEnabled: Boolean
        get() = prefs.getBoolean("reduced_motion", false)
        set(value) = prefs.edit().putBoolean("reduced_motion", value).apply()

    var masterVolume: Float
        get() = prefs.getFloat("master_volume", 0.85f)
        set(value) = prefs.edit().putFloat("master_volume", value.coerceIn(0f, 1f)).apply()

    var equippedSkin: String
        get() = prefs.getString("equipped_skin", "classic") ?: "classic"
        set(value) = prefs.edit().putString("equipped_skin", value).apply()

    var equippedTrail: String
        get() = prefs.getString("equipped_trail", "stardust") ?: "stardust"
        set(value) = prefs.edit().putString("equipped_trail", value).apply()

    var dailyStreak: Int
        get() = prefs.getInt("daily_streak", 1)
        set(value) = prefs.edit().putInt("daily_streak", value).apply()

    var lastPlayEpochDay: Long
        get() = prefs.getLong("last_play_epoch", 0L)
        set(value) = prefs.edit().putLong("last_play_epoch", value).apply()

    fun getLevelStars(level: Int): Int {
        return prefs.getInt("stars_lvl_$level", 0)
    }

    fun setLevelStars(level: Int, stars: Int) {
        val existing = getLevelStars(level)
        if (stars > existing) {
            prefs.edit().putInt("stars_lvl_$level", stars.coerceIn(0, 3)).apply()
        }
    }

    fun getLevelHighScore(level: Int): Int {
        return prefs.getInt("score_lvl_$level", 0)
    }

    fun setLevelHighScore(level: Int, score: Int) {
        val existing = getLevelHighScore(level)
        if (score > existing) {
            prefs.edit().putInt("score_lvl_$level", score).apply()
        }
    }

    fun isCosmeticUnlocked(id: String): Boolean {
        if (id == "classic" || id == "stardust") return true
        return prefs.getBoolean("cosmetic_$id", false)
    }

    fun unlockCosmetic(id: String) {
        prefs.edit().putBoolean("cosmetic_$id", true).apply()
    }

    fun checkDailyStreak(): Pair<Int, Boolean> {
        val today = System.currentTimeMillis() / (1000L * 60L * 60L * 24L)
        val lastDay = lastPlayEpochDay

        val isNewDay = today > lastDay
        if (isNewDay) {
            if (today == lastDay + 1) {
                dailyStreak += 1
            } else if (today > lastDay + 1) {
                dailyStreak = 1
            }
            lastPlayEpochDay = today
        }
        return Pair(dailyStreak, isNewDay)
    }

    // Achievements list
    fun getAchievements(): List<AchievementItem> {
        val base = listOf(
            AchievementItem("first_pop", "First Pop", "Pop your very first bubble cluster", 100, "🎯"),
            AchievementItem("trick_shot", "Bank Master", "Execute a wall-bounce bank shot", 150, "⚡"),
            AchievementItem("chain_master", "Chain Reaction", "Trigger a 5+ orphan cluster cascade", 250, "💥"),
            AchievementItem("fever_mode", "Fever Frenzy", "Activate Fever Mode with high combo", 300, "🔥"),
            AchievementItem("boss_slayer", "Boss Vanquisher", "Defeat the Nebula Core boss", 500, "👑"),
            AchievementItem("star_collector", "Star Hunter", "Earn 3 stars on any 5 levels", 400, "⭐"),
            AchievementItem("bubble_god", "Mega Pop", "Clear 8+ bubbles in a single shot", 350, "🌌")
        )
        for (item in base) {
            item.isUnlocked = prefs.getBoolean("ach_unlocked_${item.id}", false)
            item.isClaimed = prefs.getBoolean("ach_claimed_${item.id}", false)
        }
        return base
    }

    fun unlockAchievement(id: String) {
        if (!prefs.getBoolean("ach_unlocked_$id", false)) {
            prefs.edit().putBoolean("ach_unlocked_$id", true).apply()
        }
    }

    fun claimAchievement(id: String): Int {
        val achievements = getAchievements()
        val target = achievements.find { it.id == id } ?: return 0
        if (target.isUnlocked && !target.isClaimed) {
            val verifiedReward = AntiCheatGuard.validateCoinReward("achievement_${target.id}", target.rewardCoins)
            prefs.edit().putBoolean("ach_claimed_$id", true).apply()
            coins += verifiedReward
            return verifiedReward
        }
        return 0
    }
}
