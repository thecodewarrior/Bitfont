package dev.thecodewarrior.bitfont.fonteditor.utils

import com.ibm.icu.lang.UCharacter
import com.ibm.icu.lang.UCharacterCategory
import dev.thecodewarrior.bitfont.fonteditor.FontList
import dev.thecodewarrior.bitfont.fonteditor.GlyphInfo
import dev.thecodewarrior.bitfont.fonteditor.GlyphMetrics
import org.lwjgl.nuklear.NkCommandBuffer
import org.lwjgl.nuklear.NkImage
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.stb.STBTTVertex
import org.lwjgl.stb.STBTruetype.STBTT_vcubic
import org.lwjgl.stb.STBTruetype.STBTT_vcurve
import org.lwjgl.stb.STBTruetype.STBTT_vline
import org.lwjgl.stb.STBTruetype.STBTT_vmove
import org.lwjgl.stb.STBTruetype.stbtt_FreeShape
import org.lwjgl.stb.STBTruetype.stbtt_GetCodepointShape
import org.lwjgl.system.MemoryStack
import java.awt.Color

class ReferenceGlyph(
    val fonts: FontList,
) {
    var metrics: GlyphMetrics? = null
        private set
    var shape: STBTTVertex.Buffer? = null
        private set

    val textureId = GL11.glGenTextures()
    val image: NkImage = NkImage.malloc()

    init {
        nk_image_id(textureId, image)
    }

    fun fill(drawList: DrawList, x: Float, y: Float, scale: Float, color: Color) {
        val metrics = metrics ?: return
        drawList.image(
            x + (metrics.left * scale), y + (-metrics.top * scale),
            (metrics.right - metrics.left) * scale, (metrics.top - metrics.bottom) * scale,
            image,
            color
        )
    }

    fun stroke(drawList: DrawList, x: Float, y: Float, scale: Float, thickness: Float, color: Color) {
        drawList.add(GlyphStrokeCommand(x, y, scale, thickness, color))
    }

    fun loadGlyph(codepoint: Int, height: Float) {
        this.metrics = null
        this.shape = null
        val codepointType = UCharacter.getType(codepoint).toByte()
        if(codepointType == UCharacterCategory.UNASSIGNED || codepointType == UCharacterCategory.PRIVATE_USE)
            return

        val metrics = getMetrics(codepoint)?.withHeight(height) ?: return
        val glyph = metrics.font.get(codepoint, height, 2, 2) ?: return

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, glyph.bitmapWidth, glyph.bitmapHeight, 0, GL11.GL_RGBA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, glyph.bitmap)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)

        shape = stbtt_GetCodepointShape(metrics.font.fontInfo, codepoint)

        this.metrics = metrics
    }

    fun getMetrics(codepoint: Int): GlyphMetrics? {
        if(codepoint == '\n'.toInt() || codepoint == '\r'.toInt())
            return null // otherwise it searches through every font
        for (font in fonts) {
            val glyph = font.getMetrics(codepoint)
            if (glyph != null) {
                return glyph
            }
        }
        return null
    }

    fun free() {
        GL11.glDeleteTextures(textureId)
        this.shape?.free()
    }

    inner class GlyphStrokeCommand(
        val x: Float,
        val y: Float,
        val scale: Float,
        val thickness: Float,
        val color: Color
    ): DrawList.DrawCommand {
        override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
            val nativeColor = nkColor(color, stack)
            val metrics = metrics ?: return
            val shape = shape ?: return

            var x0 = 0f
            var y0 = 0f
            for(vertex in shape) {
                when(vertex.type()) {
                    STBTT_vmove -> {
                        x0 = vertex.x() * metrics.unitScale
                        y0 = -vertex.y() * metrics.unitScale
                    }
                    STBTT_vline -> {
                        val x1 = vertex.x() * metrics.unitScale
                        val y1 = -vertex.y() * metrics.unitScale
                        nk_stroke_line(canvas,
                            drawList.tx(x + x0 * scale), drawList.ty(y + y0 * scale),
                            drawList.tx(x + x1 * scale), drawList.ty(y + y1 * scale),
                            thickness,
                            nativeColor
                        )
                        x0 = x1
                        y0 = y1
                    }
                    STBTT_vcurve -> {
                        val x1 = vertex.x() * metrics.unitScale
                        val y1 = -vertex.y() * metrics.unitScale
                        // quadratic control point
                        val qx = vertex.cx() * metrics.unitScale
                        val qy = -vertex.cy() * metrics.unitScale

                        // compute cubic control points
                        // see https://fontforge.org/docs/techref/bezier.html#converting-truetype-to-postscript
                        val cx0 = x0 + (qx - x0) * 2 / 3
                        val cy0 = y0 + (qy - y0) * 2 / 3
                        val cx1 = x1 + (qx - x1) * 2 / 3
                        val cy1 = y1 + (qy - y1) * 2 / 3

                        nk_stroke_curve(canvas,
                            drawList.tx(x + x0 * scale), drawList.ty(y + y0 * scale),
                            drawList.tx(x + cx0 * scale), drawList.ty(y + cy0 * scale),
                            drawList.tx(x + cx1 * scale), drawList.ty(y + cy1 * scale),
                            drawList.tx(x + x1 * scale), drawList.ty(y + y1 * scale),
                            thickness,
                            nativeColor
                        )
                        x0 = x1
                        y0 = y1
                    }
                    STBTT_vcubic -> {
                        val x1 = vertex.x() * metrics.unitScale
                        val y1 = -vertex.y() * metrics.unitScale
                        val cx = vertex.cx() * metrics.unitScale
                        val cy = -vertex.cy() * metrics.unitScale
                        val cx1 = vertex.cx1() * metrics.unitScale
                        val cy1 =- vertex.cy1() * metrics.unitScale
                        nk_stroke_curve(canvas,
                            drawList.tx(x + x0 * scale), drawList.ty(y + y0 * scale),
                            drawList.tx(x + cx * scale), drawList.ty(y + cy * scale),
                            drawList.tx(x + cx1 * scale), drawList.ty(y + cy1 * scale),
                            drawList.tx(x + x1 * scale), drawList.ty(y + y1 * scale),
                            thickness,
                            nativeColor
                        )
                        x0 = x1
                        y0 = y1
                    }
                }
            }
        }
    }
}
