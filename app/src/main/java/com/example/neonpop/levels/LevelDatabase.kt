package com.example.neonpop.levels

import androidx.compose.ui.graphics.Color
import com.example.neonpop.model.BossConfig
import com.example.neonpop.model.BubbleColor
import com.example.neonpop.model.BubbleType
import com.example.neonpop.model.LevelConfig
import com.example.neonpop.model.LevelObjective

data class WorldTheme(
    val id: Int,
    val name: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundGradient: List<Color>,
    val particleTheme: String
)

object LevelDatabase {

    val WORLDS = listOf(
        WorldTheme(1, "Dream Garden", "Gentle blooming neon flora", Color(0xFF25F05D), Color(0xFFFF2E93), listOf(Color(0xFF071410), Color(0xFF0F2B1F), Color(0xFF05100B)), "petal_spark"),
        WorldTheme(2, "Neon City", "Cyberpunk alleys & bank-shot reflex tests", Color(0xFF00E5FF), Color(0xFFFFDE00), listOf(Color(0xFF0B0E26), Color(0xFF141940), Color(0xFF080B1E)), "cyber_grid"),
        WorldTheme(3, "Cloud Kingdom", "Floating islands & the Nebula Core Boss", Color(0xFFB5179E), Color(0xFF00E5FF), listOf(Color(0xFF1A092B), Color(0xFF2E124D), Color(0xFF11051D)), "star_dust"),
        WorldTheme(4, "Crystal Caverns", "Deep crystal blocks & chained geodes", Color(0xFFFF4081), Color(0xFF00E5FF), listOf(Color(0xFF180A1A), Color(0xFF2B1030), Color(0xFF100612)), "gem_shards"),
        WorldTheme(5, "Cyber Jungle", "Bioluminescent foliage & Sentinel Boss", Color(0xFF25F05D), Color(0xFFFF6D00), listOf(Color(0xFF081C14), Color(0xFF0F3627), Color(0xFF05120D)), "spore_glow"),
        WorldTheme(6, "Cosmic Arcade", "Hyper-speed kinetic retro-futuristic arcade", Color(0xFFFFDE00), Color(0xFFFF2E93), listOf(Color(0xFF1F0C26), Color(0xFF3B1549), Color(0xFF14071A)), "pixel_sparks"),
        WorldTheme(7, "Sunset Festival", "Twilight neon celebration with intense combos", Color(0xFFFF6D00), Color(0xFFFF4081), listOf(Color(0xFF290E1A), Color(0xFF4A182E), Color(0xFF1A0810)), "confetti_fire"),
        WorldTheme(8, "Glitch Galaxy", "Dimension warp & Grand Leviathan finale", Color(0xFF00E5FF), Color(0xFFB5179E), listOf(Color(0xFF050314), Color(0xFF130D36), Color(0xFF03010D)), "glitch_aurora")
    )

    fun getWorld(worldId: Int): WorldTheme {
        return WORLDS.find { it.id == worldId } ?: WORLDS.first()
    }

    private val allColors = BubbleColor.entries

    val ALL_LEVELS: List<LevelConfig> = (1..300).map { lvl ->
        generateLevel(lvl)
    }

    fun getLevel(levelNumber: Int): LevelConfig {
        return ALL_LEVELS.getOrNull(levelNumber - 1) ?: ALL_LEVELS.first()
    }

    // 16 Colorful Theme Presets in "Blind Type" style (high contrast, vivid neon stop bands)
    private fun getBlindBackgroundTheme(levelNumber: Int): Triple<List<Color>, Color, String> {
        val themes = listOf(
            Triple(listOf(Color(0xFF1D002B), Color(0xFF4A0072), Color(0xFF12001C)), Color(0xFF00E5FF), "Cyber Lilac Blind"),
            Triple(listOf(Color(0xFF2B0017), Color(0xFF880E4F), Color(0xFF15000B)), Color(0xFFFF4081), "Dragon Rose Blind"),
            Triple(listOf(Color(0xFF021B14), Color(0xFF004D40), Color(0xFF01120D)), Color(0xFF25F05D), "Emerald Neon Blind"),
            Triple(listOf(Color(0xFF2E1600), Color(0xFFE65100), Color(0xFF170800)), Color(0xFFFFDE00), "Solar Amber Blind"),
            Triple(listOf(Color(0xFF011A2E), Color(0xFF0D47A1), Color(0xFF000E1A)), Color(0xFF00F5D4), "Electric Ocean Blind"),
            Triple(listOf(Color(0xFF220038), Color(0xFF4A148C), Color(0xFF10001D)), Color(0xFFE056FD), "Cosmic Orchid Blind"),
            Triple(listOf(Color(0xFF2E0209), Color(0xFFB71C1C), Color(0xFF160004)), Color(0xFFFF2E93), "Crimson Blaze Blind"),
            Triple(listOf(Color(0xFF2E1202), Color(0xFFBF360C), Color(0xFF140600)), Color(0xFFFF6D00), "Sunset Glow Blind"),
            Triple(listOf(Color(0xFF02182B), Color(0xFF01579B), Color(0xFF000D18)), Color(0xFF00E5FF), "Glacial Cyan Blind"),
            Triple(listOf(Color(0xFF2B0924), Color(0xFF8E24AA), Color(0xFF140210)), Color(0xFFFF85C2), "Synthwave Pink Blind"),
            Triple(listOf(Color(0xFF00241E), Color(0xFF00695C), Color(0xFF00120E)), Color(0xFF25F05D), "Acid Matrix Blind"),
            Triple(listOf(Color(0xFF2B2000), Color(0xFFF57F17), Color(0xFF140F00)), Color(0xFFFFDE00), "Golden Mirage Blind"),
            Triple(listOf(Color(0xFF110729), Color(0xFF311B92), Color(0xFF080216)), Color(0xFF7209B7), "Starlight Void Blind"),
            Triple(listOf(Color(0xFF2B0324), Color(0xFFAD1457), Color(0xFF13000F)), Color(0xFFFFDE00), "Arcade Neon Blind"),
            Triple(listOf(Color(0xFF04241F), Color(0xFF004D40), Color(0xFF011310)), Color(0xFF64FFDA), "Mystic Tide Blind"),
            Triple(listOf(Color(0xFF1C002B), Color(0xFF6A1B9A), Color(0xFF0C0014)), Color(0xFF00E5FF), "Glitch Warp Blind")
        )
        return themes[(levelNumber - 1) % themes.size]
    }

    private fun generateLevel(levelNumber: Int): LevelConfig {
        val worldId = (((levelNumber - 1) / 38) + 1).coerceIn(1, 8)
        // Rule: first 20 levels have 10 shots; after 20 levels have 20 shots!
        val maxShots = if (levelNumber <= 20) 10 else 20

        val isBoss = levelNumber % 10 == 0
        val isRescue = !isBoss && (levelNumber % 4 == 3)
        val objective = when {
            isBoss -> LevelObjective.BOSS_DEFEAT
            isRescue -> LevelObjective.RESCUE_STARS
            levelNumber % 5 == 0 -> LevelObjective.SCORE_TARGET
            else -> LevelObjective.CLEAR_ALL
        }

        // Palette of 2 to 5 vibrant contrasting colors
        val colorCount = when {
            levelNumber <= 5 -> 2
            levelNumber <= 15 -> 3
            levelNumber <= 30 -> 4
            else -> 4
        }
        val palette = (0 until colorCount).map { allColors[(it * 2 + levelNumber) % allColors.size] }

        // Grid rows: between 4 and 7 rows
        val numRows = when {
            levelNumber <= 8 -> 4
            levelNumber <= 20 -> 5
            levelNumber <= 80 -> 6
            else -> 7
        }

        val (bgColors, accentColor, blindThemeName) = getBlindBackgroundTheme(levelNumber)
        val shapeArchetype = (levelNumber - 1) % 16
        val (grid, shapeName) = buildClusterShape(shapeArchetype, numRows, palette, isBoss, isRescue, levelNumber)

        val bossConfig: BossConfig? = if (isBoss) {
            val bossHp = (4 + (levelNumber / 20) * 2).coerceAtMost(20)
            BossConfig(
                name = "Core Titan #$levelNumber",
                maxHp = bossHp,
                currentHp = bossHp,
                anchorRows = listOf(1, 2),
                anchorCols = listOf(2, 3),
                subtitle = "Shatter outer barriers to defeat the core!"
            )
        } else null

        val targetScore = 1200 + levelNumber * 200

        return LevelConfig(
            levelNumber = levelNumber,
            worldId = worldId,
            title = if (isBoss) "Boss: $shapeName" else shapeName,
            objective = objective,
            maxShots = maxShots,
            targetScore = targetScore,
            allowedColors = palette,
            gridLayout = grid,
            boss = bossConfig,
            backgroundColors = bgColors,
            primaryAccent = accentColor,
            clusterShapeName = shapeName
        )
    }

    /**
     * Builds distinct geometrical cluster shapes for balloons.
     */
    private fun buildClusterShape(
        shapeType: Int,
        numRows: Int,
        palette: List<BubbleColor>,
        isBoss: Boolean,
        isRescue: Boolean,
        levelNumber: Int
    ): Pair<List<List<BubbleType?>>, String> {
        val grid = mutableListOf<List<BubbleType?>>()
        val shapeName: String

        // Function to determine whether cell (r, c) is active in this shape
        val isCellFilled: (Int, Int, Int) -> Boolean
        when (shapeType) {
            0 -> {
                // Heart Cluster
                shapeName = "Neon Heart"
                isCellFilled = { r, c, cols ->
                    when (r) {
                        0 -> c in 1..2 || c in (cols - 3)..(cols - 2)
                        1 -> c in 0..(cols - 1) && c != (cols / 2)
                        2 -> c in 0..(cols - 1)
                        3 -> c in 1..(cols - 2)
                        4 -> c in 2..(cols - 3)
                        else -> c in 3..(cols - 4)
                    }
                }
            }
            1 -> {
                // Diamond / Rhombus Cluster
                shapeName = "Crystal Diamond"
                val midR = numRows / 2
                isCellFilled = { r, c, cols ->
                    val distFromMid = kotlin.math.abs(r - midR)
                    val halfW = (cols / 2) - distFromMid + 1
                    c in (cols / 2 - halfW)..(cols / 2 + halfW)
                }
            }
            2 -> {
                // Crown / Trident Cluster
                shapeName = "Royal Crown"
                isCellFilled = { r, c, cols ->
                    when (r) {
                        0 -> c == 0 || c == 1 || c == (cols / 2) || c == (cols - 2) || c == (cols - 1)
                        1 -> c in 0..(cols - 1) && c != 2 && c != (cols - 3)
                        else -> c in 1..(cols - 2)
                    }
                }
            }
            3 -> {
                // Butterfly / Dual Wings Cluster
                shapeName = "Cyber Butterfly"
                isCellFilled = { r, c, cols ->
                    val centerCol = cols / 2
                    // Leaves a bank-shot air corridor in center
                    (c < centerCol - 1) || (c > centerCol)
                }
            }
            4 -> {
                // Chevron / V-Formation
                shapeName = "V-Chevron"
                isCellFilled = { r, c, cols ->
                    val vColLeft = r
                    val vColRight = cols - 1 - r
                    c in (vColLeft - 1)..(vColLeft + 1) || c in (vColRight - 1)..(vColRight + 1) || (r == 0)
                }
            }
            5 -> {
                // Twin Pillars & Arch Cluster
                shapeName = "Twin Pillars"
                isCellFilled = { r, c, cols ->
                    (r == 0) || (c in 0..1) || (c in (cols - 2)..(cols - 1)) || (r == 1 && c in 2..(cols - 3))
                }
            }
            6 -> {
                // Hex-Ring / Honeycomb Hollow Core
                shapeName = "Honeycomb Ring"
                isCellFilled = { r, c, cols ->
                    if (r in 1..2 && c in 2..(cols - 3)) false
                    else c in 1..(cols - 2) || r == 0
                }
            }
            7 -> {
                // Hourglass Cluster
                shapeName = "Neon Hourglass"
                val midR = numRows / 2
                isCellFilled = { r, c, cols ->
                    if (r == midR) c in (cols / 2 - 1)..(cols / 2 + 1)
                    else true
                }
            }
            8 -> {
                // Lotus / Starburst Cluster
                shapeName = "Solar Lotus"
                isCellFilled = { r, c, cols ->
                    val symC = if (c < cols / 2) c else (cols - 1 - c)
                    (symC + r) % 2 == 0 || r == 0 || r == 1
                }
            }
            9 -> {
                // Zigzag Cascade Cluster
                shapeName = "Zigzag Bolt"
                isCellFilled = { r, c, cols ->
                    val shift = (r * 2) % (cols - 2)
                    c in shift..(shift + 3) || r == 0
                }
            }
            10 -> {
                // Pyramid Cluster
                shapeName = "Cyber Pyramid"
                isCellFilled = { r, c, cols ->
                    c in r..(cols - 1 - r)
                }
            }
            11 -> {
                // Inverted Chandelier Cluster
                shapeName = "Chandelier"
                isCellFilled = { r, c, cols ->
                    (r == 0) || (r % 2 == 1 && c in 1..(cols - 2)) || (r % 2 == 0 && (c == 1 || c == cols / 2 || c == cols - 2))
                }
            }
            12 -> {
                // Island Archipelago Cluster (3 separate islands)
                shapeName = "Triple Islands"
                isCellFilled = { r, c, cols ->
                    (c in 0..1) || (c in (cols / 2 - 1)..(cols / 2)) || (c in (cols - 2)..(cols - 1))
                }
            }
            13 -> {
                // Fortress Shield Cluster
                shapeName = "Fortress Shield"
                isCellFilled = { r, c, cols ->
                    when (r) {
                        0 -> true
                        1 -> c in 0..(cols - 1)
                        2 -> c in 1..(cols - 2)
                        3 -> c in 1..(cols - 2)
                        else -> c in 2..(cols - 3)
                    }
                }
            }
            14 -> {
                // Cross / Nova Cluster
                shapeName = "Nova Cross"
                isCellFilled = { r, c, cols ->
                    r == 0 || r == 2 || c in (cols / 2 - 1)..(cols / 2 + 1)
                }
            }
            else -> {
                // Serpentine Wave Cluster
                shapeName = "Serpentine Wave"
                isCellFilled = { r, c, cols ->
                    val waveCenter = ((cols / 2) + ((r % 2) * 2 - 1) * 2).coerceIn(1, cols - 2)
                    r == 0 || c in (waveCenter - 2)..(waveCenter + 2)
                }
            }
        }

        // Fill grid rows with bubbles according to geometry and color patterns
        for (r in 0 until numRows) {
            val cols = if (r % 2 == 0) 8 else 7
            val rowItems = mutableListOf<BubbleType?>()

            for (c in 0 until cols) {
                val filled = isCellFilled(r, c, cols)
                if (!filled) {
                    rowItems.add(null)
                    continue
                }

                val symCol = if (c < cols / 2) c else (cols - 1 - c)
                // Color pattern: alternating rings or stripes
                val colorIdx = when {
                    r == 0 -> (levelNumber) % palette.size
                    c == 0 || c == cols - 1 -> (levelNumber + 1) % palette.size
                    else -> (symCol + r + levelNumber) % palette.size
                }
                val baseNormal = BubbleType.Normal(palette[colorIdx])

                val cellItem: BubbleType? = when {
                    isBoss && r in 1..2 && c in 2..4 -> {
                        if (r == 1 && c == 3) BubbleType.BlackVoid
                        else BubbleType.Crystal(hitsLeft = 2)
                    }
                    isRescue && r == 1 && (c == 2 || c == cols - 3) -> BubbleType.StarTrapped
                    // Strategic booster surprises
                    (r == 2 || r == 3) && c == cols / 2 && levelNumber >= 5 && (levelNumber % 3 == 0) -> BubbleType.Bomb
                    r == 1 && c == 1 && levelNumber >= 8 && (levelNumber % 4 == 0) -> BubbleType.Lightning
                    r == 2 && symCol == 0 && levelNumber >= 12 && (levelNumber % 5 == 0) -> BubbleType.Crystal(hitsLeft = 2)
                    r == 0 && symCol == 1 && levelNumber >= 15 && (levelNumber % 6 == 0) -> BubbleType.Rainbow
                    r == 3 && symCol == 1 && levelNumber >= 20 && (levelNumber % 7 == 0) -> BubbleType.Prism
                    r == 2 && symCol == 2 && levelNumber >= 25 && (levelNumber % 8 == 0) -> BubbleType.Freeze
                    else -> baseNormal
                }
                rowItems.add(cellItem)
            }
            grid.add(rowItems)
        }

        return Pair(grid, "$shapeName #$levelNumber")
    }
}
