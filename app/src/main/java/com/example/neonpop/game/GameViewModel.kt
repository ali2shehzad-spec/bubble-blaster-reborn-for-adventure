package com.example.neonpop.game

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.neonpop.audio.HapticsHelper
import com.example.neonpop.audio.ProceduralSoundEngine
import com.example.neonpop.data.SaveManager
import com.example.neonpop.levels.LevelDatabase
import com.example.neonpop.model.ActiveProjectile
import com.example.neonpop.model.BubbleCell
import com.example.neonpop.model.BubbleColor
import com.example.neonpop.model.BubbleType
import com.example.neonpop.model.FallingBubble
import com.example.neonpop.model.FloatingText
import com.example.neonpop.model.GameMode
import com.example.neonpop.model.GameScreen
import com.example.neonpop.model.LevelConfig
import com.example.neonpop.model.LevelObjective
import com.example.neonpop.model.MascotMood
import com.example.neonpop.model.Particle
import com.example.neonpop.model.ParticleType
import com.example.neonpop.model.TrajectoryRay
import com.example.neonpop.physics.HexGridPhysics
import com.example.neonpop.security.AntiCheatGuard
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class GameUiState(
    val currentScreen: GameScreen = GameScreen.HOME,
    val selectedWorldId: Int = 1,
    val levelConfig: LevelConfig = LevelDatabase.getLevel(1),
    val gameMode: GameMode = GameMode.CLASSIC,
    val score: Int = 0,
    val comboCount: Int = 0,
    val feverProgress: Float = 0f,
    val isFeverActive: Boolean = false,
    val shotsRemaining: Int = 20,
    val timeRemainingSeconds: Int = 60,
    val starsRescued: Int = 0,
    val totalStarsToRescue: Int = 0,
    val mascotMood: MascotMood = MascotMood.IDLE,
    val aimAngleRad: Float = -1.57f,
    val isAiming: Boolean = false,
    val currentBubble: BubbleType = BubbleType.Normal(BubbleColor.ELECTRIC_BLUE),
    val nextBubble: BubbleType = BubbleType.Normal(BubbleColor.NEON_RED),
    val trajectory: TrajectoryRay? = null,
    val projectile: ActiveProjectile? = null,
    val screenShake: Float = 0f,
    val bossHp: Int = 0,
    val bossMaxHp: Int = 0,
    val levelCompletedStars: Int = 0,
    val levelCoinsEarned: Int = 0,
    val levelAccuracyPercent: Int = 100,
    val levelBestCombo: Int = 0,
    val levelBubblesPopped: Int = 0,
    val bannerText: String? = null,
    val bannerSubtext: String? = null,
    val equippedSkinColourId: Int = 0,
    val boosterUsedThisRound: Boolean = false,
    val totalCoins: Int = 0,
    val selectedPlayLevel: Int = 1,
    val currentUnlockedLevel: Int = 1
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val saveManager = SaveManager(application)
    val soundEngine = ProceduralSoundEngine()
    val haptics = HapticsHelper(application)
    val physics = HexGridPhysics(maxRows = 16, evenCols = 8, oddCols = 7)

    private val _uiState = MutableStateFlow(
        GameUiState(
            equippedSkinColourId = saveManager.equippedSkinColourId,
            totalCoins = saveManager.coins,
            selectedPlayLevel = saveManager.selectedPlayLevel,
            currentUnlockedLevel = saveManager.currentUnlockedLevel
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // 2D grid of bubble cells
    var grid: Array<Array<BubbleCell?>> = Array(physics.maxRows) { arrayOfNulls(physics.evenCols) }
    val entityLock = Any()
    val fallingBubbles = mutableListOf<FallingBubble>()
    val particles = mutableListOf<Particle>()
    val floatingTexts = mutableListOf<FloatingText>()

    fun getFallingBubblesSnapshot(): List<FallingBubble> = synchronized(entityLock) { ArrayList(fallingBubbles) }
    fun getParticlesSnapshot(): List<Particle> = synchronized(entityLock) { ArrayList(particles) }
    fun getFloatingTextsSnapshot(): List<FloatingText> = synchronized(entityLock) { ArrayList(floatingTexts) }

    fun getCellSafe(r: Int, c: Int): BubbleCell? {
        val currentGrid = grid
        if (r in currentGrid.indices && c in currentGrid[r].indices) {
            return currentGrid[r][c]
        }
        return null
    }

    private var gameLoopJob: Job? = null
    private val random = Random()
    private var boardWidth = 1080f
    private var boardHeight = 1920f
    private var gridTop = 160f
    private var launcherX = 540f
    private var launcherY = 1650f

    // Performance and statistics tracking
    private var shotsFiredCount = 0
    private var successfulHitsCount = 0
    private var totalBubblesPoppedCount = 0
    private var maxComboAchieved = 0

    private val crashHandler = CoroutineExceptionHandler { _, throwable ->
        android.util.Log.e("GameViewModel", "Recovered from coroutine error: ${throwable.message}", throwable)
        _uiState.value = _uiState.value.copy(projectile = null)
        startGameLoop()
    }

    init {
        soundEngine.sfxEnabled = saveManager.sfxEnabled
        soundEngine.musicEnabled = saveManager.musicEnabled
        soundEngine.masterVolume = saveManager.masterVolume
        haptics.hapticsEnabled = saveManager.hapticsEnabled

        soundEngine.startDynamicMusic()
        startGameLoop()
    }

    fun pauseGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    fun resumeGameLoop() {
        if (gameLoopJob == null || gameLoopJob?.isActive != true) {
            startGameLoop()
        }
    }

    fun setBoardDimensions(width: Float, height: Float, top: Float, customLauncherY: Float? = null) {
        if (width <= 10f || height <= 10f) return
        boardWidth = width
        boardHeight = height
        gridTop = top
        launcherX = width / 2f
        launcherY = customLauncherY ?: (height - 180f).coerceAtLeast(height * 0.65f)
    }

    fun navigateTo(screen: GameScreen) {
        soundEngine.playUiTap()
        _uiState.value = _uiState.value.copy(
            currentScreen = screen,
            totalCoins = saveManager.coins,
            selectedPlayLevel = saveManager.selectedPlayLevel,
            currentUnlockedLevel = saveManager.currentUnlockedLevel
        )
    }

    fun selectPlayLevel(level: Int) {
        val target = level.coerceIn(1, 300)
        saveManager.selectedPlayLevel = target
        _uiState.value = _uiState.value.copy(selectedPlayLevel = target)
        soundEngine.playUiTap()
    }

    fun resetToLevelOne() {
        saveManager.selectedPlayLevel = 1
        _uiState.value = _uiState.value.copy(selectedPlayLevel = 1)
        soundEngine.playUiTap()
    }

    fun equipSkinColour(id: Int) {
        saveManager.equippedSkinColourId = id
        _uiState.value = _uiState.value.copy(equippedSkinColourId = id)
        soundEngine.playUiTap()
    }

    fun buySkinColour(id: Int, price: Int): Boolean {
        if (price <= 0) return false // Anti-cheat: prevent 0 or negative price exploits
        if (saveManager.coins >= price && !saveManager.isSkinColourUnlocked(id)) {
            saveManager.coins -= price
            saveManager.unlockSkinColour(id)
            saveManager.equippedSkinColourId = id
            _uiState.value = _uiState.value.copy(
                equippedSkinColourId = id,
                totalCoins = saveManager.coins
            )
            soundEngine.playCoin()
            return true
        }
        return false
    }

    fun selectWorld(worldId: Int) {
        soundEngine.playUiTap()
        _uiState.value = _uiState.value.copy(selectedWorldId = worldId)
    }

    fun startLevel(levelNumber: Int, mode: GameMode = GameMode.CLASSIC) {
        val verifiedLevel = AntiCheatGuard.validateLevelSelect(levelNumber, saveManager.currentUnlockedLevel)
        val config = LevelDatabase.getLevel(verifiedLevel)
        saveManager.selectedPlayLevel = verifiedLevel
        shotsFiredCount = 0
        successfulHitsCount = 0
        totalBubblesPoppedCount = 0
        maxComboAchieved = 0

        // Reset grid
        grid = Array(physics.maxRows) { arrayOfNulls(physics.evenCols) }
        synchronized(entityLock) {
            fallingBubbles.clear()
            particles.clear()
            floatingTexts.clear()
        }

        var starCount = 0
        for (r in config.gridLayout.indices) {
            val rowList = config.gridLayout[r]
            val maxCols = physics.getColsForRow(r)
            for (c in 0 until maxCols) {
                if (c < rowList.size) {
                    val type = rowList[c]
                    if (type != null) {
                        grid[r][c] = BubbleCell(row = r, col = c, type = type)
                        if (type is BubbleType.StarTrapped) {
                            starCount++
                        }
                    }
                }
            }
        }

        val b1 = generateNextBubble(config)
        val b2 = generateNextBubble(config)

        val bossHp = config.boss?.maxHp ?: 0

        _uiState.value = _uiState.value.copy(
            currentScreen = GameScreen.PLAYING,
            levelConfig = config,
            gameMode = mode,
            score = 0,
            comboCount = 0,
            feverProgress = 0f,
            isFeverActive = false,
            shotsRemaining = if (mode == GameMode.ZEN) 999 else if (config.levelNumber <= 20) 10 else 20,
            timeRemainingSeconds = if (config.timeLimitSeconds > 0) config.timeLimitSeconds else 75,
            starsRescued = 0,
            totalStarsToRescue = starCount,
            mascotMood = MascotMood.IDLE,
            currentBubble = b1,
            nextBubble = b2,
            projectile = null,
            bossHp = bossHp,
            bossMaxHp = bossHp,
            bannerText = null,
            bannerSubtext = null,
            boosterUsedThisRound = false,
            selectedPlayLevel = levelNumber,
            currentUnlockedLevel = saveManager.currentUnlockedLevel,
            totalCoins = saveManager.coins
        )

        soundEngine.playLaunch()
    }

    private fun generateNextBubble(config: LevelConfig): BubbleType {
        val pool = config.allowedColors.ifEmpty { listOf(BubbleColor.ELECTRIC_BLUE, BubbleColor.NEON_RED) }
        return BubbleType.Normal(pool.random())
    }

    fun swapBubbles() {
        if (_uiState.value.projectile != null) return
        soundEngine.playPop(3)
        haptics.pop()
        val cur = _uiState.value.currentBubble
        val nxt = _uiState.value.nextBubble
        _uiState.value = _uiState.value.copy(
            currentBubble = nxt,
            nextBubble = cur
        )
    }

    fun applyBooster(type: BubbleType) {
        if (_uiState.value.projectile != null) return
        if (_uiState.value.boosterUsedThisRound) return
        soundEngine.playLaser()
        haptics.combo()
        _uiState.value = _uiState.value.copy(
            currentBubble = type,
            boosterUsedThisRound = true
        )
    }

    fun onAimDrag(touchX: Float, touchY: Float) {
        if (_uiState.value.projectile != null) return
        val dx = touchX - launcherX
        val dy = (touchY - launcherY).coerceAtMost(-15f)

        var angle = atan2(dy, dx)
        // Clamp and sanitize angle between -165 deg and -15 deg
        angle = AntiCheatGuard.sanitizeAimAngle(angle)

        val ray = physics.calculateTrajectory(
            startX = launcherX,
            startY = launcherY,
            angleRad = angle,
            boardWidth = boardWidth,
            gridTop = gridTop,
            grid = grid
        )

        _uiState.value = _uiState.value.copy(
            isAiming = true,
            aimAngleRad = angle,
            trajectory = ray,
            mascotMood = MascotMood.AIMING
        )
    }

    fun onTapShoot(touchX: Float, touchY: Float) {
        try {
            if (_uiState.value.projectile != null) return
            val dx = touchX - launcherX
            val dy = (touchY - launcherY).coerceAtMost(-15f)

            var angle = atan2(dy, dx)
            angle = AntiCheatGuard.sanitizeAimAngle(angle)

            _uiState.value = _uiState.value.copy(aimAngleRad = angle)
            shootBubble(angle)
        } catch (t: Throwable) {
            android.util.Log.e("GameViewModel", "onTapShoot safe recovery: ${t.message}", t)
        }
    }

    fun onAimRelease() {
        try {
            if (_uiState.value.projectile != null) return
            val angle = _uiState.value.aimAngleRad
            shootBubble(angle)
        } catch (t: Throwable) {
            android.util.Log.e("GameViewModel", "onAimRelease safe recovery: ${t.message}", t)
        }
    }

    private fun shootBubble(angleRad: Float) {
        val sanitizedAngle = AntiCheatGuard.sanitizeAimAngle(angleRad)
        // Anti-cheat: prevent firing without shots or double projectiles
        if (_uiState.value.gameMode != GameMode.ZEN && _uiState.value.shotsRemaining <= 0) return
        if (_uiState.value.projectile != null) return

        val speed = physics.getBubbleRadius(boardWidth) * 0.95f
        val vx = cos(sanitizedAngle) * speed
        val vy = sin(sanitizedAngle) * speed

        val projectile = ActiveProjectile(
            x = launcherX,
            y = launcherY,
            vx = vx,
            vy = vy,
            type = _uiState.value.currentBubble
        )

        shotsFiredCount++
        soundEngine.playLaunch()
        haptics.pop()

        val nextNext = generateNextBubble(_uiState.value.levelConfig)

        _uiState.value = _uiState.value.copy(
            isAiming = false,
            trajectory = null,
            projectile = projectile,
            currentBubble = _uiState.value.nextBubble,
            nextBubble = nextNext,
            shotsRemaining = (_uiState.value.shotsRemaining - 1).coerceAtLeast(0),
            mascotMood = MascotMood.IDLE
        )
    }

    // Central 60fps Loop with crash immunity
    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(Dispatchers.Default + crashHandler) {
            while (isActive) {
                try {
                    updatePhysics()
                } catch (t: Throwable) {
                    android.util.Log.e("GameViewModel", "Physics loop exception safely recovered: ${t.message}", t)
                }
                delay(16L) // ~60 FPS
            }
        }
    }

    private fun updatePhysics() {
        val state = _uiState.value
        if (state.currentScreen != GameScreen.PLAYING) return

        // Anti-cheat: clamp delta time to neutralize speed hacks
        val dt = AntiCheatGuard.clampDeltaTime(0.016f)
        val timeScale = (dt / 0.016f).coerceIn(0.5f, 2.0f)

        // 1. Update Projectile in flight
        val proj = state.projectile
        if (proj != null) {
            val radius = physics.getBubbleRadius(boardWidth)
            proj.x += proj.vx * timeScale
            proj.y += proj.vy * timeScale

            // Left/Right Wall Bounces
            if (proj.x <= radius) {
                proj.x = radius
                proj.vx = kotlin.math.abs(proj.vx)
                proj.bounces++
                soundEngine.playBounce()
                haptics.pop()
                spawnSparks(proj.x, proj.y, Color(0xFF00E5FF), 4)
            } else if (proj.x >= boardWidth - radius) {
                proj.x = boardWidth - radius
                proj.vx = -kotlin.math.abs(proj.vx)
                proj.bounces++
                soundEngine.playBounce()
                haptics.pop()
                spawnSparks(proj.x, proj.y, Color(0xFF00E5FF), 4)
            }

            // Check Ceiling Collision
            if (proj.y <= gridTop + radius) {
                val snap = physics.findClosestEmptyCell(proj.x, proj.y, radius, 0f, gridTop, grid)
                if (snap != null) {
                    onProjectileSnap(snap.first, snap.second, proj)
                } else {
                    _uiState.value = _uiState.value.copy(projectile = null)
                    checkLevelProgress()
                }
            } else if (proj.y < -100f || proj.y > boardHeight + 200f) {
                _uiState.value = _uiState.value.copy(projectile = null)
                checkLevelProgress()
            } else {
                // Check Bubble Collision
                val hit = physics.findHitBubble(proj.x, proj.y, radius, 0f, gridTop, grid)
                if (hit != null) {
                    val snap = physics.findBestSnapCell(hit.first, hit.second, proj.x, proj.y, radius, 0f, gridTop, grid)
                    if (snap != null) {
                        onProjectileSnap(snap.first, snap.second, proj)
                    } else {
                        _uiState.value = _uiState.value.copy(projectile = null)
                        checkLevelProgress()
                    }
                }
            }
        }

        synchronized(entityLock) {
            // 2. Update Falling detached bubbles (skip iterator allocation if empty)
            if (fallingBubbles.isNotEmpty()) {
                val fallIterator = fallingBubbles.iterator()
                while (fallIterator.hasNext()) {
                    val fb = fallIterator.next()
                    fb.vy += 0.85f * timeScale // gravity
                    fb.x += fb.vx * timeScale
                    fb.y += fb.vy * timeScale
                    fb.rotation += fb.vRot * timeScale
                    fb.alpha = (fb.alpha - 0.015f * timeScale).coerceAtLeast(0f)

                    if (fb.y > boardHeight + 100f || fb.alpha <= 0.01f) {
                        fallIterator.remove()
                    }
                }
            }

            // 3. Update Particles (skip iterator allocation if empty)
            if (particles.isNotEmpty()) {
                val pIter = particles.iterator()
                while (pIter.hasNext()) {
                    val p = pIter.next()
                    p.currentLife += 0.035f * timeScale
                    p.x += p.vx * timeScale
                    p.y += p.vy * timeScale
                    p.vy += 0.25f * timeScale // slight gravity
                    p.alpha = (1f - (p.currentLife / p.maxLife)).coerceIn(0f, 1f)
                    if (p.currentLife >= p.maxLife || p.alpha <= 0.01f) {
                        pIter.remove()
                    }
                }
            }

            // 4. Update Floating Texts (skip iterator allocation if empty)
            if (floatingTexts.isNotEmpty()) {
                val ftIter = floatingTexts.iterator()
                while (ftIter.hasNext()) {
                    val ft = ftIter.next()
                    ft.y += ft.vy * timeScale
                    ft.life -= 0.025f * timeScale
                    ft.alpha = ft.life.coerceIn(0f, 1f)
                    if (ft.life <= 0f) {
                        ftIter.remove()
                    }
                }
            }
        }

        // 5. Update Fever decay
        if (state.isFeverActive) {
            val newProgress = (state.feverProgress - 0.0025f).coerceAtLeast(0f)
            if (newProgress <= 0f) {
                _uiState.value = _uiState.value.copy(isFeverActive = false, feverProgress = 0f)
                soundEngine.setFeverMusic(false)
            } else {
                _uiState.value = _uiState.value.copy(feverProgress = newProgress)
            }
        }
    }

    private fun onProjectileSnap(row: Int, col: Int, proj: ActiveProjectile) {
        _uiState.value = _uiState.value.copy(projectile = null)
        if (row !in grid.indices || col !in grid[row].indices) {
            checkLevelProgress()
            return
        }
        val cell = BubbleCell(row = row, col = col, type = proj.type)
        grid[row][col] = cell

        val radius = physics.getBubbleRadius(boardWidth)
        val (cx, cy) = physics.getBubbleCenter(row, col, radius, 0f, gridTop)

        // Handle Special Bubble Interactions
        when (val t = proj.type) {
            is BubbleType.Bomb -> {
                soundEngine.playExplosion()
                haptics.explosion()
                spawnBombExplosion(row, col, cx, cy)
                return
            }
            is BubbleType.Lightning -> {
                soundEngine.playLaser()
                haptics.combo()
                spawnLightningClear(row, cx, cy)
                return
            }
            is BubbleType.Rainbow -> {
                // Rainbow clears touching group
                val neighbors = physics.getNeighbors(row, col)
                val targetColor = neighbors.mapNotNull {
                    if (it.first in grid.indices && it.second in grid[it.first].indices) {
                        when (val nt = grid[it.first][it.second]?.type) {
                            is BubbleType.Normal -> nt.color
                            else -> null
                        }
                    } else null
                }.firstOrNull() ?: BubbleColor.ELECTRIC_BLUE
                grid[row][col] = BubbleCell(row, col, BubbleType.Normal(targetColor))
            }
            is BubbleType.Freeze -> {
                soundEngine.playFreeze()
                haptics.combo()
                spawnFreezeShatter(row, col, cx, cy)
                return
            }
            else -> {}
        }

        // Standard Match Check
        val matches = physics.findMatches(row, col, grid)
        if (matches.size >= 3) {
            successfulHitsCount++
            val combo = _uiState.value.comboCount + 1
            maxComboAchieved = maxOf(maxComboAchieved, combo)
            val isTrickShot = proj.bounces >= 1
            val basePoints = matches.size * 120 * combo * (if (_uiState.value.isFeverActive) 2 else 1)

            soundEngine.playPop(combo)
            soundEngine.playCombo(combo)
            haptics.combo()

            // Pop matched cells
            for ((mr, mc) in matches) {
                val matchedCell = grid[mr][mc] ?: continue
                val (bx, by) = physics.getBubbleCenter(mr, mc, radius, 0f, gridTop)
                spawnPopParticles(bx, by, getBubblePrimaryColor(matchedCell.type))
                grid[mr][mc] = null
                totalBubblesPoppedCount++
            }

            // Check Detached / Orphan Clusters
            val floating = physics.findFloatingClusters(grid)
            var droppedCount = 0
            for ((fr, fc) in floating) {
                val orphan = grid[fr][fc] ?: continue
                val (ox, oy) = physics.getBubbleCenter(fr, fc, radius, 0f, gridTop)
                grid[fr][fc] = null
                droppedCount++
                totalBubblesPoppedCount++

                synchronized(entityLock) {
                    fallingBubbles.add(
                        FallingBubble(
                            x = ox,
                            y = oy,
                            vx = (random.nextFloat() * 8f - 4f),
                            vy = (random.nextFloat() * -3f - 2f),
                            rotation = 0f,
                            vRot = (random.nextFloat() * 0.15f - 0.075f),
                            type = orphan.type,
                            size = radius
                        )
                    )
                }
            }

            val orphanBonus = droppedCount * 250
            val totalEarned = basePoints + orphanBonus

            // Check Banners / Trick shot recognition
            var banner: String? = null
            var subtext: String? = null
            if (isTrickShot) {
                banner = "TRICK SHOT!"
                subtext = "+${totalEarned} PTS"
                saveManager.unlockAchievement("trick_shot")
            } else if (matches.size >= 6) {
                banner = "MEGA POP!"
                subtext = "+${totalEarned} PTS"
                saveManager.unlockAchievement("bubble_god")
            } else if (droppedCount >= 4) {
                banner = "CHAIN REACTION!"
                subtext = "${droppedCount} Bubbles Dropped!"
                saveManager.unlockAchievement("chain_master")
            }

            // Update Fever Meter
            val newFever = (_uiState.value.feverProgress + 0.25f).coerceIn(0f, 1f)
            val activatedFever = newFever >= 1f && !_uiState.value.isFeverActive
            if (activatedFever) {
                soundEngine.playFeverFanfare()
                soundEngine.setFeverMusic(true)
                saveManager.unlockAchievement("fever_mode")
            }

            // Floating Text
            synchronized(entityLock) {
                floatingTexts.add(
                    FloatingText(
                        text = "+$totalEarned",
                        x = cx,
                        y = cy,
                        color = Color(0xFFFFDE00)
                    )
                )
            }

            // Boss Damage
            var curBossHp = _uiState.value.bossHp
            if (curBossHp > 0) {
                curBossHp = (curBossHp - 1).coerceAtLeast(0)
            }

            _uiState.value = _uiState.value.copy(
                score = _uiState.value.score + totalEarned,
                comboCount = combo,
                feverProgress = if (activatedFever) 1f else newFever,
                isFeverActive = _uiState.value.isFeverActive || activatedFever,
                bossHp = curBossHp,
                mascotMood = if (combo >= 3 || activatedFever) MascotMood.EXCITED else MascotMood.HAPPY,
                bannerText = banner,
                bannerSubtext = subtext
            )

            saveManager.unlockAchievement("first_pop")

            // Check Win Condition
            checkLevelProgress()
        } else {
            // Missed match - combo resets
            soundEngine.playPop(0)
            _uiState.value = _uiState.value.copy(
                comboCount = 0,
                mascotMood = MascotMood.SHOCKED,
                bannerText = null,
                bannerSubtext = null
            )
            checkLevelProgress()
        }
    }

    private fun spawnBombExplosion(centerR: Int, centerC: Int, cx: Float, cy: Float) {
        val radius = physics.getBubbleRadius(boardWidth)
        val popped = mutableListOf<Pair<Int, Int>>()
        for (dr in -2..2) {
            for (dc in -2..2) {
                val nr = centerR + dr
                val nc = centerC + dc
                if (nr in grid.indices && nc in grid[nr].indices) {
                    if (grid[nr][nc] != null) {
                        popped.add(Pair(nr, nc))
                    }
                }
            }
        }

        for ((r, c) in popped) {
            val cell = if (r in grid.indices && c in grid[r].indices) grid[r][c] else null ?: continue
            val (bx, by) = physics.getBubbleCenter(r, c, radius, 0f, gridTop)
            spawnPopParticles(bx, by, Color(0xFFFF5252))
            grid[r][c] = null
            totalBubblesPoppedCount++
        }

        synchronized(entityLock) {
            floatingTexts.add(FloatingText("BOOM! +${popped.size * 200}", cx, cy, Color(0xFFFF5252)))
        }
        _uiState.value = _uiState.value.copy(
            score = _uiState.value.score + popped.size * 200,
            screenShake = 15f
        )
        checkLevelProgress()
    }

    private fun spawnLightningClear(row: Int, cx: Float, cy: Float) {
        if (row !in grid.indices) return
        val radius = physics.getBubbleRadius(boardWidth)
        var count = 0
        for (c in grid[row].indices) {
            val cell = grid[row][c]
            if (cell != null) {
                val (bx, by) = physics.getBubbleCenter(row, c, radius, 0f, gridTop)
                spawnPopParticles(bx, by, Color(0xFFFFDE00))
                grid[row][c] = null
                count++
                totalBubblesPoppedCount++
            }
        }
        synchronized(entityLock) {
            floatingTexts.add(FloatingText("LASER! +${count * 250}", cx, cy, Color(0xFFFFDE00)))
        }
        _uiState.value = _uiState.value.copy(
            score = _uiState.value.score + count * 250,
            screenShake = 12f
        )
        checkLevelProgress()
    }

    private fun spawnFreezeShatter(centerR: Int, centerC: Int, cx: Float, cy: Float) {
        val radius = physics.getBubbleRadius(boardWidth)
        val neighbors = physics.getNeighbors(centerR, centerC) + listOf(Pair(centerR, centerC))
        var count = 0
        for ((r, c) in neighbors) {
            if (r in grid.indices && c in grid[r].indices && grid[r][c] != null) {
                val (bx, by) = physics.getBubbleCenter(r, c, radius, 0f, gridTop)
                spawnPopParticles(bx, by, Color(0xFF00E5FF))
                grid[r][c] = null
                count++
                totalBubblesPoppedCount++
            }
        }
        synchronized(entityLock) {
            floatingTexts.add(FloatingText("FROST! +${count * 180}", cx, cy, Color(0xFF00E5FF)))
        }
        _uiState.value = _uiState.value.copy(score = _uiState.value.score + count * 180)
        checkLevelProgress()
    }

    private fun checkLevelProgress() {
        val config = _uiState.value.levelConfig
        val remainingBubbles = grid.flatten().filterNotNull()

        // Check Win Condition
        val isWon = when (config.objective) {
            LevelObjective.CLEAR_ALL -> remainingBubbles.isEmpty()
            LevelObjective.RESCUE_STARS -> remainingBubbles.none { it.type is BubbleType.StarTrapped }
            LevelObjective.SCORE_TARGET -> _uiState.value.score >= config.targetScore
            LevelObjective.BOSS_DEFEAT -> _uiState.value.bossHp <= 0
        }

        if (isWon) {
            onLevelWon()
            return
        }

        // Check Loss Condition (Out of shots in standard mode)
        if (_uiState.value.gameMode != GameMode.ZEN && _uiState.value.shotsRemaining <= 0) {
            onLevelLost()
        }
    }

    private fun onLevelWon() {
        val score = _uiState.value.score
        val config = _uiState.value.levelConfig
        val stars = when {
            score >= config.targetScore * 1.4f -> 3
            score >= config.targetScore -> 2
            else -> 1
        }
        val accuracy = if (shotsFiredCount > 0) ((successfulHitsCount.toFloat() / shotsFiredCount) * 100).toInt() else 100
        val coinsEarned = AntiCheatGuard.validateCoinReward("level_win", 10)

        saveManager.setLevelStars(config.levelNumber, stars)
        saveManager.setLevelHighScore(config.levelNumber, score)
        saveManager.coins += coinsEarned
        saveManager.totalXp += 200

        // Unlock next level up to 300
        if (config.levelNumber >= saveManager.currentUnlockedLevel && config.levelNumber < 300) {
            saveManager.currentUnlockedLevel = config.levelNumber + 1
        }
        if (config.levelNumber < 300) {
            saveManager.selectedPlayLevel = config.levelNumber + 1
        }

        if (config.levelNumber == 10 || config.levelNumber == 20 || config.levelNumber == 30) {
            saveManager.unlockAchievement("boss_slayer")
        }

        soundEngine.playLevelComplete()
        haptics.victory()

        _uiState.value = _uiState.value.copy(
            currentScreen = GameScreen.LEVEL_COMPLETE,
            levelCompletedStars = stars,
            levelCoinsEarned = coinsEarned,
            levelAccuracyPercent = accuracy,
            levelBestCombo = maxComboAchieved,
            levelBubblesPopped = totalBubblesPoppedCount,
            totalCoins = saveManager.coins,
            selectedPlayLevel = saveManager.selectedPlayLevel,
            currentUnlockedLevel = saveManager.currentUnlockedLevel,
            mascotMood = MascotMood.VICTORY
        )
    }

    private fun onLevelLost() {
        soundEngine.playPop(1)
        _uiState.value = _uiState.value.copy(
            currentScreen = GameScreen.LEVEL_FAILED,
            mascotMood = MascotMood.SHOCKED
        )
    }

    private fun getBubblePrimaryColor(type: BubbleType): Color {
        return when (type) {
            is BubbleType.Normal -> type.color.primaryColor
            is BubbleType.Rainbow -> Color(0xFFFF2E93)
            is BubbleType.Bomb -> Color(0xFFFF5252)
            is BubbleType.Lightning -> Color(0xFFFFDE00)
            is BubbleType.Prism -> Color(0xFF00E5FF)
            is BubbleType.Freeze -> Color(0xFF00E5FF)
            is BubbleType.StarTrapped -> Color(0xFFFFD700)
            else -> Color(0xFF00E5FF)
        }
    }

    private fun spawnPopParticles(x: Float, y: Float, color: Color) {
        val count = 12
        synchronized(entityLock) {
            for (i in 0 until count) {
                val angle = (2.0 * PI * i / count).toFloat()
                val speed = random.nextFloat() * 6f + 2f
                particles.add(
                    Particle(
                        x = x,
                        y = y,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        color = color,
                        size = random.nextFloat() * 5f + 3f,
                        alpha = 1f,
                        maxLife = 0.5f,
                        type = ParticleType.SPARK
                    )
                )
            }
        }
    }

    private fun spawnSparks(x: Float, y: Float, color: Color, count: Int) {
        synchronized(entityLock) {
            for (i in 0 until count) {
                particles.add(
                    Particle(
                        x = x,
                        y = y,
                        vx = (random.nextFloat() * 4f - 2f),
                        vy = (random.nextFloat() * -3f - 1f),
                        color = color,
                        size = 3f,
                        alpha = 1f,
                        maxLife = 0.35f,
                        type = ParticleType.SPARK
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        soundEngine.release()
    }
}
