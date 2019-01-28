package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.typesetting.BitfontAtlas
import games.thecodewarrior.bitfont.typesetting.TypesetCharacter
import games.thecodewarrior.bitfont.typesetting.Typesetter
import games.thecodewarrior.bitfont.typesetting.TypesettingOptions
import games.thecodewarrior.bitfont.utils.Colors
import games.thecodewarrior.bitfont.utils.Constants
import games.thecodewarrior.bitfont.utils.extensions.addAll
import games.thecodewarrior.bitfont.utils.extensions.u32
import games.thecodewarrior.bitfont.utils.opengl.Java2DTexture
import glm_.vec2.Vec2
import imgui.ImGui
import imgui.InputTextFlag
import imgui.WindowFlag
import imgui.functionalProgramming.withItemWidth
import imgui.g
import imgui.internal.Rect
import kotlin.math.max

class TestingWindow(val document: BitfontDocument): IMWindow() {
    val bitfont: Bitfont = document.bitfont
    var atlas = BitfontAtlas(bitfont)
    val atlasTexture = Java2DTexture(atlas.width, atlas.height)
    val typesetter = Typesetter(bitfont)

    override val title: String
        get() = "${bitfont.name}: Testing"

    var testString: String = ""
    var scale = 2
        set(value) {
            field = max(value, 1)
        }
    var canvas = Rect()

    init {
        val g = atlasTexture.edit()
        g.drawImage(atlas.image(), 0, 0, null)

        atlasTexture.filters = false
    }

    override fun main(): Unit = with(ImGui) {
        withItemWidth(win.contentsRegionRect.width - 300f) {
            val arr = testString.replace("\n", "\\n").toCharArray().let { name ->
                CharArray(name.size + 1000).also { name.copyInto(it) }
            }
            if(inputText("##testText", arr, InputTextFlag.EnterReturnsTrue.i))
                testString = String(g.inputTextState.textW.sliceArray(0 until g.inputTextState.textW.indexOf('\u0000'))).replace("\\n", "\n")
        }
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

    fun drawCanvas() {
        drawList.addRectFilled(
            canvas.min,
            canvas.max,
            Colors.layoutTest.background.u32
        )

        val cursor = canvas.min + Vec2(bitfont.lineHeight) * scale - Vec2(0.5)
        drawList.addTriangleFilled(cursor, cursor + Vec2(-scale, -scale), cursor + Vec2(-scale, scale), Colors.layoutTest.originIndicator.u32)

        val layout = typesetter.typeset(testString)
        layout.characters.forEach { char ->
            drawGlyph(char)
        }
    }

    fun drawGlyph(char: TypesetCharacter) {
        val grid = char.glyph?.image ?: return
        val minPos = canvas.min + (char.glyphPos + Vec2(bitfont.lineHeight)) * scale
        val maxPos = minPos + Vec2(scale * grid.width, scale * grid.height)
        val texCoords = atlas.texCoords(char.codepoint)
        drawList.addImage(atlasTexture.texID, minPos, maxPos, Vec2(texCoords.xy), Vec2(texCoords.z, texCoords.w))
        drawList.addRect(minPos, maxPos, Colors.layoutTest.boundingBoxes.u32)
        drawList.addLine(minPos, maxPos, Colors.layoutTest.boundingBoxes.u32)
    }
}