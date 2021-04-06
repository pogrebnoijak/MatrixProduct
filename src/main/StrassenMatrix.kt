package main

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class StrassenMatrix(matrix: AArray<Int>, private val nTreads: Int = 4) : Matrix(matrix) {
    operator fun times(other: StrassenMatrix): StrassenMatrix {
        if (width != other.height)
            throw IllegalArgumentException("Matrix.times: wrong matrix dimensions")
        var scale = maxOf(height, width, other.width)
        scale = generateSequence(1) { it * 2 }.first { it >= scale }
        fun matrixOf (m: AArray<Int>) = matrixBuilder(scale, scale) { h: Int, w: Int ->
            m.getOrNull(h)?.getOrNull(w) ?: 0
        }
        val res = times(matrixOf(matrix), matrixOf(other.matrix), scale, Executors.newFixedThreadPool(nTreads))
        return StrassenMatrix(matrixBuilder(height, other.width) { h: Int, w: Int -> res[h][w] })
    }

    override fun equals(other: Any?): Boolean {
        return when(other) {
            is StrassenMatrix   -> (0 until height).all { i -> matrix[i].contentEquals(other.matrix[i]) }
            else                -> false
        }
    }
}

private fun times(mat1: AArray<Int>, mat2: AArray<Int>, scale: Int, executors: ExecutorService? = null): AArray<Int> {
    val newScale = scale/2

    fun sum(x1: Int, y1: Int, x2: Int, y2: Int, isM1: Boolean, op: (Int, Int) -> Int): AArray<Int> {
        return when (Pair(isM1, x2 == -1 && y2 == -1)) {
            true to true    -> matrixBuilder(newScale, newScale) {
                    h: Int, w: Int -> mat1[h + x1][w + y1] }
            true to false   -> matrixBuilder(newScale, newScale) {
                    h: Int, w: Int -> op(mat1[h + x1][w + y1], mat1[h + x2][w + y2]) }
            false to true   -> matrixBuilder(newScale, newScale) {
                    h: Int, w: Int -> mat2[h + x1][w + y1] }
            false to false  -> matrixBuilder(newScale, newScale) {
                    h: Int, w: Int -> op(mat2[h + x1][w + y1], mat2[h + x2][w + y2]) }
            else            -> throw RuntimeException("definitely won't happen")
        }
    }

    fun getM(a1: Pair<Int, Int>, a2: Pair<Int, Int>, b1: Pair<Int, Int>, b2: Pair<Int, Int>,
             opA: (Int, Int) -> Int = Int::plus, opB: (Int, Int) -> Int = Int::plus): AArray<Int> {
        fun trans(a: Int) = when(a) {
            1       -> 0
            2       -> newScale
            else    -> -1 // no exists
        }
        return times(
            sum(trans(a1.first), trans(a1.second), trans(a2.first), trans(a2.second), true, opA),
            sum(trans(b1.first), trans(b1.second), trans(b2.first), trans(b2.second), false, opB),
            newScale
        )
    }

    if (scale <= 64) return matrixBuilder(scale, scale) { h: Int, w: Int -> // 64 optimal
        (0 until scale).sumBy { i -> mat1[h][i] * mat2[i][w] }
    }

    val m_i: AAArray<Int> = Array(7) { arrayOf() }
    if (executors != null) {
        executors.submit { m_i[0] = getM(Pair(1, 1), Pair(2, 2), Pair(1, 1), Pair(2, 2)) }
        executors.submit { m_i[1] = getM(Pair(2, 1), Pair(2, 2), Pair(1, 1), Pair(-1, -1)) }
        executors.submit { m_i[2] = getM(Pair(1, 1), Pair(-1, -1), Pair(1, 2), Pair(2, 2), opB = Int::minus) }
        executors.submit { m_i[3] = getM(Pair(2, 2), Pair(-1, -1), Pair(2, 1), Pair(1, 1), opB = Int::minus) }
        executors.submit { m_i[4] = getM(Pair(1, 1), Pair(1, 2), Pair(2, 2), Pair(-1, -1)) }
        executors.submit { m_i[5] = getM(Pair(2, 1), Pair(1, 1), Pair(1, 1), Pair(1, 2), opA = Int::minus) }
        executors.submit { m_i[6] = getM(Pair(1, 2), Pair(2, 2), Pair(2, 1), Pair(2, 2), opA = Int::minus) }
        executors.shutdown()
        executors.awaitTermination(1, TimeUnit.DAYS)
    }
    else {
        m_i[0] = getM(Pair(1, 1), Pair(2, 2), Pair(1, 1), Pair(2, 2))
        m_i[1] = getM(Pair(2, 1), Pair(2, 2), Pair(1, 1), Pair(-1, -1))
        m_i[2] = getM(Pair(1, 1), Pair(-1, -1), Pair(1, 2), Pair(2, 2), opB = Int::minus)
        m_i[3] = getM(Pair(2, 2), Pair(-1, -1), Pair(2, 1), Pair(1, 1), opB = Int::minus)
        m_i[4] = getM(Pair(1, 1), Pair(1, 2), Pair(2, 2), Pair(-1, -1))
        m_i[5] = getM(Pair(2, 1), Pair(1, 1), Pair(1, 1), Pair(1, 2), opA = Int::minus)
        m_i[6] = getM(Pair(1, 2), Pair(2, 2), Pair(2, 1), Pair(2, 2), opA = Int::minus)
    }

    return matrixBuilder(scale, scale) { h: Int, w: Int ->
        when (Pair(h < newScale, w < newScale)) {
            true to true    -> m_i[0][h][w] + m_i[3][h][w] - m_i[4][h][w] + m_i[6][h][w]
            true to false   -> m_i[2][h][w - newScale] + m_i[4][h][w - newScale]
            false to true   -> m_i[1][h - newScale][w] + m_i[3][h - newScale][w]
            false to false  -> m_i[0][h - newScale][w - newScale] - m_i[1][h - newScale][w - newScale] +
                m_i[2][h - newScale][w - newScale] + m_i[5][h - newScale][w - newScale]
            else            -> throw RuntimeException("definitely won't happen")
        }
    }
}
