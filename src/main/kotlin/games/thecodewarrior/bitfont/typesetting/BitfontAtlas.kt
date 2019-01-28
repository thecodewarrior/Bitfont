package games.thecodewarrior.bitfont.typesetting

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.utils.Colors
import games.thecodewarrior.bitfont.utils.IndexColorModel
import games.thecodewarrior.bitfont.utils.RectanglePacker
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class BitfontAtlas(val font: Bitfont) {
    private var packer = RectanglePacker<Int>(1, 1, 0)
    private var width: Int
    private var height: Int
    val rects = Int2ObjectOpenHashMap<RectanglePacker.Rectangle>()

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
