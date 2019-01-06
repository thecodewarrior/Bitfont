package games.thecodewarrior.bitfonteditor.ucd

import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class UnicodeCharacterDatabase(val path: Path) {
    val blocks by lazy {
        val file = UCDFile(path.resolve("Blocks.txt"))
        file.load()

        return@lazy file.data.mapValues { it.value[0] }
    }

    val codepoints by lazy {
        val file = UCDFile(path.resolve("UnicodeData.txt"))
        file.load()

        val tree = TreeMap<UInt, UCDCodepoint>()
        file.data.map {
            UCDCodepoint.create(it.key.start, it.value)
        }.associateByTo(tree) { it.codepoint }

        return@lazy tree
    }

    class UCDFile(val path: Path) {
        val data = mutableMapOf<ClosedRange<UInt>, List<String>>()

        fun load() {
            val lines = Files.readAllLines(path)
            lines.forEach { fullLine ->
                val line = fullLine.split('#')[0]
                if(line.isBlank())
                    return@forEach
                val columns = line.split(';').map { it.trim() }
                val codepoints = columns[0].split("..").map { it.toUInt(16) }
                val codepointRange = codepoints[0]..codepoints.getOrElse(1) { codepoints[0] }
                data[codepointRange] = columns.drop(1)
            }
        }
    }

    data class UCDCodepoint(val codepoint: UInt, val name: String) {
        companion object {
            fun create(codepoint: UInt, data: List<String>): UCDCodepoint {
                val name = data[0]
                val generalCategory = data[1]
                val canonicalCombiningClass = data[2]
                val bidiClass = data[3]
                val decompositionTypeAndMapping = data[4]
                val numericTypeOrValue1 = data[5]
                val numericTypeOrValue2 = data[6]
                val numericTypeOrValue3 = data[7]
                val bidiMirrored = data[8]
                val unicode1Name = data[9]
                val isoComment = data[10]
                val simpleUppercaseMapping = data[11]
                val simpleLowercaseMapping = data[12]
                val simpleTitlecaseMapping = data[13]

                return UCDCodepoint(codepoint, if(name == "<control>") unicode1Name else name)
            }
        }
    }
}

