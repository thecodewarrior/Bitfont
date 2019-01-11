package games.thecodewarrior.bitfonteditor

import games.thecodewarrior.bitfont.file.BitfontBundle
import griffon.core.artifact.GriffonView
import griffon.core.controller.Action
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import javafx.fxml.FXML
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.TabPane
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.Window
import org.codehaus.griffon.runtime.javafx.artifact.AbstractJavaFXGriffonView

import javax.annotation.Nonnull
import javax.annotation.Nullable
import java.io.File
import java.util.Collections

@ArtifactProviderFor(GriffonView::class)
class ContainerView: AbstractJavaFXGriffonView() {
    @set:MVCMember
    lateinit var controller: ContainerController
    @set:MVCMember
    lateinit var model: ContainerModel

    lateinit var fileChooser: DirectoryChooser
    lateinit var stage: Stage

    @Override
    override fun initUI() {
        stage = getApplication().createApplicationContainer(Collections.emptyMap()) as Stage
        stage.title = getApplication().configuration.getAsString("application.title")
        stage.width = 480.0
        stage.height = 320.0
        stage.scene = init()
        getApplication().getWindowManager<Any>().attach("mainWindow", stage)

        fileChooser = DirectoryChooser()
        fileChooser.title = getApplication().configuration.getAsString("application.title", "Open File")
    }

    // build the UI
    fun init(): Scene {
        val scene = Scene(Group())
        scene.fill = Color.WHITE
        scene.stylesheets.add("bootstrapfx.css")

        val node = loadFromFXML()
        (scene.root as Group).children.addAll(node)
        connectActions(node, controller)

        return scene
    }

    fun selectFile(): File? {
        return fileChooser.showDialog(stage)
    }
}