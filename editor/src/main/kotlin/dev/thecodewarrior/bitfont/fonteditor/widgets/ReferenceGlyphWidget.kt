package dev.thecodewarrior.bitfont.fonteditor.widgets

import dev.thecodewarrior.bitfont.fonteditor.NuklearFonts
import dev.thecodewarrior.bitfont.fonteditor.utils.Dirtyable
import dev.thecodewarrior.bitfont.fonteditor.utils.DrawList
import dev.thecodewarrior.bitfont.fonteditor.utils.Freeable
import dev.thecodewarrior.bitfont.fonteditor.utils.ReferenceGlyph
import org.lwjgl.nuklear.NkContext
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class ReferenceGlyphWidget(): NkWidget(), Freeable, Dirtyable {
    var guides = GuideSet()
    val glyph = ReferenceGlyph(NuklearFonts.sans)
    var hardPaddingX: Float by dirtying(0.05f)
    var hardPaddingY: Float by dirtying(0.05f)
    var paddingX: Float by dirtying(0.1f)
    var paddingY: Float by dirtying(0.15f)
    var codepoint: Int by dirtying(0)

    private val backgroundColor = Color.BLACK
    private val axisColor = Color(0xf58231)
    private val guideColor = Color(0x4363d8)
    private val glyphColor = Color.WHITE
    private val markingsColor = Color(0x3cb44b)

    private var glyphX: Float = 0f
    private var glyphY: Float = 0f

    private var dirty: Boolean = false
    override fun markDirty() { dirty = true }

    override fun resize(width: Float, height: Float) {
        markDirty()
    }

    override fun draw(
        ctx: NkContext, drawList: DrawList,
        rom: Boolean,
        width: Float, height: Float,
        mouseX: Float, mouseY: Float
    ) {
        if(dirty) {
            dirty = false
            val fullWidth = width
            val paddingX = width * this.paddingX
            val hardPaddingX = width * this.hardPaddingX

            val fullHeight = height
            val paddingY = height * this.paddingY
            val hardPaddingY = height * this.hardPaddingY
            glyph.loadGlyph(codepoint) { metrics ->

                // the default scale based on the font metrics
                var scale = (fullHeight - paddingY * 2) / (metrics.capHeight - metrics.descender)

                val top = max(metrics.capHeight, metrics.top)
                val bottom = min(metrics.descender, metrics.bottom)

                val baseline = paddingY + metrics.capHeight * scale
                if(baseline - top * scale < hardPaddingY) {
                    scale = (baseline - hardPaddingY) / top
                }
                if(baseline - bottom * scale > fullHeight - hardPaddingY) {
                    scale = (fullHeight - baseline - hardPaddingY) / -bottom
                }
                glyphY = baseline

                val left = min(metrics.left, 0f)
                val right = max(metrics.right, metrics.advance)

                glyphX = paddingX
                if(paddingX + right * scale > fullWidth - hardPaddingX) {
                    scale = (fullWidth - paddingX - hardPaddingX) / right
                }
                if(paddingX + left * scale < hardPaddingX) {
                    scale = min(scale, (fullWidth - hardPaddingX * 2) / (right - left))
                    glyphX = hardPaddingX + -left * scale
                }

                scale
            }
        }
        drawList.fillRect(0, 0, width, height, 0, backgroundColor)
        val metrics = glyph.metrics

        if(metrics == null) {
            val paddingX = width * this.paddingX
            if(guides.baseline && guides.whileBlank)
                drawList.strokeLine(0f, glyphY, width, glyphY, guides.thickness, axisColor)
            if(guides.yAxis && guides.whileBlank)
            drawList.strokeLine(paddingX, 0, paddingX, height, guides.thickness, axisColor)
        } else {
            if(guides.capHeight)
                drawList.strokeLine(0f, glyphY - metrics.capHeight, width, glyphY - metrics.capHeight, guides.thickness, guideColor)
            if(guides.baseline) {
                if(guides.advance) {
                    drawList.strokeLine(0f, glyphY, glyphX, glyphY, guides.thickness, axisColor)
                    drawList.strokeLine(glyphX + metrics.advance, glyphY, width, glyphY, guides.thickness, axisColor)
                } else {

                    drawList.strokeLine(0, glyphY, width, glyphY, guides.thickness, axisColor)
                }
            }
            if(guides.xHeight)
            drawList.strokeLine(0f, glyphY - metrics.xHeight, width, glyphY - metrics.xHeight, guides.thickness, guideColor)
            if(guides.descender)
            drawList.strokeLine(0f, glyphY - metrics.descender, width, glyphY - metrics.descender, guides.thickness, guideColor)
            if(guides.yAxis)
                drawList.strokeLine(glyphX, 0f, glyphX, height, guides.thickness, axisColor)
            if(guides.advance) {
                drawList.strokeLine(
                    glyphX + 0.5, glyphY,
                    glyphX + metrics.advance, glyphY,
                    guides.thickness,
                    markingsColor
                )
                drawList.strokeLine(
                    glyphX + metrics.advance, glyphY - 3,
                    glyphX + metrics.advance, glyphY + 3,
                    2, markingsColor
                )
            }
            glyph.fill(drawList, glyphX, glyphY, 1f, glyphColor)
        }
    }

    override fun free() {
        glyph.free()
    }

    data class GuideSet(
        var thickness: Float = 1f,
        /**
         * Whether to still show guides when there's no glyph
         */
        var whileBlank: Boolean = true,
        var baseline: Boolean = false,
        var yAxis: Boolean = false,
        var capHeight: Boolean = false,
        var xHeight: Boolean = false,
        var descender: Boolean = false,
        var advance: Boolean = false,
    ) {
        constructor(thickness: Float = 1f, all: Boolean = false, whileBlank: Boolean = all): this(
            thickness,
            whileBlank,
            all, all, all, all, all, all
        )
    }
}