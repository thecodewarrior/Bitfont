package games.thecodewarrior.bitfonteditor


import games.thecodewarrior.bitfonteditor.util.CanvasWrapper
import griffon.core.artifact.GriffonView
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import javafx.fxml.FXML
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.ScrollPane
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Pane
import java.awt.Color
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import org.codehaus.griffon.runtime.javafx.artifact.AbstractJavaFXGriffonView
import java.io.File
import java.util.Collections.emptyMap
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.properties.Delegates

@ArtifactProviderFor(GriffonView::class)
class OverviewView: AbstractJavaFXGriffonView() {
    @MVCMember
    lateinit var model: OverviewModel
    @MVCMember
    lateinit var controller: OverviewController

    @FXML
    lateinit var rootPane: BorderPane
    @FXML
    lateinit var contentPane: FlowPane

    var glyphItems = listOf<GlyphItem>()

    lateinit var fileChooser: DirectoryChooser
    lateinit var stage: Stage
    lateinit var scene: Scene

    fun selectFile(): File? {
        return fileChooser.showDialog(stage)
    }

    override fun initUI() {
        stage = getApplication().createApplicationContainer(emptyMap()) as Stage
        stage.title = getApplication().configuration.getAsString("application.title")
        stage.width = 400.0
        stage.height = 120.0
        stage.scene = init()
        stage.show()

        fileChooser = DirectoryChooser()
        fileChooser.title = getApplication().configuration.getAsString("application.title", "Save File")
    }

    // build the UI
    private fun init(): Scene {
        scene = Scene(Group())
        scene.fill = javafx.scene.paint.Color.WHITE
        scene.stylesheets.add("bootstrapfx.css")

        val node = loadFromFXML()
//        model.inputProperty().bindBidirectional(input.textProperty())
//        model.outputProperty().bindBidirectional(output.textProperty())
        (scene.root as Group).children.addAll(node)
        connectActions(node, controller)

        scene.heightProperty().addListener { _ -> resize() }
        scene.widthProperty().addListener { _ -> resize() }

        contentPane.prefHeight = GlyphItem.ITEM_HEIGHT * 8.0
        contentPane.prefWidth = GlyphItem.ITEM_WIDTH * 8.0
        return scene
    }

    fun updateItems() {
        glyphItems.forEachIndexed { i, it -> it.glyphInfo = model.getInfo((prefix + i).toUInt()) }
    }

    private fun resize() {
        rootPane.prefHeight = scene.height
        rootPane.prefWidth = scene.width
    }

    class GlyphItem(glyphInfo: OverviewModel.GlyphInfo): Canvas(ITEM_WIDTH.toDouble(), ITEM_HEIGHT.toDouble()) {
        val wrapper = CanvasWrapper(this)
        val infoLayer = wrapper.layer(0, ::drawInfo)
        var glyphInfo by Delegates.observable(glyphInfo) { _, _, _ ->
            infoLayer.redraw()
        }

        fun drawInfo(l: CanvasWrapper.Layer) {
            l.g.color = Color(glyphInfo.hashCode() and 0xFFFFFF)
            l.g.fillRect(1, 1, 48, 73)
            l.g.drawString(glyphInfo.codepoint.toString(), 10, 30)
        }

        companion object {
            const val ITEM_HEIGHT = 75
            const val ITEM_WIDTH = 50
        }
    }
}