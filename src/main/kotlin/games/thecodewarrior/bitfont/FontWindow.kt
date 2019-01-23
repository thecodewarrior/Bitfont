package games.thecodewarrior.bitfont

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import games.thecodewarrior.bitfont.data.BitFont
import games.thecodewarrior.bitfont.utils.Constants
import games.thecodewarrior.bitfont.utils.extensions.toPrettyJsonString
import games.thecodewarrior.bitfont.utils.ifMac
import games.thecodewarrior.bitfont.utils.keys
import glm_.func.common.clamp
import imgui.ImGui
import imgui.InputTextFlag
import imgui.WindowFlag
import imgui.imgui.imgui_demoDebugInformations.Companion.showHelpMarker
import imgui.functionalProgramming.button
import imgui.functionalProgramming.menu
import imgui.functionalProgramming.menuBar
import imgui.g
import java.io.File

class FontWindow(val bitFont: BitFont): IMWindow() {
    override val title: String
        get() = "Font"
    override val children: MutableList<IMWindow>
        get() = mutableListOf(glyphWindow)



    var glyphWindow = GlyphEditor(bitFont)

    init {
        windowFlags.add(WindowFlag.MenuBar)
    }

    override fun main() = with(ImGui) {
        text("This is some useful text.")

        val arr = bitFont.name.toCharArray().let { name ->
            CharArray(name.size + 10).also { name.copyInto(it) }
        }
        if(inputText("Name", arr, InputTextFlag.EnterReturnsTrue.i))
            bitFont.name = String(g.inputTextState.textW.sliceArray(0 until g.inputTextState.textW.indexOf('\u0000')))
        sameLine(); showHelpMarker("The distance between the baselines of consecutive lines")
        inputInt("Line Height", bitFont::lineHeight)
        sameLine(); showHelpMarker("The distance between the baselines of consecutive lines")
        inputInt("Ascender", bitFont::ascender)
        sameLine(); showHelpMarker("The height of ascenders (the top of d, l, etc.) above the baseline")
        inputInt("Descender", bitFont::descender)
        sameLine(); showHelpMarker("The height of descenders (the bottom of p, g, etc.) below the baseline")

        inputInt("Cap height", bitFont::capHeight)
        sameLine(); showHelpMarker("The height of capital letters (X, N, etc.) above the baseline, ignoring letters like A or O which may overshoot this line")
        inputInt("x height", bitFont::xHeight)
        sameLine(); showHelpMarker("The height of the short lowercase letters (x, n, etc.) above the baseline, ignoring letters like d or l which overshoot this line")

        button("Edit") {
            glyphWindow = GlyphEditor(bitFont)
            glyphWindow.visible = true
        }

        drawMenu()
    }

    fun drawMenu() = with(ImGui) {
        menuBar { keys {
            menu("File") {
                //                menuItem("Open", "Ctrl+O")
//                menu("Open Recent") {
//                    menuItem("fish_hat.c")
//                    menuItem("fish_hat.inl")
//                    menuItem("fish_hat.h")
//                    menu("More..") {
//                        menuItem("Hello")
//                        menuItem("Sailor")
//                    }
//                }
                if(menuItem("Save", ifMac("⌘S", "Ctrl+S")) || (isWindowHovered() && "prim+S".pressed())) {
                    val s = bitFont.writeJson().toJsonString(true)
                    File("font.json").writeText(s)
                }
                if(menuItem("Open", ifMac("⌘O", "Ctrl+O")) || (isWindowHovered() && "prim+O".pressed())) {
                    val json = Parser.default().parse(StringBuilder(File("font.json").readText())) as JsonObject
                    val bitFont = BitFont.readJson(json)
                    val newWindow = FontWindow(bitFont)
                    newWindow.visible = true
                    Main.windows.add(newWindow)
                }
//                menuItem("Save As..")
//                separator()
//                menuItem("Checked", selected = true)
//                menuItem("Quit", "Alt+F4")
            }
        } }
    }

}