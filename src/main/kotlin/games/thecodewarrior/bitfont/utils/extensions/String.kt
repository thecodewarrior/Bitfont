package games.thecodewarrior.bitfont.utils.extensions

fun String.splitWithDelimiters(regex: String) = this.split("((?<=$regex)|(?=$regex))".toRegex())

private val regexCache = mutableMapOf<String, Regex>()
val String.re: Regex get() = regexCache.getOrPut(this) { this.toRegex() }

fun String.startsWith(re: Regex): Boolean {
    return re.find(this)?.range?.start == 0
}

fun String.removePrefix(re: Regex): String {
    val match = re.find(this) ?: return this
    if(match.range.start != 0) return this
    return substring(match.range.endInclusive)
}
