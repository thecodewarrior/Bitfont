package games.thecodewarrior.bitfonteditor

import games.thecodewarrior.bitfont.file.BlockFile
import games.thecodewarrior.bitfont.utils.BitGrid
import games.thecodewarrior.bitfonteditor.observablefiles.ObservableGlyph
import games.thecodewarrior.bitfonteditor.util.observable
import games.thecodewarrior.bitfonteditor.util.observableBy
import griffon.core.artifact.GriffonModel;
import griffon.metadata.ArtifactProviderFor;
import org.codehaus.griffon.runtime.core.AbstractObservable
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonModel;

@ArtifactProviderFor(GriffonModel::class)
class EditorModel: AbstractGriffonModel() {
//    @MVCMember
//    lateinit var bundle: BitfontBundle
    var glyph = ObservableGlyph(BlockFile.Glyph())
}
