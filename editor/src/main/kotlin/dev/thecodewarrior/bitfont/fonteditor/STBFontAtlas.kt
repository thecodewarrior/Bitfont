package dev.thecodewarrior.bitfont.fonteditor

import dev.thecodewarrior.bitfont.fonteditor.utils.Freeable
import dev.thecodewarrior.bitfont.fonteditor.utils.GlobalAllocations
import dev.thecodewarrior.bitfont.utils.RectanglePacker
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.lwjgl.nuklear.NkHandle
import org.lwjgl.nuklear.NkUserFont
import org.lwjgl.nuklear.NkUserFontGlyph
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype.stbtt_FindGlyphIndex
import org.lwjgl.stb.STBTruetype.stbtt_GetCodepointBitmapBoxSubpixel
import org.lwjgl.stb.STBTruetype.stbtt_GetCodepointBox
import org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics
import org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics
import org.lwjgl.stb.STBTruetype.stbtt_InitFont
import org.lwjgl.stb.STBTruetype.stbtt_MakeCodepointBitmapSubpixelPrefilter
import org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

class FontAtlas(val fonts: FontList, val fontHeight: Float) : Freeable {
    private var textureSize = generateSequence(64) { it * 2 }.first { it > fontHeight }

    private val gpuMaxTexSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE)

    val userFont = NkUserFont.malloc()

    private val fontTexID = GL11.glGenTextures()

    private val packer = RectanglePacker<GlyphInfo>(textureSize, textureSize, 1)
    private val metricsCache = Int2ObjectOpenHashMap<GlyphMetrics?>()
    private val cache = Int2ObjectOpenHashMap<GlyphInfo?>()

    private var debugImage: BufferedImage? = BufferedImage(textureSize, textureSize, BufferedImage.TYPE_INT_ARGB)

    init {
        GlobalAllocations.add(this)
        initTexture()
        GlobalAllocations.add(userFont)
            .width { handle: Long, h: Float, text: Long, len: Int ->
                var textWidth = 0f
                MemoryStack.stackPush().use { stack ->
                    val unicode = stack.mallocInt(1)
                    var index = 0
                    while (index < len) {
                        val codepointLength = nnk_utf_decode(text + index, MemoryUtil.memAddress(unicode), len - index)
                        if (codepointLength == 0 || unicode[0] == NK_UTF_INVALID) {
                            break
                        }

                        textWidth += this[unicode[0]]?.advance ?: 0f

                        index += codepointLength
                    }
                }
                textWidth
            }
            .height(fontHeight)
            .query { handle: Long, font_height: Float, glyph: Long, codepoint: Int, next_codepoint: Int ->
                val ufg = NkUserFontGlyph.create(glyph)
                this[codepoint]?.configure(ufg, textureSize)
            }
            .texture { it: NkHandle ->
                it.id(fontTexID)
            }
        reverseMap[userFont] = this
    }

    fun getMetrics(codepoint: Int): GlyphMetrics? {
        return metricsCache.getOrPut(codepoint) {
            if(codepoint == '\n'.toInt() || codepoint == '\r'.toInt())
                return@getOrPut null // otherwise it searches through every font
            for (font in fonts) {
                val glyph = font.getMetrics(codepoint)
                if (glyph != null) {
                    return@getOrPut glyph
                }
            }
            return@getOrPut null
        }
    }

    operator fun get(codepoint: Int): GlyphInfo? {
        return cache.getOrPut(codepoint) {
            if(codepoint == '\n'.toInt() || codepoint == '\r'.toInt())
                return@getOrPut null // otherwise it searches through every font
            for (font in fonts) {
                val glyph = font.get(codepoint, fontHeight, 4, 4)
                if (glyph != null) {
                    insert(glyph)
                    return@getOrPut glyph
                }
            }
            return@getOrPut null
        }
    }

    private fun insert(glyph: GlyphInfo) {
        var newRect = packer.insert(glyph.bitmapWidth, glyph.bitmapHeight, glyph)
        if (newRect == null) {
            expand()
            newRect = packer.insert(glyph.bitmapWidth, glyph.bitmapHeight, glyph) ?: return
        }
        glyph.rect = newRect
        draw(glyph)
    }

    private fun draw(glyph: GlyphInfo) {
        val rect = glyph.rect ?: return

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fontTexID)
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, rect.x, rect.y, glyph.bitmapWidth, glyph.bitmapHeight, GL11.GL_RGBA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, glyph.bitmap)

        debugImage?.also { debugImage ->
            val pixels = glyph.bitmap.asIntBuffer()
            for (yOffset in 0 until glyph.bitmapHeight) {
                for (xOffset in 0 until glyph.bitmapWidth) {
                    val color = pixels.get()
                    debugImage.setRGB(rect.x + xOffset, rect.y + yOffset, color)
                }
            }
        }
    }

    private fun initTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fontTexID)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, textureSize, textureSize, 0, GL11.GL_RGBA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, 0)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        if (debugImage != null)
            debugImage = BufferedImage(textureSize, textureSize, BufferedImage.TYPE_INT_ARGB)
    }

    private fun expand() {
        textureSize *= 2
        if (textureSize > gpuMaxTexSize)
            throw IllegalStateException("Ran out of atlas space after packing ${cache.size} glyphs. OpenGL max " +
                "texture size is $gpuMaxTexSize x $gpuMaxTexSize.")
        packer.expand(textureSize, textureSize)
        initTexture()
        cache.values.forEach { if(it != null) draw(it) }
    }

    override fun free() {
        cache.values.forEach {
            if(it != null)
                MemoryUtil.memFree(it.bitmap)
        }
    }

    companion object {
        private val reverseMap = mutableMapOf<NkUserFont, FontAtlas>()

        fun find(userFont: NkUserFont): FontAtlas? {
            return reverseMap[userFont]
        }
    }
}

data class GlyphInfo(
    val font: TTFFont, val codepoint: Int,
    val advance: Float,
    val width: Float, val height: Float,
    val offsetX: Float, val offsetY: Float,
    val bitmapWidth: Int, val bitmapHeight: Int,
    val bitmap: ByteBuffer
) {
    var minU: Float = 0f
    var minV: Float = 0f
    var maxU: Float = 0f
    var maxV: Float = 0f
    var rect: RectanglePacker.Rectangle? = null

    fun configure(ufg: NkUserFontGlyph, textureSize: Int) {
        ufg.width(width)
        ufg.height(height)
        ufg.offset().set(offsetX, offsetY)
        ufg.xadvance(advance)
//        ufg.uv(0).set(minU, minV)
//        ufg.uv(1).set(maxU, maxV)
        rect?.also { rect ->
            ufg.uv(0).set(
                rect.x / textureSize.toFloat(),
                rect.y / textureSize.toFloat()
            )
            ufg.uv(1).set(
                (rect.x + rect.width) / textureSize.toFloat(),
                (rect.y + rect.height) / textureSize.toFloat()
            )
        }
    }
}

data class GlyphMetrics(
    val font: TTFFont, val codepoint: Int,
    val advance: Float, val lsb: Float,
    val left: Float, val bottom: Float, val right: Float, val top: Float,
) {
    val ascent: Float get() = font.vMetrics.ascent
    val descent: Float get() = font.vMetrics.descent
    val lineGap: Float get() = font.vMetrics.lineGap
}

class TTFFont(val style: String, val weight: String, val file: String) {
    private var loaded = false

    private val fontInfo: STBTTFontinfo = STBTTFontinfo.create()
    private var ttf: ByteBuffer? = null

    val vMetrics: VMetrics by lazy {
        load()
        MemoryStack.stackPush().use { stack ->
            val ascent = stack.mallocInt(1)
            val descent = stack.mallocInt(1)
            val lineGap = stack.mallocInt(1)
            stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap)
            val scale = stbtt_ScaleForPixelHeight(fontInfo, 1f)
            VMetrics(ascent.get() * scale, descent.get() * scale, lineGap.get() * scale)
        }
    }

    /**
     * Gets the glyph info, not including the bitmap. Returns null if the specified codepoint isn't present in this font.
     */
    fun getMetrics(codepoint: Int): GlyphMetrics? {
        load()
        if (stbtt_FindGlyphIndex(fontInfo, codepoint) == 0)
            return null

        MemoryStack.stackPush().use { stack ->
            val scale = stbtt_ScaleForPixelHeight(fontInfo, 1f)

            val advance = stack.mallocInt(1)
            val lsb = stack.mallocInt(1)
            stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, lsb)
            val left = stack.mallocInt(1)
            val bottom = stack.mallocInt(1)
            val right = stack.mallocInt(1)
            val top = stack.mallocInt(1)
            stbtt_GetCodepointBox(fontInfo, codepoint, left, bottom, right, top)

            return GlyphMetrics(
                this, codepoint,
                advance.get() * scale,
                lsb.get() * scale,
                left.get() * scale,
                bottom.get() * scale,
                right.get() * scale,
                top.get() * scale,
            )
        }
    }


    /**
     * Gets the glyph info, including the bitmap. Returns null if the specified codepoint isn't present in this font.
     */
    fun get(codepoint: Int, height: Float, oversamplingX: Int, oversamplingY: Int): GlyphInfo? {
        load()
        if (stbtt_FindGlyphIndex(fontInfo, codepoint) == 0)
            return null

        MemoryStack.stackPush().use { stack ->
            val scale = stbtt_ScaleForPixelHeight(fontInfo, height)
            val d = stack.mallocInt(1)
            stbtt_GetFontVMetrics(fontInfo, null, d, null)
            val descent = d[0] * scale

            val advance = stack.mallocInt(1)
            stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null)

            val x0 = stack.ints(0)
            val y0 = stack.ints(0)
            val x1 = stack.ints(0)
            val y1 = stack.ints(0)
            stbtt_GetCodepointBitmapBoxSubpixel(fontInfo, codepoint, scale * oversamplingX, scale * oversamplingY, 0f, 0f, x0, y0, x1, y1)

            val bitmapWidth = x1[0] - x0[0] + oversamplingX - 1
            val bitmapHeight = y1[0] - y0[0] + oversamplingY - 1
            val stbBitmap = MemoryUtil.memAlloc(bitmapWidth * bitmapHeight)
            val subX = stack.floats(0f)
            val subY = stack.floats(0f)
            stbtt_MakeCodepointBitmapSubpixelPrefilter(
                fontInfo, stbBitmap,
                bitmapWidth, bitmapHeight, bitmapWidth, // width, height, stride
                scale * oversamplingX, scale * oversamplingY, // scale
                0f, 0f, // subpxel shift
                oversamplingX, oversamplingY,
                subX, subY,
                codepoint
            )

            val texture = MemoryUtil.memAlloc(bitmapWidth * bitmapHeight * 4)
            // Convert R8 to RGBA8
            for (i in 0 until stbBitmap.capacity()) {
                texture.putInt(stbBitmap[i].toInt() shl 24 or 0x00FFFFFF)
            }
            texture.flip()
            MemoryUtil.memFree(stbBitmap)

            return GlyphInfo(
                this, codepoint,
                advance[0] * scale,
                bitmapWidth.toFloat() / oversamplingX,
                bitmapHeight.toFloat() / oversamplingY,
                x0[0].toFloat() / oversamplingX + subX[0],
                y0[0].toFloat() / oversamplingY + subY[0] + height + descent,
                bitmapWidth, bitmapHeight,
                texture
            )
        }
    }

    fun load() {
        if (loaded)
            return
        loaded = true
        val ttf = Constants.readResourceBuffer(file)
        stbtt_InitFont(fontInfo, ttf)
        this.ttf = ttf
    }

    data class VMetrics(val ascent: Float, val descent: Float, val lineGap: Float)
}

