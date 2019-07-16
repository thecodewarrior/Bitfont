package dev.thecodewarrior.bitfont.editor.utils.extensions

import dev.thecodewarrior.bitfont.data.Glyph
import dev.thecodewarrior.bitfont.editor.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.utils.math.Vec2
import dev.thecodewarrior.bitfont.editor.utils.math.vec
import dev.thecodewarrior.bitfont.typesetting.AttributedGlyph

fun dev.thecodewarrior.bitfont.data.Glyph.draw(imgui: ImGui, pos: Vec2, scale: Int, color: Int) {
    for(x in 0 until image.width) {
        for(y in 0 until image.height) {
            if(image[x, y]) {
                val pxPos = pos + vec(x + bearingX, y + bearingY) * scale
                imgui.windowDrawList.addRectFilled(pxPos, pxPos + vec(scale, scale), color)
            }
        }
    }
}

fun dev.thecodewarrior.bitfont.typesetting.AttributedGlyph.draw(imgui: ImGui, pos: Vec2, scale: Int, color: Int) {
    this.glyph.draw(imgui, pos, scale, color)
}
