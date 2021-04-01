package main

fun main() {
    val m1 = Matrix(arrayOf(arrayOf(1,4),arrayOf(2,5)))
    val m2 = Matrix(arrayOf(arrayOf(3,2),arrayOf(5,8)))
    val m11 = StrassenMatrix(arrayOf(arrayOf(1,4),arrayOf(2,5)))
    val m22 = StrassenMatrix(arrayOf(arrayOf(3,2),arrayOf(5,8)))
    println(m1 * m2)
    println(m11 * m22)
}
