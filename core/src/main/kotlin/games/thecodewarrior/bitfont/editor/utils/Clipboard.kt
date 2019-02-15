package games.thecodewarrior.bitfont.editor.utils

interface Clipboard {
    var contents: String?
}

object InternalClipboard: Clipboard {
    override var contents: String? = null
}
