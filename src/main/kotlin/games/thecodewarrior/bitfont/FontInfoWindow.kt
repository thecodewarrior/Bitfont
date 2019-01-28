package games.thecodewarrior.bitfont

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.data.UnifontImporter
import games.thecodewarrior.bitfont.utils.ReferenceFonts
import games.thecodewarrior.bitfont.utils.extensions.addAll
import games.thecodewarrior.bitfont.utils.ifMac
import games.thecodewarrior.bitfont.utils.keys
import games.thecodewarrior.bitfont.utils.nameslist.NamesList
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
import kotlin.math.abs
import kotlin.math.max

class FontInfoWindow(val document: BitfontDocument): IMWindow() {
    val bitfont: Bitfont = document.bitfont

    override val title: String
        get() = "${bitfont.name}: Font Information"
    var lastSave: Long = System.currentTimeMillis()

    init {
        windowFlags.addAll(WindowFlag.MenuBar, WindowFlag.AlwaysAutoResize)
    }

    override fun main() = with(ImGui) {
        if(System.currentTimeMillis() - lastSave > 60_000) {
            val s = bitfont.writeJson().toJsonString(true)
            File("font-autosave-${LocalDateTime.now().minute % 10}.json").writeText(s)
            lastSave = System.currentTimeMillis()
        }
        withItemWidth(150f) {
            val arr = bitfont.name.toCharArray().let { name ->
                CharArray(name.size + 1000).also { name.copyInto(it) }
            }
            if(inputText("Name", arr, InputTextFlag.EnterReturnsTrue.i))
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
            inputInt("Spacing", bitfont::spacing)
            sameLine(); showHelpMarker("The amount of space between consecutive characters")
        }

        withItemWidth(175f) {
            pushAllowKeyboardFocus(false)
            listBox("##style", document::referenceStyle,
                ReferenceFonts.styles, 3)
            val speed = 1f / max(1f, abs(getMouseDragDelta(0).y) / 10)
            alignTextToFramePadding()
            text("Size")
            sameLine()
            withItemWidth(175f - cursorPosX) {
                pushAllowKeyboardFocus(false)
                dragFloat("Size", document::referenceSize, speed, 1f, 1000f)
            }
        }

        button("Edit") {
            document.editorWindow = GlyphEditorWindow(document)
            document.editorWindow.visible = true
        }
        sameLine()
        button("Browse") {
            val browser = GlyphBrowserWindow(document)
            browser.visible = true
            document.browserWindows.add(browser)
        }
        sameLine()
        button("Test") {
            val test = TestingWindow(document)
            test.visible = true
            document.testWindows.add(test)
        }
        sameLine()
        button("Input") {
            val input = InputTestWindow(document)
            input.visible = true
            document.inputWindows.add(input)
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
                if(menuItem("Save", ifMac("Cmd+S", "Ctrl+S"))) {
                    val formatter = DateTimeFormatter.ofPattern("uuuuMMdd.kkmmss")
                    val s = bitfont.writeJson().toJsonString(true)
                    File("font.json").writeText(s)
                    File("${bitfont.name.replace("[^A-Za-z0-9_]+".toRegex(), "_")}.json").writeText(s)
                    File("font-${LocalDateTime.now().format(formatter)}.json").writeText(s)
                    lastSave = System.currentTimeMillis()
                }
                if(menuItem("Open", ifMac("Cmd+O", "Ctrl+O"))) {
                    val json = Parser.default().parse(StringBuilder(File("font.json").readText())) as JsonObject
                    val bitfont = Bitfont.readJson(json)
                    val newDocument = BitfontDocument(bitfont)
                    Main.documents.add(newDocument)
                }
                if(menuItem("New", ifMac("Cmd+N", "Ctrl+N"))) {
                    val bitfont = Bitfont("Untitled", 16, 10, 4, 9, 6, 2)
                    val newDocument = BitfontDocument(bitfont)
                    Main.documents.add(newDocument)
                }
                if(menuItem("Import")) {
                    val bitfont = UnifontImporter.import(File("unifont.hex"))
                    val newDocument = BitfontDocument(bitfont)
                    Main.documents.add(newDocument)
                }
                if(menuItem("Optimize")) {
                    bitfont.glyphs.forEach { _, glyph -> glyph.crop() }
                }
                if(menuItem("NamesList")) {
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