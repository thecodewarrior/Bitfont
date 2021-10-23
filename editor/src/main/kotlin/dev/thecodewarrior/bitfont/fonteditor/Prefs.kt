package dev.thecodewarrior.bitfont.fonteditor

import java.util.prefs.Preferences

object Prefs {
    private val prefs = Preferences.userNodeForPackage(App::class.java)

    operator fun get(key: String, default: String): String {
        return prefs.get(key, default)
    }

    operator fun get(key: String): String? {
        return prefs.get(key, null)
    }

    operator fun set(key: String, value: String?) {
        if(value == null)
            prefs.remove(key)
        else
            prefs.put(key, value)
    }
}