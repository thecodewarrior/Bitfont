package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.data.Glyph
import dev.thecodewarrior.bitfont.editor.utils.DrawList
import dev.thecodewarrior.bitfont.typesetting.AttributedGlyph
import dev.thecodewarrior.bitfont.utils.Vec2i
import org.lwjgl.nuklear.NkColor
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkImage
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.abs

/**
 * An abstract base class for windows that have a font test area (e.g. the typesetter and text layout test windows)
 */
abstract class AbstractFontTestWindow(width: Float, height: Float): Window(width, height) {
    private var testTextureWidth = 32
    private var testTextureHeight = 32
    private val testTextureID = GL11.glGenTextures()
    private val testTextureImage = NkImage.malloc()
    private var testTextureBufferedImage = BufferedImage(testTextureWidth, testTextureHeight, BufferedImage.TYPE_INT_ARGB)
    private var testTextureBuffer = MemoryUtil.memAlloc(testTextureWidth * testTextureHeight * 4)
    private val testAreaDrawList = DrawList()

    private var isDirty = true

    protected var testAreaScale = 3

    init {
        flags = flags or NK_WINDOW_NO_SCROLLBAR or NK_WINDOW_SCALABLE or NK_WINDOW_CLOSABLE

        // initialize the texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, testTextureID)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        testTextureImage.handle { it.id(testTextureID) }
        ensureTextureSize(64, 64)
    }

    abstract fun pushControls(ctx: NkContext)
    abstract fun redraw(logicalWidth: Int, logicalHeight: Int, image: BufferedImage, drawList: DrawList)

    fun markDirty() {
        isDirty = true
    }

    fun drawGlyph(image: BufferedImage, glyph: Glyph, posX: Int, posY: Int, color: Int) {
        val glyphX = posX + glyph.bearingX
        val glyphY = posY + glyph.bearingY
        for(x in 0 until glyph.image.width) {
            for(y in 0 until glyph.image.height) {
                if(glyph.image[x, y]) {
                    val pixelX = glyphX + x
                    val pixelY = glyphY + y
                    if(pixelX >= 0 && pixelX < image.width && pixelY >= 0 && pixelY < image.height) {
                        image.setRGB(pixelX, pixelY, color)
                    }
                }
            }
        }
    }

    // ========= implementation stuff =========

    override fun pushContents(ctx: NkContext) {
        pushControls(ctx)
        MemoryStack.stackPush().use { stack ->
            // === Compute the remaining bounding box ===
            // create a zero-height layout space to get the top-left corner
            nk_layout_space_begin(ctx, NK_DYNAMIC, 0f, 1)
            val remainingBounds = NkRect.mallocStack(stack)
            nk_layout_widget_bounds(ctx, remainingBounds)

            // calculate the bottom-right corner of the window
            val contentRegion = NkRect.mallocStack(stack)
            nk_window_get_content_region(ctx, contentRegion)
            remainingBounds.h(contentRegion.h() + contentRegion.y() - remainingBounds.y())

            // apply padding
            val padding = ctx.style().window().padding()
            remainingBounds.x(remainingBounds.x() + padding.x())
            remainingBounds.w(remainingBounds.w() - 2 * padding.x())
            remainingBounds.h(remainingBounds.h() - padding.y()) // bottom padding, otherwise it gets clipped

            if(isDirty)
                doRedraw(remainingBounds)

            remainingBounds.w((testTextureWidth * testAreaScale).toFloat())
            remainingBounds.h((testTextureHeight * testAreaScale).toFloat())

            testTextureImage.w(0.toShort())
            testTextureImage.h(0.toShort())
            val canvas = nk_window_get_canvas(ctx)!!
            val white = NkColor.mallocStack(stack)
            white.set(255.toByte(), 255.toByte(), 255.toByte(), 255.toByte())
            nk_draw_image(canvas, remainingBounds, testTextureImage, white)
            testAreaDrawList.transformX = remainingBounds.x()
            testAreaDrawList.transformY = remainingBounds.y()
            testAreaDrawList.transformScale = testAreaScale.toFloat()
            testAreaDrawList.push(ctx)

            nk_layout_space_end(ctx)
        }
    }

    /**
     * Call [redraw] and perform related tasks
     */
    private fun doRedraw(remainingBounds: NkRect) {
        val logicalWidth = (remainingBounds.w() / testAreaScale).toInt()
        val logicalHeight = (remainingBounds.h() / testAreaScale).toInt()
        ensureTextureSize(logicalWidth, logicalHeight)

        for(y in 0 until testTextureHeight) {
            for(x in 0 until testTextureWidth) {
                testTextureBufferedImage.setRGB(x, y, 0)
            }
        }

        testAreaDrawList.clear()
        redraw(logicalWidth, logicalHeight, testTextureBufferedImage, testAreaDrawList)

        val pixels = testTextureBufferedImage.getRGB(0, 0, testTextureWidth, testTextureHeight, null, 0, testTextureWidth)
        for(i in pixels.indices) {
            val rgb = pixels[i]
            testTextureBuffer.put(i * 4 + 0, (rgb ushr 16).toByte())
            testTextureBuffer.put(i * 4 + 1, (rgb ushr 8).toByte())
            testTextureBuffer.put(i * 4 + 2, (rgb ushr 0).toByte())
            testTextureBuffer.put(i * 4 + 3, (rgb ushr 24).toByte())
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, testTextureID)
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, testTextureWidth, testTextureHeight, GL11.GL_RGBA,
            GL12.GL_UNSIGNED_INT_8_8_8_8_REV, testTextureBuffer)

        isDirty = false
    }

    /**
     * Ensure the texture is at least [width] x [height] pixels in size, resizing it if necessary
     *
     *
     */
    private fun ensureTextureSize(width: Int, height: Int) {
        if (width <= testTextureWidth && height <= testTextureHeight)
            return
        if (width > testTextureWidth)
            testTextureWidth *= 2
        if (height > testTextureHeight)
            testTextureHeight *= 2

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, testTextureID)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, testTextureWidth, testTextureHeight, 0, GL11.GL_RGBA,
            GL12.GL_UNSIGNED_INT_8_8_8_8_REV, 0)
//        testTextureImage.w(testTextureWidth.toShort())
//        testTextureImage.h(testTextureHeight.toShort())
        testTextureBufferedImage = BufferedImage(testTextureWidth, testTextureHeight, BufferedImage.TYPE_INT_ARGB)
        MemoryUtil.memFree(testTextureBuffer)
        testTextureBuffer = MemoryUtil.memAlloc(testTextureWidth * testTextureHeight * 4)
    }

    override fun free() {
        GL11.glDeleteTextures(testTextureID)
        testTextureImage.free()
    }
}
