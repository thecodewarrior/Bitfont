package games.thecodewarrior.bitfont.editor.utils

// suppress classname as nested objects are used for namespacing.
// Colors.editor.background instead of Colors.editorBackground
@Suppress("ClassName")
object Colors {
    val transparent = java.awt.Color(1f, 1f, 1f, 0f)
    val maroon = Color("800000")
    val red = Color("e6194b")
    val pink = Color("fabebe")

    val brown = Color("9a6324")
    val orange = Color("f58231")

    val yellow = Color("ffe119")
    val beige = Color("fffac8")

    val green = Color("3cb44b")
    val mint = Color("aaffc3")

    val teal = Color("469990")
    val cyan = Color("42d4f4")

    val navy = Color("000075")
    val blue = Color("4363d8")

    val lavender = Color("e6beff")
    val magenta = Color("f032e6")

    val black = Color("000000")
    val grey = Color("a9a9a9")
    val white = Color("ffffff")

    object editor {
        val background = Color("0A0A0A")
        val axes = orange
        val selection = magenta
        val grid = Color("3b3b46")
        val guides = blue
        val advance = green

        object referencePanel {
            val axes = orange
            val guides = blue
            val glyph = white
        }
    }

    object browser {
        val background = editor.background
        val baseline = maroon
        val glyph = white
        val missingGlyph = Color("afafaf")
        val cellHighlight = red
        val gridLines = Color("3b3b46")
        val hexLabels = Color("3b3b46")
    }

    object inputTest {
        val background = editor.background
        val originIndicator = orange
        val cursor = white
        val text = white
    }

    object layoutTest {
        val background = editor.background
        val originIndicator = orange
        val text = white
        val boundingBoxes = magenta
    }

    object main {
        val background = Color("738c99")
    }


    val satan = Color("ff00ff")
}