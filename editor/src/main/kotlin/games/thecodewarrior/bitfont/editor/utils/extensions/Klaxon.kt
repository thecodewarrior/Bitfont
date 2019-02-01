package games.thecodewarrior.bitfont.editor.utils.extensions

import com.beust.klaxon.JsonBase
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser

fun Klaxon.toPrettyJsonString(value: Any): String {
    val builder = StringBuilder(this.toJsonString(value))
    return (Parser.default().parse(builder) as JsonBase).toJsonString(true)
}