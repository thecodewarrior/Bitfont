package games.thecodewarrior.bitfont.editor

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import games.thecodewarrior.bitfont.editor.data.Bitfont
import games.thecodewarrior.bitfont.editor.data.UnifontImporter
import games.thecodewarrior.bitfont.editor.typesetting.BitfontAtlas
import games.thecodewarrior.bitfont.editor.utils.ReferenceFonts
import games.thecodewarrior.bitfont.editor.utils.extensions.addAll
import games.thecodewarrior.bitfont.editor.utils.keys
import imgui.ImGui
import imgui.InputTextFlag
import imgui.WindowFlag
import imgui.imgui.imgui_demoDebugInformations.Companion.showHelpMarker
import imgui.functionalProgramming.button
import imgui.functionalProgramming.menu
import imgui.functionalProgramming.menuBar
import imgui.functionalProgramming.withItemWidth
import imgui.g
import org.msgpack.core.MessagePack
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max

class FontInfoWindow(val document: BitfontDocument): IMWindow() {
    val bitfont: Bitfont = document.bitfont

    override val title: String
        get() = "${bitfont.name}: Font Information"
    var lastSave: Long = System.currentTimeMillis()

    /**
     * A file-friendly verison of the font name
     */
    val fName: String get() = bitfont.name.replace("\\s+".toRegex(), "-").replace("[/:]+".toRegex(), "_")

    init {
        windowFlags.addAll(WindowFlag.MenuBar, WindowFlag.AlwaysAutoResize)
    }

    override fun main() = with(ImGui) {
        if(System.currentTimeMillis() - lastSave > 60_000) {
            File("autosaves").mkdirs()
            File("autosaves/font-$fName-${LocalDateTime.now().minute % 10}.bitfont").writeBytes(bitfont.packToBytes())
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
            inputInt("Ascent", bitfont::ascent)
            sameLine(); showHelpMarker("The height of the \"top\" of the font above the baseline (added to the descent to get the line spacing)")
            inputInt("Descent", bitfont::descent)
            sameLine(); showHelpMarker("The depth of \"bottom\" of the font  below the baseline (added to the ascent to get the line spacing)")

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
                if(menuItem("Save")) {
                    val formatter = DateTimeFormatter.ofPattern("uuuuMMdd.kkmmss")
                    File("fonts").mkdirs()

                    val bytes = bitfont.packToBytes()
                    File("fonts/$fName.bitfont").writeBytes(bytes)

                    File("autosaves").mkdirs()
                    File("autosaves/font-$fName-backup-${LocalDateTime.now().format(formatter)}.bitfont").writeBytes(bytes)

                    lastSave = System.currentTimeMillis()
                }
                menu("Open") {
                    try {
                        val files = File("fonts").listFiles { _, name -> name.endsWith(".bitfont") }
                        var opened = false
                        files.forEach { file ->
                            if(menuItem(file.name) && !opened) {
                                opened = true
                                val bytes = file.readBytes()
                                val newDocument = BitfontDocument(Bitfont.unpack(bytes))
                                Main.documents.add(newDocument)
                            }
                        }
                    } catch(e: Exception) {
                        e.printStackTrace()
                    }
                }
                if(menuItem("New")) {
                    val bitfont = Bitfont("Untitled", 10, 4, 9, 6, 2)
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
                if(menuItem("Pack")) {
                    val atlas = BitfontAtlas(bitfont)
                    ImageIO.write(atlas.image(), "png", File("atlas.png"))
                }
                if(menuItem("Close")) {
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