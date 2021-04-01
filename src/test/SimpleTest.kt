package test

import main.Matrix
import main.StrassenMatrix
import main.matrixBuilder
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

class SimpleTest {
    @Test
    fun `times matrix`() {
        val m1 = StrassenMatrix(arrayOf(arrayOf(1,4),arrayOf(2,5)))
        val m2 = StrassenMatrix(arrayOf(arrayOf(3,2),arrayOf(5,8)))
        assertTrue(m1 as Matrix * m2 as Matrix == m1 * m2)
    }

    @Test
    fun `times matrix 2`() {
        val m1 = StrassenMatrix(arrayOf(arrayOf(2,5,1,6,2,3),arrayOf(9,10,12,4,2,6),arrayOf(9,10,12,4,2,6)))
        val m2 = StrassenMatrix(arrayOf(arrayOf(3,2,5,7,8),arrayOf(5,8,7,16,22),arrayOf(12,0,2,10,2),
            arrayOf(9,15,2,0,0),arrayOf(0,19,2,1,1),arrayOf(17,9,9,4,5)))
        assertTrue(m1 as Matrix * m2 as Matrix == m1 * m2)
    }

    @Test
    fun `times matrix 3`() {
        val m1 = StrassenMatrix(arrayOf(arrayOf(6)))
        val m2 = StrassenMatrix(arrayOf(arrayOf(5)))
        assertTrue(m1 as Matrix * m2 as Matrix == m1 * m2)
    }

    @Test
    fun `times matrixBig`() {
        (10 until 200 step 10).forEach { i ->
            val m1 = StrassenMatrix(matrixBuilder(i, i) { h: Int, w: Int -> h + w })
            val matrix: Matrix
            val stMatrix: StrassenMatrix
            val timeInMillisMatrix = measureTimeMillis {
                matrix = m1 as Matrix * m1 as Matrix
            }
            val timeInMillisStMatrix = measureTimeMillis {
                stMatrix = m1 * m1
            }
            assertTrue(matrix == stMatrix)
            println("$i x $i: * Matrix worked $timeInMillisMatrix ms, * StrassenMatrix worked $timeInMillisStMatrix ms")
        }
    }
}