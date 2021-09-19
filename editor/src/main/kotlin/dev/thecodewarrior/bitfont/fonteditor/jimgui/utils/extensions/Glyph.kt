package dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.extensions

import dev.thecodewarrior.bitfont.data.Glyph
import dev.thecodewarrior.bitfont.fonteditor.jimgui.imgui.ImGui
import dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.math.Vec2
import dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.math.vec

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

//fun TypesetGlyph.draw(imgui: ImGui, pos: Vec2, scale: Int, color: Int) {
//    this.textObject.draw(imgui, pos, scale, color)
//}
