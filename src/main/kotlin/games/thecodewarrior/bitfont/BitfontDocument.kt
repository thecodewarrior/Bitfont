package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.data.Bitfont

class BitfontDocument(val bitfont: Bitfont) {
    var infoWindow = FontInfoWindow(this)
    var editorWindow = GlyphEditorWindow(this)
    var browserWindow = GlyphBrowserWindow(this)

    fun push() {
        infoWindow.visible = true
        infoWindow.push()
        editorWindow.push()
        browserWindow.push()
    }

    companion object {
        fun blank(): BitfontDocument {
            return BitfontDocument(Bitfont("Untitled", 16, 10, 4, 9, 6))
        }
    }
}