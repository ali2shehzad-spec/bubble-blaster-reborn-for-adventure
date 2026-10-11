package com.example.neonpop.model

import androidx.compose.ui.graphics.Color

/**
 * 7 Vibrant Neon Colors with high aesthetic polish for Gen Z 2026.
 */
enum class BubbleColor(
    val displayName: String,
    val primaryColor: Color,
    val highlightColor: Color,
    val glowColor: Color,
    val symbolCode: String // For accessibility colorblind patterns
) {
    NEON_RED("Neon Red", Color(0xFFFF2E93), Color(0xFFFF85C2), Color(0x66FF2E93), "♥"),
    ELECTRIC_BLUE("Electric Blue", Color(0xFF00E5FF), Color(0xFF80F2FF), Color(0x6600E5FF), "◆"),
    ACID_GREEN("Acid Green", Color(0xFF25F05D), Color(0xFF8CF8AC), Color(0x6625F05D), "▲"),
    CYBER_YELLOW("Cyber Yellow", Color(0xFFFFDE00), Color(0xFFFFF07A), Color(0x66FFDE00), "★"),
    NEON_PURPLE("Neon Purple", Color(0xFFB5179E), Color(0xFFDF74CE), Color(0x66B5179E), "●"),
    SOLAR_ORANGE("Solar Orange", Color(0xFFFF6D00), Color(0xFFFFAA66), Color(0x66FF6D00), "✦"),
    DREAM_PINK("Dream Pink", Color(0xFFFF4081), Color(0xFFFF99BE), Color(0x66FF4081), "✿");

    companion object {
        fun random(colors: List<BubbleColor> = entries): BubbleColor {
            return colors.random()
        }
    }
}

/**
 * Bubble types including normal and power-up varieties.
 */
sealed class BubbleType {
    data class Normal(val color: BubbleColor) : BubbleType()
    data object Rainbow : BubbleType() // Matches any color
    data object Bomb : BubbleType() // Explodes 2-radius
    data object Lightning : BubbleType() // Clears full row
    data object Prism : BubbleType() // Adaptable chameleon
    data object BlackHole : BubbleType() // Gravitational pull and implode
    data object Freeze : BubbleType() // Shatters adjacent
    data class Paint(val color: BubbleColor) : BubbleType() // Repaints adjacent
    data object Meteor : BubbleType() // Penetrates
    data object Ghost : BubbleType() // Passes through 1st layer

    // Obstacles
    data class Crystal(val hitsLeft: Int = 2) : BubbleType() // 2 hits to break
    data class Chain(val color: BubbleColor, val locked: Boolean = true) : BubbleType()
    data object BlackVoid : BubbleType() // Indestructible except by dropping or bomb
    data object Mystery : BubbleType() // Transforms on hit
    data class Glitch(val colors: List<BubbleColor>, val activeIndex: Int = 0) : BubbleType()
    data object StarTrapped : BubbleType() // Objective to rescue
}

/**
 * Grid cell holding a bubble with animation attributes.
 */
data class BubbleCell(
    val row: Int,
    val col: Int,
    val type: BubbleType,
    val animScale: Float = 1f,
    val pulseAlpha: Float = 1f,
    val isPopping: Boolean = false,
    val isFalling: Boolean = false
)

/**
 * Trajectory segment representation for aim laser.
 */
data class TrajectoryPoint(
    val x: Float,
    val y: Float
)

data class TrajectoryRay(
    val points: List<TrajectoryPoint>,
    val targetRow: Int = -1,
    val targetCol: Int = -1,
    val willSnap: Boolean = false,
    val bounces: Int = 0
)

/**
 * Projectile in flight.
 */
data class ActiveProjectile(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val type: BubbleType,
    val isPenetrating: Boolean = false,
    var bounces: Int = 0
)

/**
 * Falling detached bubble physics object.
 */
data class FallingBubble(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var vRot: Float,
    val type: BubbleType,
    var alpha: Float = 1f,
    var size: Float
)

/**
 * Particle for neon pops, sparks, and stars.
 */
enum class ParticleType { SPARK, GLOW_RING, STAR, CONFETTI, SMOKE }

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var size: Float,
    var alpha: Float,
    val maxLife: Float,
    var currentLife: Float = 0f,
    val type: ParticleType = ParticleType.SPARK
)

/**
 * Floating feedback texts like "TRICK SHOT!", "+500", "x3 COMBO".
 */
data class FloatingText(
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var scale: Float = 1f,
    var alpha: Float = 1f,
    var vy: Float = -1.8f,
    var life: Float = 1f
)

/**
 * Mascot moods.
 */
enum class MascotMood {
    IDLE,
    AIMING,
    HAPPY,
    EXCITED,
    SHOCKED,
    SLEEPY,
    VICTORY,
    FEVER
}

/**
 * Game Modes.
 */
enum class GameMode {
    CLASSIC,
    TIME_ATTACK,
    LIMITED_SHOTS,
    ZEN,
    DAILY,
    BOSS
}

/**
 * Level objective.
 */
enum class LevelObjective {
    CLEAR_ALL,
    RESCUE_STARS,
    SCORE_TARGET,
    BOSS_DEFEAT
}

/**
 * Boss specifications.
 */
data class BossConfig(
    val name: String,
    val maxHp: Int,
    var currentHp: Int,
    val anchorRows: List<Int>,
    val anchorCols: List<Int>,
    val subtitle: String
)

/**
 * Level description.
 */
data class LevelConfig(
    val levelNumber: Int,
    val worldId: Int,
    val title: String,
    val objective: LevelObjective,
    val maxShots: Int,
    val targetScore: Int,
    val allowedColors: List<BubbleColor>,
    val gridLayout: List<List<BubbleType?>>,
    val boss: BossConfig? = null,
    val timeLimitSeconds: Int = 0,
    val backgroundColors: List<Color> = listOf(Color(0xFF0C0721), Color(0xFF1B0D38), Color(0xFF070414)),
    val primaryAccent: Color = Color(0xFF00E5FF),
    val clusterShapeName: String = "Classic"
)

data class SkinColourItem(
    val id: Int,
    val name: String,
    val color: Color,
    val price: Int = 30000
)

/**
 * App Navigation Screens.
 */
enum class GameScreen {
    HOME,
    WORLD_MAP,
    PLAYING,
    PAUSED,
    LEVEL_COMPLETE,
    LEVEL_FAILED,
    DAILY_CHALLENGE,
    COSMETICS,
    SKIN_COLOUR,
    ACHIEVEMENTS,
    SETTINGS
}
