package games.thecodewarrior.bitfonteditor.observablefiles

import games.thecodewarrior.bitfont.file.BlockFile
import games.thecodewarrior.bitfont.utils.BitGrid
import games.thecodewarrior.bitfonteditor.util.mutableObservableValueBy
import games.thecodewarrior.bitfonteditor.util.observableBy
import games.thecodewarrior.bitfonteditor.util.observableListOf
import javafx.beans.value.ObservableObjectValue
import javafx.beans.value.ObservableValue
import javafx.beans.value.ObservableValueBase
import org.codehaus.griffon.runtime.core.AbstractObservable

class ObservableGlyph(val glyph: BlockFile.Glyph): AbstractObservable() {
    var codepoint: UInt  by observableBy(glyph::codepoint)
    var bearingX: Short  by observableBy(glyph::bearingX)
    var bearingY: Short  by observableBy(glyph::bearingY)
    var advance: UShort by observableBy(glyph::advance)
    val imageObservable = mutableObservableValueBy(glyph::image, true)
    var image: BitGrid by imageObservable
}

