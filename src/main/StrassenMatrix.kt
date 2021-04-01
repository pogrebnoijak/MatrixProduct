package main

class StrassenMatrix(matrix: AArray<Int>) : Matrix(matrix) {
    operator fun times(other: StrassenMatrix): StrassenMatrix {
        if (width != other.height)
            throw IllegalArgumentException("Matrix.times: wrong matrix dimensions")
        var scale = maxOf(height, width, other.width)
        scale = generateSequence(1) { it * 2 }.first { it >= scale }
        fun matrixOf (m: AArray<Int>) = matrixBuilder(scale, scale) { h: Int, w: Int ->
            m.getOrNull(h)?.getOrNull(w) ?: 0
        }
        val res = times(matrixOf(matrix), matrixOf(other.matrix), scale)
        return StrassenMatrix(matrixBuilder(height, other.width) { h: Int, w: Int -> res[h][w] })
    }

    override fun equals(other: Any?): Boolean {
        return when(other) {
            is StrassenMatrix   -> (0 until height).all { i -> matrix[i].contentEquals(other.matrix[i]) }
            else                -> false
        }
    }
}

private fun times(mat1: AArray<Int>, mat2: AArray<Int>, scale: Int): AArray<Int> {
    val newScale = scale/2

    fun sum(x1: Int, y1: Int, x2: Int, y2: Int, isM1: Boolean, op: (Int, Int) -> Int): AArray<Int>{
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
            else    -> -1
        }
        return times(
            sum(trans(a1.first), trans(a1.second), trans(a2.first), trans(a2.second), true, opA),
            sum(trans(b1.first), trans(b1.second), trans(b2.first), trans(b2.second), false, opB),
            newScale
        )
    }

    if (scale == 1) return arrayOf(arrayOf(mat1[0][0] * mat2[0][0]))
    if (scale == 2) return matrixBuilder(scale, scale) {
            h: Int, w: Int -> mat1[h][0] * mat2[0][w] + mat1[h][1] * mat2[1][w] }

    val m1 = getM(Pair(1,1), Pair(2,2), Pair(1,1), Pair(2,2))
    val m2 = getM(Pair(2,1), Pair(2,2), Pair(1,1), Pair(-1,-1))
    val m3 = getM(Pair(1,1), Pair(-1,-1), Pair(1,2), Pair(2,2), opB = Int::minus)
    val m4 = getM(Pair(2,2), Pair(-1,-1), Pair(2,1), Pair(1,1), opB = Int::minus)
    val m5 = getM(Pair(1,1), Pair(1,2), Pair(2,2), Pair(-1,-1))
    val m6 = getM(Pair(2,1), Pair(1,1), Pair(1,1), Pair(1,2), opA = Int::minus)
    val m7 = getM(Pair(1,2), Pair(2,2), Pair(2,1), Pair(2,2), opA = Int::minus)

    return matrixBuilder(scale, scale) { h: Int, w: Int ->
        when (Pair(h < newScale, w < newScale)) {
            true to true    -> m1[h][w] + m4[h][w] - m5[h][w] + m7[h][w]
            true to false   -> m3[h][w - newScale] + m5[h][w - newScale]
            false to true   -> m2[h - newScale][w] + m4[h - newScale][w]
            false to false  -> m1[h - newScale][w - newScale] - m2[h - newScale][w - newScale] +
                m3[h - newScale][w - newScale] + m6[h - newScale][w - newScale]
            else            -> throw RuntimeException("definitely won't happen")
        }
    }
}

//private fun timesFast(mat1: AArray<Int>, mat2: AArray<Int>, scale: Int): AArray<Int> {
//    // later
//    return mat1
//}