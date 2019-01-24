package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.utils.Color
import games.thecodewarrior.bitfont.utils.Constants
import games.thecodewarrior.bitfont.utils.ReferenceFonts
import games.thecodewarrior.bitfont.utils.alignedText
import games.thecodewarrior.bitfont.utils.extensions.ImGuiDrags
import games.thecodewarrior.bitfont.utils.glyphProfile
import games.thecodewarrior.bitfont.utils.opengl.Java2DTexture
import glm_.func.common.clamp
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import imgui.Dir
import imgui.ImGui
import imgui.WindowFlag
import imgui.functionalProgramming.withChild
import imgui.functionalProgramming.withItemWidth
import imgui.internal.Rect
import java.awt.Color
import java.awt.Graphics2D
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class GlyphBrowserWindow(val document: BitfontDocument): IMWindow() {
    val bitfont = document.bitfont

    override val title: String
        get() = "${bitfont.name}: Glyphs"

    init {
        windowFlags.add(WindowFlag.AlwaysAutoResize)
    }

    var page = 0
        set(value) {
            if(value != field)
                needsRedraw = true
            field = value
        }
    var needsRedraw = true
    var canvas = Rect()
    val controlsWidth: Float = 175f

    var referenceStyle = 0
        set(value) {
            if(value != field)
                needsRedraw = true
            field = value
        }
    val cellSize = 32
    val referenceImages = Java2DTexture(cellSize*16, cellSize*16)

    var wasHovered = false

    override fun main() = with(ImGui) {
        if(isWindowHovered() && !wasHovered) needsRedraw = true
        wasHovered = isWindowHovered()

        var menuHeight = -cursorPos
        val contentRect = win.contentsRegionRect
        menuHeight = menuHeight + cursorPos
        withChild("Controls", Vec2(controlsWidth, contentRect.height), false) {
            drawControls()
        }

        sameLine()

        val canvasPos = contentRect.min + Vec2(controlsWidth + 5, menuHeight.y)
        canvas = Rect(canvasPos, canvasPos + Vec2(cellSize*16))
        itemSize(canvas)
        pushClipRect(canvas.min, canvas.max, true)
        itemHoverable(canvas, "canvas".hashCode())
        itemAdd(canvas, "canvas".hashCode())
        drawCanvas()
        popClipRect()
    }

    fun drawControls() = with(ImGui) { withItemWidth(controlsWidth) {
        pushButtonRepeat(true)
        if (arrowButton("##left", Dir.Left)) page--
        sameLine()
        withItemWidth(controlsWidth - frameHeight*2 - style.itemSpacing.x*2) {
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

        alignedText("Reference Font", Vec2(0.5), width = controlsWidth)
        listBox("##style", ::referenceStyle,
            ReferenceFonts.styles, 3)
    } }

    fun drawCanvas() = with(ImGui) {
        drawList.addRectFilled(
            canvas.min,
            canvas.max,
            Constants.editorBackground
        )

        if(needsRedraw) {
            val g = referenceImages.edit(true, true)
            g.color = Color("3b3b46")
            for(x in 0..15) {
                g.drawLine(x*cellSize, 0, x*cellSize, cellSize*16)
            }
            for(y in 0..15) {
                g.drawLine(0, y*cellSize, cellSize*16, y*cellSize)
            }

            for(x in 0..15) {
                for(y in 0..15) {
                    drawCell(g, x, y)
                }
            }
        }

        val bb = Rect(canvas.min, canvas.min + Vec2(cellSize*16))
        drawList.addImage(referenceImages.texID, bb.min, bb.max)
        needsRedraw = false

        val cursorPos = cell(io.mousePos - canvas.min)
        if(cursorPos.x in 0 .. 15 && cursorPos.y in 0 .. 15) {
            drawList.addRect(canvas.min + pos(cursorPos), canvas.min + pos(cursorPos + Vec2i(1)), Constants.SimpleColors.red)

            if(isWindowHovered() && isMouseClicked(0)) {
                val codepoint = (page shl 8) or (cursorPos.y shl 4) or cursorPos.x
                document.editorWindow.codepoint = codepoint
                document.editorWindow.visible = true
                document.editorWindow.focus()
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

        val codepoint = (page shl 8) or (y shl 4) or x
        val chr = String(Character.toChars(codepoint))

        val font = ReferenceFonts.style(referenceStyle)[codepoint]

        val metrics = font.getLineMetrics(chr, frc)
        val profile = font.createGlyphVector(frc, chr)


        val scale = cellSize / (metrics.ascent + metrics.descent)
        val baseline = metrics.ascent * scale
        val origin = Vec2i(x*cellSize, y*cellSize) + Vec2i((cellSize - profile.visualBounds.width*scale)/2 - profile.visualBounds.minX*scale, baseline)

        g.color = Constants.SimpleColors.maroonAwt
        g.drawLine(x*cellSize, origin.y, (x+1)*cellSize, origin.y)
        if(bitfont.glyphs[codepoint]?.isEmpty() == false)
            g.color = Color.WHITE
        else
            g.color = Color("afafaf")
        g.font = font.deriveFont(scale)
        g.drawString(String(Character.toChars(codepoint)), origin.x, origin.y)
    }
}