package games.thecodewarrior.bitfonteditor.util

import javafx.embed.swing.SwingFXUtils
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.math.max

class CanvasWrapper(val canvas: Canvas) {

    private var fullImage: BufferedImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)

    val width: Int
        get() = fullImage.width
    val height: Int
        get() = fullImage.height
    var g: Graphics2D = fullImage.createGraphics()
        private set

    fun redraw(function: () -> Unit) {
        if(fullImage.width != canvas.width.toInt() || fullImage.height != canvas.height.toInt()) {
            fullImage = BufferedImage(
                max(1, canvas.width.toInt()),
                max(1, canvas.height.toInt()),
                BufferedImage.TYPE_INT_ARGB)
        }
        g.dispose()
        g = fullImage.createGraphics()
        g.background = Color(1f, 1f, 1f, 0f)
        g.clearRect(0, 0, fullImage.width, fullImage.height)

        function()

        val gc: GraphicsContext = canvas.graphicsContext2D
        gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
        gc.drawImage(SwingFXUtils.toFXImage(fullImage, null), 0.0, 0.0)
    }
}