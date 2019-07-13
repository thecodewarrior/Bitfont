package games.thecodewarrior.bitfont.editor.utils.extensions

import games.thecodewarrior.bitfont.data.Glyph
import games.thecodewarrior.bitfont.editor.imgui.ImGui
import games.thecodewarrior.bitfont.editor.utils.math.Vec2
import games.thecodewarrior.bitfont.editor.utils.math.vec
import games.thecodewarrior.bitfont.typesetting.AttributedGlyph

fun Glyph.draw(imgui: ImGui, pos: Vec2, scale: Int, color: Int) {
    for(x in 0 until image.width) {
        for(y in 0 until image.height) {
            if(image[x, y]) {
                val pxPos = pos + vec(x + bearingX, y + bearingY) * scale
                imgui.windowDrawList.addRectFilled(pxPos, pxPos + vec(scale, scale), color)
            }
        }
    }
}

fun AttributedGlyph.draw(imgui: ImGui, pos: Vec2, scale: Int, color: Int) {
    this.glyph.draw(imgui, pos, scale, color)
}
