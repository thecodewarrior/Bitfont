package games.thecodewarrior.bitfont.typesetting

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.utils.Colors
import games.thecodewarrior.bitfont.utils.IndexColorModel
import games.thecodewarrior.bitfont.utils.RectanglePacker
import glm_.vec4.Vec4
import imgui.internal.Rect
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class BitfontAtlas(val font: Bitfont) {
    private var packer = RectanglePacker<Int>(1, 1, 0)
    var width: Int
        private set
    var height: Int
        private set
    private val rects = Int2ObjectOpenHashMap<RectanglePacker.Rectangle>()

    init {
        width = 128
        height = 128
        pack()
    }

    fun pack() {
        while(true) {
            rects.clear()
            packer = RectanglePacker(width, height, 0)
            try {
                insert()
                return
            } catch(e: AtlasSizeException) {
                println("Failed to pack into $width x $height atlas, expanding and trying again.")
                val scale = sqrt(font.glyphs.size.toDouble() / rects.size) + 0.1
                width = ceil(width * scale).toInt()
                height = ceil(height * scale).toInt()
            }
        }
    }

    fun insert() {
        for((codepoint, glyph) in font.glyphs) {
            val rect = packer.insert(glyph.image.width, glyph.image.height, codepoint)
            rects[codepoint] = rect ?: throw AtlasSizeException()
        }
    }

    fun texCoords(codepoint: Int): Vec4 {
        val rect = rects[codepoint] ?: return Vec4()
        val width = width.toDouble()
        val height = height.toDouble()
        return Vec4(rect.x/width, rect.y/height, (rect.x + rect.width)/width, (rect.y + rect.height)/height)
    }

    private class AtlasSizeException: RuntimeException()

    fun image(): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, IndexColorModel(Colors.transparent, Color.WHITE))
        for((codepoint, glyph) in font.glyphs) {
            val rect = rects[codepoint] ?: continue
            for(x in 0 until min(rect.width, glyph.image.width)) {
                for(y in 0 until min(rect.height, glyph.image.height)) {
                    image.setRGB(rect.x + x, rect.y + y, if(glyph.image[x, y]) Color.WHITE.rgb else Colors.transparent.rgb)
                }
            }
        }
        return image
    }
}
