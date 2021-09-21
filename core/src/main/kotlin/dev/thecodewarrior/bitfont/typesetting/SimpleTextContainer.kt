package dev.thecodewarrior.bitfont.typesetting

public open class SimpleTextContainer(
    /**
     * The width of the container
     */
    override var width: Int,
    /**
     * The height of the container. Defaults to [Int.MAX_VALUE]
     */
    override var height: Int = Int.MAX_VALUE,
    /**
     * The maximum number of lines that this container can have. Defaults to [Int.MAX_VALUE]
     */
    override var maxLines: Int = Int.MAX_VALUE
): TextContainer {
    public override val lines: MutableList<TextContainer.TypesetLine> = mutableListOf()
}