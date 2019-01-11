package games.thecodewarrior.bitfonteditor

import griffon.core.artifact.GriffonService
import griffon.metadata.ArtifactProviderFor
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonService
import java.awt.Color
import java.awt.Font
import java.awt.Image
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.inject.Singleton

@Singleton
@ArtifactProviderFor(GriffonService::class)
class CreateReferenceTestsService: AbstractGriffonService() {
    fun exportFonts() {
        val dir = File("/Users/code/Documents/refTests/src")
        dir.list().forEach { name ->
            val font = Font.createFont(Font.TRUETYPE_FONT, dir.resolve(name))
            exportFont("${name.removeSuffix(".ttf")}.aa", font, true)
            exportFont("${name.removeSuffix(".ttf")}.a", font, false)
        }
    }

    fun exportFont(name: String, font: Font, aa: Boolean) {
        val testStr = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789"
        var y = 0
        val sizes = listOf(1, 2, 3, 5).flatMap { listOf(6*it, 8*it, 12*it) }.reversed()
        val totalHeight = sizes.sum() * 2

        val image = BufferedImage(totalHeight*3, totalHeight, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        g.color = Color.BLACK
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, if(aa) RenderingHints.VALUE_TEXT_ANTIALIAS_ON else RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, if(aa) RenderingHints.VALUE_FRACTIONALMETRICS_OFF else RenderingHints.VALUE_FRACTIONALMETRICS_OFF)

        sizes.forEach { size ->
            y += size
            val sized = font.sizeHeightTo(testStr, size.toFloat())
            g.font = sized
            g.drawString(testStr, 10, y)
            y += size
        }

        ImageIO.write(image, "png", File("/Users/code/Documents/refTests/out/$name.png"))
    }

    fun Font.sizeHeightTo(text: String, target: Float): Font {
        val image = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        return sizeToBy(target) { font ->
            font.createGlyphVector(g.fontRenderContext, text).visualBounds.height.toFloat()
        }
    }

    fun Font.sizeToBy(target: Float, getMetric: (font: Font) -> Float): Font {

        var size: Float = target
        var nextJump = size/2
        var font = this.deriveFont(size)
        for(i in 0..10) {
            val metric = getMetric(font)
            if(metric == target) {
                break
            } else {
                if(metric > target) {
                    size -= nextJump
                } else {
                    size += nextJump
                }
                nextJump /= 2
                font = font.deriveFont(size)
            }
        }

        return font
    }
}
