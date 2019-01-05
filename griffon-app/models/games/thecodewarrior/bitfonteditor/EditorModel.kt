package games.thecodewarrior.bitfonteditor

import games.thecodewarrior.bitfonteditor.util.lateObservable
import games.thecodewarrior.bitfonteditor.util.observable
import griffon.core.artifact.GriffonModel;
import griffon.metadata.ArtifactProviderFor;
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonModel;

@ArtifactProviderFor(GriffonModel::class)
class EditorModel: AbstractGriffonModel() {
}