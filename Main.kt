package minesweeper

import kotlin.random.Random

typealias Field2D = MutableList<MutableList<Cell>>

object GameSettings {
    const val WIDTH = 9
    const val HEIGHT = 9

    const val WINNING_SPEECH = "Congratulations! You found all the mines!"
    const val LOSING_SPEECH = "You stepped on a mine and failed!"

    val nearCords = mutableListOf(
        Pair(-1, -1), Pair(-1, 0), Pair(-1, 1),
        Pair(0, -1), Pair(0, 1),
        Pair(1, -1), Pair(1, 0), Pair(1, 1)
    )
}

object Signs {
    const val BOMB = 'X'
    const val EMPTY = '.'
    const val ZERO = '0'
    const val FLAG = '*'
    const val EXPLORED = '/'
}

class Command(val cell: Pair<Int, Int>, val name: String)

class Cell {
    var hidden: Char = Signs.EMPTY
    var open: Char = Signs.EMPTY
    var isExplored: Boolean = false

    fun isBomb(): Boolean {
        return hidden == Signs.BOMB
    }

    fun isOpenDigit(): Boolean {
        return Character.isDigit(open)
    }

    fun changeFlag() {
        open = if (open == Signs.FLAG) Signs.EMPTY else Signs.FLAG
    }

    override fun toString(): String {
        return open.toString()
    }
}

fun askBombs(): Int {
    println("How many mines do you want on the field?")
    return readLine()!!.toInt()
}

fun createField(height: Int, width: Int): Field2D {
    return MutableList(height) { MutableList(width) { Cell() } }
}

fun setBombs(field: Field2D, bombs: Int, start: Pair<Int, Int>) {
    repeat(bombs) {
        var x: Int
        var y: Int
        do {
            x = Random.nextInt(0, 9)
            y = Random.nextInt(0, 9)
        } while (field[y][x].hidden == Signs.BOMB || Pair(x, y) == start)
        field[y][x].hidden = Signs.BOMB
    }
}

fun setNumbers(field: Field2D) {
    for (y in 0 until field.size) {
        for (x in 0 until field[y].size) {
            calculateNumber(x, y, field)
        }
    }
}

fun calculateNumber(x: Int, y: Int, field: Field2D) {
    val bombs = countBombs(x, y, field)
    if (!field[y][x].isBomb() && bombs > 0) {
        field[y][x].hidden = Signs.ZERO + bombs
    }
}

fun countBombs(x: Int, y: Int, field: Field2D): Int {
    var counter = 0
    for (cord in GameSettings.nearCords) {
        counter += if (isBomb(x + cord.first, y + cord.second, field)) 1 else 0
    }
    return counter
}

fun isBomb(x: Int, y: Int, field: Field2D): Boolean {
    return correctCords(Pair(x, y), field) && field[y][x].isBomb()
}

fun showField(field: Field2D) {
    println(" │123456789│\n—│—————————│")
    field.forEachIndexed { index, line ->
        println("${index + 1}|${line.joinToString("")}|")
    }
    println("—│—————————│")
}

fun showLostField(field: Field2D) {
    for (line in field) {
        for (cell in line) {
            if (cell.isBomb()) {
                cell.open = Signs.BOMB
            }
        }
    }
    showField(field)
}

fun checkWin(field: Field2D): Boolean {
    var allExplored = true
    var allMarked = true

    for (line in field) {
        for (cell in line) {
            if (!cell.isBomb() && !cell.isExplored) {
                allExplored = false
            }

            if (cell.isBomb().xor(cell.open == '*')) {
                allMarked = false
            }
        }
    }

    return allExplored || allMarked
}

fun askForCommand(): Command {
    println("Set/unset mines marks or claim a cell as free:")
    val input = readLine()!!.split(" ")
    return Command(Pair(input[0].toInt() - 1, input[1].toInt() - 1), input[2])
}

fun canBeFlagged(cords: Pair<Int, Int>, field: Field2D): Boolean {
    return !field[cords.second][cords.first].isOpenDigit()
}

fun changeFlag(cords: Pair<Int, Int>, field: Field2D) {
    field[cords.second][cords.first].changeFlag()
}

fun correctCords(cords: Pair<Int, Int>, field: Field2D): Boolean {
    return cords.second in 0 until field.size
            && cords.first in 0 until field[cords.second].size
}

fun notAbleToExplore(cell: Cell): Boolean {
    return cell.isExplored || cell.isBomb()
}

fun exploreCell(cords: Pair<Int, Int>, field: Field2D) {
    if (!correctCords(cords, field)) {
        return
    }

    val cell = field[cords.second][cords.first]
    if (notAbleToExplore(cell)) {
        return
    }
    cell.isExplored = true

    val counter = countBombs(cords.first, cords.second, field)
    if (counter == 0) {
        cell.open = Signs.EXPLORED
        for (delta in GameSettings.nearCords) {
            exploreCell(Pair(cords.first + delta.first, cords.second + delta.second), field)
        }
    } else {
        cell.open = cell.hidden
    }
}

fun playGame(field: Field2D, bombs: Int): Boolean {

    var gameStarted = false

    while (!checkWin(field) || !gameStarted) {
        showField(field)
        val command = askForCommand()
        if (command.name == "free") {
            if (!gameStarted) {
                gameStarted = true

                setBombs(field, bombs, command.cell)
                setNumbers(field)
            }
            if (field[command.cell.second][command.cell.first].isBomb()) {
                return false
            }
            exploreCell(command.cell, field)
        } else if (command.name == "mine") {
            changeFlag(command.cell, field)
        }
    }
    return true
}

fun main() {

    val bombs = askBombs()
    val field = createField(GameSettings.HEIGHT, GameSettings.WIDTH)

    val hasWon = playGame(field, bombs)

    if (hasWon) {
        showField(field)
        println(GameSettings.WINNING_SPEECH)
    } else {
        showLostField(field)
        println(GameSettings.LOSING_SPEECH)
    }
}