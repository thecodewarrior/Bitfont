package dev.thecodewarrior.bitfont.fonteditor.utils

import dev.thecodewarrior.bitfont.fonteditor.App
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkPluginFilter
import org.lwjgl.nuklear.NkPluginFilterI
import org.lwjgl.nuklear.NkTextEdit
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.text.ParseException

class NkEditor(
    var flags: Int,
    var filter: NkPluginFilter? = null,
    var changeListener: ((editor: NkEditor, old: String, new: String) -> Unit)? = null
) : Freeable {
    val textEditor: NkTextEdit = NkTextEdit.malloc()
    private var _state: String = ""
        set(value) {
            if(field != value) {
                val old = field
                field = value
                changeListener?.invoke(this, old, value)
            }
        }

    var text: String
        get() = _state
        set(value) {
            MemoryStack.stackPush().use { stack ->
                nk_str_clear(textEditor.string())
                nk_str_append_str_utf8(textEditor.string(), stack.UTF8(value, true))
                _state = value
            }
        }

    init {
        nk_textedit_init(textEditor, App.ALLOCATOR, 10)
    }

    fun push(ctx: NkContext): Int {
        val ret = nk_edit_buffer(ctx, flags, textEditor, filter)
        try {
            val string = textEditor.string()
            val pointer = string.buffer().memory().ptr()!!
            _state = MemoryUtil.memUTF8(pointer, nk_str_len_char(string))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ret
    }

    override fun free() {
        textEditor.free()
    }
}
