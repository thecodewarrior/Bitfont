package games.thecodewarrior.bitfonteditor

import games.thecodewarrior.bitfont.file.BitfontBundle
import games.thecodewarrior.bitfont.file.BlockFile
import games.thecodewarrior.bitfont.file.UCDFile
import griffon.core.artifact.GriffonModel
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonModel
import java.util.TreeMap

@ArtifactProviderFor(GriffonModel::class)
class OverviewModel: AbstractGriffonModel() {
    @MVCMember
    lateinit var bundle: BitfontBundle
}