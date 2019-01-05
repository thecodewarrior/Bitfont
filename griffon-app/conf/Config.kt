import griffon.util.AbstractMapResourceBundle

class Config : AbstractMapResourceBundle() {
    override fun initialize(entries: MutableMap<String, Any>) {
        entries.put("application", hashMapOf(
            "title" to "bitfont-editor",
            "startupGroups" to listOf("container"),
            "autoshutdown" to true
        ))
        entries.put("mvcGroups", hashMapOf(
            "container" to hashMapOf(
                "model" to "games.thecodewarrior.bitfonteditor.ContainerModel",
                "view" to "games.thecodewarrior.bitfonteditor.ContainerView",
                "controller" to "games.thecodewarrior.bitfonteditor.ContainerController"
            ),
            "editor" to hashMapOf(
                "model" to "games.thecodewarrior.bitfonteditor.EditorModel",
                "view" to "games.thecodewarrior.bitfonteditor.EditorView",
                "controller" to "games.thecodewarrior.bitfonteditor.EditorController"
            )
        ))
    }
}