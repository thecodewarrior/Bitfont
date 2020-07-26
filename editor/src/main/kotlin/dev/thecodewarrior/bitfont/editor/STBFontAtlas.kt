package dev.thecodewarrior.bitfont.editor

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
import org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics
import org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics
import org.lwjgl.stb.STBTruetype.stbtt_InitFont
import org.lwjgl.stb.STBTruetype.stbtt_MakeCodepointBitmapSubpixelPrefilter
import org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

class LiveFont(val fonts: List<LazyFont>) {
    var width = 8192
    var height = 8192

    private val gpuMaxTexSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE)

    val userFont = NkUserFont.create()

    private val fontTexID = GL11.glGenTextures()

    private val packer = RectanglePacker<GlyphInfo>(width, height, 1)
    private val cache = Int2ObjectOpenHashMap<GlyphInfo?>()

    private val debugImage: BufferedImage? = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

    init {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fontTexID)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, 0)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)

        userFont
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
            .height(NuklearFonts.fontHeight.toFloat())
            .query { handle: Long, font_height: Float, glyph: Long, codepoint: Int, next_codepoint: Int ->
                val ufg = NkUserFontGlyph.create(glyph)
                this[codepoint]?.configure(ufg)
            }
            .texture { it: NkHandle ->
                it.id(fontTexID)
            }
    }

    operator fun get(codepoint: Int): GlyphInfo? {
        return cache.getOrPut(codepoint) {
            for (font in fonts) {
                val glyph = font.get(codepoint, 4, 4)
                if (glyph != null) {
                    insert(glyph)
                    return@getOrPut glyph
                }
            }
            return@getOrPut null
        }
    }

    private fun insert(glyph: GlyphInfo) {
        val newRect = packer.insert(glyph.bitmapWidth, glyph.bitmapHeight, glyph) ?: return
        draw(glyph, newRect.x, newRect.y)

        glyph.minU = newRect.x / width.toFloat()
        glyph.minV = newRect.y / height.toFloat()
        glyph.maxU = (newRect.x + newRect.width) / width.toFloat()
        glyph.maxV = (newRect.y + newRect.height) / height.toFloat()
    }

    private fun draw(glyph: GlyphInfo, x: Int, y: Int) {
        val bitmap = glyph.bitmap ?: return

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fontTexID)
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, glyph.bitmapWidth, glyph.bitmapHeight, GL11.GL_RGBA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, bitmap)

        debugImage?.also { debugImage ->
            bitmap.rewind()
            val pixels = bitmap.asIntBuffer()
            for (yOffset in 0 until glyph.bitmapHeight) {
                for (xOffset in 0 until glyph.bitmapWidth) {
                    val color = pixels.get()
                    debugImage.setRGB(x + xOffset, y + yOffset, color)
                }
            }
        }

        MemoryUtil.memFree(bitmap)
        glyph.bitmap = null
    }
}

object NuklearFonts {
    val fontHeight: Int = 18

    val SERIF_DISPLAY: LiveFont
    val SERIF_MONO: LiveFont
    val SERIF: LiveFont

    val SANS_DISPLAY: LiveFont
    val SANS_MONO: LiveFont
    val SANS: LiveFont

    val FONTS: Map<String, LiveFont>

    init {
        val all = mutableListOf<String>()
        val serifd = mutableListOf<String>()
        val serifm = mutableListOf<String>()
        val serif = mutableListOf<String>()

        val sansd = mutableListOf<String>()
        val sansm = mutableListOf<String>()
        val sans = mutableListOf<String>()

        val other = mutableListOf<String>()

        Constants.resource("reference/fonts.txt").bufferedReader().lineSequence().drop(1).forEach { line ->
            val (style, _, _, file) = line.trim().split(":")

            all.add(file)
            when (style) {
                "serifd" -> serifd.add(file)
                "serifm" -> serifm.add(file)
                "serif" -> serif.add(file)

                "sansd" -> sansd.add(file)
                "sansm" -> sansm.add(file)
                "sans" -> sans.add(file)

                "other" -> other.add(file)
            }
        }

        val fonts = all.associateWith { LazyFont("reference/$it", fontHeight) }

        SERIF_DISPLAY = LiveFont((serifd + serif + sans + other).map { fonts.getValue(it) })
        SERIF_MONO = LiveFont((serifm + serif + sans + other).map { fonts.getValue(it) })
        SERIF = LiveFont((serif + sans + other).map { fonts.getValue(it) })

        SANS_DISPLAY = LiveFont((sansd + sans + other).map { fonts.getValue(it) })
        SANS_MONO = LiveFont((sansm + sans + other).map { fonts.getValue(it) })
        SANS = LiveFont((sans + other).map { fonts.getValue(it) })

        FONTS = mapOf(
            "Serif" to SERIF,
            "Sans-Serif" to SANS,
            "Serif Display" to SERIF_DISPLAY,
            "Sans-Serif Display" to SANS_DISPLAY,
            "Serif Mono" to SERIF_MONO,
            "Sans-Serif Mono" to SANS_MONO
        )
    }
}

data class GlyphInfo(
    val font: LazyFont, val codepoint: Int,
    val advance: Float,
    val width: Float, val height: Float,
    val offsetX: Float, val offsetY: Float
) {
    var minU: Float = 0f
    var minV: Float = 0f
    var maxU: Float = 0f
    var maxV: Float = 0f
    var bitmapWidth: Int = 0
    var bitmapHeight: Int = 0
    var bitmap: ByteBuffer? = null

    fun configure(ufg: NkUserFontGlyph) {
        ufg.width(width)
        ufg.height(height)
        ufg.offset().set(offsetX, offsetY)
        ufg.xadvance(advance)
        ufg.uv(0).set(minU, minV)
        ufg.uv(1).set(maxU, maxV)
    }
}

class LazyFont(val name: String, val height: Int) {
    private var loaded = false

    val fontInfo: STBTTFontinfo = STBTTFontinfo.create()
    var scale: Float = 0f
    var descent: Float = 0f

    /**
     * Gets the glyph info, including the bitmap. Returns null if the specified codepoint isn't present in this font.
     * This object is not cached in order to allow multiple users for a single [LazyFont] without them clobbering each
     * other's data
     */
    fun get(codepoint: Int, oversamplingX: Int, oversamplingY: Int): GlyphInfo? {
        load()
        if (stbtt_FindGlyphIndex(fontInfo, codepoint) == 0)
            return null
        MemoryStack.stackPush().use { stack ->
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

            val glyph = GlyphInfo(
                this, codepoint,
                advance[0] * scale,
                bitmapWidth.toFloat() / oversamplingX,
                bitmapHeight.toFloat() / oversamplingY,
                x0[0].toFloat() / oversamplingX + subX[0],
                y0[0].toFloat() / oversamplingY + subY[0] + height + descent
            )
            glyph.bitmap = texture
            glyph.bitmapWidth = bitmapWidth
            glyph.bitmapHeight = bitmapHeight

            return glyph
        }
    }

    fun load() {
        if (loaded)
            return
        val ttf = Constants.readResourceBuffer(name)

        MemoryStack.stackPush().use { stack ->
            stbtt_InitFont(fontInfo, ttf)
            scale = stbtt_ScaleForPixelHeight(fontInfo, height.toFloat())
            val d = stack.mallocInt(1)
            stbtt_GetFontVMetrics(fontInfo, null, d, null)
            descent = d[0] * scale
        }
    }
}

