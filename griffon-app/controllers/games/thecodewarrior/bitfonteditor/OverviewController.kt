package games.thecodewarrior.bitfonteditor

import griffon.core.artifact.GriffonController
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonController

import griffon.core.controller.ControllerAction

@ArtifactProviderFor(GriffonController::class)
class OverviewController: AbstractGriffonController() {
    @MVCMember
    lateinit var model: OverviewModel
    @MVCMember
    lateinit var view: OverviewView

    override fun mvcGroupInit(args: Map<String, Any>) {
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
    fun edit() {
        val mvcIdentifier = "" + System.currentTimeMillis()
        application.mvcGroupManager.createMVC("editor", mvcIdentifier, mapOf("bundle" to model.bundle))
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