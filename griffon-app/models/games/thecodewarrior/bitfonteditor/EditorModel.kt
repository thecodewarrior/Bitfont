package games.thecodewarrior.bitfonteditor

import games.thecodewarrior.bitfont.file.BitfontBundle
import games.thecodewarrior.bitfont.file.BlockFile
import games.thecodewarrior.bitfont.file.FontFile
import games.thecodewarrior.bitfonteditor.observablefiles.ObservableGlyph
import games.thecodewarrior.bitfonteditor.util.lateObservable
import griffon.core.artifact.GriffonModel
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonModel

@ArtifactProviderFor(GriffonModel::class)
class EditorModel: AbstractGriffonModel() {
    @MVCMember
    lateinit var bundle: BitfontBundle
    var codepoint: UInt = 68u

    var glyph by lateObservable<ObservableGlyph>()

    override fun mvcGroupInit(args: Map<String, Any>) {
        val bundle = args["bundle"] as BitfontBundle
        val blockName = bundle.font.blocks.find { codepoint in it.min..it.max }?.filename
            ?: {
                val block = bundle.ucd.blocks.find { codepoint in it.min .. it.max }!!
                bundle.font.blocks.add(FontFile.Block(block.filename, block.min, block.max))
                block.filename
            }()

        val block = bundle.blocks.getOrPut(blockName) { BlockFile() }
        val glyph = block.glyphs.find { it.codepoint == codepoint }
            ?: BlockFile.Glyph(codepoint = codepoint).also { block.glyphs.add(it) }
        this.glyph = ObservableGlyph(glyph)
    }
}
