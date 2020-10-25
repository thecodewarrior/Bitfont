package dev.thecodewarrior.bitfont.editor.jimgui

import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.data.file.BitfontFile
import dev.thecodewarrior.bitfont.data.file.BitfontFileFormat
import dev.thecodewarrior.bitfont.editor.jimgui.data.UnifontImporter
import dev.thecodewarrior.bitfont.editor.jimgui.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.jimgui.imgui.withNative
import dev.thecodewarrior.bitfont.editor.jimgui.testingwindow.GlyphGeneratorTestWindow
import dev.thecodewarrior.bitfont.editor.jimgui.testingwindow.TextLayoutTestWindow
import dev.thecodewarrior.bitfont.editor.jimgui.testingwindow.TypesetterTestWindow
import dev.thecodewarrior.bitfont.editor.jimgui.typesetting.BitfontAtlas
import dev.thecodewarrior.bitfont.editor.jimgui.utils.ReferenceFonts
import dev.thecodewarrior.bitfont.editor.jimgui.utils.keys
import org.ice1000.jimgui.flag.JImInputTextFlags
import org.ice1000.jimgui.flag.JImWindowFlags
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
        windowFlags = windowFlags or JImWindowFlags.MenuBar or JImWindowFlags.AlwaysAutoResize
    }

    override fun main(imgui: ImGui) {
        if(System.currentTimeMillis() - lastSave > 60_000) {
            File("autosaves").mkdirs()
            val bitfontFile = BitfontFileFormat.pack(bitfont)
            File("autosaves/font-$fName-${LocalDateTime.now().minute % 10}.bitfont").writeBytes(bitfontFile.packToBytes())
            lastSave = System.currentTimeMillis()
        }
        imgui.setNextItemWidth(150f)
        val arr = bitfont.name.toByteArray().let { name ->
            ByteArray(name.size + 1000).also { name.copyInto(it) }
        }
        if(imgui.inputText("Name", arr, JImInputTextFlags.EnterReturnsTrue))
            bitfont.name = String(arr.sliceArray(0 until arr.indexOf(0.toByte())))

        imgui.pushItemWidth(100f)
        run {
            bitfont::ascent.withNative {
                imgui.inputInt("Ascent", it)
            }
            //imgui.sameLine(); imgui.showHelpMarker("The height of the \"top\" of the font above the baseline (added to the descent to get the line spacing)")

            bitfont::descent.withNative {
                imgui.inputInt("Descent", it)
            }
            //imgui.sameLine(); imgui.showHelpMarker("The depth of \"bottom\" of the font  below the baseline (added to the ascent to get the line spacing)")

            bitfont::capHeight.withNative {
                imgui.inputInt("Cap height", it)
            }
            //imgui.sameLine(); imgui.showHelpMarker("The height of capital letters (X, N, etc.) above the baseline, ignoring letters like A or O which may overshoot this line")

            bitfont::xHeight.withNative {
                imgui.inputInt("x height", it)
            }
            //imgui.sameLine(); imgui.showHelpMarker("The height of the short lowercase letters (x, n, etc.) above the baseline, ignoring letters like d or l which overshoot this line")

            bitfont::spacing.withNative {
                imgui.inputInt("Spacing", it)
            }
            //imgui.sameLine(); imgui.showHelpMarker("The amount of space between consecutive characters")
        }
        imgui.popItemWidth()

        imgui.pushItemWidth(175f)
        run {
            imgui.pushAllowKeyboardFocus(false)
            document.referenceStyle = imgui.listBox("##style", document.referenceStyle, ReferenceFonts.styles, 3)
            val speed = 1f / max(1f, abs(imgui.io.mouseDeltaY) / 10)
            imgui.alignTextToFramePadding()
            imgui.text("Size")
            imgui.sameLine()

            imgui.setNextItemWidth(175f - imgui.cursorPosX)

            imgui.pushAllowKeyboardFocus(false)

            document::referenceSize.withNative {
                imgui.dragFloat("Size", it, speed, 1f, 1000f)
            }
        }
        imgui.popItemWidth()

        if(imgui.button("Edit")) {
//            document.editorWindow = GlyphEditorWindow(document)
//            document.editorWindow.visible = true
        }
        imgui.sameLine()
        if(imgui.button("Browse")) {
            val browser = GlyphBrowserWindow(document)
            browser.visible = true
            document.children.add(browser)
        }
        imgui.text("Tests")
        fun test(name: String, constructor: (BitfontDocument) -> IMWindow) {
            if(imgui.button(name)) {
                val test = constructor(document)
                test.visible = true
                document.children.add(test)
            }
        }
//        test("Old Typesetting", ::TestingWindow)
        test("Glyph Generator", ::GlyphGeneratorTestWindow)
        test("Typesetter", ::TypesetterTestWindow)
        test("Text Layout", ::TextLayoutTestWindow)
    }

    override fun drawMenu(imgui: ImGui) {
        if(!imgui.beginMenuBar()) return
        imgui.keys {
            imgui.menu("File") {
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
                if(imgui.menuItem("Save")) {
                    val formatter = DateTimeFormatter.ofPattern("uuuuMMdd.kkmmss")
                    File("fonts").mkdirs()

                    val bytes = BitfontFileFormat.pack(bitfont).packToBytes()
                    File("fonts/$fName.bitfont").writeBytes(bytes)

                    File("autosaves").mkdirs()
                    File("autosaves/font-$fName-backup-${LocalDateTime.now().format(formatter)}.bitfont").writeBytes(bytes)

                    lastSave = System.currentTimeMillis()
                }
                imgui.menu("Open") {
                    try {
                        val files = File("fonts").listFiles { _, name -> name.endsWith(".bitfont") }
                        var opened = false
                        files.forEach { file ->
                            if(imgui.menuItem(file.name) && !opened) {
                                opened = true
                                val bitfontFile = BitfontFile.unpack(file.inputStream())
                                val newDocument = BitfontDocument(BitfontFileFormat.unpack(bitfontFile))
                                Main.documents.add(newDocument)
                            }
                        }
                    } catch(e: Exception) {
                        e.printStackTrace()
                    }
                }
                if(imgui.menuItem("New")) {
                    val bitfont = Bitfont("Untitled", 10, 4, 9, 6, 2)
                    val newDocument = BitfontDocument(bitfont)
                    Main.documents.add(newDocument)
                }
                imgui.menu("Import") {
                    imgui.menu("Unifont") {
                        if(imgui.menuItem("BMP (unifont.hex)")) {
                            Main.documents.add(BitfontDocument(importUnifont("Unifont BMP", false, "unifont.hex")))
                            Main.documents.add(BitfontDocument(importUnifont("Unifont BMP", true, "unifont.hex")))
                        }
                        if(imgui.menuItem("Plane 1 (unifont_upper.hex)")) {
                            Main.documents.add(BitfontDocument(importUnifont("Unifont Plane 1", false, "unifont_upper.hex")))
                            Main.documents.add(BitfontDocument(importUnifont("Unifont Plane 1", true, "unifont_upper.hex")))
                        }
                        if(imgui.menuItem("All (unifont.hex, unifont_upper.hex)")) {
                            Main.documents.add(BitfontDocument(importUnifont("Unifont", false, "unifont.hex", "unifont_upper.hex")))
                            Main.documents.add(BitfontDocument(importUnifont("Unifont", true, "unifont.hex", "unifont_upper.hex")))
                        }
                        if(imgui.menuItem("CSUR (unifont_csur.hex, unifont_csur_lower.hex)")) {
                            Main.documents.add(BitfontDocument(importUnifont("Unifont CSUR", false, "unifont.hex", "unifont_upper.hex")))
                            Main.documents.add(BitfontDocument(importUnifont("Unifont CSUR", true, "unifont.hex", "unifont_upper.hex")))
                        }
                    }
                }
                imgui.menu("Advanced") {
                    if (imgui.menuItem("Optimize")) {
                        bitfont.glyphs.forEach { (_, glyph) -> glyph.crop() }
                    }
                    if (imgui.menuItem("Pack")) {
                        val atlas = BitfontAtlas(bitfont)
                        ImageIO.write(atlas.image(), "png", File("atlas.png"))
                    }
                    if (imgui.menuItem("Copy Codepoints")) {
                        val builder = StringBuilder()
                        var previous = 0
                        bitfont.glyphs.keys.sorted().forEach { codepoint ->
                            val glyph = bitfont.glyphs[codepoint]
                            glyph.crop()
                            if(!glyph.image.isEmpty()) {
                                if(previous / 256 != codepoint / 256)
                                    builder.append('\n')
//                                val combiningClass = CombiningClass[UCharacter.getCombiningClass(codepoint)]
//                                if(combiningClass != CombiningClass.NOT_REORDERED)
//                                    builder.append('◌')
                                builder.append(Character.toChars(codepoint))
                                previous = codepoint
                            }
                        }
                        imgui.clipboardText = builder.toString()
                    }
                }
                if(imgui.menuItem("Close")) {
                    Main.documents.remove(document)
                }
//                menuItem("Save As..")
//                separator()
//                menuItem("Checked", selected = true)
//                menuItem("Quit", "Alt+F4")
            }
        }
        imgui.endMenuBar()
    }

    fun importUnifont(name: String, autoAdvance: Boolean, vararg files: String): Bitfont {
        val lines = files.flatMap { File(it).readLines() }
        val bitfont = UnifontImporter.import(lines)

        bitfont.name = name + (if(autoAdvance) " (auto advance)" else " (fixed advance)")

        if(autoAdvance) {
            bitfont.glyphs.forEach { (_, it) ->
//                if (!it.isEmpty())
//                    it.advance = null
            }
        }

        return bitfont
    }
}