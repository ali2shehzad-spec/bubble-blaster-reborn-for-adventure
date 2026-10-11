package com.example

import com.example.neonpop.data.SkinColourDatabase
import com.example.neonpop.levels.LevelDatabase
import com.example.neonpop.model.BubbleCell
import com.example.neonpop.model.BubbleColor
import com.example.neonpop.model.BubbleType
import com.example.neonpop.physics.HexGridPhysics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testLevelDatabase300LevelsAndShotLimits() {
        assertEquals(300, LevelDatabase.ALL_LEVELS.size)
        assertEquals(8, LevelDatabase.WORLDS.size)

        for (i in 1..300) {
            val level = LevelDatabase.getLevel(i)
            assertEquals(i, level.levelNumber)
            assertTrue(level.gridLayout.isNotEmpty())

            if (i <= 20) {
                assertEquals("Level $i should have 10 shots", 10, level.maxShots)
            } else {
                assertEquals("Level $i should have 20 shots", 20, level.maxShots)
            }
        }
    }

    @Test
    fun testSkinColourDatabase500Colours() {
        assertEquals(500, SkinColourDatabase.ALL_SKIN_COLOURS.size)
        val defaultColour = SkinColourDatabase.getSkinColour(0)
        assertEquals("Classic Bub Emerald", defaultColour.name)
        assertEquals(30000, defaultColour.price)

        val colour500 = SkinColourDatabase.getSkinColour(499)
        assertEquals(499, colour500.id)
        assertEquals(30000, colour500.price)
    }

    @Test
    fun testHexGridGeometryAndNeighbors() {
        val physics = HexGridPhysics(maxRows = 16, evenCols = 8, oddCols = 7)
        assertEquals(8, physics.getColsForRow(0))
        assertEquals(7, physics.getColsForRow(1))

        // Check even row neighbors
        val evenNeighbors = physics.getNeighbors(2, 2)
        assertEquals(6, evenNeighbors.size)

        // Check odd row neighbors
        val oddNeighbors = physics.getNeighbors(1, 2)
        assertEquals(6, oddNeighbors.size)

        // Top ceiling row neighbors
        val topNeighbors = physics.getNeighbors(0, 0)
        assertEquals(2, topNeighbors.size)
    }

    @Test
    fun testMatch3FloodFill() {
        val physics = HexGridPhysics(maxRows = 10, evenCols = 8, oddCols = 7)
        val grid: Array<Array<BubbleCell?>> = Array(10) { arrayOfNulls(8) }

        val redType = BubbleType.Normal(BubbleColor.NEON_RED)
        grid[0][0] = BubbleCell(0, 0, redType)
        grid[0][1] = BubbleCell(0, 1, redType)
        grid[1][0] = BubbleCell(1, 0, redType)

        val matches = physics.findMatches(0, 0, grid)
        assertEquals(3, matches.size)
    }

    @Test
    fun testFloatingClusterDetection() {
        val physics = HexGridPhysics(maxRows = 10, evenCols = 8, oddCols = 7)
        val grid: Array<Array<BubbleCell?>> = Array(10) { arrayOfNulls(8) }

        val blue = BubbleType.Normal(BubbleColor.ELECTRIC_BLUE)
        // Row 0 anchor
        grid[0][0] = BubbleCell(0, 0, blue)

        // Isolated bubble in row 3
        grid[3][3] = BubbleCell(3, 3, blue)

        val floating = physics.findFloatingClusters(grid)
        assertEquals(1, floating.size)
        assertTrue(floating.contains(Pair(3, 3)))
    }
}
