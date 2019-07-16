package dev.thecodewarrior.bitfont.editor.utils.nameslist

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.io.InputStream

class NamesList {
    val chars = Int2ObjectOpenHashMap<CharEntry>()

    fun read(inputStream: InputStream) {
        inputStream.use { _ ->
            val lines = inputStream.bufferedReader().lineSequence()
            readLines(lines.mapIndexed { i, line ->
                try {
                    NamesListLine.parse(line)
                } catch (e: RuntimeException) {
                    throw RuntimeException("Error reading NamesList.txt on line ${i+1}", e)
                }
            })
        }
    }

    fun readLines(lines: Sequence<NamesListLine>) {
        var currentChar: CharEntry? = null
        for((i, line) in lines.withIndex()) {
            try {
                when(line) {
                    is AliasLine,
                    is FormalAliasLine,
                    is CommentLine,
                    is CrossRef,
                    is Decomposition,
                    is CompatMapping,
                    is NoticeLine,
                    is VariationLine -> {
                        currentChar?.lines?.add(line)
                    }
                    is NameLine -> {
                        if(currentChar != null) {
                            chars[currentChar.codepoint] = currentChar
                        }
                        currentChar = CharEntry(line.char, line.name)
                    }
                    else -> {
                        if(currentChar != null) {
                            chars[currentChar.codepoint] = currentChar
                            currentChar = null
                        }
                    }
                }
            } catch (e: RuntimeException) {
                throw RuntimeException("Error processing NamesList.txt on line ${i+1}", e)
            }
        }
    }
}

