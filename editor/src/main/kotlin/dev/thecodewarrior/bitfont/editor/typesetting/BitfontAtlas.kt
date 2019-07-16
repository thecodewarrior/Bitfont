package dev.thecodewarrior.bitfont.editor.typesetting

import dev.thecodewarrior.bitfont.editor.utils.Colors
import dev.thecodewarrior.bitfont.editor.utils.math.Vec4
import dev.thecodewarrior.bitfont.editor.utils.math.vec
import dev.thecodewarrior.bitfont.utils.RectanglePacker
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt

class BitfontAtlas(val font: dev.thecodewarrior.bitfont.data.Bitfont) {
    private var packer = RectanglePacker<Int>(1, 1, 0)
    var width: Int
        private set
    var height: Int
        private set
    private val rects = Int2ObjectOpenHashMap<RectanglePacker.Rectangle>()
    private val passes = Int2ObjectOpenHashMap<Color>()

    private lateinit var defaultRect: RectanglePacker.Rectangle

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
        val colors = listOf(Colors.maroon, Colors.green, Colors.red, Colors.navy, Colors.pink, Colors.yellow,
            Colors.mint, Colors.orange, Colors.teal, Colors.beige, Colors.cyan, Colors.blue,
            Colors.lavender, Colors.brown, Colors.magenta)
        var pass = 0

        defaultRect = packer.insert(font.defaultGlyph.image.width, font.defaultGlyph.image.height, -1)
            ?: throw AtlasSizeException()
        passes[-1] = colors[pass % colors.size]
        for((codepoint, glyph) in font.glyphs) {
            var newRect: RectanglePacker.Rectangle? = packer.insert(glyph.image.width, glyph.image.height, codepoint)
            while(newRect == null) {
                ImageIO.write(image(), "png", File("atlas-pass$pass.png"))
                width = ceil(width*1.5).toInt()
                height = ceil(height*1.5).toInt()
                packer.expand(width, height)
                pass++
                newRect = packer.insert(glyph.image.width, glyph.image.height, codepoint)
            }
            rects[codepoint] = newRect
            passes[codepoint] = colors[pass % colors.size]
        }
    }

    fun texCoords(codepoint: Int): Vec4 {
        val rect = rects[codepoint] ?: return vec(0, 0, 0, 0)
        val width = width.toDouble()
        val height = height.toDouble()
        return vec(rect.x/width, rect.y/height, (rect.x + rect.width)/width, (rect.y + rect.height)/height)
    }

    private class AtlasSizeException: RuntimeException()

    fun image(): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        g.color = Colors.satan
        g.fillRect(0, 0, image.width, image.height)

        for((codepoint, glyph) in font.glyphs) {
            val rect = rects[codepoint] ?: continue
            for(x in 0 until min(rect.width, glyph.image.width)) {
                for(y in 0 until min(rect.height, glyph.image.height)) {
                    image.setRGB(rect.x + x, rect.y + y, if(glyph.image[x, y])
                        (passes[codepoint] ?: Color.WHITE).rgb
                    else
                        Colors.transparent.rgb
                    )
                }
            }
        }
        return image
    }
}
