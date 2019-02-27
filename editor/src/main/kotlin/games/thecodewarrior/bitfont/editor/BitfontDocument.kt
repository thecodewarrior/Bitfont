package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.data.Bitfont
import glm_.func.common.clamp

class BitfontDocument(val bitfont: Bitfont) {
    var infoWindow = FontInfoWindow(this)
    var editorWindow = GlyphEditorWindow(this)
    init {
        editorWindow.visible = false
    }
    var otherWindows = mutableListOf<IMWindow>()

    var referenceStyle = 0
    var referenceSize = 1f
        set(value) { field = value.clamp(1f, 1000f) }

    fun push() {
        infoWindow.visible = true
        infoWindow.push()
        editorWindow.push()
        otherWindows.removeIf { !it.visible }
        otherWindows.toList().forEach { it.push() }
    }

    companion object {
        fun blank(): BitfontDocument {
            return BitfontDocument(Bitfont("Untitled", 10, 4, 9, 6, 2))
        }
    }
}