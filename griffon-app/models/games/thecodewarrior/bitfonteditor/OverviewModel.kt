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

    private val codepointMap: Map<UInt, UCDFile.Codepoint> by lazy {
        bundle.ucd.codepoints.associateByTo(TreeMap()) {
            it.codepoint
        }
    }

    fun getInfo(codepoint: UInt): GlyphInfo {
        return GlyphInfo(codepoint, null)// codepointMap[codepoint]
    }

    data class GlyphInfo(val codepoint: UInt, val glyph: BlockFile.Glyph?) {
        companion object {
            val NULL = GlyphInfo(0u, null)
        }
    }
}