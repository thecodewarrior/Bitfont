package games.thecodewarrior.bitfont.editor.utils.extensions

import games.thecodewarrior.bitfont.editor.data.BitGrid
import glm_.vec2.Vec2i

operator fun BitGrid.set(pos: Vec2i, value: Boolean) {
    this[pos.toBit()] = value
}

operator fun BitGrid.get(pos: Vec2i): Boolean {
    return this[pos.toBit()]
}
