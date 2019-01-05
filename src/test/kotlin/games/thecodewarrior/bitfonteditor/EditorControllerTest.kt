package games.thecodewarrior.bitfonteditor

import griffon.core.test.GriffonUnitRule
import griffon.core.test.TestFor
import javafx.embed.swing.JFXPanel
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@TestFor(EditorController::class)
class EditorControllerTest {
    init {
        // force initialization JavaFX Toolkit
        JFXPanel()
    }

    lateinit var controller: EditorController

    @Rule @JvmField
    val griffon:GriffonUnitRule = GriffonUnitRule()

    @Test
    fun smoke() {
        Assert.fail("Not yet implemented!")
    }
}