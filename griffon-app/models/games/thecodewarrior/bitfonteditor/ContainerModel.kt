package games.thecodewarrior.bitfonteditor

import games.thecodewarrior.bitfonteditor.util.observable
import griffon.core.artifact.GriffonModel
import griffon.metadata.ArtifactProviderFor
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonModel

@ArtifactProviderFor(GriffonModel::class)
class ContainerModel: AbstractGriffonModel() {

//    init {
//        addPropertyChangeListener("mvcIdentifier") { e ->
//            var document: Document? = null
//            if (e.newValue != null) {
//                val model = getApplication().mvcGroupManager.getModel(mvcIdentifier, EditorModel::class.java)
//                document = model.document
//            } else {
//                document = Document()
//            }
//            documentModel.document = document
//        }
//    }
}