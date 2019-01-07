package games.thecodewarrior.bitfonteditor.util

import javafx.embed.swing.SwingFXUtils
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.math.max

class CanvasWrapper(val canvas: Canvas) {

    private val rootLayer = Layer(0, ::drawRoot)
    val layers = mutableListOf<Layer>()

    private var ignoreUpdate = false

    fun redrawAll() {
        ignoreUpdate = true
        layers.forEach { it.redraw() }
        ignoreUpdate = false
        updateCanvas()
    }

    private fun drawRoot(l: Layer) {
        layers.sortBy { it.zIndex }
        layers.forEach { layer ->
            l.g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.opacity)
            l.g.drawImage(layer.image, 0, 0, null)
        }
    }

    private fun updateCanvas() {
        if(ignoreUpdate) return
        ignoreUpdate = true
        rootLayer.redraw()
        ignoreUpdate = false
        val gc: GraphicsContext = canvas.graphicsContext2D
        gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
        gc.drawImage(SwingFXUtils.toFXImage(rootLayer.image, null), 0.0, 0.0)
    }

    fun layer(zIndex: Int = 0, draw: (l: Layer) -> Unit): Layer {
        val layer = Layer(zIndex, draw)
        layers.add(layer)
        return layer
    }

    inner class Layer(zIndex: Int, private val drawFun: (l: Layer) -> Unit) {
        var image: BufferedImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
            private set

        var zIndex: Int = zIndex
            set(value) {
                field = value
                updateCanvas()
            }
        var opacity: Float = 1f
            set(value) {
                field = value
                updateCanvas()
            }

        val width: Int
            get() = image.width
        val height: Int
            get() = image.height
        var g: Graphics2D = image.createGraphics()
            private set

        fun redraw() {
            if(image.width != canvas.width.toInt() || image.height != canvas.height.toInt()) {
                image = BufferedImage(
                    max(1, canvas.width.toInt()),
                    max(1, canvas.height.toInt()),
                    BufferedImage.TYPE_INT_ARGB)
            }
            g.dispose()
            g = image.createGraphics()
            g.background = Color(1f, 1f, 1f, 0f)
            g.clearRect(0, 0, image.width, image.height)

            drawFun(this)

            updateCanvas()
        }
    }
}