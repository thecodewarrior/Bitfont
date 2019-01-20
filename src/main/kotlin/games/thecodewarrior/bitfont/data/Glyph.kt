package games.thecodewarrior.bitfont.data

class Glyph(val codepoint: Int) {
    var bearingX: Int = 0
    var bearingY: Int = 0
    var image: BitGrid = BitGrid(1, 1)
}