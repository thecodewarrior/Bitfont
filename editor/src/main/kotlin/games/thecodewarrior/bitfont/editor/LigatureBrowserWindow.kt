package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.data.BitGrid
import games.thecodewarrior.bitfont.data.Glyph
import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.extensions.BitVec2i
import games.thecodewarrior.bitfont.editor.utils.extensions.draw
import games.thecodewarrior.bitfont.editor.utils.extensions.toIm
import games.thecodewarrior.bitfont.editor.utils.extensions.u32
import games.thecodewarrior.bitfont.typesetting.AttributedString
import games.thecodewarrior.bitfont.typesetting.MutableAttributedString
import games.thecodewarrior.bitfont.typesetting.TypesetString
import glm_.vec2.Vec2
import imgui.ImGui
import imgui.internal.Rect
import imgui.functionalProgramming.button
import org.lwjgl.glfw.GLFW
import kotlin.math.max
import kotlin.math.min

class LigatureBrowserWindow(val document: BitfontDocument): IMWindow() {
    val bitfont = document.bitfont

    override val title: String
        get() = "${bitfont.name}: Ligatures"

    var previewString: MutableAttributedString = MutableAttributedString("")
    var typesetString = TypesetString(bitfont, previewString, -1)
    val scale: Int
        get() {
            return max(1, (canvas.height - 10).toInt()/typesetString.bounds.y)
        }
    var canvas = Rect()

    var needsRedraw = false
    var wasHovered = false
    var selectedLigature: String? = null

    override fun main() = with(ImGui) {
        if(isWindowHovered() && !wasHovered) needsRedraw = true
        wasHovered = isWindowHovered()

        val canvasPos = win.contentsRegionRect.min
        canvas = Rect(canvasPos, canvasPos + Vec2(win.contentsRegionRect.width, 150))
        itemSize(canvas)
        pushClipRect(canvas.min, canvas.max, true)
        itemHoverable(canvas, "canvas".hashCode())
        itemAdd(canvas, "canvas".hashCode())
        drawCanvas()
        popClipRect()
        button("Edit Glyph") {
            selectedLigature?.also { lig ->
                if(lig !in bitfont.ligatures) return@also
                val index = bitfont.ligatures.getInt(lig)

                document.editorWindow.index = index
                document.editorWindow.visible = true
                document.editorWindow.focus()
            }
        }
        sameLine()
        button("Delete Ligature") {
            selectedLigature?.also { lig ->
                if(lig !in bitfont.ligatures) return@also
                val index = bitfont.ligatures.getInt(lig)
                bitfont.glyphs.remove(index)
                bitfont.ligatures.removeInt(lig)

                document.editorWindow.index = 0
                document.editorWindow.dataMap.remove(index)
                selectedLigature = null
            }
        }

        columns(3, "", false)

        if(selectable("New from clipboard", false))
            (new@{
                val key = GLFW.glfwGetClipboardString(0) ?: return@new
                if(key in bitfont.ligatures) {
                    selectedLigature = key
                    return@new
                }

                val index = bitfont.nextSpecialGlyph()
                bitfont.glyphs[index] = createLigatureGlyph(key)
                bitfont.ligatures[key] = index
                selectedLigature = key

                document.editorWindow.index = index
                document.editorWindow.visible = true
                document.editorWindow.focus()
            })()
        nextColumn()

        bitfont.ligatures.object2IntEntrySet()
            .sortedBy { it.key }
            .forEach { (key, glyphIndex) ->
                if(selectable("$key##ligature_$glyphIndex", selectedLigature == key))
                    selectedLigature = key
                nextColumn()
            }

        columns(1)
    }

    fun createLigatureGlyph(key: String): Glyph {
        val assembledGlyph = Glyph()
        val typesetString = TypesetString(bitfont, AttributedString(key), -1)

        val baseline = typesetString.glyphs.firstOrNull()?.pos?.y ?: return assembledGlyph

        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var maxY = Int.MIN_VALUE
        typesetString.glyphs.forEach { glyph ->
            val minPos = glyph.pos + glyph.glyph.bearing
            minX = min(minX, minPos.x)
            minY = min(minY, minPos.y)
            val maxPos = minPos + glyph.glyph.image.size
            maxX = max(maxX, maxPos.x)
            maxY = max(maxY, maxPos.y)
        }
        val min = BitVec2i(minX, minY)
        val max = BitVec2i(maxX, maxY)

        val image = BitGrid(maxX-minX, maxY-minY)
        typesetString.glyphs.forEach { glyph ->
            image.draw(glyph.glyph.image, glyph.pos + glyph.glyph.bearing - min)
        }

        assembledGlyph.image = image
        assembledGlyph.bearingX = minX
        assembledGlyph.bearingY = minY - baseline
        val last = typesetString.glyphs.last()
        assembledGlyph.advance = last.pos.x + last.glyph.calcAdvance(0)

        return assembledGlyph
    }

    fun drawCanvas() {
        drawList.addRectFilled(
            canvas.min,
            canvas.max,
            Colors.layoutTest.background.u32
        )

        val selectedLigature = selectedLigature ?: return

        typesetString = TypesetString(bitfont, AttributedString(selectedLigature), -1)

        typesetString.glyphs.forEach {
            drawGlyph(it)
        }
    }

    fun drawGlyph(char: TypesetString.GlyphRender) {
        val textOrigin = canvas.min + Vec2(5, 5)
        char.glyph.draw(textOrigin + char.pos.toIm() * scale, scale, Colors.white.u32)
    }
}