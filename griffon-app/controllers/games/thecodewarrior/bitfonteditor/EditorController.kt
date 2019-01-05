package games.thecodewarrior.bitfonteditor

import griffon.core.artifact.GriffonController
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonController

import griffon.core.controller.ControllerAction

import java.io.IOException

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
}