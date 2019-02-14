package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.editor.mode.DefaultEditorMode
import games.thecodewarrior.bitfont.utils.Attribute
import games.thecodewarrior.bitfont.typesetting.TypesetString
import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.extensions.cString
import games.thecodewarrior.bitfont.editor.utils.extensions.color
import games.thecodewarrior.bitfont.editor.utils.extensions.draw
import games.thecodewarrior.bitfont.editor.utils.extensions.fromGlfw
import games.thecodewarrior.bitfont.editor.utils.extensions.toIm
import games.thecodewarrior.bitfont.editor.utils.extensions.u32
import games.thecodewarrior.bitfont.typesetting.font
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import imgui.FocusedFlag
import imgui.ImGui
import imgui.functionalProgramming.withItemWidth
import imgui.g
import imgui.internal.Rect
import kotlin.math.max

class InputTestWindow(val document: BitfontDocument): IMWindow() {
    val bitfont: Bitfont = document.bitfont

    override val title: String
        get() = "${bitfont.name}: Testing"

    val textInput = Editor(bitfont, -1)
    var scale = 2
        set(value) {
            field = max(value, 1)
        }
    var canvas = Rect()
    val textOrigin: Vec2
        get() = canvas.min + Vec2(5, 5)

    val prevKeysDown = BooleanArray(ImGui.io.keysDown.size)

    init {
    }

    fun handleInput() = with(ImGui) {
        textInput.inputModifiers(Modifiers(*listOfNotNull(
            if(io.keyShift) Modifier.SHIFT else null,
            if(io.keyCtrl) Modifier.CONTROL else null,
            if(io.keyAlt) Modifier.ALT else null,
            if(io.keySuper) Modifier.SUPER else null
        ).toTypedArray()))
        textInput.inputKeyChanges(
            io.keysDown.withIndex().filter { (i, isDown) ->
                val wasDown = prevKeysDown[i]
                prevKeysDown[i] = isDown
                isDown != wasDown
            }.associate { (i, isDown) ->
                Key.fromGlfw(i) to isDown
            }
        )
        val text = io.inputCharacters.cString()
        if(text.isNotEmpty())
            textInput.inputText(text)
    }

    override fun main(): Unit = with(ImGui) {
        if(isWindowFocused(FocusedFlag.RootAndChildWindows) && g.activeId == 0) handleInput()
//        withItemWidth(win.contentsRegionRect.width - 300f) {
//            val arr = testString.replace("\n", "\\n").toCharArray().let { name ->
//                CharArray(name.size + 1000).also { name.copyInto(it) }
//            }
//            if(inputText("##testText", arr, InputTextFlag.EnterReturnsTrue.i))
//                testString = String(g.inputTextState.textW.sliceArray(0 until g.inputTextState.textW.indexOf('\u0000'))).replace("\\n", "\n")
//        }
        withItemWidth(150f) {
            sameLine()
            inputInt("Scale", ::scale)
        }

        val canvasPos = win.contentsRegionRect.min + Vec2(0, frameHeightWithSpacing)

        canvas = Rect(canvasPos, canvasPos + Vec2(win.contentsRegionRect.width, win.contentsRegionRect.max.y - canvasPos.y))
        itemSize(canvas)
        pushClipRect(canvas.min, canvas.max, true)
        itemHoverable(canvas, "canvas".hashCode())
        itemAdd(canvas, "canvas".hashCode())
        drawCanvas()
        popClipRect()
    }

    private var lastCursorChange = System.currentTimeMillis()
    private var lastCursor = 0
    fun drawCanvas() {
        drawList.addRectFilled(
            canvas.min,
            canvas.max,
            Colors.layoutTest.background.u32
        )

        val originIndicator = textOrigin - Vec2(0.5)
        drawList.addTriangleFilled(
            originIndicator,
            originIndicator + Vec2(-scale, -scale),
            originIndicator + Vec2(-scale, scale),
            Colors.layoutTest.originIndicator.u32
        )
        val textRegion = Rect(textOrigin, canvas.max)

        textInput.width = (textRegion.width / scale).toInt()

        val mode = textInput.mode as DefaultEditorMode
        if(mode.cursor != lastCursor) {
            lastCursor = mode.cursor
            lastCursorChange = System.currentTimeMillis()
        }
        var cursorGlyph: TypesetString.GlyphRender? = null

        textInput.typesetString.glyphs.forEach {
            if(it.characterIndex == mode.cursor || (cursorGlyph == null && it.characterIndex == mode.cursor-1)) {
                cursorGlyph = it
            }
            drawGlyph(it)
        }

        val blinkSpeed = 500
        if((System.currentTimeMillis()-lastCursorChange) % (blinkSpeed*2) < blinkSpeed) {
            cursorGlyph?.also { glyph ->
                val cursorPos = if (glyph.characterIndex == mode.cursor) glyph.pos.toIm() else glyph.posAfter.toIm()
                val font = glyph.attributes[Attribute.font] ?: bitfont
                val cursorMin = cursorPos - Vec2i(1, font.ascent)
                val cursorMax = cursorPos + Vec2i(0, font.descent)

                drawList.addRect(textOrigin + cursorMin * scale, textOrigin + cursorMax * scale, Colors.white.u32)
            }
        }
    }

    fun drawGlyph(char: TypesetString.GlyphRender) {
        val color = char.attributes[Attribute.color] ?: Colors.layoutTest.text
        val font = char.attributes[Attribute.font] ?: bitfont
        char.glyph.draw(textOrigin + char.pos.toIm() * scale, scale, color.u32)
    }
}
