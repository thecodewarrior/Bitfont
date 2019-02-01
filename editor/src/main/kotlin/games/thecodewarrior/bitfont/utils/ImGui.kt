package games.thecodewarrior.bitfont.utils

import glm_.vec2.Vec2
import imgui.ImGui
import imgui.internal.Rect

fun ImGui.alignedText(text: String, align: Vec2, width: Float, height: Float = frameHeight) {
    val labelBB = Rect(currentWindow.dc.cursorPos, currentWindow.dc.cursorPos + Vec2(width, height))
    itemSize(labelBB)
    itemAdd(labelBB, 0)
    renderTextClipped(labelBB.min, labelBB.max, text, text.length, null, align)
}
