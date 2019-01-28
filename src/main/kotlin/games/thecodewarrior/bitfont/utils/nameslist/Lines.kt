package games.thecodewarrior.bitfont.utils.nameslist

import games.thecodewarrior.bitfont.utils.extensions.component1
import games.thecodewarrior.bitfont.utils.extensions.component2
import games.thecodewarrior.bitfont.utils.extensions.component3
import games.thecodewarrior.bitfont.utils.extensions.component4
import games.thecodewarrior.bitfont.utils.extensions.component5
import games.thecodewarrior.bitfont.utils.extensions.re
import games.thecodewarrior.bitfont.utils.extensions.removePrefix
import games.thecodewarrior.bitfont.utils.extensions.startsWith

private val nameRE = "[A-Z0-9 -]+"
private val charRE = "[0-9A-F]{4,6}"
private val lcnameRE = "[a-z0-9 -]+(?:-$charRE)?"
private val lctagRE = "[A-Za-z]+"
private val charListRE = "$charRE(?: $charRE)*"
private val stringRE = "[\u0020-\u007e\u00A0-\u02ff]+"
private val labelRE = "[\u0020-\u0027\u002A-\u007e\u00A0-\u02ff]+"
private val varselRE = "(?:$charRE|ALT[0-9])"
private val varselListRE = "\\{$charListRE\\}"
private val tabRE = "\t+"
private val lineRE = "$stringRE$"
private val expandLineRE = """(?:\b\\?$charRE\b|$stringRE?)+"""

private val expandLineElementRE = """(?:(\b\\?$charRE\b)|($stringRE?))""" // single expandLine element w/ capture groups
private val commentLineRE = """$tabRE(\* )?($expandLineRE)""".toRegex()
private val crossRefParenRE = """${tabRE}x \(($lcnameRE|<$lcnameRE>) - ($charRE)\)""".toRegex()
private val crossRefRE = """${tabRE}x ($charRE)(?: ($lcnameRE))?""".toRegex()
private val compatMappingRE = """$tabRE# (?:(<$lctagRE>) )?($expandLineRE)""".toRegex()
private val noticeLineRE = """@\+$tabRE(\* )?($expandLineRE)""".re
private val variationLineRE = """$tabRE~ ($charRE) ($varselRE) ($labelRE)(\($lcnameRE\))?""".toRegex()
private val nameLineRE = """($charRE)$tabRE($nameRE|<$lcnameRE>)(?: (?:\(($labelRE)\))? ?(\*)?)?""".toRegex()
private val variationSubheaderRE = """@~(?:$tabRE(?:(!)($varselListRE)?)?($lineRE)?)?""".toRegex()
private val mixedSubheaderRE = """@@${variationSubheaderRE.pattern}""".toRegex()
private val altglyphSubheaderRE = """@@~(?:(!)|($lineRE))?""".toRegex()
private val blockHeaderRE = """@@$tabRE($charRE)$tabRE(?:($labelRE)(?: \(($labelRE)\))?)$tabRE($charRE)""".toRegex()


//@formatter:off
private val nameIdentifierRE               = charRE.re
private val commentLineIdentifierRE        = tabRE.re
private val aliasLineIdentifierRE          = "$tabRE=".re
private val formalAliasLineIdentifierRE    = "$tabRE%".re
private val crossRefIdentifierRE           = "${tabRE}x".re
private val variationLineIdentifierRE      = "$tabRE~".re
private val fileCommentIdentifierRE        = ";".re
private val emptyLineIdentifierRE          = "$".re
private val ignoredIdentifierRE            = "$tabRE;".re
private val sidebarLineIdentifierRE        = ";;".re
private val decompositionIdentifierRE      = "$tabRE:".re
private val compatMappingIdentifierRE      = "$tabRE#".re
private val noticeLineIdentifierRE         = "@\\+$tabRE".re
private val titleIdentifierRE              = "@@@$tabRE".re
private val subtitleIdentifierRE           = "@@@\\+$tabRE".re
private val subheaderIdentifierRE          = "@$tabRE".re
private val variationSubheaderIdentifierRE = "@~".re
private val altglyphSubheaderIdentifierRE  = "@@~".re
private val mixedSubheaderIdentifierRE     = "@@@~".re
private val blockHeaderIdentifierRE        = "@@$tabRE".re
private val pagebreakIdentifierRE          = "@@$".re
private val indexTabIdentifierRE           = "@@\\+$".re
//@formatter:on

sealed class NamesListLine {
    companion object {
        fun parse(line: String): NamesListLine {
            //@formatter:off
            return when {
                line.startsWith(nameIdentifierRE)               -> nameLine(line)
                // commentLineIdentifierRE is a superset of a lot of these, and so goes at the end
                line.startsWith(aliasLineIdentifierRE)          -> aliasLine(line)
                line.startsWith(formalAliasLineIdentifierRE)    -> formalAliasLine(line)
                line.startsWith(crossRefIdentifierRE)           -> crossRef(line)
                line.startsWith(variationLineIdentifierRE)      -> variationLine(line)
                // fileCommentIdentifierRE is a superset of sidebarLineIdentifierRE, and so goes at the end
                line.startsWith(emptyLineIdentifierRE)          -> EmptyLine
                line.startsWith(ignoredIdentifierRE)            -> ignoredLine(line)
                line.startsWith(sidebarLineIdentifierRE)        -> sidebarLine(line)
                line.startsWith(decompositionIdentifierRE)      -> decomposition(line)
                line.startsWith(compatMappingIdentifierRE)      -> compatMapping(line)
                line.startsWith(noticeLineIdentifierRE)         -> noticeLine(line)
                line.startsWith(titleIdentifierRE)              -> titleLine(line)
                line.startsWith(subtitleIdentifierRE)           -> subtitleLine(line)
                line.startsWith(subheaderIdentifierRE)          -> subheaderLine(line)
                line.startsWith(variationSubheaderIdentifierRE) -> variationSubheader(line)
                line.startsWith(altglyphSubheaderIdentifierRE)  -> altglyphSubheader(line)
                line.startsWith(mixedSubheaderIdentifierRE)     -> mixedSubheader(line)
                line.startsWith(blockHeaderIdentifierRE)        -> blockHeader(line)
                line.startsWith(pagebreakIdentifierRE)          -> PageBreak
                line.startsWith(indexTabIdentifierRE)           -> IndexTab

                line.startsWith(fileCommentIdentifierRE)        -> fileCommentLine(line)
                line.startsWith(commentLineIdentifierRE)        -> commentLine(line)
                else -> throw IllegalArgumentException("Unable to identify line type: $line")
            }
            //@formatter:on
        }
    }
}

data class NameLine(val char: Int, val name: String, val label: String, val hasCommentStar: Boolean): NamesListLine()
private fun nameLine(line: String): NameLine {
    val (_, char, name, comment, commentStar) = nameLineRE.matchEntire(line)
    return NameLine(char.toInt(16), name, comment, commentStar != "")
}

data class CommentLine(val hasBullet: Boolean, val expandLine: ExpandLine): NamesListLine()
private fun commentLine(line: String): CommentLine {
    val (_, star, text) = commentLineRE.matchEntire(line)
    return CommentLine(star != "", expandLine(text))
}

data class AliasLine(val text: String): NamesListLine()
private fun aliasLine(line: String): AliasLine {
    return AliasLine(line.removePrefix("$tabRE= "))
}

data class FormalAliasLine(val name: String): NamesListLine()
private fun formalAliasLine(line: String): FormalAliasLine {
    return FormalAliasLine(line.removePrefix("$tabRE% "))
}

data class CrossRef(val codepoint: Int, val name: String): NamesListLine()
private fun crossRef(line: String): CrossRef {
    return crossRefParenRE.matchEntire(line)?.let {
        val (_, name, hex) = it
        CrossRef(hex.toInt(16), name)
    } ?: crossRefRE.matchEntire(line)!!.let {
        val (_, hex, name) = it
        CrossRef(hex.toInt(16), name)
    }
}

data class VariationLine(val codepoint: Int, val varsel: String, val label: String, val name: String): NamesListLine()
private fun variationLine(line: String): VariationLine {
    val (_, char, varsel, label, name) = variationLineRE.matchEntire(line)
    return VariationLine(char.toInt(16), varsel, label, name)
}

data class FileCommentLine(val text: String): NamesListLine()
private fun fileCommentLine(line: String): FileCommentLine {
    return FileCommentLine(line.removePrefix(";"))
}

object EmptyLine: NamesListLine()

data class IgnoredLine(val text: String): NamesListLine()
private fun ignoredLine(line: String): IgnoredLine {
    return IgnoredLine(line.removePrefix("$tabRE;".re))
}

data class SidebarLine(val text: String): NamesListLine()
private fun sidebarLine(line: String): SidebarLine {
    return SidebarLine(line.removePrefix(";;"))
}

data class Decomposition(val expandLine: ExpandLine): NamesListLine()
private fun decomposition(line: String): Decomposition {
    return Decomposition(expandLine(line.removePrefix("$tabRE: ".re)))
}

data class CompatMapping(val tag: String, val expandLine: ExpandLine): NamesListLine()
private fun compatMapping(line: String): CompatMapping {
    val (_, tag, body) = compatMappingRE.matchEntire(line)
    return CompatMapping(tag, expandLine(body))
}

data class NoticeLine(val hasBullet: Boolean, val expandLine: ExpandLine): NamesListLine()
private fun noticeLine(line: String): NoticeLine {
    val (_, star, text) = noticeLineRE.matchEntire(line)
    return NoticeLine(star != "", expandLine(text))
}

data class TitleLine(val text: String): NamesListLine()
private fun titleLine(line: String): TitleLine {
    return TitleLine(line.removePrefix("@@@$tabRE".re))
}

data class SubtitleLine(val text: String): NamesListLine()
private fun subtitleLine(line: String): SubtitleLine {
    return SubtitleLine(line.removePrefix("@@@\\+$tabRE".re))
}

data class SubheaderLine(val text: String): NamesListLine()
private fun subheaderLine(line: String): SubheaderLine {
    return SubheaderLine(line.removePrefix("@$tabRE".re))
}

data class VariationSubheader(val hasBang: Boolean, val varsel: List<Int>, val text: String): NamesListLine()
private fun variationSubheader(line: String): VariationSubheader {
    val (_, bang, varsel, text) = variationSubheaderRE.matchEntire(line)
    return VariationSubheader(bang != "", charRE.re.findAll(varsel).map { it.value.toInt(16) }.toList(), text)
}

data class AltglyphSubheader(val hasBang: Boolean, val text: String): NamesListLine()
private fun altglyphSubheader(line: String): AltglyphSubheader {
    val (_, bang, text) = altglyphSubheaderRE.matchEntire(line)
    return AltglyphSubheader(bang != "", text)
}

data class MixedSubheader(val hasBang: Boolean, val varsel: List<Int>, val text: String): NamesListLine()
private fun mixedSubheader(line: String): MixedSubheader {
    val (_, bang, varsel, text) = mixedSubheaderRE.matchEntire(line)
    return MixedSubheader(bang != "", charRE.re.findAll(varsel).map { it.value.toInt(16) }.toList(), text)
}

data class BlockHeader(val start: Int, val name: String, val end: Int): NamesListLine()
private fun blockHeader(line: String): BlockHeader {
    val (_, blockStart, blockName, isoBlockName, blockEnd) = blockHeaderRE.matchEntire(line)
    return BlockHeader(blockStart.toInt(16), blockName, blockEnd.toInt(16))
}

object PageBreak: NamesListLine()
object IndexTab: NamesListLine()

data class ExpandLine(val elements: List<ExpandLineElement>)
sealed class ExpandLineElement {
    data class Text(val text: String): ExpandLineElement()
    data class Codepoint(val codepoint: Int): ExpandLineElement()
}
private fun expandLine(str: String): ExpandLine {
    val elements = mutableListOf<ExpandLineElement>()
    var accumulator = ""
    expandLineElementRE.re.findAll(str).forEach { match ->
        val (_, char, text) = match
        if(char != "" && !char.startsWith("\\")) {
            if(accumulator != "") {
                elements.add(ExpandLineElement.Text(accumulator))
                accumulator = ""
            }
            elements.add(ExpandLineElement.Codepoint(char.toInt(16)))
        } else {
            // char and text are mutually exclusive, so we can just concatenate them to get whichever has text
            accumulator += (char + text).replace("""\\(.)""".re, "$1")
        }
    }
    if(accumulator != "") elements.add(ExpandLineElement.Text(accumulator))
    return ExpandLine(elements)
}
