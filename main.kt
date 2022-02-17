package minesweeper

import kotlin.random.Random

typealias Field2D = MutableList<MutableList<Cell>>

object GameSettings {
    const val WIDTH = 9
    const val HEIGHT = 9
}

class Command(val x: Int, val y: Int, val name: String)

class Cell {
    var sign: Char = '.'
    var bomb: Boolean = false

    fun isDigit(): Boolean {
        return Character.isDigit(sign)
    }

    fun changeFlag() {
        sign = if (sign == '.') '*' else '.'
    }

    override fun toString(): String {
        return sign.toString()
    }
}

fun askBombs(): Int {
    println("How many mines do you want on the field?")
    return readLine()!!.toInt()
}

fun createField(height: Int, width: Int): Field2D {
    return MutableList(height) { MutableList(width) { Cell() } }
}

fun setBombs(field: Field2D, bombs: Int) {
    repeat(bombs) {
        var x: Int
        var y: Int
        do {
            x = Random.nextInt(0, 9)
            y = Random.nextInt(0, 9)
        } while (field[x][y].bomb)
        field[x][y].bomb = true
    }
}

fun setNumbers(field: Field2D) {
    for (x in 0 until field.size) {
        for (y in 0 until field[x].size) {
            updateNumber(x, y, field)
        }
    }
}

fun updateNumber(x: Int, y: Int, field: Field2D) {
    val bombs = countBombs(x, y, field)
    if (!field[x][y].bomb && bombs > 0) {
        field[x][y].sign = '0' + bombs
    }
}

fun countBombs(x: Int, y: Int, field: Field2D): Int {
    val cords = mutableListOf(
        Pair(x - 1, y - 1), Pair(x - 1, y), Pair(x - 1, y + 1),
        Pair(x, y - 1), Pair(x, y + 1),
        Pair(x + 1, y - 1), Pair(x + 1, y), Pair(x + 1, y + 1)
    )

    var counter = 0
    for (cord in cords) {
        counter += if (isBomb(cord.first, cord.second, field)) 1 else 0
    }
    return counter
}

fun isBomb(x: Int, y: Int, field: MutableList<MutableList<Cell>>): Boolean {
    return !(x !in 0 until field.size
            || y !in 0 until field[x].size
            || !field[x][y].bomb)
}

fun showField(field: Field2D) {
    println(" │123456789│\n—│—————————│")
    field.forEachIndexed { index, line ->
        println("${index + 1}|${line.joinToString("")}|")
    }
    println("—│—————————│")
}

fun checkWin(field: Field2D): Boolean {
    for (line in field) {
        for (cell in line) {
            if (cell.bomb.xor(cell.sign == '*')) {
                return false
            }
        }
    }
    return true
}

fun askForCommand(): Command {
    println("Set/unset mines marks or claim a cell as free:")
    val input = readLine()!!.split(" ")
    return Command(input[0].toInt(), input[1].toInt(), input[2])
}

fun correctCords(cords: Pair<Int, Int>, field: Field2D): Boolean {
    return !field[cords.second][cords.first].isDigit()
}

fun changeFlag(cords: Pair<Int, Int>, field: Field2D) {
    field[cords.second][cords.first].changeFlag()
}

fun main() {

    val bombs = askBombs()
    val field = createField(GameSettings.HEIGHT, GameSettings.WIDTH)

    setBombs(field, bombs)
    setNumbers(field)

    while (!checkWin(field)) {
        showField(field)
        val command = askForCommand()

    }

    println("Congratulations! You found all the mines!")
}