package games.thecodewarrior.bitfont.utils.extensions

fun String.splitWithDelimiters(regex: String) = this.split("((?<=$regex)|(?=$regex))".toRegex())
