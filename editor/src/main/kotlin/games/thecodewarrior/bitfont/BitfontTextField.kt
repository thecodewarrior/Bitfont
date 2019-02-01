package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.typesetting.TextLayout
import games.thecodewarrior.bitfont.typesetting.Typesetter
import games.thecodewarrior.bitfont.typesetting.TypesettingOptions
import glm_.func.common.clamp
import glm_.vec2.Vec2i

open class BitfontTextField(val bitfont: Bitfont) {
    protected val typesetter = Typesetter(bitfont)
    var options: TypesettingOptions = TypesettingOptions()

    // logic
    private var _text = StringBuffer()
    var text: String
        get() = _text.toString()
        set(value) {
            val changed = _text.toString() != value
            _text = StringBuffer(value)
            if(changed) {
                updateLayout()
                updateCursor()
            }
        }

    protected var _cursor = 0
    var cursor: Int
        get() = _cursor
        set(value) {
            _cursor = value
            updateCursor()
        }

    open val rendering = TextFieldRendering()
    open class TextFieldRendering {
        var cursor: Vec2i = Vec2i()
    }

    var layout: TextLayout = TextLayout()
        protected set

    protected open fun updateLayout() {
        layout = typesetter.typeset(text, options)
    }

    protected open fun updateCursor() {
        _cursor = _cursor.clamp(0, text.length)
        rendering.cursor = if(cursor >= layout.characters.size)
            layout.endPos
        else
            layout.characters[cursor].pos
    }

    /**
     * Delete the text from [min] (inclusive) to [max] (exclusive), adjusting the cursor accordingly
     */
    fun delete(min: Int, max: Int) {
        _text.delete(min, max)
        when {
            _cursor > max -> _cursor -= max - min
            _cursor > min -> _cursor = min
        }
        updateLayout()
        updateCursor()
    }

    fun insert(index: Int, str: String) {
        _text.insert(index, str)
        if(_cursor >= index) _cursor += str.length
        updateLayout()
        updateCursor()
    }
}
