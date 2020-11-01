package dev.thecodewarrior.bitfont.editor.jimgui.utils

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
    val gray = grey
    val white = Color("ffffff")

    object editor {
        val background get() = Color("0A0A0A")
        val axes get() = orange
        val selection get() = magenta
        val grid get() = Color("3b3b46")
        val guides get() = blue
        val advance get() = green

        object referencePanel {
            val axes get() = orange
            val guides get() = blue
            val glyph get() = white
        }
    }

    object browser {
        val background get() = editor.background
        val baseline get() = maroon
        val glyph get() = white
        val missingGlyph get() = Color("afafaf")
        val cellHighlight get() = red
        val gridLines get() = Color("3b3b46")
        val hexLabels get() = Color("3b3b46")
    }

    object inputTest {
        val background get() = editor.background
        val originIndicator get() = orange
        val cursor get() = white
        val text get() = white
    }

    object layoutTest {
        val background get() = editor.background
        val originIndicator get() = orange
        val text get() = white
        val boundingBoxes get() = magenta
    }


    object textLayout {
        val background get() = editor.background
        val text get() = white
        val lineFragment get() = grey
        val exclusionPath get() = orange
    }

    object main {
        val background get() = Color("738c99")
    }


    val satan = Color("ff00ff")
}