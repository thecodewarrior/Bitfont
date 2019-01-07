package games.thecodewarrior.bitfonteditor

import games.thecodewarrior.bitfont.file.BitfontBundle
import griffon.core.artifact.GriffonModel;
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor;
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonModel;

@ArtifactProviderFor(GriffonModel::class)
class OverviewModel: AbstractGriffonModel() {
    @MVCMember
    lateinit var bundle: BitfontBundle
}