package games.thecodewarrior.bitfont.utils.extensions

import games.thecodewarrior.bitfont.data.Glyph
import glm_.vec2.Vec2
import imgui.ImGui

fun Glyph.draw(pos: Vec2, scale: Int, color: Int) {
    for(x in 0 until image.width) {
        for(y in 0 until image.height) {
            if(image[x, y]) {
                val pxPos = pos + Vec2(x + bearingX, y + bearingY) * scale
                ImGui.windowDrawList.addRectFilled(pxPos, pxPos + Vec2(scale), color)
            }
        }
    }
}