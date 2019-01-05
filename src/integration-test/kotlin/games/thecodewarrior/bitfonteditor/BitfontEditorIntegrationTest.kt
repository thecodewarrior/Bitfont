package games.thecodewarrior.bitfonteditor

import griffon.javafx.test.GriffonTestFXRule
import org.junit.Rule
import org.junit.Test

import org.testfx.api.FxAssert
import org.testfx.matcher.control.LabeledMatchers

class BitfontEditorIntegrationTest {
    @Rule @JvmField
    val testfx: GriffonTestFXRule = GriffonTestFXRule("mainWindow")

    @Test
    fun clickButton() {
        // given:
        FxAssert.verifyThat("#clickLabel", LabeledMatchers.hasText("0"))

        // when:
        testfx.clickOn("#clickActionTarget")

        // then:
        FxAssert.verifyThat("#clickLabel", LabeledMatchers.hasText("1"))
    }
}
