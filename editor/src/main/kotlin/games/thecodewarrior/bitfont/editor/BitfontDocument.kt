package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.editor.data.Bitfont
import glm_.func.common.clamp

class BitfontDocument(val bitfont: Bitfont) {
    var infoWindow = FontInfoWindow(this)
    var editorWindow = GlyphEditorWindow(this)
    var browserWindows = mutableListOf<GlyphBrowserWindow>()
    var testWindows = mutableListOf<TestingWindow>()
    var inputWindows = mutableListOf<InputTestWindow>()

    var referenceStyle = 0
    var referenceSize = 1f
        set(value) { field = value.clamp(1f, 1000f) }

    fun push() {
        infoWindow.visible = true
        infoWindow.push()
        editorWindow.push()
        browserWindows.removeIf { !it.visible }
        browserWindows.toList().forEach { it.push() }
        testWindows.removeIf { !it.visible }
        testWindows.toList().forEach { it.push() }
        inputWindows.removeIf { !it.visible }
        inputWindows.toList().forEach { it.push() }
    }

    companion object {
        fun blank(): BitfontDocument {
            return BitfontDocument(Bitfont("Untitled", 10, 4, 9, 6, 2))
        }
    }
}