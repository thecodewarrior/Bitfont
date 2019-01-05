package games.thecodewarrior.bitfonteditor


import griffon.core.artifact.GriffonView
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import javafx.fxml.FXML
import javafx.scene.control.Tab
import javafx.scene.control.TextArea
import org.codehaus.griffon.runtime.javafx.artifact.AbstractJavaFXGriffonView

import javax.annotation.Nonnull
import java.util.Objects

@ArtifactProviderFor(GriffonView::class)
class EditorView: AbstractJavaFXGriffonView() {
    @MVCMember
    lateinit var model: EditorModel
    @MVCMember
    lateinit var parentView: ContainerView
    @MVCMember
    lateinit var tabName: String

    @FXML
    lateinit var editor: TextArea

    lateinit var tab: Tab

    override fun initUI() {
        tab = Tab(tabName)
        tab.id = mvcGroup.mvcId
        tab.content = loadFromFXML()
//        parentView.tabGroup.getTabs().add(tab)

//        model.document.addPropertyChangeListener("contents") { e -> editor.setText(e.getNewValue() as String) }

//        editor.textProperty().addListener { observable, oldValue, newValue ->
//            model.document.dirty = editor.text != model.document.contents
//        }
    }

    override fun mvcGroupDestroy() {
//        runInsideUISync { parentView.tabGroup.tabs.remove(tab) }
    }
}