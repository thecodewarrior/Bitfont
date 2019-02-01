package games.thecodewarrior.bitfont.editor.utils.extensions

import imgui.Dir
import imgui.ID
import imgui.ImGui
import imgui.g
import imgui.internal.Rect
import kotlin.reflect.KMutableProperty0

fun <T: Number> inputScalarAsWidgetReplacement(bb: Rect, id: ID, label: String, data: KMutableProperty0<T>,
    valueToString: (T) -> String, stringToValue: (T, String) -> Pair<T, String>): Boolean = with(ImGui) {

    val window = currentWindow

    /*  Our replacement widget will override the focus ID (registered previously to allow for a TAB focus to happen)
        On the first frame, g.ScalarAsInputTextId == 0, then on subsequent frames it becomes == id  */
    setActiveId(g.scalarAsInputTextId, window)
    g.activeIdAllowNavDirFlags = (1 shl Dir.Up.i) or (1 shl Dir.Down.i)
    hoveredId = 0
    focusableItemUnregister(window)

    val value = data()
    val stringValue = valueToString(value).toCharArray()
    val dataBuf = CharArray(stringValue.size + 16)
    stringValue.copyInto(dataBuf)
    val valueChanged = inputTextEx(label, dataBuf, bb.size, 0)
    if (g.scalarAsInputTextId == 0) {   // First frame we started displaying the InputText widget
        assert(g.activeId == id) // InputText ID expected to match the Slider ID
        g.scalarAsInputTextId = g.activeId
        hoveredId = id
    }

    return when {
        valueChanged -> {
            val editedString = String(g.inputTextState.textW).replace("\u0000", "")
            val (newValue, newString) = stringToValue(value, editedString)
            if(value != newValue) data.set(newValue)
            value != newValue
        }
        else -> false
    }
}
