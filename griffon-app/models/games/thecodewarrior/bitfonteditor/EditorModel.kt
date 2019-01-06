package games.thecodewarrior.bitfonteditor

import games.thecodewarrior.bitfont.file.BitfontBundle
import games.thecodewarrior.bitfont.file.BlockFile
import games.thecodewarrior.bitfont.utils.BitGrid
import games.thecodewarrior.bitfonteditor.util.lateObservable
import games.thecodewarrior.bitfonteditor.util.observable
import griffon.core.artifact.GriffonModel;
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor;
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonModel;

@ArtifactProviderFor(GriffonModel::class)
class EditorModel: AbstractGriffonModel() {
//    @MVCMember
//    lateinit var bundle: BitfontBundle
    var glyph = BlockFile.Glyph()
    var grid by observable(BitGrid(16, 16))

}