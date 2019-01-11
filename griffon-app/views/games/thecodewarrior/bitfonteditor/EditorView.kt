package games.thecodewarrior.bitfonteditor


import games.thecodewarrior.bitfont.utils.BitGrid
import games.thecodewarrior.bitfont.utils.Color
import games.thecodewarrior.bitfont.utils.Vec2
import games.thecodewarrior.bitfont.utils.vec
import games.thecodewarrior.bitfonteditor.observablefiles.ObservableGlyph
import games.thecodewarrior.bitfonteditor.util.CanvasWrapper
import games.thecodewarrior.bitfonteditor.util.listen
import games.thecodewarrior.bitfonteditor.util.strokeWidth
import griffon.core.artifact.GriffonView
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import javafx.fxml.FXML
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import java.awt.Color
import javafx.stage.Stage
import org.codehaus.griffon.runtime.javafx.artifact.AbstractJavaFXGriffonView
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@ArtifactProviderFor(GriffonView::class)
class EditorView: AbstractJavaFXGriffonView() {
    @MVCMember
    lateinit var controller: EditorController
    @MVCMember
    lateinit var model: EditorModel

    lateinit var scene: Scene
    @FXML
    lateinit var rootPane: BorderPane
    @FXML
    lateinit var editorPane: Pane
    @FXML
    lateinit var editorCanvas: Canvas

    lateinit var canvasWrapper: CanvasWrapper
    lateinit var guideLayer: CanvasWrapper.Layer
    lateinit var glyphLayer: CanvasWrapper.Layer
    lateinit var metricsLayer: CanvasWrapper.Layer
    lateinit var glyphImage: BufferedImage

    var origin: Vec2 = Vec2(0, 0)
    var scale: Int = 20
    var pos = vec(0, 0)
    var pixelPos = vec(0, 0)

    override fun initUI() {
        val stage = getApplication().createApplicationContainer(emptyMap()) as Stage
        stage.title = getApplication().configuration.getAsString("application.title")
        stage.width = 400.0
        stage.height = 400.0
        stage.scene = initScene()
        stage.show()
    }

    private fun initScene(): Scene {
        scene = Scene(Group())
        scene.fill = javafx.scene.paint.Color.WHITE
        scene.stylesheets.add("bootstrapfx.css")

        val node = loadFromFXML()
        (scene.root as Group).children.addAll(node)
        connectActions(node, controller)
        init()
        return scene
    }

    // build the UI
    private fun init() {
        canvasWrapper = CanvasWrapper(editorCanvas)

        guideLayer = canvasWrapper.layer(0, ::drawGuides)
        guideLayer.opacity = 0.5f
        glyphLayer = canvasWrapper.layer(1, ::drawGlyph)
        metricsLayer = canvasWrapper.layer(2, ::drawMetrics)

        scene.heightProperty().addListener { _ -> resize() }
        scene.widthProperty().addListener { _ -> resize() }

        glyphImage = model.glyph.image.getImage(glyphBG, glyphFG)
        model.glyph.imageObservable.addListener { _ ->
            glyphImage = model.glyph.image.getImage(glyphBG, glyphFG)
            glyphLayer.redraw()
        }
        model.glyph.listen(ObservableGlyph::bearingX) { old, new ->
            glyphLayer.redraw()
        }
        model.glyph.listen(ObservableGlyph::bearingY) { old, new ->
            glyphLayer.redraw()
        }
        resize()
    }

    private fun resize() {
        rootPane.prefHeight = scene.height
        rootPane.prefWidth = scene.width

        editorCanvas.width = editorPane.width
        editorCanvas.height = editorPane.height
        origin = Vec2(editorPane.width.toInt()/2, editorPane.height.toInt()/2)

        canvasWrapper.redrawAll()
    }

    fun drawGuides(l: CanvasWrapper.Layer) {
        l.g.translate(origin.x, origin.y)
        l.g.color = Color("7f7f7f")
        l.g.strokeWidth = 1f
        val pixelsX = -((origin.x + scale - 1)/scale) .. ((l.width - origin.x + scale - 1)/scale)
        val pixelsY = -((origin.y + scale - 1)/scale) .. ((l.height - origin.y + scale - 1)/scale)
        pixelsX.forEach { x ->
            l.g.drawLine(scale * x, -origin.y, scale * x, l.height-origin.y)
        }
        pixelsY.forEach { y ->
            l.g.drawLine(-origin.x, scale * y, l.width-origin.x, scale * y)
        }

        l.g.font = l.g.font.deriveFont(8f*scale)
        val glyph = l.g.font.createGlyphVector(l.g.fontRenderContext, String(Character.toChars(model.codepoint.toInt())))
        l.g.draw(glyph.outline)
    }

    fun pixel(canvasPos: Vec2): Vec2 {
        pos = canvasPos
        pixelPos = (canvasPos - origin).map { floor(it / scale.toDouble()).toInt() }
        metricsLayer.redraw()
        return pixelPos
    }

    fun drawGlyph(l: CanvasWrapper.Layer) {
        l.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        l.g.translate(origin.x, origin.y)

        l.g.scale(scale.toDouble(), scale.toDouble())
        l.g.drawImage(glyphImage, model.glyph.bearingX.toInt(), -model.glyph.bearingY.toInt(), null)
    }

    fun drawMetrics(l: CanvasWrapper.Layer) {
        l.g.color = Color.BLACK
        l.g.strokeWidth = 2f
        l.g.drawLine(0, origin.y, l.width, origin.y)
        l.g.drawLine(origin.x, 0, origin.x, l.height)
    }

    @FXML fun canvasMousePressed(e: MouseEvent) {
        controller.mouseDown(e.button, pixel(Vec2(e.x.toInt(), e.y.toInt())))
    }

    @FXML fun canvasMouseReleased(e: MouseEvent) {
        controller.mouseUp(e.button, pixel(Vec2(e.x.toInt(), e.y.toInt())))
    }

    var lastPixel: Vec2? = null
    @FXML fun canvasMouseMoved(e: MouseEvent) {
        val pixel = pixel(Vec2(e.x.toInt(), e.y.toInt()))
        if(pixel != lastPixel) {
            lastPixel?.also { controller.mouseExitedPixel(it) }
            controller.mouseEnteredPixel(pixel)
            lastPixel = pixel
        }
    }

    @FXML fun canvasMouseExited(e: MouseEvent) {
        lastPixel?.also { controller.mouseExitedPixel(it) }
        lastPixel = null
    }

    companion object {
        val backgroundColor = Color("f4f4f4")
        val glyphBG = Color(1f, 0f, 0f, 0.1f)
        val glyphFG = Color(0, 0, 0)
    }
}