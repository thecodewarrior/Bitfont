package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.data.Bitfont

class BitfontDocument(val bitfont: Bitfont) {
    var infoWindow = FontInfoWindow(this)
    var editorWindow = GlyphEditorWindow(this)
    var browserWindows = mutableListOf<GlyphBrowserWindow>()
    var testWindows = mutableListOf<TestingWindow>()
    var inputWindows = mutableListOf<InputTestWindow>()

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
            return BitfontDocument(Bitfont("Untitled", 16, 10, 4, 9, 6, 2))
        }
    }
}