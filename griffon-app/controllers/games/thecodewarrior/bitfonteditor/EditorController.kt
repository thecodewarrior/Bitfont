package games.thecodewarrior.bitfonteditor

import games.thecodewarrior.bitfont.utils.BitGrid
import games.thecodewarrior.bitfont.utils.Vec2
import games.thecodewarrior.bitfont.utils.vec
import griffon.core.artifact.GriffonController
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonController

import griffon.core.controller.ControllerAction
import javafx.scene.input.MouseButton
import kotlin.math.max

@ArtifactProviderFor(GriffonController::class)
class EditorController: AbstractGriffonController() {
    @MVCMember
    lateinit var model: EditorModel
    @MVCMember
    lateinit var view: EditorView

    override fun mvcGroupInit(args: Map<String, Any>) {
//        model.document = args["document"] as Document
        runOutsideUI {
//            try {
//                val content = model.document.file.readText()
//                runInsideUIAsync { model.document.contents = content }
//            } catch (e: IOException) {
//                log.warn("Can't open file", e)
//            }
        }
    }

    fun mouseDown(button: MouseButton, pos: Vec2) {
        val glyphPos = pos + vec(-model.glyph.bearingX, model.glyph.bearingY)
        val newPos = expandToFit(glyphPos)
        model.glyph.image[newPos] = !model.glyph.image[newPos]
        model.glyph.imageObservable.fire()
    }

    fun mouseUp(button: MouseButton, pos: Vec2) {
    }

    fun mouseEnteredPixel(pos: Vec2) {

    }

    fun mouseExitedPixel(pos: Vec2) {
    }

    /**
     * Expands the glyph's image to fit the specified position and returns the adjusted position in the new image
     */
    private fun expandToFit(pos: Vec2): Vec2 {
        if(pos !in model.glyph.image) {
            val expandMin = (-pos).map { max(0, it) }
            val expandMax = (pos - model.glyph.image.size).map { max(0, it+1) }
            val newSize = model.glyph.image.size + expandMin + expandMax
            val newGrid = BitGrid(newSize.x, newSize.y)
            newGrid.draw(model.glyph.image, expandMin)
            model.glyph.image = newGrid
            model.glyph.bearingX = (model.glyph.bearingX - expandMin.x).toShort()
            model.glyph.bearingY = (model.glyph.bearingY + expandMin.y).toShort()
            return pos + expandMin
        }
        return pos
    }

    @ControllerAction
    fun saveFile() {
//        try {
//            model.document.file.writeText(view.editor.getText())
//            runInsideUIAsync { model.document.contents = view.editor.text }
//        } catch (e: IOException) {
//            log.warn("Can't save file", e)
//        }
    }

    @ControllerAction
    fun closeFile() {
        destroyMVCGroup(mvcGroup.mvcId)
    }

    fun Vec2.lineTo(other: Vec2): List<Vec2> {
        val x1 = x
        val y1 = y
        val x2 = other.x
        val y2 = other.y

        var d = 0
        val dy = Math.abs(y2 - y1)
        val dx = Math.abs(x2 - x1)
        val dy2 = dy shl 1
        val dx2 = dx shl 1
        val ix = if (x1 < x2)  1 else -1
        val iy = if (y1 < y2)  1 else -1
        var xx = x1
        var yy = y1

        val list = mutableListOf<Vec2>()
        if (dy <= dx) {
            while (true) {
                list.add(Vec2(xx, yy))
                if (xx == x2) break
                xx += ix
                d  += dy2
                if (d > dx) {
                    yy += iy
                    d  -= dx2
                }
            }
        }
        else {
            while (true) {
                list.add(Vec2(xx, yy))
                if (yy == y2) break
                yy += iy
                d  += dx2
                if (d > dy) {
                    xx += ix
                    d  -= dy2
                }
            }
        }
        return list
    }
}