package games.thecodewarrior.bitfonteditor


import griffon.core.artifact.GriffonView
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.Window
import jdk.nashorn.tools.ShellFunctions.input
import org.codehaus.griffon.runtime.javafx.artifact.AbstractJavaFXGriffonView
import java.io.File
import java.util.Collections.emptyMap

@ArtifactProviderFor(GriffonView::class)
class OverviewView: AbstractJavaFXGriffonView() {
    @MVCMember
    lateinit var model: OverviewModel
    @MVCMember
    lateinit var controller: OverviewController

    lateinit var fileChooser: DirectoryChooser
    lateinit var stage: Stage

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
        val scene = Scene(Group())
        scene.fill = Color.WHITE
        scene.stylesheets.add("bootstrapfx.css")

        val node = loadFromFXML()
//        model.inputProperty().bindBidirectional(input.textProperty())
//        model.outputProperty().bindBidirectional(output.textProperty())
        (scene.root as Group).children.addAll(node)
        connectActions(node, controller)

        return scene
    }

    fun selectFile(): File? {
        return fileChooser.showDialog(stage)
    }
}