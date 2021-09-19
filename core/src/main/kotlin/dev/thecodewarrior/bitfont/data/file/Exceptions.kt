package dev.thecodewarrior.bitfont.data.file

import java.lang.RuntimeException

public class FileFormatException: RuntimeException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}