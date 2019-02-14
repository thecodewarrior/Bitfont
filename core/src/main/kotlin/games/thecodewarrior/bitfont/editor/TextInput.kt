package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.typesetting.AttributedString
import games.thecodewarrior.bitfont.typesetting.MutableAttributedString
import games.thecodewarrior.bitfont.typesetting.TypesetString

class TextInput(font: Bitfont, width: Int) {
    var font = font
        set(value) {
            if(field != value) {
                field = value
                update()
            }
        }
    var width = width
        set(value) {
            if(field != value) {
                field = value
                update()
            }
        }

    /*    /======== Contents ========\    */
    private var contents: MutableAttributedString = MutableAttributedString("")
    val attributedString: AttributedString get() = contents.staticCopy()

    /*    /======== Rendering ========\    */
    lateinit var typesetString: TypesetString
        private set

    init {
        update()
    }

    private fun update() {
        typesetString = TypesetString(font, contents, width)
    }

    fun inputText(text: String) {
        contents.append(text)
        update()
    }

    fun inputKeyChanges(changes: Map<Key, Boolean>) {
        if(changes[Key.BACKSPACE] == true) {
            if(contents.isNotEmpty())
                contents.delete(contents.length-1, contents.length)
            update()
        }
    }
}