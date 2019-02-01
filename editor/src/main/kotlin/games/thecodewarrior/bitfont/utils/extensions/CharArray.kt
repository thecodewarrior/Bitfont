package games.thecodewarrior.bitfont.utils.extensions

fun CharArray.cString(): String {
    val nullPos = indexOf(0.toChar())
    return String(this, 0, if(nullPos == -1) size else nullPos)
}