package games.thecodewarrior.bitfont.utils

import com.ibm.icu.lang.UCharacter

/**
 * Information about how to lay out various combining characters. Data is sourced from the Unicode Character Database
 * page here https://unicode.org/reports/tr44/#Canonical_Combining_Class_Values
 */
enum class CombiningClass(
    /**
     * The unicode combining character code.
     */
    val unicode: Int,
    /**
     * The horizontal alignment with respect to the parent glyph.
     */
    val xAlign: XAlignment,
    /**
     * The vertical alignment with respect to the parent glyph.
     */
    val yAlign: YAlignment,
    /**
     * Whether the character should be "attached" to its parent. Normally combining characters have a gap added between
     * them and their parent, but when this is true the gap isn't added.
     */
    val attached: Boolean
) {
    NOT_REORDERED        (0,   XAlignment.CENTER, YAlignment.CENTER, false),
    OVERLAY              (1,   XAlignment.CENTER, YAlignment.CENTER, false),
    NUKTA                (7,   XAlignment.CENTER, YAlignment.ABOVE, false),
    KANA_VOICING         (8,   XAlignment.CENTER, YAlignment.ABOVE, false),
    VIRAMA               (9,   XAlignment.CENTER, YAlignment.ABOVE, false),
    ATTACHED_BELOW_LEFT  (200, XAlignment.LEFT_CORNER, YAlignment.BELOW, true),
    ATTACHED_BELOW       (202, XAlignment.CENTER, YAlignment.BELOW, true),
    ATTACHED_BELOW_RIGHT (204, XAlignment.RIGHT_CORNER, YAlignment.BELOW, true),
    ATTACHED_LEFT        (208, XAlignment.LEFT, YAlignment.CENTER, true),
    ATTACHED_RIGHT       (210, XAlignment.RIGHT, YAlignment.CENTER, true),
    ATTACHED_ABOVE_LEFT  (212, XAlignment.LEFT_CORNER, YAlignment.ABOVE, true),
    ATTACHED_ABOVE       (214, XAlignment.CENTER, YAlignment.ABOVE, true),
    ATTACHED_ABOVE_RIGHT (216, XAlignment.RIGHT_CORNER, YAlignment.ABOVE, true),
    BELOW_LEFT           (218, XAlignment.LEFT_CORNER, YAlignment.BELOW, false),
    BELOW                (220, XAlignment.CENTER, YAlignment.BELOW, false),
    BELOW_RIGHT          (222, XAlignment.RIGHT_CORNER, YAlignment.BELOW, false),
    LEFT                 (224, XAlignment.LEFT, YAlignment.CENTER, false),
    RIGHT                (226, XAlignment.RIGHT, YAlignment.CENTER, false),
    ABOVE_LEFT           (228, XAlignment.LEFT_CORNER, YAlignment.ABOVE, false),
    ABOVE                (230, XAlignment.CENTER, YAlignment.ABOVE, false),
    ABOVE_RIGHT          (232, XAlignment.RIGHT_CORNER, YAlignment.ABOVE, false),
    DOUBLE_BELOW         (233, XAlignment.DOUBLE, YAlignment.BELOW, false),
    DOUBLE_ABOVE         (234, XAlignment.DOUBLE, YAlignment.ABOVE, false),
    IOTA_SUBSCRIPT       (240, XAlignment.CENTER, YAlignment.CENTER, false);

    companion object {
        private val values: Map<Int, CombiningClass> = values().associateBy { it.unicode }

        operator fun get(codepoint: Int): CombiningClass {
            return fromUnicode(UCharacter.getCombiningClass(codepoint))
        }

        fun fromUnicode(unicode: Int): CombiningClass {
            return values[unicode] ?: NOT_REORDERED
        }
    }

    enum class XAlignment {
        /**
         * The right edge of this glyph is aligned with the left edge of the parent glyph.
         * [(image)](https://i.imgur.com/34WfnCc.png)
         */
        LEFT,
        /**
         * The left edge of the combining glyph is aligned with the left side of the parent glyph.
         * [(image)](https://i.imgur.com/jZ4yUFE.png)
         *
         */
        LEFT_CORNER,
        /**
         * The left edge of this glyph is aligned with the right edge of the parent glyph.
         * [(image)](https://i.imgur.com/3MqKDht.png)
         */
        RIGHT,
        /**
         * The right edge of the combining glyph is aligned with the right side of the parent glyph.
         * [(image)](https://i.imgur.com/7FA6nW9.png)
         */
        RIGHT_CORNER,
        /**
         * The center of the combining glyph is aligned with the center of the parent glyph.
         * [(image)](https://i.imgur.com/6QY4RIk.png)
         */
        CENTER,
        /**
         * The center of the combining glyph is aligned with the right of the parent glyph. (these generally span two
         * characters)
         * [(image)](https://i.imgur.com/qQrkv91.png)
         */
        DOUBLE
    }

    enum class YAlignment {
        /**
         * The bottom of the combining glyph is aligned with the top of the parent glyph.
         */
        ABOVE,
        /**
         * The top of the combining glyph is aligned with the bottom of the parent glyph.
         */
        BELOW,
        /**
         * The center of the combining glyph is aligned with the center of the parent glyph.
         */
        CENTER
    }
}