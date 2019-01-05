package games.thecodewarrior.bitfonteditor

import griffon.core.artifact.GriffonController
import griffon.core.controller.ControllerAction
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import griffon.transform.Threading
import griffon.util.CollectionUtils
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonController
import javax.inject.Inject

@ArtifactProviderFor(GriffonController::class)
class ContainerController: AbstractGriffonController() {
    @MVCMember
    private lateinit var model: ContainerModel
    @MVCMember
    private lateinit var view: ContainerView

    @Inject
    private lateinit var fontCreationService: FontCreationService

    @ControllerAction
    @Threading(Threading.Policy.SKIP)
    fun open() {
        val file = view.selectFile()
        if (file != null) {
//            val mvcIdentifier = file.getName() + "-" + System.currentTimeMillis()
//            createMVC("editor", mvcIdentifier, mapOf(
//                "document" to Document(file, file.name),
//                "tabName" to file.name)
//            )
        }
    }

    @ControllerAction
    fun new() {
        fontCreationService.create()
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