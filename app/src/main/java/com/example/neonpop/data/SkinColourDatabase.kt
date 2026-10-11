package com.example.neonpop.data

import androidx.compose.ui.graphics.Color
import com.example.neonpop.model.SkinColourItem
import kotlin.math.abs
import kotlin.math.roundToInt

object SkinColourDatabase {

    private val colorFamilies = listOf(
        "Dragon Emerald", "Cyber Magenta", "Neon Rose", "Cotton Candy", "Blush Glow",
        "Electric Blue", "Aqua Spark", "Cyan Pulse", "Oceanic Breeze", "Skyline Ice",
        "Classic Lime", "Mint Sorbet", "Acid Lime", "Jade Velvet", "Pistachio Dream",
        "Cyber Gold", "Solar Amber", "Honey Glaze", "Sunburst Yellow", "Champagne Spark",
        "Violet Dusk", "Amethyst Aura", "Cosmic Orchid", "Lavender Mist", "Purple Nebula",
        "Crimson Flame", "Ruby Sparkle", "Coral Sunset", "Scarlet Velvet", "Peach Blossom",
        "Obsidian Silk", "Holo Platinum", "Pearl Shimmer", "Copper Gleam", "Bronze Radiance"
    )

    val ALL_SKIN_COLOURS: List<SkinColourItem> = (0 until 500).map { id ->
        if (id == 0) {
            SkinColourItem(
                id = 0,
                name = "Classic Bub Emerald",
                color = Color(0xFF25F05D),
                price = 30000
            )
        } else {
            // Golden angle hue distribution for 500 vibrant, diverse, beautiful colors
            val hue = ((id * 137.507764f) % 360f)
            val satTier = (id % 4)
            val saturation = 0.65f + satTier * 0.08f
            val lightTier = ((id / 4) % 5)
            val lightness = 0.38f + lightTier * 0.11f

            val rgb = hslToRgb(hue, saturation, lightness)
            val family = colorFamilies[id % colorFamilies.size]
            val shadeNumber = (id / colorFamilies.size) + 1

            SkinColourItem(
                id = id,
                name = "$family #$shadeNumber",
                color = Color(rgb.first, rgb.second, rgb.third),
                price = 30000
            )
        }
    }

    fun getSkinColour(id: Int): SkinColourItem {
        return ALL_SKIN_COLOURS.getOrNull(id) ?: ALL_SKIN_COLOURS.first()
    }

    private fun hslToRgb(h: Float, s: Float, l: Float): Triple<Float, Float, Float> {
        val c = (1f - abs(2f * l - 1f)) * s
        val x = c * (1f - abs(((h / 60f) % 2f) - 1f))
        val m = l - c / 2f

        val (rPrime, gPrime, bPrime) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Triple(
            (rPrime + m).coerceIn(0f, 1f),
            (gPrime + m).coerceIn(0f, 1f),
            (bPrime + m).coerceIn(0f, 1f)
        )
    }
}
