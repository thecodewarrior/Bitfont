package dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.nameslist

data class Block(val summary: String)

sealed class BlockEntryItem

data class CharEntry(var codepoint: Int, var name: String, var lines: MutableList<NamesListLine> = mutableListOf()): BlockEntryItem() {
    companion object {
    }
}
