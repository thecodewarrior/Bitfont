package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.editor.mode.DefaultEditorMode
import games.thecodewarrior.bitfont.editor.mode.MacEditorMode
import games.thecodewarrior.bitfont.utils.Attribute
import games.thecodewarrior.bitfont.typesetting.TypesetString
import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.GlfwClipboard
import games.thecodewarrior.bitfont.editor.utils.extensions.cString
import games.thecodewarrior.bitfont.editor.utils.extensions.color
import games.thecodewarrior.bitfont.editor.utils.extensions.copy
import games.thecodewarrior.bitfont.editor.utils.extensions.draw
import games.thecodewarrior.bitfont.editor.utils.extensions.fromGlfw
import games.thecodewarrior.bitfont.editor.utils.extensions.toBit
import games.thecodewarrior.bitfont.editor.utils.extensions.toIm
import games.thecodewarrior.bitfont.editor.utils.extensions.u32
import games.thecodewarrior.bitfont.typesetting.font
import games.thecodewarrior.bitfont.utils.extensions.endExclusive
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import imgui.FocusedFlag
import imgui.ImGui
import imgui.functionalProgramming.withItemWidth
import imgui.g
import imgui.internal.Rect
import kotlin.math.max
import kotlin.math.roundToInt

class InputTestWindow(val document: BitfontDocument): IMWindow() {
    val bitfont: Bitfont = document.bitfont

    override val title: String
        get() = "${bitfont.name}: Testing"

    init {
        DefaultEditorMode.operatingSystemMode = ::MacEditorMode
    }
    val editor = Editor(bitfont, -1)
    val mode = editor.mode as DefaultEditorMode
    var selection: IntRange? = null

    var scale = 2
        set(value) {
            field = max(value, 1)
        }
    var canvas = Rect()
    val textOrigin: Vec2
        get() = canvas.min + Vec2(5, 5)

    val prevKeysDown = BooleanArray(ImGui.io.keysDown.size)
    val prevMouseDown = BooleanArray(ImGui.io.mouseDown.size)
    var prevMousePos = Vec2i(0, 0)

    init {
        mode.clipboard = GlfwClipboard
    }

    fun handleInput() = with(ImGui) {
        editor.inputModifiers(Modifiers(*listOfNotNull(
            if(io.keyShift) Modifier.SHIFT else null,
            if(io.keyCtrl) Modifier.CONTROL else null,
            if(io.keyAlt) Modifier.ALT else null,
            if(io.keySuper) Modifier.SUPER else null
        ).toTypedArray()))

        io.keysDown.forEachIndexed { i, isDown ->
            val wasDown = prevKeysDown[i]
            prevKeysDown[i] = isDown
            if(isDown && !wasDown)
                editor.inputKeyDown(Key.fromGlfw(i))
            else if(!isDown && wasDown)
                editor.inputKeyUp(Key.fromGlfw(i))
        }

        val relativeMousePos =
            if(io.mousePos.x == -Float.MAX_VALUE)
                prevMousePos
            else
                (Vec2(io.mousePos - textOrigin) / scale).let {
                    Vec2i(it.x.roundToInt(), it.y.roundToInt())
                }
        if(relativeMousePos != prevMousePos) {
            editor.inputMouseMove(relativeMousePos.toBit())
            prevMousePos = relativeMousePos
        }
        io.mouseDown.forEachIndexed { i, isDown ->
            if(prevMouseDown[i] == isDown) return@forEachIndexed
            prevMouseDown[i] = isDown
            if(isDown)
                editor.inputMouseDown(MouseButton.values()[i+1])
            else
                editor.inputMouseUp(MouseButton.values()[i+1])
        }

        val text = io.inputCharacters.cString()
        if(text.isNotEmpty())
            editor.inputText(text)
    }

    override fun main(): Unit = with(ImGui) {
        if(isWindowFocused(FocusedFlag.RootAndChildWindows) && g.activeId == 0) handleInput()
        editor.update()
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

        editor.width = (textRegion.width / scale).toInt()

        if(mode.cursor != lastCursor) {
            lastCursor = mode.cursor
            lastCursorChange = System.currentTimeMillis()
        }
        selection = mode.selectionRange
        selection?.let { selection ->
            editor.typesetString.lines.forEach { line ->
                line.glyphs.forEach { glyph ->
                    if (glyph.characterIndex in selection && glyph.posAfter.y == glyph.pos.y) {
                        drawList.addRectFilled(
                            textOrigin + (glyph.pos.toIm() - Vec2i(0, line.maxAscent)) * scale,
                            textOrigin + (glyph.posAfter.toIm() + Vec2i(0, line.maxDescent)) * scale,
                            Colors.cyan.copy(alpha = 0.5f).u32
                        )
                    }
                }
            }
        }
        editor.typesetString.glyphs.forEach {
            drawGlyph(it)
        }

        val blinkSpeed = 500
        if(selection == null && (System.currentTimeMillis()-lastCursorChange) % (blinkSpeed*2) < blinkSpeed) {
            val cursorMin = mode.cursorPos.toIm() - Vec2i(0, bitfont.ascent)
            val cursorMax = mode.cursorPos.toIm() + Vec2i(0, bitfont.descent)

            drawList.addRectFilled(textOrigin - Vec2i(1, 0) + cursorMin * scale, textOrigin + cursorMax * scale, Colors.white.u32)
        }
    }

    fun drawGlyph(char: TypesetString.GlyphRender) {
        val color = char.attributes[Attribute.color] ?: Colors.layoutTest.text
        char.glyph.draw(textOrigin + char.pos.toIm() * scale, scale, color.u32)
    }
}
