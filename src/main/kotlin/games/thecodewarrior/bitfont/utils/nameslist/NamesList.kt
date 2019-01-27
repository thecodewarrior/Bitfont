package games.thecodewarrior.bitfont.utils.nameslist

import games.thecodewarrior.bitfont.utils.extensions.component1
import games.thecodewarrior.bitfont.utils.extensions.component2
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class NamesList {
    val chars = Int2ObjectOpenHashMap<CharEntry>()
    fun read(lines: Sequence<String>) {
        var entry: CharEntry? = null
        for((i, line) in lines.withIndex()) {
            try {
                val parsed = NamesListLine.parse(line)
            } catch (e: RuntimeException) {
                throw RuntimeException("Error reading NamesList.txt on line ${i+1}", e)
            }
        }
    }
}

