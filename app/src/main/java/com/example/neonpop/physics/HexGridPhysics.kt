package com.example.neonpop.physics

import com.example.neonpop.model.ActiveProjectile
import com.example.neonpop.model.BubbleCell
import com.example.neonpop.model.BubbleColor
import com.example.neonpop.model.BubbleType
import com.example.neonpop.model.TrajectoryPoint
import com.example.neonpop.model.TrajectoryRay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class HexGridPhysics(
    val maxRows: Int = 18,
    val evenCols: Int = 8,
    val oddCols: Int = 7
) {
    // Row height factor for hexagon close packing
    val rowHeightFactor = sqrt(3.0).toFloat() // ~1.732f

    fun getColsForRow(row: Int): Int {
        return if (row % 2 == 0) evenCols else oddCols
    }

    fun getBubbleRadius(boardWidth: Float): Float {
        // 8 bubbles across = 16 * R
        return boardWidth / (evenCols * 2f)
    }

    fun getRowHeight(radius: Float): Float {
        return radius * rowHeightFactor
    }

    fun getCellSafe(grid: Array<Array<BubbleCell?>>, r: Int, c: Int): BubbleCell? {
        if (r in grid.indices && c in grid[r].indices) {
            return grid[r][c]
        }
        return null
    }

    fun getBubbleCenter(row: Int, col: Int, radius: Float, gridLeft: Float, gridTop: Float): Pair<Float, Float> {
        val y = gridTop + radius + row * getRowHeight(radius)
        val x = if (row % 2 == 0) {
            gridLeft + radius + col * (2f * radius)
        } else {
            gridLeft + (2f * radius) + col * (2f * radius)
        }
        return Pair(x, y)
    }

    /**
     * Finds adjacent neighbor coordinates in hexagonal grid.
     */
    fun getNeighbors(row: Int, col: Int): List<Pair<Int, Int>> {
        val neighbors = mutableListOf<Pair<Int, Int>>()
        val isEven = row % 2 == 0

        val deltas = if (isEven) {
            listOf(
                Pair(0, -1), Pair(0, 1),
                Pair(-1, -1), Pair(-1, 0),
                Pair(1, -1), Pair(1, 0)
            )
        } else {
            listOf(
                Pair(0, -1), Pair(0, 1),
                Pair(-1, 0), Pair(-1, 1),
                Pair(1, 0), Pair(1, 1)
            )
        }

        for ((dr, dc) in deltas) {
            val nr = row + dr
            val nc = col + dc
            if (nr in 0 until maxRows && nc in 0 until getColsForRow(nr)) {
                neighbors.add(Pair(nr, nc))
            }
        }
        return neighbors
    }

    /**
     * Calculates trajectory with wall bounces and target cell prediction.
     */
    fun calculateTrajectory(
        startX: Float,
        startY: Float,
        angleRad: Float,
        boardWidth: Float,
        gridTop: Float,
        grid: Array<Array<BubbleCell?>>,
        maxBounces: Int = 3
    ): TrajectoryRay {
        val radius = getBubbleRadius(boardWidth)
        val leftWall = radius
        val rightWall = boardWidth - radius
        val ceilingY = gridTop + radius

        val points = mutableListOf<TrajectoryPoint>()
        points.add(TrajectoryPoint(startX, startY))

        var curX = startX
        var curY = startY
        var vx = cos(angleRad)
        var vy = sin(angleRad)

        // Safety check to ensure shooting upwards
        if (vy > -0.05f) {
            vy = -0.99f
            vx = 0.05f
        }

        var bounces = 0
        var targetRow = -1
        var targetCol = -1
        var willSnap = false

        val stepDist = radius * 0.4f
        val maxSteps = 450
        var steps = 0

        while (steps < maxSteps && bounces <= maxBounces) {
            steps++
            curX += vx * stepDist
            curY += vy * stepDist

            // Left wall bounce
            if (curX <= leftWall) {
                curX = leftWall
                vx = abs(vx)
                bounces++
                points.add(TrajectoryPoint(curX, curY))
            }
            // Right wall bounce
            else if (curX >= rightWall) {
                curX = rightWall
                vx = -abs(vx)
                bounces++
                points.add(TrajectoryPoint(curX, curY))
            }

            // Ceiling collision
            if (curY <= ceilingY) {
                curY = ceilingY
                points.add(TrajectoryPoint(curX, curY))
                // Find closest slot in row 0
                val snap = findClosestEmptyCell(curX, curY, radius, 0f, gridTop, grid)
                if (snap != null) {
                    targetRow = snap.first
                    targetCol = snap.second
                    willSnap = true
                }
                break
            }

            // Check collision with existing active grid bubbles
            val hitCell = findHitBubble(curX, curY, radius, 0f, gridTop, grid)
            if (hitCell != null) {
                points.add(TrajectoryPoint(curX, curY))
                val snap = findBestSnapCell(hitCell.first, hitCell.second, curX, curY, radius, 0f, gridTop, grid)
                if (snap != null) {
                    targetRow = snap.first
                    targetCol = snap.second
                    willSnap = true
                }
                break
            }
        }

        if (points.size == 1) {
            points.add(TrajectoryPoint(curX, curY))
        }

        return TrajectoryRay(points, targetRow, targetCol, willSnap, bounces)
    }

    /**
     * Checks if projectile collides with any cell in grid.
     */
    fun findHitBubble(
        px: Float,
        py: Float,
        radius: Float,
        gridLeft: Float,
        gridTop: Float,
        grid: Array<Array<BubbleCell?>>
    ): Pair<Int, Int>? {
        val hitDistanceSq = (radius * 1.85f) * (radius * 1.85f)
        for (r in grid.indices) {
            val cols = getColsForRow(r).coerceAtMost(grid[r].size)
            for (c in 0 until cols) {
                val cell = grid[r][c] ?: continue
                if (cell.isPopping || cell.isFalling) continue
                val (bx, by) = getBubbleCenter(r, c, radius, gridLeft, gridTop)
                val dx = px - bx
                val dy = py - by
                if (dx * dx + dy * dy <= hitDistanceSq) {
                    return Pair(r, c)
                }
            }
        }
        return null
    }

    /**
     * Finds the best adjacent empty neighbor to snap to.
     */
    fun findBestSnapCell(
        hitRow: Int,
        hitCol: Int,
        px: Float,
        py: Float,
        radius: Float,
        gridLeft: Float,
        gridTop: Float,
        grid: Array<Array<BubbleCell?>>
    ): Pair<Int, Int>? {
        val neighbors = getNeighbors(hitRow, hitCol)
        var bestPair: Pair<Int, Int>? = null
        var bestDistSq = Float.MAX_VALUE

        for ((nr, nc) in neighbors) {
            if (nr in grid.indices && nc in grid[nr].indices && grid[nr][nc] == null) {
                val (cx, cy) = getBubbleCenter(nr, nc, radius, gridLeft, gridTop)
                val dx = px - cx
                val dy = py - cy
                val distSq = dx * dx + dy * dy
                if (distSq < bestDistSq) {
                    bestDistSq = distSq
                    bestPair = Pair(nr, nc)
                }
            }
        }

        // If all neighbors are occupied, search all empty slots
        return bestPair ?: findClosestEmptyCell(px, py, radius, gridLeft, gridTop, grid)
    }

    fun findClosestEmptyCell(
        px: Float,
        py: Float,
        radius: Float,
        gridLeft: Float,
        gridTop: Float,
        grid: Array<Array<BubbleCell?>>
    ): Pair<Int, Int>? {
        var bestPair: Pair<Int, Int>? = null
        var bestDistSq = Float.MAX_VALUE

        for (r in grid.indices) {
            val cols = getColsForRow(r).coerceAtMost(grid[r].size)
            for (c in 0 until cols) {
                if (grid[r][c] == null) {
                    // Must be connected to ceiling or to another bubble
                    val isAtCeiling = r == 0
                    val hasNeighbor = getNeighbors(r, c).any { (nr, nc) ->
                        nr in grid.indices && nc in grid[nr].indices && grid[nr][nc] != null
                    }
                    if (isAtCeiling || hasNeighbor) {
                        val (cx, cy) = getBubbleCenter(r, c, radius, gridLeft, gridTop)
                        val dx = px - cx
                        val dy = py - cy
                        val distSq = dx * dx + dy * dy
                        if (distSq < bestDistSq) {
                            bestDistSq = distSq
                            bestPair = Pair(r, c)
                        }
                    }
                }
            }
        }
        return bestPair
    }

    /**
     * Flood-fill search for matching bubbles.
     */
    fun findMatches(
        startRow: Int,
        startCol: Int,
        grid: Array<Array<BubbleCell?>>
    ): Set<Pair<Int, Int>> {
        val startCell = getCellSafe(grid, startRow, startCol) ?: return emptySet()
        val targetColor = when (val t = startCell.type) {
            is BubbleType.Normal -> t.color
            is BubbleType.Paint -> t.color
            is BubbleType.Chain -> if (!t.locked) t.color else null
            else -> null
        }

        val visited = mutableSetOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(Pair(startRow, startCol))
        visited.add(Pair(startRow, startCol))

        while (queue.isNotEmpty()) {
            val (r, c) = queue.removeFirst()
            for ((nr, nc) in getNeighbors(r, c)) {
                val neighborCell = getCellSafe(grid, nr, nc) ?: continue
                if (visited.contains(Pair(nr, nc))) continue
                if (neighborCell.isPopping || neighborCell.isFalling) continue

                val isMatch = when (val nt = neighborCell.type) {
                    is BubbleType.Normal -> targetColor == null || nt.color == targetColor
                    is BubbleType.Rainbow -> true
                    is BubbleType.Prism -> true
                    is BubbleType.Paint -> targetColor == null || nt.color == targetColor
                    is BubbleType.Chain -> !nt.locked && (targetColor == null || nt.color == targetColor)
                    else -> false
                }

                if (isMatch) {
                    visited.add(Pair(nr, nc))
                    queue.add(Pair(nr, nc))
                }
            }
        }

        return visited
    }

    /**
     * Finds floating orphan bubbles that have no path to ceiling (row 0).
     */
    fun findFloatingClusters(grid: Array<Array<BubbleCell?>>): Set<Pair<Int, Int>> {
        val connectedToCeiling = mutableSetOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()

        if (grid.isEmpty()) return emptySet()

        // Enqueue all active bubbles in row 0
        val topCols = getColsForRow(0).coerceAtMost(grid[0].size)
        for (c in 0 until topCols) {
            val cell = grid[0][c]
            if (cell != null && !cell.isPopping && !cell.isFalling) {
                connectedToCeiling.add(Pair(0, c))
                queue.add(Pair(0, c))
            }
        }

        // BFS traversal through all connected neighbors
        while (queue.isNotEmpty()) {
            val (r, c) = queue.removeFirst()
            for ((nr, nc) in getNeighbors(r, c)) {
                val neighbor = getCellSafe(grid, nr, nc)
                if (neighbor != null && !neighbor.isPopping && !neighbor.isFalling) {
                    if (connectedToCeiling.add(Pair(nr, nc))) {
                        queue.add(Pair(nr, nc))
                    }
                }
            }
        }

        // Any active bubble not connected to ceiling is floating!
        val floating = mutableSetOf<Pair<Int, Int>>()
        for (r in grid.indices) {
            val cols = getColsForRow(r).coerceAtMost(grid[r].size)
            for (c in 0 until cols) {
                val cell = grid[r][c]
                if (cell != null && !cell.isPopping && !cell.isFalling) {
                    if (!connectedToCeiling.contains(Pair(r, c))) {
                        floating.add(Pair(r, c))
                    }
                }
            }
        }
        return floating
    }
}
