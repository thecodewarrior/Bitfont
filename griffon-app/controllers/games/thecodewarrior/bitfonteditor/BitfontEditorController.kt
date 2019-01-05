package games.thecodewarrior.bitfonteditor

import griffon.core.artifact.GriffonController
import griffon.core.controller.ControllerAction
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonController
import griffon.transform.Threading
import javax.annotation.Nonnull

@ArtifactProviderFor(GriffonController::class)
class BitfontEditorController : AbstractGriffonController() {
    @set:[MVCMember Nonnull]
    lateinit var model: BitfontEditorModel

    @ControllerAction
    @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
    fun click() {
        val count = Integer.parseInt(model.clickCount)
        model.clickCount = (count + 1).toString()
    }
}