package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.ReferenceFonts
import games.thecodewarrior.bitfont.editor.utils.extensions.ImGuiDrags
import games.thecodewarrior.bitfont.editor.utils.extensions.u32
import games.thecodewarrior.bitfont.editor.utils.opengl.Java2DTexture
import glm_.func.common.clamp
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import imgui.Dir
import imgui.ImGui
import imgui.WindowFlag
import imgui.functionalProgramming.withChild
import imgui.functionalProgramming.withItemWidth
import imgui.internal.Rect
import java.awt.Graphics2D
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import kotlin.math.floor
import kotlin.math.roundToInt

class CharacterBrowserWindow(val document: BitfontDocument): IMWindow() {
    val bitfont = document.bitfont

    override val title: String
        get() = "${bitfont.name}: Glyphs"

    init {
        windowFlags.add(WindowFlag.AlwaysAutoResize)
    }

    var page = 0
        set(value) {
            if(value.clamp(0, 0x10ff) != field)
                needsRedraw = true
            field = value.clamp(0, 0x10ff)
        }
    var needsRedraw = true
    var canvas = Rect()
    val detail = CharacterDetailPane(document)
    var lastSelectTime = 0L

    val cellSize = 32
    val referenceImages = Java2DTexture(cellSize*17, cellSize*17)

    var wasHovered = false

    override fun main() = with(ImGui) {
        if(isWindowHovered() && !wasHovered) needsRedraw = true
        wasHovered = isWindowHovered()

        var menuHeight = -cursorPos
        val contentRect = win.contentsRegionRect
        menuHeight = menuHeight + cursorPos
        withChild("Controls", Vec2(detail.width, contentRect.height), false) {
            drawControls()
        }

        sameLine()

        val canvasPos = contentRect.min + Vec2(detail.width + 5, menuHeight.y)
        canvas = Rect(canvasPos, canvasPos + Vec2(cellSize*17))
        itemSize(canvas)
        pushClipRect(canvas.min, canvas.max, true)
        itemHoverable(canvas, "canvas".hashCode())
        itemAdd(canvas, "canvas".hashCode())
        drawCanvas()
        popClipRect()
    }

    fun drawControls() = with(ImGui) { withItemWidth(detail.width) {
        pushButtonRepeat(true)
        if (arrowButton("##left", Dir.Left)) page--
        sameLine()
        withItemWidth(detail.width - frameHeight*2 - style.itemSpacing.x*2) {
            ImGuiDrags.dragScalar(
                label = "##pageDrag",
                value = ::page,
                speed = 0.5,
                power = 1.0,
                minValue = 0,
                maxValue = 0x10FFFF,
                valueToDisplay = { "U+%04X".format(it) },
                correct = { it.roundToInt() },
                valueToString = { "%04X".format(it) },
                stringToValue = { current, new -> (new.toIntOrNull(16) ?: current) to "" }
            )
        }
        sameLine()
        if (arrowButton("##right", Dir.Right)) page++
        popButtonRepeat()

        separator()

        detail.draw()
    } }

    fun drawCanvas() = with(ImGui) {
        drawList.addRectFilled(
            canvas.min,
            canvas.max,
            Colors.browser.background.u32
        )

        if(needsRedraw) {
            val g = referenceImages.edit(true, true)
            g.color = Colors.browser.gridLines
            for(x in 1..16) {
                g.drawLine(x*cellSize, cellSize, x*cellSize, cellSize*17)
            }
            for(y in 1..16) {
                g.drawLine(cellSize, y*cellSize, cellSize*17, y*cellSize)
            }

            for(x in 0..16) {
                for(y in 0..16) {
                    drawCell(g, x, y)
                }
            }
        }

        val bb = Rect(canvas.min, canvas.min + Vec2(cellSize*17))
        drawList.addImage(referenceImages.texID, bb.min, bb.max)
        needsRedraw = false

        val cursorPos = cell(io.mousePos - canvas.min)
        if(cursorPos.x in 1 .. 16 && cursorPos.y in 1 .. 16) {
            drawList.addRect(canvas.min + pos(cursorPos), canvas.min + pos(cursorPos + Vec2i(1)), Colors.browser.cellHighlight.u32)

            if(isWindowHovered() && isMouseClicked(0)) {
                val codepoint = (page shl 8) or ((cursorPos.y-1) shl 4) or (cursorPos.x-1)
                detail.codepoint = codepoint
                if(System.currentTimeMillis() - lastSelectTime < 250) {
                    document.editorWindow.index = codepoint
                    document.editorWindow.visible = true
                    document.editorWindow.focus()
                }
                lastSelectTime = System.currentTimeMillis()
            }
        }
    }

    fun cell(relativeMouse: Vec2): Vec2i {
        val gridScaled = relativeMouse / cellSize
        return Vec2i(floor(gridScaled.x).toInt(), floor(gridScaled.y).toInt())
    }

    fun pos(cell: Vec2i): Vec2 {
        return Vec2(cell * cellSize)
    }

    fun pos(point: Vec2): Vec2 {
        return point * cellSize
    }

    fun drawCell(g: Graphics2D, x: Int, y: Int) {
        val frc = FontRenderContext(AffineTransform(), true, true)

        val codepoint: Int = when {
            x == 0 -> " 0123456789ABCDEF"[y].toInt()
            y == 0 -> " 0123456789ABCDEF"[x].toInt()
            else -> (page shl 8) or ((y-1) shl 4) or (x-1)
        }
        val chr = String(Character.toChars(codepoint))

        val font = ReferenceFonts.style(document.referenceStyle)[codepoint]

        val metrics = font.getLineMetrics(chr, frc)
        val profile = font.createGlyphVector(frc, chr)


        val scale = cellSize / (metrics.ascent + metrics.descent)
        val baseline = metrics.ascent * scale
        val origin = Vec2i(x*cellSize, y*cellSize) + Vec2i((cellSize - profile.visualBounds.width*scale)/2 - profile.visualBounds.minX*scale, baseline)

        if(x == 0 || y == 0) {
            g.color = Colors.browser.hexLabels
        } else {
            g.color = Colors.browser.baseline
            g.drawLine(x * cellSize, origin.y, (x + 1) * cellSize, origin.y)
            if (bitfont.glyphs[codepoint]?.isEmpty() == false)
                g.color = Colors.browser.glyph
            else
                g.color = Colors.browser.missingGlyph
        }
        g.font = font.deriveFont(scale)
        g.drawString(String(Character.toChars(codepoint)), origin.x, origin.y)
    }
}