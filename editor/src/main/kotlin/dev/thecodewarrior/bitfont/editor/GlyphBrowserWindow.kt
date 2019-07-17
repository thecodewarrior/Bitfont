package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.utils.Colors
import dev.thecodewarrior.bitfont.editor.utils.ReferenceFonts
import dev.thecodewarrior.bitfont.editor.utils.extensions.clamp
import dev.thecodewarrior.bitfont.editor.utils.extensions.im
import dev.thecodewarrior.bitfont.editor.utils.math.Rect
import dev.thecodewarrior.bitfont.editor.utils.math.Vec2
import dev.thecodewarrior.bitfont.editor.utils.math.rect
import dev.thecodewarrior.bitfont.editor.utils.math.vec
import dev.thecodewarrior.bitfont.editor.utils.opengl.Java2DTexture
import dev.thecodewarrior.bitfont.utils.Vec2i
import org.ice1000.jimgui.flag.JImDirection
import org.ice1000.jimgui.flag.JImHoveredFlags
import org.ice1000.jimgui.flag.JImWindowFlags
import java.awt.Color
import java.awt.Graphics2D
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import kotlin.math.floor
import kotlin.math.roundToInt

class GlyphBrowserWindow(val document: BitfontDocument): IMWindow() {
    val bitfont = document.bitfont

    override val title: String
        get() = "${bitfont.name}: Glyphs"

    init {
        windowFlags = JImWindowFlags.AlwaysAutoResize
    }

    var page = 0
        set(value) {
            if(value.clamp(0, 0x10ff) != field)
                needsRedraw = true
            field = value.clamp(0, 0x10ff)
        }
    var needsRedraw = true
    var canvas = rect(0, 0, 0, 0)
    val detail = GlyphDetailPane(document)
    var lastSelectTime = 0L

    val cellSize = 32
    val referenceImages = Java2DTexture(cellSize*17, cellSize*17)

    var wasHovered = false

    override fun main(imgui: ImGui) {
        if (imgui.isWindowHovered(JImHoveredFlags.Default) && !wasHovered) needsRedraw = true
        wasHovered = imgui.isWindowHovered(JImHoveredFlags.Default)

        var menuHeight = -imgui.cursorPos
        val contentRect = imgui.windowContentRegionRect
        menuHeight += imgui.cursorPos
        if (imgui.beginChild("Controls".hashCode(), detail.width, contentRect.heightf, false)) {
            drawControls(imgui)
            imgui.endChild()
        }

        imgui.sameLine()

        val canvasPos = contentRect.min + vec(detail.width + 5, menuHeight.y)
        canvas = rect(canvasPos, canvasPos + vec(cellSize*17, cellSize*17))
        imgui.pushClipRect(canvas.min.xf, canvas.min.yf, canvas.max.xf, canvas.max.yf, true)
        imgui.rect(canvas.widthf, canvas.heightf, Color(0, 0, 0, 0).im, 0f, 0f)
        drawCanvas(imgui)
        imgui.popClipRect()
    }

    fun drawControls(imgui: ImGui) {
        imgui.pushItemWidth(detail.width)
        imgui.pushButtonRepeat(true)
        if (imgui.arrowButton("##left", JImDirection.Left)) page--
        imgui.sameLine()

        imgui.pushItemWidth(detail.width - imgui.frameHeight*2 - imgui.style.itemSpacingX*2)
//            ImGuiDrags.dragScalar(
//                label = "##pageDrag",
//                value = ::page,
//                speed = 0.5,
//                power = 1.0,
//                minValue = 0,
//                maxValue = 0x10FFFF,
//                valueToDisplay = { "U+%04X".format(it) },
//                correct = { it.roundToInt() },
//                valueToString = { "%04X".format(it) },
//                stringToValue = { current, new -> (new.toIntOrNull(16) ?: current) to "" }
//            )
        imgui.popItemWidth()

        imgui.sameLine()
        if (imgui.arrowButton("##right", JImDirection.Right)) page++
        imgui.popButtonRepeat()

        imgui.separator()

        detail.draw(imgui)
        imgui.popItemWidth()
    }

    private fun drawCanvas(imgui: ImGui) {
        imgui.windowDrawList.addRectFilled(
            canvas.min.xf, canvas.min.yf,
            canvas.max.xf, canvas.max.yf,
            Colors.browser.background.rgb
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

        val bb = Rect(canvas.min, canvas.min + vec(cellSize*17, cellSize*17))
//        drawList.addImage(referenceImages.texID, bb.min, bb.max)
        needsRedraw = false

        val cursorPos = cell(imgui.io.mousePos - canvas.min)
        if(cursorPos.x in 1 .. 16 && cursorPos.y in 1 .. 16) {
            dev.thecodewarrior.bitfont.editor.IMWindow.Companion.drawList.addRect(
                canvas.min + pos(cursorPos),
                canvas.min + pos(cursorPos + Vec2i(1, 1)),
                Colors.browser.cellHighlight.rgb
            )

            if(imgui.isWindowHovered(JImHoveredFlags.Default) && imgui.isMouseClicked(0)) {
                val codepoint = (page shl 8) or ((cursorPos.y-1) shl 4) or (cursorPos.x-1)
                detail.codepoint = codepoint
                if(System.currentTimeMillis() - lastSelectTime < 250) {
//                    document.editorWindow.codepoint = codepoint
//                    document.editorWindow.codepointHistory.push(codepoint)
//                    document.editorWindow.visible = true
//                    document.editorWindow.focus()
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
        return vec(cell.x * cellSize, cell.y * cellSize)
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
        val origin = Vec2i(x*cellSize, y*cellSize) + Vec2i(( (cellSize - profile.visualBounds.width*scale)/2 - profile.visualBounds.minX*scale ).toInt(), baseline.toInt())

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