package dev.thecodewarrior.bitfont.typesetting

/**
 * A custom element that can be added to an attributed string and laid out like text. Embeds are added as attributes on
 * base characters. One embed can be added to multiple characters, so many embeds can be singletons.
 *
 * Embeds have no intrinsic meaning. It's up to the renderer to handle drawing them in a platform-specific way.
 *
 * These base characters can be anything and will be used for things like line breaking and combining characters.
 * Internally a text embed is similar to a very funny glyph, with the backing character staying intact.
 *
 * For most embeds you should be able to use [pua], which is a character in Unicode's private use area.
 */
public abstract class TextEmbed : TextObject {
    public companion object {
        /**
         * A [private use area](https://en.wikipedia.org/wiki/Private_Use_Areas) character that can be used as an
         * attachment point for an embed. Embeds don't *need* to use this character, but it's convenient.
         */
        @JvmStatic
        public val pua: Char = '\uE000'
    }
}