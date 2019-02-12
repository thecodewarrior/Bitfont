package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.editor.utils.Constants
import games.thecodewarrior.bitfont.editor.utils.nameslist.AliasLine
import games.thecodewarrior.bitfont.editor.utils.nameslist.CommentLine
import games.thecodewarrior.bitfont.editor.utils.nameslist.CompatMapping
import games.thecodewarrior.bitfont.editor.utils.nameslist.CrossRef
import games.thecodewarrior.bitfont.editor.utils.nameslist.Decomposition
import games.thecodewarrior.bitfont.editor.utils.nameslist.ExpandLine
import games.thecodewarrior.bitfont.editor.utils.nameslist.ExpandLineElement
import games.thecodewarrior.bitfont.editor.utils.nameslist.FormalAliasLine
import games.thecodewarrior.bitfont.editor.utils.nameslist.NoticeLine
import games.thecodewarrior.bitfont.editor.utils.nameslist.VariationLine
import imgui.ImGui
import imgui.functionalProgramming.button

class GlyphDetailPane(val document: BitfontDocument) {
    val width: Float
        get() = 175f
    var codepoint = 0

    fun draw() = with(ImGui) {
        val defaultFontScale = ImGui.font.scale
        val info = Constants.namesList.chars[codepoint]

        ImGui.font.scale = 1.75f
        if(info == null) {
            text("U+%04X".format(codepoint))
            ImGui.font.scale = defaultFontScale
            return
        }
        text(info.name)
        ImGui.font.scale = defaultFontScale
/*
        info.lines.forEach { line ->
            when(line) {
                is AliasLine -> textWrapped("= ${line.text}")
                is FormalAliasLine -> textWrapped("% ${line.name}")
                is CommentLine -> {
                    if(line.hasBullet) {
                        bullet()
                    }
                    draw(line.expandLine)
                }
                is CrossRef -> {
                    textWrapped("-> %04X - %s".format(line.codepoint, line.name))
                }
                is Decomposition -> {}
                is CompatMapping -> {}
                is NoticeLine -> {}
                is VariationLine -> {}
            }
        }
*/

        button("Edit") {
            document.editorWindow.codepoint = codepoint
            document.editorWindow.codepointHistory.push(codepoint)
            document.editorWindow.visible = true
            document.editorWindow.focus()
        }
    }

    fun draw(line: ExpandLine) = with(ImGui) {
        line.elements.forEach { element ->
            when(element) {
                is ExpandLineElement.Text -> {
                    sameLine()
                    textWrapped(element.text)
                }
                is ExpandLineElement.Codepoint -> {
                    sameLine()
                    smallButton("%04X##%s".format(element.codepoint, System.identityHashCode(element)))
                }
            }
        }
    }
}