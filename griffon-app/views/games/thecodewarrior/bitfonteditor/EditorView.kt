package games.thecodewarrior.bitfonteditor


import games.thecodewarrior.bitfont.utils.Color
import games.thecodewarrior.bitfont.utils.Pos
import games.thecodewarrior.bitfonteditor.util.CanvasWrapper
import games.thecodewarrior.bitfonteditor.util.drawPolyline
import games.thecodewarrior.bitfonteditor.util.strokeWidth
import griffon.core.artifact.GriffonView
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import javafx.fxml.FXML
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.ScrollPane
import javafx.scene.control.Tab
import javafx.scene.control.TextArea
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import java.awt.Color
import javafx.stage.Stage
import org.codehaus.griffon.runtime.javafx.artifact.AbstractJavaFXGriffonView

import javax.annotation.Nonnull
import java.util.Objects

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
    var origin: Pos = Pos(0, 0)

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
        scene.heightProperty().addListener { _ -> resize() }
        scene.widthProperty().addListener { _ -> resize() }

        canvasWrapper = CanvasWrapper(editorCanvas)

        resize()
    }

    private fun resize() {
        rootPane.prefHeight = scene.height
        rootPane.prefWidth = scene.width

        editorCanvas.width = editorPane.width
        editorCanvas.height = editorPane.height

        redrawCanvas()
    }

    fun redrawCanvas() {
        val c = canvasWrapper
        c.redraw {
            drawBackgroundGuides(c)
        }
    }

    fun drawBackgroundGuides(c: CanvasWrapper) {
        c.g.color = Color.BLACK
        c.g.strokeWidth = 3f
        c.g.drawPolyline(
            10, 10,
            10, c.height-10,
            c.width-10, c.height-10,
            c.width-10, 10,
            10, 10
        )
    }

    companion object {
        val backgroundColor = Color("f4f4f4")
    }
}