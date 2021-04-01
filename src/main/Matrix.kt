package main

typealias AArray<T> = Array<Array<T>>

open class Matrix(val width: Int, val height: Int, val matrix: AArray<Int>) {
    init {
        if (matrix.any { it.size != width })
            throw IllegalArgumentException("Matrix: not a matrix")
    }

    constructor(matrix: AArray<Int>) : this(matrix.getOrNull(0)?.size ?: 0, matrix.size, matrix)

    operator fun times(other: Matrix): Matrix {
        if (width != other.height)
            throw IllegalArgumentException("Matrix.times: wrong matrix dimensions")
        return Matrix(matrixBuilder(height, other.width) { h: Int, w: Int ->
            (0 until width).sumBy { i -> this[h, i] * other[i, w] }
        })
    }

    operator fun get(x: Int, y: Int) = matrix[x][y]

    operator fun set(x: Int, y: Int, z: Int) {
        matrix[x][y] = z
    }

    override fun toString(): String = matrix.joinToString (postfix = "\n",
        transform = { it.joinToString(prefix = "\n\t[", postfix = "]") } )

    override fun equals(other: Any?): Boolean {
        return when(other) {
            is Matrix    -> (0 until height).all { i -> matrix[i].contentEquals(other.matrix[i]) }
            else            -> false
        }
    }
}

internal fun matrixBuilder(height: Int, width: Int, builder: (Int, Int) -> Int): AArray<Int> =
    Array(height) { h ->
        Array(width) { w ->
            builder(h,w)
        }
    }

