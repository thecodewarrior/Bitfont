package games.thecodewarrior.bitfonteditor

import griffon.javafx.test.GriffonTestFXRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

import org.junit.Assert.fail

class EditorIntegrationTest {
    @Rule @JvmField
    val testfx: GriffonTestFXRule = GriffonTestFXRule("mainWindow")

    @Test
    fun smokeTest() {
        Assert.fail("Not yet implemented!")
    }
}
