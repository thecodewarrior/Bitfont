package games.thecodewarrior.bitfonteditor

import games.thecodewarrior.bitfont.file.BitfontBundle
import griffon.core.artifact.GriffonController
import griffon.core.controller.ControllerAction
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import griffon.transform.Threading
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonController
import javax.inject.Inject

@ArtifactProviderFor(GriffonController::class)
class ContainerController: AbstractGriffonController() {
    @MVCMember
    private lateinit var model: ContainerModel
    @MVCMember
    private lateinit var view: ContainerView

    @Inject
    private lateinit var updateUCDService: UpdateUCDService

    @ControllerAction
    @Threading(Threading.Policy.SKIP)
    fun open() {
        val bundle = BitfontBundle()
        val file = view.selectFile() ?: return
        bundle.open(file.toPath())

        val mvcIdentifier = "" + System.currentTimeMillis()
        application.mvcGroupManager.createMVC("overview", mvcIdentifier, mapOf("bundle" to bundle))
    }

    @ControllerAction
    fun new() {
        val bundle = BitfontBundle()

        updateUCDService.updateUCD(bundle.ucd)

        val mvcIdentifier = "" + System.currentTimeMillis()
        application.mvcGroupManager.createMVC("overview", mvcIdentifier, mapOf("bundle" to bundle))
//        application.mvcGroupManager.createMVC("editor", mvcIdentifier, mapOf())
    }

    @ControllerAction
    fun save() {
    }

    @ControllerAction
    fun close() {
    }

    @ControllerAction
    fun quit() {
        getApplication().shutdown()
    }
}