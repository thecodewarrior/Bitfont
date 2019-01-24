package games.thecodewarrior.bitfont

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.utils.extensions.addAll
import games.thecodewarrior.bitfont.utils.ifMac
import games.thecodewarrior.bitfont.utils.keys
import imgui.ImGui
import imgui.InputTextFlag
import imgui.WindowFlag
import imgui.imgui.imgui_demoDebugInformations.Companion.showHelpMarker
import imgui.functionalProgramming.button
import imgui.functionalProgramming.menu
import imgui.functionalProgramming.menuBar
import imgui.functionalProgramming.withItemWidth
import imgui.g
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FontInfoWindow(val document: BitfontDocument): IMWindow() {
    val bitfont: Bitfont = document.bitfont

    override val title: String
        get() = "${bitfont.name}: Font Information"

    init {
        windowFlags.addAll(WindowFlag.MenuBar, WindowFlag.AlwaysAutoResize)
    }

    override fun main() = with(ImGui) {
        withItemWidth(150f) {
            val arr = bitfont.name.toCharArray().let { name ->
                CharArray(name.size + 10).also { name.copyInto(it) }
            }
            if (inputText("Name", arr, InputTextFlag.EnterReturnsTrue.i))
                bitfont.name = String(g.inputTextState.textW.sliceArray(0 until g.inputTextState.textW.indexOf('\u0000')))
        }
        withItemWidth(100f) {
            inputInt("Line Height", bitfont::lineHeight)
            sameLine(); showHelpMarker("The distance between the baselines of consecutive lines")
            inputInt("Ascender", bitfont::ascender)
            sameLine(); showHelpMarker("The height of ascenders (the top of d, l, etc.) above the baseline")
            inputInt("Descender", bitfont::descender)
            sameLine(); showHelpMarker("The height of descenders (the bottom of p, g, etc.) below the baseline")

            inputInt("Cap height", bitfont::capHeight)
            sameLine(); showHelpMarker("The height of capital letters (X, N, etc.) above the baseline, ignoring letters like A or O which may overshoot this line")
            inputInt("x height", bitfont::xHeight)
            sameLine(); showHelpMarker("The height of the short lowercase letters (x, n, etc.) above the baseline, ignoring letters like d or l which overshoot this line")
        }

        button("Edit") {
            document.editorWindow = GlyphEditorWindow(document)
            document.editorWindow.visible = true
        }
        sameLine()
        button("Browse") {
            document.browserWindow = GlyphBrowserWindow(document)
            document.browserWindow.visible = true
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
                if(menuItem("Save", ifMac("Cmd+S", "Ctrl+S")) || (isWindowHovered() && "prim+S".pressed())) {
                    val formatter = DateTimeFormatter.ofPattern("uuuuMMdd.kkmmss")
                    val s = bitfont.writeJson().toJsonString(true)
                    File("font.json").writeText(s)
                    File("font-${LocalDateTime.now().format(formatter)}.json").writeText(s)
                }
                if(menuItem("Open", ifMac("Cmd+O", "Ctrl+O")) || (isWindowHovered() && "prim+O".pressed())) {
                    val json = Parser.default().parse(StringBuilder(File("font.json").readText())) as JsonObject
                    val bitfont = Bitfont.readJson(json)
                    val newDocument = BitfontDocument(bitfont)
                    Main.documents.add(newDocument)
                }
                if(menuItem("Close", ifMac("Cmd+W", "Ctrl+W"))) {
                    Main.documents.remove(document)
                }
//                menuItem("Save As..")
//                separator()
//                menuItem("Checked", selected = true)
//                menuItem("Quit", "Alt+F4")
            }
        } }
    }

}