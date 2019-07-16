package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.editor.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.utils.extensions.clamp

class BitfontDocument(val bitfont: dev.thecodewarrior.bitfont.data.Bitfont) {
    var infoWindow = dev.thecodewarrior.bitfont.editor.FontInfoWindow(this)
//    var editorWindow = GlyphEditorWindow(this)
    val children = infoWindow.children

    var referenceStyle = 0
    var referenceSize = 1f
        set(value) { field = value.clamp(1f, 1000f) }

    fun push(imgui: ImGui) {
        children.removeIf {
//            it != editorWindow &&
            !it.visible
        }
        infoWindow.visible = true
        infoWindow.push(imgui)
    }

    companion object {
        fun blank(): dev.thecodewarrior.bitfont.editor.BitfontDocument {
            return dev.thecodewarrior.bitfont.editor.BitfontDocument(dev.thecodewarrior.bitfont.data.Bitfont("Untitled", 10, 4, 9, 6, 2))
        }
    }
}