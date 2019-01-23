package games.thecodewarrior.bitfont.utils.serialization

import com.beust.klaxon.JsonObject
import com.beust.klaxon.KlaxonJson

interface JsonWritable<J> {
    fun writeJson(): J
}

interface JsonReadable<J, T> {
    fun readJson(j: J): T
}
