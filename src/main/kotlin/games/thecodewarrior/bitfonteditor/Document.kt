package games.thecodewarrior.bitfonteditor

import games.thecodewarrior.bitfont.file.BitfontBundle
import games.thecodewarrior.bitfonteditor.util.lateObservable
import games.thecodewarrior.bitfonteditor.util.observable
import org.codehaus.griffon.runtime.core.AbstractObservable
import java.io.File

open class Document(): AbstractObservable() {
    var title: String by observable("")
    var file: File by lateObservable()
    var contents: BitfontBundle by lateObservable()
    var dirty: Boolean by observable(false)

    constructor(file: File, title: String) : this() {
        this.file = file
        contents = BitfontBundle()
        this.title = title
    }

    fun copyTo(doc: Document ) {
        doc.title = title
        doc.contents = contents
        doc.dirty = dirty
        doc.file = file
    }
}
