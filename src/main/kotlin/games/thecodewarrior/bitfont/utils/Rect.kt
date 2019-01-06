package games.thecodewarrior.bitfont.utils

class Rect(val minX: Int, val minY: Int, val width: Int, val height: Int) {
    val maxX = minX + width
    val maxY = minY + height
}
