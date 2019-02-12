package games.thecodewarrior.bitfont.utils

enum class CombiningClass(val unicode: Int, val xAlign: Int, val yAlign: Int, val attached: Boolean) {
    NOT_REORDERED        (0,    0,  0, false),
    OVERLAY              (1,    0,  0, false),
    NUKTA                (7,    0, -1, false),
    KANA_VOICING         (8,    0, -1, false),
    VIRAMA               (9,    0, -1, false),
    ATTACHED_BELOW_LEFT  (200, -1,  1,  true),
    ATTACHED_BELOW       (202,  0,  1,  true),
    ATTACHED_BELOW_RIGHT (204,  1,  1,  true),
    ATTACHED_LEFT        (208, -2,  0,  true),
    ATTACHED_RIGHT       (210,  2,  0,  true),
    ATTACHED_ABOVE_LEFT  (212, -1, -1,  true),
    ATTACHED_ABOVE       (214,  0, -1,  true),
    ATTACHED_ABOVE_RIGHT (216,  1, -1,  true),
    BELOW_LEFT           (218, -1,  1, false),
    BELOW                (220,  0,  1, false),
    BELOW_RIGHT          (222,  1,  1, false),
    LEFT                 (224, -2,  0, false),
    RIGHT                (226,  2,  0, false),
    ABOVE_LEFT           (228, -1, -1, false),
    ABOVE                (230,  0, -1, false),
    ABOVE_RIGHT          (232,  1, -1, false),
    DOUBLE_BELOW         (233,  3,  1, false),
    DOUBLE_ABOVE         (234,  3, -1, false),
    IOTA_SUBSCRIPT       (240,  0,  0, false);

    companion object {
        private val values: Map<Int, CombiningClass> = values().associateBy { it.unicode }

        operator fun get(unicode: Int): CombiningClass {
            return values[unicode] ?: NOT_REORDERED
        }
    }
}